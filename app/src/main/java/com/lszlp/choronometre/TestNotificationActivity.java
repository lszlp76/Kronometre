package com.lszlp.choronometre;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

public class TestNotificationActivity extends AppCompatActivity {

    private static final String TAG = "TestNotificationActivity";

    private TextView tvDebugInfo;
    private NotificationManager notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_notification);

        tvDebugInfo = findViewById(R.id.tvDebugInfo);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        setupButtons();
        loadDebugInfo();
    }

    private void setupButtons() {
        Button btnTestNotification = findViewById(R.id.btnTestNotification);
        Button btnCheckPermissions = findViewById(R.id.btnCheckPermissions);
        Button btnOpenSettings = findViewById(R.id.btnOpenSettings);
        Button btnClose = findViewById(R.id.btnClose);

        btnTestNotification.setOnClickListener(v -> testNotification());
        btnCheckPermissions.setOnClickListener(v -> checkPermissions());
        btnOpenSettings.setOnClickListener(v -> openNotificationSettings());
        btnClose.setOnClickListener(v -> finish());
    }

    private void loadDebugInfo() {
        StringBuilder debugInfo = new StringBuilder();

        debugInfo.append("=== NOTIFICATION SYSTEM DEBUG ===\n\n");

        if (notificationManager != null) {
            // Temel bildirim ayarları
            debugInfo.append("🔔 NOTIFICATION SETTINGS:\n");
            debugInfo.append("• Notifications enabled: ").append(notificationManager.areNotificationsEnabled()).append("\n");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                debugInfo.append("• DND Policy Access: ").append(notificationManager.isNotificationPolicyAccessGranted()).append("\n");
            }

            // ChronometerService channel bilgisi
            debugInfo.append("\n⏰ CHRONOMETER SERVICE CHANNEL:\n");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel chronoChannel = notificationManager.getNotificationChannel(Constants.CHANNEL_ID);
                if (chronoChannel != null) {
                    debugInfo.append("• Channel exists: YES\n");
                    debugInfo.append("• Name: ").append(chronoChannel.getName()).append("\n");
                    debugInfo.append("• Importance: ").append(getImportanceText(chronoChannel.getImportance())).append("\n");
                    debugInfo.append("• Lockscreen Visibility: ").append(getVisibilityText(chronoChannel.getLockscreenVisibility())).append("\n");
                    debugInfo.append("• Can bypass DND: ").append(chronoChannel.canBypassDnd()).append("\n");
                    debugInfo.append("• Description: ").append(chronoChannel.getDescription()).append("\n");
                    debugInfo.append("• Group: ").append(chronoChannel.getGroup()).append("\n");
                } else {
                    debugInfo.append("• Channel exists: NO ❌\n");
                    debugInfo.append("• Channel ID: ").append(Constants.CHANNEL_ID).append("\n");
                }
            } else {
                debugInfo.append("• Channels not supported (API < 26)\n");
            }

            // Tüm channel'ları listele
            debugInfo.append("\n📋 ALL NOTIFICATION CHANNELS:\n");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                var channels = notificationManager.getNotificationChannels();
                if (channels.isEmpty()) {
                    debugInfo.append("• No channels found\n");
                } else {
                    for (var channel : channels) {
                        debugInfo.append("• ").append(channel.getId())
                                .append(" [").append(getImportanceText(channel.getImportance())).append("]")
                                .append(" [").append(getVisibilityText(channel.getLockscreenVisibility())).append("]")
                                .append("\n");
                    }
                }
            }
        } else {
            debugInfo.append("❌ NotificationManager is null!\n");
        }

        // Cihaz bilgileri
        debugInfo.append("\n📱 DEVICE INFORMATION:\n");
        debugInfo.append("• Android Version: ").append(Build.VERSION.SDK_INT).append(" (").append(Build.VERSION.RELEASE).append(")\n");
        debugInfo.append("• Device: ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL).append("\n");
        debugInfo.append("• Brand: ").append(Build.BRAND).append("\n");
        debugInfo.append("• Product: ").append(Build.PRODUCT).append("\n");

        // Uygulama bilgileri
        debugInfo.append("\n📦 APPLICATION INFORMATION:\n");
        debugInfo.append("• Package: ").append(getPackageName()).append("\n");

        tvDebugInfo.setText(debugInfo.toString());

        // Logcat'e de yaz
        Log.d(TAG, debugInfo.toString());
    }

    private void testNotification() {
        try {
            createTestNotificationChannel();

            Notification notification = new NotificationCompat.Builder(this, Constants.TEST_CHANNEL_ID)
                    .setContentTitle("Test Notification")
                    .setContentText("This is a test notification from debug activity")
                    .setSmallIcon(R.drawable.ic_baseline_timer_24)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setAutoCancel(true)
                    .build();

            notificationManager.notify(Constants.TEST_NOTIFICATION_ID, notification);

            Toast.makeText(this, "Test notification sent! Check your status bar.", Toast.LENGTH_LONG).show();
            Log.d(TAG, "Test notification sent successfully");

        } catch (Exception e) {
            Toast.makeText(this, "Error sending test notification: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "Error sending test notification", e);
        }
    }

    private void createTestNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    Constants.TEST_CHANNEL_ID,
                    "Test Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Channel for testing notifications");
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void checkPermissions() {
        StringBuilder permissionInfo = new StringBuilder();

        permissionInfo.append("🔐 PERMISSION CHECK:\n\n");

        if (notificationManager != null) {
            permissionInfo.append("• Notifications enabled: ").append(notificationManager.areNotificationsEnabled()).append("\n");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                boolean hasDndAccess = notificationManager.isNotificationPolicyAccessGranted();
                permissionInfo.append("• Do Not Disturb access: ").append(hasDndAccess).append("\n");

                if (!hasDndAccess) {
                    permissionInfo.append("\n⚠️ DND access is required for lock screen notifications!\n");
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = notificationManager.getNotificationChannel(Constants.CHANNEL_ID);
                if (channel != null) {
                    permissionInfo.append("• Chronometer channel importance: ").append(getImportanceText(channel.getImportance())).append("\n");
                    permissionInfo.append("• Lock screen visibility: ").append(getVisibilityText(channel.getLockscreenVisibility())).append("\n");

                    if (channel.getImportance() == NotificationManager.IMPORTANCE_NONE) {
                        permissionInfo.append("\n❌ Channel importance is NONE - notifications won't show!\n");
                    }

                    if (channel.getLockscreenVisibility() == Notification.VISIBILITY_PRIVATE) {
                        permissionInfo.append("\n⚠️ Lock screen visibility is PRIVATE - limited information on lock screen\n");
                    } else if (channel.getLockscreenVisibility() == Notification.VISIBILITY_SECRET) {
                        permissionInfo.append("\n❌ Lock screen visibility is SECRET - won't show on lock screen!\n");
                    }
                } else {
                    permissionInfo.append("\n❌ Chronometer channel not found! ID: ").append(Constants.CHANNEL_ID).append("\n");
                }
            }
        }

        new android.app.AlertDialog.Builder(this)
                .setTitle("Permission Check")
                .setMessage(permissionInfo.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private void openNotificationSettings() {
        try {
            Intent intent = new Intent();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
            } else {
                intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                intent.putExtra("app_package", getPackageName());
                intent.putExtra("app_uid", getApplicationInfo().uid);
            }
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Cannot open notification settings", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error opening notification settings", e);
        }
    }

    private String getImportanceText(int importance) {
        switch (importance) {
            case NotificationManager.IMPORTANCE_NONE: return "NONE";
            case NotificationManager.IMPORTANCE_MIN: return "MIN";
            case NotificationManager.IMPORTANCE_LOW: return "LOW";
            case NotificationManager.IMPORTANCE_DEFAULT: return "DEFAULT";
            case NotificationManager.IMPORTANCE_HIGH: return "HIGH";
            case NotificationManager.IMPORTANCE_MAX: return "MAX";
            default: return "UNKNOWN (" + importance + ")";
        }
    }

    private String getVisibilityText(int visibility) {
        switch (visibility) {
            case Notification.VISIBILITY_PUBLIC: return "PUBLIC";
            case Notification.VISIBILITY_PRIVATE: return "PRIVATE";
            case Notification.VISIBILITY_SECRET: return "SECRET";
            default: return "UNKNOWN (" + visibility + ")";
        }
    }
}