package com.teinproductions.tein.countdownwidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Calendar;

public class CountdownWidgetProvider extends AppWidgetProvider {

    public static final String COUNTDOWN_WIDGET_UPDATE = "com.teinproductions.tein.countdownwidget.COUNTDOWN_WIDGET_UPDATE";

    private void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        Calendar date = Calendar.getInstance();
        // I/O
        date.set(Calendar.YEAR, 2015);
        date.set(Calendar.MONTH, Calendar.MAY);
        date.set(Calendar.DAY_OF_MONTH, 28);
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.setTimeInMillis(date.getTimeInMillis()); // for log format

        Calendar current = Calendar.getInstance();
        current.setTimeInMillis(System.currentTimeMillis());

        Log.d("WIDGET", date.toString());
        Log.d("WIDGET", current.toString());

        final long diffInMillis = date.getTimeInMillis() - current.getTimeInMillis();
        Calendar diff = Calendar.getInstance();
        diff.setTimeInMillis(diffInMillis);

        final int YEARS = diff.get(Calendar.YEAR) - 1970;
        Log.d("WIDGET", "YEARS = " + YEARS);
        final int YEARS_IN_DAYS = (int) (YEARS * 365.242);
        Log.d("WIDGET", "YEARS_IN_DAYS = " + YEARS_IN_DAYS);
        final int DAYS = diff.get(Calendar.DAY_OF_YEAR) + YEARS_IN_DAYS;
        Log.d("WIDGET", "DAY_OF_YEAR = " + diff.get(Calendar.DAY_OF_YEAR));
        Log.d("WIDGET", "DAYS = " + DAYS);
        final int HOURS = diff.get(Calendar.HOUR_OF_DAY);
        Log.d("WIDGET", "HOURS = " + HOURS);
        final int MINUTES = diff.get(Calendar.MINUTE);
        Log.d("WIDGET", "MINUTES = " + MINUTES);

        String string = DAYS + "d" + HOURS + "h" + MINUTES + "m";
        SpannableString ss = new SpannableString(string);
        for (String letter : new String[]{"d", "h", "m"}) {
            ss.setSpan(new RelativeSizeSpan(0.50f), string.indexOf(letter), string.indexOf(letter) + 1, 0);
        }

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.countdown_widget);
        views.setTextViewText(R.id.countdown_textView, ss);

        Log.d("WIDGET", "text: " + ss);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(COUNTDOWN_WIDGET_UPDATE)) {
            ComponentName componentName = new ComponentName(context.getPackageName(), getClass().getName());
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);
            for (int appWidgetId : appWidgetIds) {
                updateWidget(context, appWidgetManager, appWidgetId);
            }
        }
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Log.d("WIDGET", "onEnabled");

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis() + 1000, 60000, getPendingIntent(context));
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Log.d("WIDGET", "onDisabled");
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(getPendingIntent(context));
    }

    private PendingIntent getPendingIntent(Context context) {
        Intent intent = new Intent(COUNTDOWN_WIDGET_UPDATE);
        return PendingIntent.getBroadcast(context, 123456789, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
