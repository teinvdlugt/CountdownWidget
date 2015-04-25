package com.teinproductions.tein.countdownwidget;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;

public class Countdown {

    private String name;
    private boolean showName, showDays, showHours, showMinutes, useCapitals;
    private int textSize;
    private long millis;

    public Countdown(long millis, String name, boolean showName, boolean showDays, boolean showHours,
                     boolean showMinutes, boolean useCapitals, int textSize) {
        this.name = name;
        this.showName = showName;
        this.showDays = showDays;
        this.showHours = showHours;
        this.showMinutes = showMinutes;
        this.useCapitals = useCapitals;
        this.textSize = textSize;
        this.millis = millis;
    }

    public ContentValues toContentValues(int appWidgetId) {
        ContentValues values = new ContentValues();
        values.put(ConfigurationActivity.APPWIDGET_ID, appWidgetId);
        values.put(ConfigurationActivity.MILLIS, Long.toString(millis));
        values.put(ConfigurationActivity.NAME, name);
        values.put(ConfigurationActivity.SHOW_NAME, showName ? 1 : 0);
        values.put(ConfigurationActivity.SHOW_DAYS, showDays ? 1 : 0);
        values.put(ConfigurationActivity.SHOW_HOURS, showHours ? 1 : 0);
        values.put(ConfigurationActivity.SHOW_MINUTES, showMinutes ? 1 : 0);
        values.put(ConfigurationActivity.USE_CAPITALS, useCapitals ? 1 : 0);
        values.put(ConfigurationActivity.TEXT_SIZE, textSize);

        return values;
    }

    public static Countdown fromCursor(Cursor cursor) {
        try {
            cursor.moveToFirst();

            int millisColumn = cursor.getColumnIndex(ConfigurationActivity.MILLIS);
            int nameColumn = cursor.getColumnIndex(ConfigurationActivity.NAME);
            int showNameColumn = cursor.getColumnIndex(ConfigurationActivity.SHOW_NAME);
            int showDaysColumn = cursor.getColumnIndex(ConfigurationActivity.SHOW_DAYS);
            int showHoursColumn = cursor.getColumnIndex(ConfigurationActivity.SHOW_HOURS);
            int showMinutesColumn = cursor.getColumnIndex(ConfigurationActivity.SHOW_MINUTES);
            int useCapitalsColumn = cursor.getColumnIndex(ConfigurationActivity.USE_CAPITALS);
            int textSizeColumn = cursor.getColumnIndex(ConfigurationActivity.TEXT_SIZE);

            long millis = Long.parseLong(cursor.getString(millisColumn));
            String name = cursor.getString(nameColumn);
            boolean showName = cursor.getInt(showNameColumn) != 0;
            boolean showDays = cursor.getInt(showDaysColumn) != 0;
            boolean showHours = cursor.getInt(showHoursColumn) != 0;
            boolean showMinutes = cursor.getInt(showMinutesColumn) != 0;
            boolean useCapitals = cursor.getInt(useCapitalsColumn) != 0;
            int textSize = cursor.getInt(textSizeColumn);

            return new Countdown(millis, name, showName, showDays, showHours, showMinutes, useCapitals, textSize);
        } catch (CursorIndexOutOfBoundsException e) {
            return null;
        }
    }

    public static Countdown[] allCountdowns(Context context) {
        SQLiteDatabase db = ConfigurationActivity.getDatabase(context);
        Cursor cursor = db.query(ConfigurationActivity.TABLE_NAME, null, null, null, null, null, ConfigurationActivity.NAME);

        Countdown[] countdowns = new Countdown[cursor.getCount()];

        for (int i = 0; i < countdowns.length; i++) {
            cursor.moveToPosition(i);

            int millisColumn = cursor.getColumnIndex(ConfigurationActivity.MILLIS);
            int nameColumn = cursor.getColumnIndex(ConfigurationActivity.NAME);
            int showNameColumn = cursor.getColumnIndex(ConfigurationActivity.SHOW_NAME);
            int showDaysColumn = cursor.getColumnIndex(ConfigurationActivity.SHOW_DAYS);
            int showHoursColumn = cursor.getColumnIndex(ConfigurationActivity.SHOW_HOURS);
            int showMinutesColumn = cursor.getColumnIndex(ConfigurationActivity.SHOW_MINUTES);
            int useCapitalsColumn = cursor.getColumnIndex(ConfigurationActivity.USE_CAPITALS);
            int textSizeColumn = cursor.getColumnIndex(ConfigurationActivity.TEXT_SIZE);

            long millis = Long.parseLong(cursor.getString(millisColumn));
            String name = cursor.getString(nameColumn);
            boolean showName = cursor.getInt(showNameColumn) != 0;
            boolean showDays = cursor.getInt(showDaysColumn) != 0;
            boolean showHours = cursor.getInt(showHoursColumn) != 0;
            boolean showMinutes = cursor.getInt(showMinutesColumn) != 0;
            boolean useCapitals = cursor.getInt(useCapitalsColumn) != 0;
            int textSize = cursor.getInt(textSizeColumn);

            countdowns[i] = new Countdown(millis, name, showName, showDays, showHours, showMinutes, useCapitals, textSize);
        }

        cursor.close();
        db.close();

        return countdowns;
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

    public boolean isShowDays() {
        return showDays;
    }

    public void setShowDays(boolean showDays) {
        this.showDays = showDays;
    }

    public boolean isShowHours() {
        return showHours;
    }

    public void setShowHours(boolean showHours) {
        this.showHours = showHours;
    }

    public boolean isShowMinutes() {
        return showMinutes;
    }

    public void setShowMinutes(boolean showMinutes) {
        this.showMinutes = showMinutes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isShowName() {
        return showName;
    }

    public void setShowName(boolean showName) {
        this.showName = showName;
    }

    public long getMillis() {
        return millis;
    }

    public void setMillis(long millis) {
        this.millis = millis;
    }
}
