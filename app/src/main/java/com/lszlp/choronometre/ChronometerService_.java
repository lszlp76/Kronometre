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
/*
public class ChronometerService_ extends Service {
    private static final String TAG = "ChronometerService";
    public static final String ACTION_START = "START";
    public static final String ACTION_STOP = "STOP";
    public static final String ACTION_PAUSE = "PAUSE";
    public static final String ACTION_RESUME = "RESUME";
    public static final String ACTION_TIME_UPDATE = "com.lszlp.choronometre.ACTION_TIME_UPDATE";
    public static final int NOTIFICATION_ID = Constants.NOTIFICATION_ID;
    public static final String CHANNEL_ID = Constants.CHANNEL_ID;

    private long startTime = 0l;
    private long pauseTime;
    private boolean isRunning = false;
    private boolean isPaused = false;
    private Handler handler;
    private Runnable updateRunnable;

    // Zaman birimi değişkenleri
    private String timeUnit = "Sec."; // Varsayılan
    private int modul = 60; // Varsayılan saniye
    private int milis = 1000; // Varsayılan
//notifydan app e mudahale etmek için broadCastreceiver kullanmak
    private BroadcastReceiver pauseResumeReceiver;
private Context context;
    // --- Zamanlayıcı Değişkenleri ---
    private Runnable runnable;
    private long elapsedTime = 0L; // Şu anki toplam geçen zaman
    private long timeBeforePause = 0L; // Pause anındaki zamanı tutar


    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        Log.d(TAG, "🔥 Service onCreate");
        createProperNotificationChannel();
        // 🔥 Broadcast Receiver'ı oluştur
        setupPauseResumeReceiver();
    }
    // 🔥 YENİ METOD: Pause/Resume Broadcast Receiver'ı kur
    private void setupPauseResumeReceiver() {
        pauseResumeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.d(TAG, "📡 Broadcast received in service: " + action);

                if (Constants.ACTION_PAUSE.equals(action)) {
                    pauseChronometer();
                } else if (Constants.ACTION_RESUME.equals(action)) {
                    resumeChronometer();
                }
            }
        };
        // Broadcast Receiver'ı kaydet
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_PAUSE);
        filter.addAction(Constants.ACTION_RESUME);

        ContextCompat.registerReceiver(this, pauseResumeReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);

        Log.d(TAG, "✅ Pause/Resume BroadcastReceiver registered");
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createProperNotificationChannel(); // HER ZAMAN KANAL OLUŞTUR

        Log.d(TAG, "🔥 onStartCommand: " + (intent != null ? intent.getAction() : "null"));

        if (intent != null) {
            String action = intent.getAction();
            // Fragment'tan gelen ayarları al
            modul = intent.getIntExtra(Constants.EXTRA_MODUL, modul); // Varsayılan değer olarak eski değeri kullan
            milis = intent.getIntExtra(Constants.EXTRA_MILIS, milis);
            timeUnit = intent.getStringExtra(Constants.EXTRA_TIME_UNIT);
            // Zaman birimi bilgilerini al
            if (Constants.ACTION_START.equals(action)) {
                if (!isRunning) {
                    // 1. BİLDİRİMİ OLUŞTUR (5 SANİYE KURALI İÇİN KRİTİK)
                    Notification notification = buildNotification("00:00:00.000");
                    // NOTIFICATION_ID bir tamsayı olmalıdır (örneğin 1)

                    startForeground(Constants.NOTIFICATION_ID, notification); // BURASI ÇOK ÖNEMLİ
                    Log.d("ChronoService", "START komutu alındı. Modul: " + modul);

                    startTime = SystemClock.elapsedRealtime();
                    timeBeforePause = elapsedTime; // Kaldığı yerden devam etmeli
                    startTimer();
                    isRunning = true;
                }
            } else if (Constants.ACTION_STOP.equals(action)) {
                stopTimer();
                isRunning = false;
            } else if (Constants.ACTION_RESET.equals(action)) {
                // Eğer varsa bu action'ı da ekleyin
                stopTimer();
                elapsedTime = 0L;
                timeBeforePause = 0L;
                isRunning = false;
                // UI'ı sıfırlaması için 0 değeri gönder
                sendTimeUpdate(0L);
            }
        }
        return START_STICKY;
//            if (intent.hasExtra(Constants.EXTRA_TIME_UNIT)) {
//                timeUnit = intent.getStringExtra(Constants.EXTRA_TIME_UNIT);
//                modul = intent.getIntExtra(Constants.EXTRA_MODUL, 60);
//                milis = intent.getIntExtra(Constants.EXTRA_MILIS, 1000);
//
//                Log.d(TAG, "🔥 Received time unit - Unit: " + timeUnit +
//                        ", Modul: " + modul + ", Milis: " + milis);
//            }
//
//            String action = intent.getAction();
//            if (Constants.ACTION_START.equals(action)) {
//                startChronometer();
//            } else if (Constants.ACTION_STOP.equals(action)) {
//                stopChronometer();
//            } else if (Constants.ACTION_PAUSE.equals(action)) {
//                pauseChronometer();
//            } else if (Constants.ACTION_RESUME.equals(action)) {
//                resumeChronometer();
//            }
//        }
//
//        return START_STICKY;
    }
    // Bildirim oluşturma metodu (varsayılan metni alarak)
    private Notification buildNotification(String contentText) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        // PendingIntent.FLAG_IMMUTABLE kullanın
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Kronometre Çalışıyor")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_baseline_timer_24) // Kendi ikonunuzu kullanın
                .setContentIntent(pendingIntent)
                .setSilent(true) // Sessiz olmasını sağlar
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
    }
    private void sendTimeUpdate(long time) {
        Intent intent = new Intent(Constants.ACTION_TIME_UPDATE);
        intent.putExtra(Constants.EXTRA_ELAPSED_TIME, time); // elapsed time'ı gönder

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        // Fragment'ın log'ları çalışmıyorsa buradan kontrol edebiliriz.
        Log.d("ChronoService", "Broadcast Sent: " + time);
    }
// ChronometerService.java

    private void startTimer() {
        if (handler == null) {
            handler = new Handler(Looper.getMainLooper());
        }

        if (runnable != null) {
            handler.removeCallbacks(runnable);
        }

        // Runnable: Sürekli çalışacak olan görev
        runnable = new Runnable() {
            @Override
            public void run() {
                // 1. Yeni zamanı hesapla
                long timePassedSinceStart = SystemClock.elapsedRealtime() - startTime;
                elapsedTime = timeBeforePause + timePassedSinceStart;

                // 2. Fragment'a yayını gönder
                sendTimeUpdate(elapsedTime);

                // 3. Kendini 10 milisaniye sonra tekrar çağır (Akıcı bir güncelleme için)
                handler.postDelayed(this, 10);

                // 4. (OPSİYONEL) Bildirimi güncelle
                updateNotification();
            }
        };

        // Runnable'ı hemen başlat
        handler.post(runnable);
    }

    private void stopTimer() {
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
        // Servis durduğunda elapsedTime'ı koru
        elapsedTime = elapsedTime;
    }


    private void createProperNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    Constants.CHANNEL_ID,
                    "Chronometer Timer",
                    NotificationManager.IMPORTANCE_LOW
            );

            // GÖRÜNÜRLÜK AYARLARI
            channel.setDescription("Shows chronometer timer when app is in background");
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.setShowBadge(true);
            channel.enableLights(true);
            channel.setLightColor(Color.GREEN);
            channel.enableVibration(false);
            channel.setSound(null, null);
            channel.setBypassDnd(false);

            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
                Log.d(TAG, "✅ Channel created: " + Constants.CHANNEL_ID);
            }
        } else {
            Log.d(TAG, "ℹ️ No channel needed (API < 26)");
        }
    }

    private void startChronometer() {
        Log.d(TAG, "🔥 STARTING CHRONOMETER - Unit: " + timeUnit);

        if (!isRunning && !isPaused) {
            isRunning = true;
            isPaused = false;
            startTime = SystemClock.elapsedRealtime();

            sendNotificationViaManager();
            startForegroundService();
            startUpdating();
        }
    }

    // 🔥 YENİ METOD: Kronometreyi duraklat
    private void pauseChronometer() {
        Log.d(TAG, "⏸️ PAUSING CHRONOMETER");

        if (isRunning && !isPaused) {
            isPaused = true;
            pauseTime = SystemClock.elapsedRealtime();

            if (handler != null && updateRunnable != null) {
                handler.removeCallbacks(updateRunnable);
            }

            // Notification'ı güncelle (buton "Çalıştır" olacak)
            updateNotification();

            Log.d(TAG, "✅ Chronometer paused");
        }
    }

    // 🔥 YENİ METOD: Kronometreyi devam ettir
    private void resumeChronometer() {
        Log.d(TAG, "▶️ RESUMING CHRONOMETER");

        if (isRunning && isPaused) {
            isPaused = false;
            // Duraklatma süresini ekleyerek startTime'ı güncelle
            long pausedDuration = SystemClock.elapsedRealtime() - pauseTime;
            startTime += pausedDuration;

            // Güncellemeyi yeniden başlat
            startUpdating();

            // Notification'ı güncelle (buton "Duraklat" olacak)
            updateNotification();

            Log.d(TAG, "✅ Chronometer resumed");
        }
    }

    private void sendNotificationViaManager() {
        try {
            Notification notification = createNotification();
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) {
                nm.notify(Constants.NOTIFICATION_ID, notification);
                Log.d(TAG, "✅ Notification sent via NotificationManager");
            } else {
                Log.e(TAG, "❌ NotificationManager is null");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error sending notification: " + e.getMessage(), e);
        }
    }

    private void startForegroundService() {
        try {
            Notification notification = createNotification();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                int foregroundServiceType = 0;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    foregroundServiceType = android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE;
                } else {
                    foregroundServiceType = android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION;
                }
                startForeground(Constants.NOTIFICATION_ID, notification, foregroundServiceType);
                Log.d(TAG, "✅ Foreground service started with type");
            } else {
                startForeground(Constants.NOTIFICATION_ID, notification);
                Log.d(TAG, "✅ Foreground service started");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error starting foreground: " + e.getMessage(), e);
        }
    }

    private Notification createNotification() {
        String timeText = formatTimeAccordingToUnit(getCurrentElapsedTime());
        Log.d(TAG, "🔥 Creating notification - Time: " + timeText + ", Unit: " + timeUnit + ", Paused: " + isPaused);

        // MainActivity'yi açmak için intent
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Durdurma butonu için intent
        Intent stopIntent = new Intent(this, ChronometerService_.class);
        stopIntent.setAction(Constants.ACTION_STOP);
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 1, stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // 🔥 PAUSE/RESUME butonu için intent
        Intent pauseResumeIntent = new Intent(this, ChronometerService_.class);
        if (isPaused) {
            pauseResumeIntent.setAction(Constants.ACTION_RESUME);
        } else {
            pauseResumeIntent.setAction(Constants.ACTION_PAUSE);
        }
        PendingIntent pauseResumePendingIntent = PendingIntent.getService(this, 2, pauseResumeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Zaman birimine göre başlık
        String title = getNotificationTitle();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Constants.CHANNEL_ID)
                .setContentTitle(title)
                .setContentText("Timer: " + timeText + (isPaused ? " (Paused)" : ""))
                .setSmallIcon(R.drawable.ic_baseline_timer_24)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setShowWhen(false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setAutoCancel(false);
                //.addAction(R.drawable.ic_baseline_timer_24, "Stop", stopPendingIntent);

        // Custom layout için
        try {
            RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification_chronometer);
            remoteViews.setTextViewText(R.id.notification_time, timeText);
            remoteViews.setTextViewText(R.id.timeUnitText, timeUnit);

           builder.setStyle(new NotificationCompat.DecoratedCustomViewStyle());
            builder.setCustomContentView(remoteViews);
            Log.d(TAG, "Custom notification layout applied - Paused: " + isPaused);

        } catch (Exception e) {
            Log.e(TAG, "Error creating custom notification layout: " + e.getMessage(), e);
        }

        Log.d(TAG, "✅ Notification created for unit: " + timeUnit + ", Paused: " + isPaused);
        return builder.build();
    }

    // 🔥 YENİ METOD: Geçerli elapsed time'ı hesapla (pause durumunu da hesaba katarak)
    private long getCurrentElapsedTime() {
        if (isPaused) {
            return pauseTime - startTime;
        } else {
            return SystemClock.elapsedRealtime() - startTime;
        }
    }

    // ZAMAN BİRİMİNE GÖRE FORMATLAMA
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

    private void startUpdating() {
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRunning && !isPaused) {
                    updateNotification();
                    handler.postDelayed(this, Math.max(100, milis / 10));
                }
            }
        };
        handler.post(updateRunnable);
        Log.d(TAG, "✅ Update timer started for unit: " + timeUnit);
    }

    private void updateNotification() {


        if (isRunning) {
            try {
                Notification notification = createNotification();
                NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (nm != null) {
                    nm.notify(Constants.NOTIFICATION_ID, notification);
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ Update error: " + e.getMessage());
            }
        }
    }

    private void stopChronometer() {
        Log.d(TAG, "🛑 STOPPING CHRONOMETER");
        if (isRunning) {
            isRunning = false;
            isPaused = false;

            if (handler != null && updateRunnable != null) {
                handler.removeCallbacks(updateRunnable);
            }

            // Notification'ı temizle
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) {
                nm.cancel(Constants.NOTIFICATION_ID);
            }

            stopForeground(true);
            stopSelf();
            Log.d(TAG, "✅ Service stopped and notification cleared");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "🔥 Service destroyed");

        // 🔥 Broadcast Receiver'ı temizle
        if (pauseResumeReceiver != null) {
            try {
                unregisterReceiver(pauseResumeReceiver);
                Log.d(TAG, "✅ Pause/Resume BroadcastReceiver unregistered");
            } catch (Exception e) {
                Log.e(TAG, "❌ Error unregistering receiver: " + e.getMessage());
            }
        }

        stopChronometer();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}*/