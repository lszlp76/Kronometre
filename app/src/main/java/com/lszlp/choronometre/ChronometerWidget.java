package com.lszlp.choronometre;


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

            // Uygulamayı açmak için Intent oluştur
            Intent launchAppIntent = new Intent(context, MainActivity.class);

            // KRİTİK DEĞİŞİKLİK: Uygulama zaten çalışıyorsa yeni bir görev başlatmak yerine
            // mevcut olanı ön plana getirmek için bu bayrakları ekleyin.
            launchAppIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);


            // PendingIntent oluştur
            PendingIntent launchPendingIntent = PendingIntent.getActivity(
                    context,
                    appWidgetId, // Her widget için benzersiz bir istek kodu kullan
                    launchAppIntent,
                    // Güvenlik ve davranış için doğru bayrakları ayarla
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
            );

            // Widget'ın ana kapsayıcısına tıklama dinleyicisini ata
            views.setOnClickPendingIntent(R.id.widget_container, launchPendingIntent);

            // Metin görünümlerini başlangıç değerleriyle güncelle
            views.setTextViewText(R.id.txtTime, "00:00:00");
            // R.id.timeUnitText yerine R.id.timeUnitTextWidget kullanılmalı (layout'a göre)
            views.setTextViewText(R.id.timeUnitTextWidget, "Cmin.");

            // Widget'ı güncelle
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
