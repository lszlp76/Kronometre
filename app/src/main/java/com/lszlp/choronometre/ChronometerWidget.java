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
            @SuppressLint("RemoteViewLayout") RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_chronometer);

            // --- START Intent: Make it unique for this widget instance ---
            Intent startIntent = new Intent(context, ChronometerService.class);
            // Use a constant for the action to avoid typos
            startIntent.setAction(Constants.ACTION_START);
            // Add the widget ID as an extra so the service knows which widget to update
            startIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            // Use the appWidgetId as the request code to ensure the PendingIntent is unique
            PendingIntent startPendingIntent = PendingIntent.getService(
                    context,
                    appWidgetId, // Unique request code
                    startIntent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
            );

            // --- STOP Intent: Also make it unique ---
            Intent stopIntent = new Intent(context, ChronometerService.class);
            // Use a constant for the action
            stopIntent.setAction(Constants.ACTION_STOP);
            stopIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            // Use a different unique request code for the stop action
            PendingIntent stopPendingIntent = PendingIntent.getService(
                    context,
                    appWidgetId + 1, // A simple way to ensure the request code is different from start
                    stopIntent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
            );

            views.setOnClickPendingIntent(R.id.btnStart, startPendingIntent);
            views.setOnClickPendingIntent(R.id.btnStop, stopPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    // It's good practice to add constants in your ChronometerService
    // or a dedicated Constants file. For now, I'll add a placeholder comment.
    // Make sure these are defined in your ChronometerService.java:
    // public static final String ACTION_START = "com.lszlp.choronometre.action.START";
    // public static final String ACTION_STOP = "com.lszlp.choronometre.action.STOP";
}
