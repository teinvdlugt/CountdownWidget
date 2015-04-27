package com.teinproductions.tein.countdownwidget;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public class AppWidget {

    static final String APPWIDGET_TABLE = "appwidgets";
    static final String APPWIDGET_ID = "appwidget_id";
    static final String USE_CAPITALS = "use_capitals";
    static final String TEXT_SIZE = "text_size";
    static final String TEXT_COLOR = "text_color";
    static final String COUNTDOWN_ID = "countdown_id";

    public static AppWidget fromId(Context context, int appWidgetId) {
        SQLiteDatabase db = Countdown.getDatabase(context);
        Cursor cursor = db.query(APPWIDGET_TABLE, null, APPWIDGET_ID + "=" + appWidgetId, null, null, null, null);
        cursor.moveToFirst();

        AppWidget toReturn = fromCurrentCursorPosition(cursor);

        cursor.close();
        db.close();
        return toReturn;
    }

    public static AppWidget fromCurrentCursorPosition(Cursor cursor) {
        try {
            int appWidgetIdColumn = cursor.getColumnIndex(APPWIDGET_ID);
            int useCapitalsColumn = cursor.getColumnIndex(USE_CAPITALS);
            int textSizeColumn = cursor.getColumnIndex(TEXT_SIZE);
            int textColorColumn = cursor.getColumnIndex(TEXT_COLOR);
            int countdownIdColumn = cursor.getColumnIndex(COUNTDOWN_ID);

            int appWidgetId = cursor.getInt(appWidgetIdColumn);
            boolean useCapitals = cursor.getInt(useCapitalsColumn) != 0;
            int textSize = cursor.getInt(textSizeColumn);
            int textColor = cursor.getInt(textColorColumn);
            int countdownId = cursor.getInt(countdownIdColumn);

            return new AppWidget(appWidgetId, countdownId, textSize, textColor, useCapitals);
        } catch (SQLiteException | CursorIndexOutOfBoundsException e) {
            return null;
        }
    }

    public static AppWidget[] allAppWidgets(Context context) {
        SQLiteDatabase db = Countdown.getDatabase(context);
        Cursor cursor = db.query(APPWIDGET_TABLE, null, null, null, null, null, null);

        AppWidget[] result = new AppWidget[cursor.getCount()];
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            result[i] = fromCurrentCursorPosition(cursor);
        }

        return result;
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(APPWIDGET_ID, appWidgetId);
        values.put(USE_CAPITALS, useCapitals ? 1 : 0);
        values.put(TEXT_SIZE, textSize);
        values.put(TEXT_COLOR, textColor);
        values.put(COUNTDOWN_ID, countdownId);

        return values;
    }


    private int countdownId = -1, textSize = 56, textColor = 255;
    public final int appWidgetId;
    private boolean useCapitals = false;

    public AppWidget(int appWidgetId, int countdownId, int textSize, int textColor, boolean useCapitals) {
        this.appWidgetId = appWidgetId;
        this.countdownId = countdownId;
        this.textSize = textSize;
        this.textColor = textColor;
        this.useCapitals = useCapitals;
    }

    public AppWidget(int appWidgetId) {
        this.appWidgetId = appWidgetId;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public int getCountdownId() {
        return countdownId;
    }

    public void setCountdownId(int countdownId) {
        this.countdownId = countdownId;
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public boolean isUseCapitals() {
        return useCapitals;
    }

    public void setUseCapitals(boolean useCapitals) {
        this.useCapitals = useCapitals;
    }
}
