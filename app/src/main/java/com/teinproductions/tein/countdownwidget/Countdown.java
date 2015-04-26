package com.teinproductions.tein.countdownwidget;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.util.ArrayList;
import java.util.Calendar;

public class Countdown {

    static final String DATABASE_NAME = "database";
    static final String COUNTDOWN_TABLE = "countdowns";
    static final String ID = "id";
    static final String MILLIS = "millis";
    static final String NAME = "name";
    static final String SHOW_NAME = "show_name";
    static final String SHOW_DAYS = "show_days";
    static final String SHOW_HOURS = "show_hours";
    static final String SHOW_MINUTES = "show_minutes";

    /**
     * Converts the data of this Countdown object to a ContentValues object,
     * to store it in the database. This doesn't include the id of the Countdown!
     *
     * @return The ContentValues object which you can store in the database
     */
    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(MILLIS, Long.toString(date.getTimeInMillis()));
        values.put(NAME, name);
        values.put(SHOW_NAME, showName ? 1 : 0);
        values.put(SHOW_DAYS, showDays ? 1 : 0);
        values.put(SHOW_HOURS, showHours ? 1 : 0);
        values.put(SHOW_MINUTES, showMinutes ? 1 : 0);

        return values;
    }

    public static Countdown fromAppwidgetId(Context context, int appWidgetId) {
        SQLiteDatabase db = getDatabase(context);

        Cursor widgetCursor = db.query(AppWidget.APPWIDGET_TABLE, new String[]{AppWidget.APPWIDGET_ID, AppWidget.COUNTDOWN_ID},
                AppWidget.APPWIDGET_ID + "=" + appWidgetId, null, null, null, null);
        widgetCursor.moveToFirst();
        int countdownId = widgetCursor.getInt(widgetCursor.getColumnIndex(AppWidget.COUNTDOWN_ID));
        widgetCursor.close();

        Cursor countdownCursor = db.query(COUNTDOWN_TABLE, null, ID + "=" + countdownId, null, null, null, null);
        countdownCursor.moveToFirst();
        Countdown toReturn = fromCurrentCursorPosition(countdownCursor);

        countdownCursor.close();
        db.close();
        return toReturn;
    }

    public static Countdown fromId(Context context, int id) {
        SQLiteDatabase db = getDatabase(context);
        Cursor cursor = db.query(COUNTDOWN_TABLE, null, ID + "=" + id, null, null, null, null);
        cursor.moveToFirst();

        Countdown toReturn = fromCurrentCursorPosition(cursor);

        cursor.close();
        db.close();
        return toReturn;
    }

    /**
     * @param context The application context
     * @param name    The name to search for in the database
     * @return The first occurrence of a Countdown with the given name. Usually there is only one
     * Countdown with that name.
     */
    public static Countdown fromName(Context context, String name) {
        SQLiteDatabase db = getDatabase(context);
        Cursor cursor = db.query(COUNTDOWN_TABLE, null, NAME + "=\"" + name + "\"", null, null, null, null);
        cursor.moveToFirst();

        Countdown toReturn = fromCurrentCursorPosition(cursor);

        cursor.close();
        db.close();
        return toReturn;
    }

    /**
     * Gets all the Countdown objects saved in the database
     *
     * @param context The application context
     * @return An array of all saved countdowns
     */
    public static Countdown[] allCountdowns(Context context) {
        SQLiteDatabase db = getDatabase(context);
        Cursor cursor = db.query(COUNTDOWN_TABLE, null, null, null, null, null, NAME);

        Countdown[] countdowns = new Countdown[cursor.getCount()];

        for (int i = 0; i < countdowns.length; i++) {
            cursor.moveToPosition(i);
            countdowns[i] = fromCurrentCursorPosition(cursor);
        }

        cursor.close();
        db.close();
        return countdowns;
    }

    /**
     * Gets the countdown object from a cursor, which must already be in the right position.
     *
     * @param cursor The cursor to get the Countdown object from
     * @return The Countdown object retrieved from the cursor's current position
     */
    public static Countdown fromCurrentCursorPosition(Cursor cursor) {
        try {
            int idColumn = cursor.getColumnIndex(ID);
            int millisColumn = cursor.getColumnIndex(MILLIS);
            int nameColumn = cursor.getColumnIndex(NAME);
            int showNameColumn = cursor.getColumnIndex(SHOW_NAME);
            int showDaysColumn = cursor.getColumnIndex(SHOW_DAYS);
            int showHoursColumn = cursor.getColumnIndex(SHOW_HOURS);
            int showMinutesColumn = cursor.getColumnIndex(SHOW_MINUTES);

            int id = cursor.getInt(idColumn);
            long millis = Long.parseLong(cursor.getString(millisColumn));
            String name = cursor.getString(nameColumn);
            boolean showName = cursor.getInt(showNameColumn) != 0;
            boolean showDays = cursor.getInt(showDaysColumn) != 0;
            boolean showHours = cursor.getInt(showHoursColumn) != 0;
            boolean showMinutes = cursor.getInt(showMinutesColumn) != 0;

            return new Countdown(id, millis, name, showName, showDays, showHours, showMinutes);
        } catch (SQLiteException | CursorIndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * Prepares the database to contain all of the necessary tables.
     *
     * @param context The context
     * @return The database with the necessary tables all set
     */
    public static SQLiteDatabase getDatabase(Context context) {
        SQLiteDatabase db = context.openOrCreateDatabase(DATABASE_NAME, 0, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS " + COUNTDOWN_TABLE + "(" +
                ID + " INTEGER PRIMARY KEY," + MILLIS + " TEXT," + NAME + " TEXT," + SHOW_NAME + " INTEGER," +
                SHOW_DAYS + " INTEGER," + SHOW_HOURS + " INTEGER," + SHOW_MINUTES + " INTEGER);");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + AppWidget.APPWIDGET_TABLE + "(" +
                AppWidget.APPWIDGET_ID + " INTEGER," + AppWidget.USE_CAPITALS + " INTEGER," + AppWidget.TEXT_SIZE + " INTEGER," +
                AppWidget.COUNTDOWN_ID + " INTEGER, FOREIGN KEY(" + AppWidget.COUNTDOWN_ID +
                ") REFERENCES " + COUNTDOWN_TABLE + "(" + ID + "));");

        return db;
    }

    /**
     * Returns all stored Countdown objects that are currently not in use by an appwidget.
     *
     * @param context The application context
     * @return An array of the available countdowns
     */
    public static Countdown[] allAvailableCountdowns(Context context) {
        Countdown[] countdowns = allCountdowns(context);
        ArrayList<Countdown> result = new ArrayList<>();

        AppWidget[] appWidgets = AppWidget.allAppWidgets(context);
        ArrayList<Integer> countdownIds = new ArrayList<>();
        for (AppWidget appWidget : appWidgets) {
            countdownIds.add(appWidget.getCountdownId());
        }

        for (Countdown countdown : countdowns) {
            if (!countdownIds.contains(countdown.getId())) {
                result.add(countdown);
            }
        }

        Countdown[] toReturn = new Countdown[result.size()];
        for (int i = 0; i < toReturn.length; i++) {
            toReturn[i] = result.get(i);
        }
        return toReturn;
    }


    private String name = "";
    private boolean showName = true, showDays = true, showHours = true, showMinutes = true;
    public final Calendar date = Calendar.getInstance();
    private int id;

    public Countdown(int id, long millis, String name, boolean showName, boolean showDays, boolean showHours,
                     boolean showMinutes) {
        this.id = id;
        this.date.setTimeInMillis(millis);
        this.name = name;
        this.showName = showName;
        this.showDays = showDays;
        this.showHours = showHours;
        this.showMinutes = showMinutes;
    }

    public Countdown() {
        this.date.setTimeInMillis(System.currentTimeMillis());
    }

    public int getId() {
        return id;
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
}
