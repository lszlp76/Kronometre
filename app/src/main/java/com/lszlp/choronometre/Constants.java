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


    // Zaman birimi sabitleri
    public static final String EXTRA_TIME_UNIT = "time_unit";
    public static final String EXTRA_MODUL = "modul";
    public static final String EXTRA_MILIS = "milis";

    // Zaman birimi tipleri
    public static final int TIME_UNIT_SECONDS = 1;
    public static final int TIME_UNIT_CMINUTES = 2;
    public static final int TIME_UNIT_DMINUTES = 3;

    public static final String ACTION_REQUEST_STATUS = "com.lszlp.choronometre.REQUEST_STATUS"; // Durum isteme
    public static final String EXTRA_IS_RUNNING = "isRunning";
    public static final String EXTRA_IS_PAUSED = "isPaused";
}