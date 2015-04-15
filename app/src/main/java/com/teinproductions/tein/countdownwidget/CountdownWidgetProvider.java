package com.teinproductions.tein.countdownwidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.widget.RemoteViews;

public class CountdownWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            final int DAYS = 67;
            final int HOURS = 11;
            final int MINUTES = 3;

            String string = DAYS + "d" + HOURS + "h" + MINUTES + "m";

            SpannableString ss = new SpannableString(string);
            for (String letter : new String[]{"d", "h", "m"}) {
                ss.setSpan(new RelativeSizeSpan(0.50f), string.indexOf(letter), string.indexOf(letter) + 1, 0);
            }

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.countdown_widget);
            views.setTextViewText(R.id.countdown_textView, ss);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
