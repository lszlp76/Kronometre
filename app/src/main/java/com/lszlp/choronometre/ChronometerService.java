package com.lszlp.choronometre;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

// DİKKAT: Constants sınıfınızın doğru değerleri içerdiğinden emin olun!
// Örn: Constants.NOTIFICATION_ID, Constants.CHANNEL_ID vb.

public class ChronometerService extends Service {
    private static final String TAG = "ChronoService";

    // --- Zamanlayıcı Değişkenleri ---
    private Handler handler;
    private Runnable updateRunnable; // UI ve Bildirim güncelleme görevimiz
    private long startTime = 0L;
    private long elapsedTime = 0L; // Şu anki toplam geçen zaman (Fragment'a gönderilen)
    private long timeBeforePause = 0L; // Duraklatmadan önceki toplam zaman

    private boolean isRunning = false;
    private boolean isPaused = false;

    // --- Ayar Değişkenleri ---
    private String timeUnit = "Sec."; // Varsayılan
    private int modul = 60; // Varsayılan saniye
    private int milis = 1000; // Varsayılan
    // Sadece bu satırı tutun:
    // --- Receiver ---
    private BroadcastReceiver pauseResumeReceiver;


    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper()); // Ana iplikte Handler oluştur
        Log.d(TAG, "🔥 Service onCreate");
        createNotificationChannel(); // Kanalı oluştur
        setupPauseResumeReceiver();  // Receiver'ı kur
        // YENİ: Durum isteği alıcısını kaydet
        LocalBroadcastManager.getInstance(this).registerReceiver(statusRequestReceiver,
                new IntentFilter(Constants.ACTION_REQUEST_STATUS));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            String action = intent.getAction();
            Log.d(TAG, "🔥 onStartCommand: " + action);

            // Fragment'tan gelen ayarları al
            modul = intent.getIntExtra(Constants.EXTRA_MODUL, modul);
            milis = intent.getIntExtra(Constants.EXTRA_MILIS, milis);
            timeUnit = intent.getStringExtra(Constants.EXTRA_TIME_UNIT);

            if (Constants.ACTION_START.equals(action)) {
                if (!isRunning) {
                    // 1. BİLDİRİM: 5 saniye kuralı için hemen çağır
                    Notification notification = buildNotification("00:00:00.000");
                    startForeground(Constants.NOTIFICATION_ID, notification);
                    Log.d(TAG, "✅ Foreground service started.");

                    // 2. Zamanlayıcıyı başlat
                    startTime = SystemClock.elapsedRealtime();
                    // Eğer uygulama resetlenmediyse, elapsedTime'ı timeBeforePause'a aktarın
                    // Ancak, Chronometer mantığında START her zaman sıfırdan başlatmayı veya
                    // kaldığı yerden devam etmeyi tetiklemelidir.
                    // Eğer TimerFragment'ta 'reset()' çağrılmadıysa, elapsedTime sıfır olmamalıdır.
                    // Start tuşu reset tuşu değilse, bu mantık yanlış olabilir.

                    // Şimdilik sıfırdan başlatma mantığı uygulanıyor:
                    // startTime = SystemClock.elapsedRealtime();
                    timeBeforePause = 0L; // Sadece sıfırdan başlıyorsa
                    elapsedTime = 0L;     // Sadece sıfırdan başlıyorsa

                    startTimer();
                    isRunning = true;
                    isPaused = false;
                }
            } else if (Constants.ACTION_STOP.equals(action)) {
                stopChronometer();
            } else if (Constants.ACTION_RESET.equals(action)) {
                resetChronometer();
            }
        }
        return START_STICKY;
    }

    // --- ZAMANLAYICI MANTIĞI ---

    private void startTimer() {
        if (updateRunnable != null) {
            handler.removeCallbacks(updateRunnable);
        }

        // Runnable: Sürekli çalışacak olan görev
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRunning && !isPaused) {
                    // 1. Yeni zamanı hesapla
                    long timePassedSinceStart = SystemClock.elapsedRealtime() - startTime;
                    elapsedTime = timeBeforePause + timePassedSinceStart;

                    // 2. Fragment'a yayını gönder
                    sendTimeUpdate(elapsedTime);

                    // 3. Bildirimi güncelle
                    updateNotification();

                    // 4. Kendini tekrar çağır (10ms daha akıcı bir görüntü verir)
                    handler.postDelayed(this, 10);
                }
            }
        };

        // Runnable'ı hemen başlat
        handler.post(updateRunnable);
        Log.d(TAG, "✅ Update timer started.");
    }
    // --- YENİ: Durum İsteklerini Dinleyen Alıcı ---
    private BroadcastReceiver statusRequestReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.ACTION_REQUEST_STATUS)) {
                sendCurrentStatus();
            }
        }
    };
    // YENİ METOT: Fragment'a mevcut durumu gönderir
    private void sendCurrentStatus() {
        Intent statusIntent = new Intent(Constants.ACTION_STATUS_RESPONSE);

        // Çalışma ve duraklatma durumunu gönder
        statusIntent.putExtra(Constants.EXTRA_IS_RUNNING, isRunning);
        statusIntent.putExtra(Constants.EXTRA_IS_PAUSED, isPaused);

        // Fragment'a gösterilmesi gereken geçen süreyi gönder.
        // Duraklatmadan önceki toplam süre (timeBeforePause) en güvenli değerdir.
        statusIntent.putExtra(Constants.EXTRA_ELAPSED_TIME, timeBeforePause);

        LocalBroadcastManager.getInstance(this).sendBroadcast(statusIntent);
    }
    private void sendTimeUpdate(long time) {
        Intent intent = new Intent(Constants.ACTION_TIME_UPDATE);
        intent.putExtra(Constants.EXTRA_ELAPSED_TIME, time);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Log.d(TAG, "Broadcast Sent: " + time);
    }

    // --- DURUM YÖNETİMİ ---

    private void pauseChronometer() {
        if (isRunning && !isPaused) {
            isPaused = true;
            timeBeforePause = elapsedTime; // Geçen süreyi kaydet
            handler.removeCallbacks(updateRunnable); // Zamanlayıcıyı durdur
            updateNotification(); // Bildirimi güncelle (Paused yazsın)
            Log.d(TAG, "⏸️ Chronometer paused.");
        }
    }

    private void resumeChronometer() {
        if (isRunning && isPaused) {
            isPaused = false;
            startTime = SystemClock.elapsedRealtime(); // Yeni başlangıç zamanını kaydet
            startTimer(); // Zamanlayıcıyı devam ettir
            Log.d(TAG, "▶️ Chronometer resumed.");
        }
    }

    private void resetChronometer() {
        stopForeground(STOP_FOREGROUND_REMOVE);
        stopSelf();

        elapsedTime = 0L;
        timeBeforePause = 0L;
        isRunning = false;
        isPaused = false;

        sendTimeUpdate(0L); // Fragment'a 0 gönder
        Log.d(TAG, "🔄 Chronometer reset.");
    }


    private void stopChronometer() {
        Log.d(TAG, "🛑 STOPPING CHRONOMETER");
        isRunning = false;

        if (handler != null && updateRunnable != null) {
            handler.removeCallbacks(updateRunnable);
        }

        stopForeground(STOP_FOREGROUND_REMOVE); // Bildirimi kaldır
        stopSelf(); // Servisi durdur
        Log.d(TAG, "✅ Service stopped and notification cleared");
    }

    // --- BİLDİRİM MANTIĞI ---

    // 🔥 1. Kanal oluşturma
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    Constants.CHANNEL_ID,
                    "Kronometre Servisi",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.setSound(null, null); // Ses yok

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
                Log.d(TAG, "✅ Channel created.");
            }
        }
    }

    // 🔥 2. Bildirim içeriği oluşturma
    private Notification buildNotification(String contentText) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE); // FLAG_IMMUTABLE şart!

        // Zamanı formatla
        String timeDisplay = formatTimeAccordingToUnit(elapsedTime);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Constants.CHANNEL_ID)
                .setContentTitle(getNotificationTitle())
                .setContentText("Time: " + timeDisplay + (isPaused ? " (Paused)" : ""))
                .setSmallIcon(R.drawable.ic_baseline_timer_24) // İkonunuzu buraya koyun
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setShowWhen(false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_LOW) // LOW, ses ve baş üstü bildirimini engeller
                .setCategory(NotificationCompat.CATEGORY_SERVICE);

        // Custom RemoteViews mantığını kullanın
        try {
            RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification_chronometer);
            remoteViews.setTextViewText(R.id.notification_time, timeDisplay);
            remoteViews.setTextViewText(R.id.timeUnitText, timeUnit);

            // Eğer Custom Layout kullanıyorsanız
            builder.setCustomContentView(remoteViews);
            builder.setStyle(new NotificationCompat.DecoratedCustomViewStyle());
        } catch (Exception e) {
            Log.e(TAG, "❌ Custom layout hatası: " + e.getMessage());
        }

        return builder.build();
    }

    // 🔥 3. Bildirimi güncelleme (Zamanlayıcıdan çağrılır)
    private void updateNotification() {
        try {
            Notification notification = buildNotification(formatTimeAccordingToUnit(elapsedTime));
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) {
                nm.notify(Constants.NOTIFICATION_ID, notification);
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Bildirim güncelleme hatası: " + e.getMessage());
        }
    }

    // --- ALICI (RECEIVER) MANTIĞI ---

    private void setupPauseResumeReceiver() {
        pauseResumeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (Constants.ACTION_PAUSE.equals(action)) {
                    pauseChronometer();
                } else if (Constants.ACTION_RESUME.equals(action)) {
                    resumeChronometer();
                }
            }
        };
        // Local Broadcast Manager ile kaydet
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_PAUSE);
        filter.addAction(Constants.ACTION_RESUME);

        // --- DEĞİŞİKLİK BURADA ---
        // ContextCompat.registerReceiver yerine LocalBroadcastManager kullan
        LocalBroadcastManager.getInstance(this).registerReceiver(pauseResumeReceiver, filter);
        // Eski satırı silin:
        // ContextCompat.registerReceiver(this, pauseResumeReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    // --- TEMİZLEME ---

    // onDestroy metodunu GÜNCELLEYİN
    @Override
    public void onDestroy() {
        super.onDestroy();
//        Log.d(TAG, "🔥 Service destroyed");
//        if (handler != null && updateRunnable != null) {
//            handler.removeCallbacks(updateRunnable);
//        }
//        if (pauseResumeReceiver != null) {
//            try {
//                // --- DEĞİŞİKLİK BURADA ---
//                // unregisterReceiver yerine LocalBroadcastManager kullan
//                LocalBroadcastManager.getInstance(this).unregisterReceiver(pauseResumeReceiver);
//                // Eski satırı silin:
//                // unregisterReceiver(pauseResumeReceiver);
//            } catch (Exception e) {
//                Log.e(TAG, "❌ Receiver kaldırılamadı: " + e.getMessage());
//            }
//        }
        // YENİ: Alıcıyı sil
        LocalBroadcastManager.getInstance(this).unregisterReceiver(statusRequestReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // --- ZAMAN FORMATLAMA METOTLARI (Aynı kaldı) ---

    // Bu metotların doğru çalıştığını varsayıyoruz.
    private String formatTimeAccordingToUnit(long elapsedMillis) {
        switch (timeUnit) {
            case "Sec.":
                return formatSeconds(elapsedMillis);
            case "Cmin.":
                return formatCentiminutes(elapsedMillis);
            case "Dmh.":
                return formatDeciminutes(elapsedMillis);
            default:
                return formatSeconds(elapsedMillis); // Varsayılan
        }

    }

    private String formatSeconds(long millis) {
        int hours = (int) (millis / 3600000);
        int minutes = (int) (millis - hours * 3600000) / 60000;
        int seconds = (int) (millis - hours * 3600000 - minutes * 60000) / 1000;
        String result = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        return result;
    }

    private String formatCentiminutes(long millis) {
        int totalCentiminutes = (int) (millis / 600);
        int hours = totalCentiminutes / 6000;
        int minutes = (totalCentiminutes % 6000) / 100;
        int centiminutes = totalCentiminutes % 100;
        String result = String.format("%02d:%02d:%02d", hours, minutes, centiminutes);
        return result;
    }

    private String formatDeciminutes(long millis) {
        int totalDeciminutes = (int) (millis / 360);
        int hours = totalDeciminutes / 10000;
        int deciminutes = (totalDeciminutes % 10000) / 100;
        int centiminutes = totalDeciminutes % 100;
        String result = String.format("%02d:%02d:%02d", hours, deciminutes, centiminutes);
        return result;
    }
    private String getNotificationTitle() {
        switch (timeUnit) {
            case "Sec.":
                return "⏱️ Time@Seconds" + (isPaused ? " ⏸️" : "");
            case "Cmin.":
                return "⏱️ Time@Centiminutes" + (isPaused ? " ⏸️" : "");
            case "Dmh.":
                return "⏱️ Time@Deciminutes)" + (isPaused ? " ⏸️" : "");
            default:
                return "⏱️ Chronometer" + (isPaused ? " ⏸️" : "");
        }
    }
}