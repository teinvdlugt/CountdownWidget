package com.teinproductions.tein.countdownwidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Calendar;

public class CountdownWidgetProvider extends AppWidgetProvider {

    public static final String COUNTDOWN_WIDGET_UPDATE = "com.teinproductions.tein.countdownwidget.COUNTDOWN_WIDGET_UPDATE";

    public static RemoteViews updateWidget(Context context, int appWidgetId) {
        Log.d("WIDGET", "updateWidget");

        Calendar date = Calendar.getInstance();
        try {
            long timeMillis = getSavedMillis(context, appWidgetId);
            date.setTimeInMillis(timeMillis);
        } catch (SQLiteException e) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.countdown_widget);
            views.setTextViewText(R.id.countdown_textView, "No date set");
            return views;
        }

        Calendar current = Calendar.getInstance();
        current.setTimeInMillis(System.currentTimeMillis());

        String diff = diffInString(current, date);

        SpannableString ss = new SpannableString(diff);
        for (String letter : new String[]{"d", "h", "m"}) {
            ss.setSpan(new RelativeSizeSpan(0.65f), diff.indexOf(letter), diff.indexOf(letter) + 1, 0);
        }

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.countdown_widget);
        views.setTextViewText(R.id.countdown_textView, ss);

        Intent configIntent = new Intent(context, ConfigurationActivity.class);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        // Just some rubbish to make the intent unique:
        configIntent.setData(Uri.parse("CONFIGURE_THE_WIDGET://widget/id" + appWidgetId));
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);
        views.setOnClickPendingIntent(R.id.root, pendingIntent);

        Log.d("WIDGET", "text: " + ss);

        return views;
    }

    public static long getSavedMillis(Context context, int appWidgetId) throws SQLiteException {
        SQLiteDatabase db = context.openOrCreateDatabase(ConfigurationActivity.FILE_NAME, 0, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS dates(" +
                "appwidgetid INTEGER, date TEXT);");
        //Cursor cursor = db.rawQuery("SELECT * FROM dates WHERE appwidgetid=" + appWidgetId, null);
        Cursor cursor = db.query("dates", new String[]{"appwidgetid", "date"},
                "appwidgetid = " + appWidgetId, null, null, null, null);

        if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
            int dateColumn = cursor.getColumnIndex("date");
            long toReturn = Long.parseLong(cursor.getString(dateColumn));
            cursor.close();
            db.close();
            return toReturn;
        }

        Log.d("WIDGET", "ERROR WITH DATABASE");
        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return 0;
    }

    public static String diffInString(Calendar current, Calendar date) {
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

        return DAYS + "d" + HOURS + "h" + MINUTES + "m";
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(COUNTDOWN_WIDGET_UPDATE)) {
            ComponentName componentName = new ComponentName(context.getPackageName(), getClass().getName());
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);
            for (int appWidgetId : appWidgetIds) {
                RemoteViews views = updateWidget(context, appWidgetId);
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        }
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Log.d("WIDGET", "onEnabled");
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Log.d("WIDGET", "onDisabled");
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(getPendingIntent(context));
    }

    public static PendingIntent getPendingIntent(Context context) {
        Intent intent = new Intent(COUNTDOWN_WIDGET_UPDATE);
        return PendingIntent.getBroadcast(context, 123456789, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
