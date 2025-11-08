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
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_chronometer);

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
            // **!!! R.id.widget_container widget'ın en dıştaki (Root) View'ının ID'si olmalıdır !!!**
            views.setOnClickPendingIntent(R.id.widget_container, launchPendingIntent); // <-- BURASI KRİTİK
            // Eğer widget'ın içinde START/STOP butonu varsa, onun intent'ini ayarlayın
            // Varsayalım ki layout'ta R.id.btnToggle adında tek bir buton var.

            views.setTextViewText(R.id.txtTime, "00:00:00");
            views.setTextViewText(R.id.timeUnitText, "Cmin.");
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }


}
