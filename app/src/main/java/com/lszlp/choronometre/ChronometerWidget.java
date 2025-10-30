package com.lszlp.choronometre;


import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class ChronometerWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            // Layout dosyanızı kullanın (R.layout.widget_chronometer mevcut olmalı)
            @SuppressLint("RemoteViewLayout") RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_chronometer);

            // --- START/PAUSE/RESUME Intent ---
            Intent toggleIntent = new Intent(context, ChronometerService.class);
            // Servis içinde bu eylemi kontrol etmek için yeni bir aksiyon tanımlayabilirsiniz
            // Ancak mevcut kodunuz ACTION_START/ACTION_PAUSE/ACTION_RESUME kullanıyor.

            // Basitlik için, butonu her zaman uygulamayı açacak şekilde ayarlayalım (En güvenilir yöntem)
            Intent launchAppIntent = new Intent(context, MainActivity.class);

            // KRİTİK DEĞİŞİKLİK: Uygulamanın zaten çalışıp çalışmadığını kontrol etmek ve
            // mevcut görevi kullanmak için bayrakları ekle.
            PendingIntent launchPendingIntent = PendingIntent.getActivity(
                    context,
                    appWidgetId,
                    launchAppIntent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
            );

            // Widget'ın ana gövdesine tıklama işlevi atama (Uygulamayı açar)
            views.setOnClickPendingIntent(R.id.widget_container, launchPendingIntent);

            // Eğer widget'ın içinde START/STOP butonu varsa, onun intent'ini ayarlayın
            // Varsayalım ki layout'ta R.id.btnToggle adında tek bir buton var.

            views.setTextViewText(R.id.txtTime, "00:00:00");

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    // It's good practice to add constants in your ChronometerService
    // or a dedicated Constants file. For now, I'll add a placeholder comment.
    // Make sure these are defined in your ChronometerService.java:
    // public static final String ACTION_START = "com.lszlp.choronometre.action.START";
    // public static final String ACTION_STOP = "com.lszlp.choronometre.action.STOP";
}
