package com.lszlp.choronometre;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class ChronometerService extends Service {
    private static final String TAG = "ChronometerService";
    public static final String ACTION_START = "START";
    public static final String ACTION_STOP = "STOP";
    public static final String ACTION_PAUSE = "PAUSE";
    public static final String ACTION_RESUME = "RESUME";
    public static final String ACTION_TIME_UPDATE = "com.lszlp.choronometre.ACTION_TIME_UPDATE";
    public static final int NOTIFICATION_ID = Constants.NOTIFICATION_ID;
    public static final String CHANNEL_ID = Constants.CHANNEL_ID;

    private long startTime;
    private long pauseTime;
    private boolean isRunning = false;
    private boolean isPaused = false;
    private Handler handler;
    private Runnable updateRunnable;

    // Zaman birimi değişkenleri
    private String timeUnit = "Sec."; // Varsayılan
    private int modul = 60; // Varsayılan saniye
    private int milis = 1000; // Varsayılan

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        Log.d(TAG, "🔥 Service onCreate");
        createProperNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "🔥 onStartCommand: " + (intent != null ? intent.getAction() : "null"));

        if (intent != null) {
            // Zaman birimi bilgilerini al
            if (intent.hasExtra(Constants.EXTRA_TIME_UNIT)) {
                timeUnit = intent.getStringExtra(Constants.EXTRA_TIME_UNIT);
                modul = intent.getIntExtra(Constants.EXTRA_MODUL, 60);
                milis = intent.getIntExtra(Constants.EXTRA_MILIS, 1000);

                Log.d(TAG, "🔥 Received time unit - Unit: " + timeUnit +
                        ", Modul: " + modul + ", Milis: " + milis);
            }

            String action = intent.getAction();
            if (Constants.ACTION_START.equals(action)) {
                startChronometer();
            } else if (Constants.ACTION_STOP.equals(action)) {
                stopChronometer();
            } else if (Constants.ACTION_PAUSE.equals(action)) {
                pauseChronometer();
            } else if (Constants.ACTION_RESUME.equals(action)) {
                resumeChronometer();
            }
        }

        return START_STICKY;
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
        Intent stopIntent = new Intent(this, ChronometerService.class);
        stopIntent.setAction(Constants.ACTION_STOP);
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 1, stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // 🔥 PAUSE/RESUME butonu için intent
        Intent pauseResumeIntent = new Intent(this, ChronometerService.class);
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
                .setAutoCancel(false)
                .addAction(R.drawable.ic_baseline_timer_24, "Stop", stopPendingIntent);

        // Custom layout için
        try {
            RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification_chronometer);
            remoteViews.setTextViewText(R.id.notification_time, timeText);

            // 🔥 Buton metnini pause/resume durumuna göre ayarla
            if (isPaused) {
                remoteViews.setTextViewText(R.id.notification_stop, "Çalıştır"); // Resume butonu
                remoteViews.setInt(R.id.notification_stop, "setBackgroundColor", Color.parseColor("#4CAF50")); // Yeşil
            } else {
                remoteViews.setTextViewText(R.id.notification_stop, "Duraklat"); // Pause butonu
                remoteViews.setInt(R.id.notification_stop, "setBackgroundColor", Color.parseColor("#FF9800")); // Turuncu
            }

            remoteViews.setOnClickPendingIntent(R.id.notification_stop, pauseResumePendingIntent);

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
                return "⏱️ Chronometer (Seconds)" + (isPaused ? " ⏸️" : "");
            case "Cmin.":
                return "⏱️ Chronometer (Centiminutes)" + (isPaused ? " ⏸️" : "");
            case "Dmh.":
                return "⏱️ Chronometer (Deciminutes)" + (isPaused ? " ⏸️" : "");
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
        stopChronometer();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}