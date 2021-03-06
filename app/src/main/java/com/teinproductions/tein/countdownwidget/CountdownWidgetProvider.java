package com.teinproductions.tein.countdownwidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import java.util.Calendar;

public class CountdownWidgetProvider extends AppWidgetProvider {

    private static final String COUNTDOWN_WIDGET_UPDATE = "com.teinproductions.tein.countdownwidget.COUNTDOWN_WIDGET_UPDATE";

    public static RemoteViews updateWidget(Context context, int appWidgetId) throws NullPointerException, SQLiteException {
        AppWidget appWidget = AppWidget.fromId(context, appWidgetId);
        Countdown countdown = Countdown.fromId(context, appWidget.getCountdownId());

        final boolean showName = countdown.isShowName();
        final String name = countdown.getName();
        final boolean showDays = countdown.isShowDays();
        final boolean showHours = countdown.isShowHours();
        final boolean showMinutes = countdown.isShowMinutes();
        final boolean useCapitals = appWidget.isUseCapitals();
        final int textSize = appWidget.getTextSize();
        final int textColorGreyScale = appWidget.getTextColor();
        final int textColor = Color.rgb(textColorGreyScale, textColorGreyScale, textColorGreyScale);

        Calendar current = Calendar.getInstance();
        current.setTimeInMillis(System.currentTimeMillis());

        String diff = diffInString(current, countdown.date, showDays, showHours, showMinutes, useCapitals);

        SpannableString ss = new SpannableString(diff);
        for (String letter : new String[]{"d", "h", "m", "D", "H", "M"}) {
            if (diff.contains(letter)) {
                ss.setSpan(new RelativeSizeSpan(0.65f), diff.indexOf(letter), diff.indexOf(letter) + 1, 0);
            }
        }

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.countdown_widget);
        views.setViewVisibility(R.id.name_textView, showName ? View.VISIBLE : View.GONE);
        if (Build.VERSION.SDK_INT >= 16) {
            views.setTextViewTextSize(R.id.countdown_textView, TypedValue.COMPLEX_UNIT_SP, textSize);
            views.setTextViewTextSize(R.id.name_textView, TypedValue.COMPLEX_UNIT_SP, textSize / 2.5f);
        }
        views.setTextViewText(R.id.name_textView, name);
        views.setTextViewText(R.id.countdown_textView, ss);
        views.setTextColor(R.id.name_textView, textColor);
        views.setTextColor(R.id.countdown_textView, textColor);

        Intent configIntent = new Intent(context, ConfigurationActivity.class);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        // Just some rubbish to make the intent unique:
        configIntent.setData(Uri.parse("CONFIGURE_THE_WIDGET://widget/id" + appWidgetId));
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);
        views.setOnClickPendingIntent(R.id.root, pendingIntent);

        return views;
    }

    private static String diffInString(Calendar current, Calendar date, boolean showDays,
                                       boolean showHours, boolean showMinutes, boolean useCapitals) {
        long diffInMillis = date.getTimeInMillis() - current.getTimeInMillis();
        boolean negative = false;

        if (diffInMillis < 0) {
            diffInMillis = -diffInMillis;
            negative = true;
        }

        Calendar diff = Calendar.getInstance();
        diff.setTimeInMillis(diffInMillis);

        int YEARS = diff.get(Calendar.YEAR) - 1970;
        int YEARS_IN_DAYS = (int) (YEARS * 365.242);
        int DAYS = diff.get(Calendar.DAY_OF_YEAR) + YEARS_IN_DAYS - 1;
        int HOURS = diff.get(Calendar.HOUR_OF_DAY);
        int MINUTES = diff.get(Calendar.MINUTE);

        if (!showDays) HOURS = HOURS + DAYS * 24;
        if (!showHours) MINUTES = MINUTES + HOURS * 60;

        String days = useCapitals ? "D" : "d";
        String hours = useCapitals ? "H" : "h";
        String minutes = useCapitals ? "M" : "m";

        StringBuilder result = new StringBuilder();
        if (negative) result.append("-");
        if (showDays) result.append(DAYS).append(days);
        if (showHours) result.append(HOURS).append(hours);
        if (showMinutes) result.append(MINUTES).append(minutes);
        return result.toString();
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
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);

        for (int appWidgetId : appWidgetIds) {
            SQLiteDatabase db = Countdown.getDatabase(context);
            db.delete(AppWidget.APPWIDGET_TABLE, AppWidget.APPWIDGET_ID + "=" + appWidgetId, null);
        }
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(getPendingIntent(context));
    }

    public static PendingIntent getPendingIntent(Context context) {
        Intent intent = new Intent(COUNTDOWN_WIDGET_UPDATE);
        return PendingIntent.getBroadcast(context, 123456789, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
