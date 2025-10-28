package com.lszlp.choronometre;

public class Constants {
    public static final String ACTION_TIME_UPDATE = "com.lszlp.choronometre.ACTION_TIME_UPDATE";
    public static final String ACTION_START = "START";
    public static final String ACTION_STOP = "STOP";
    public static final String ACTION_RESET = "RESET";
    public static final String EXTRA_ELAPSED_TIME = "elapsed";
    public static final int REQUEST_DND_ACCESS = 1001;
    public static final String ACTION_PAUSE = "PAUSE";
    public static final String ACTION_RESUME = "RESUME";
    // Notification constants - Tüm sınıflarda kullanılacak
    public static final int NOTIFICATION_ID = 1;
    public static final String CHANNEL_ID = "ChronometerChannel";
    public static final String TEST_CHANNEL_ID = "test_channel";
    public static final int TEST_NOTIFICATION_ID = 9999;

    public static final String ACTION_REQUEST_STATUS = "com.lszlp.choronometre.REQUEST_STATUS"; // Durum isteme eylemi
    public static final String ACTION_STATUS_RESPONSE = "com.lszlp.choronometre.STATUS_RESPONSE"; // Durum yanıtı eylemi
    // Zaman birimi sabitleri
    public static final String EXTRA_TIME_UNIT = "time_unit";
    public static final String EXTRA_MODUL = "modul";
    public static final String EXTRA_MILIS = "milis";

    // Zaman birimi tipleri
    public static final String TIME_UNIT_SECONDS = "Sec.";
    public static final String TIME_UNIT_CMINUTES = "Cmin.";
    public static final String TIME_UNIT_DMINUTES = "Dmh.";

   public static final String EXTRA_IS_RUNNING = "isRunning";
    public static final String EXTRA_IS_PAUSED = "isPaused";
    public static final int REQUEST_STORAGE_PERMISSION = 200;
    public static final int REQUEST_ALL_PERMISSIONS = 201; // Yeni: Tüm izinler için tek bir kod

    // --- YENİ ONDALIK HASSASİYET SABİTLERİ ---
    // SharedPreferences anahtarı
    public static final String PREF_DECIMAL_PLACES = "pref_decimal_places";
    // Varsayılan değer (0 -> 1 ondalık basamak, yani 0.0)
    public static final int DEFAULT_DECIMAL_PLACES = 0;
    public static final String ACTION_DECIMAL_UPDATE = "com.lszlp.choronometre.ACTION_DECIMAL_UPDATE";

}
