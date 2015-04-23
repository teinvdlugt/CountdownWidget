package com.teinproductions.tein.countdownwidget;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.appwidget.AppWidgetManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

public class ConfigurationActivity extends AppCompatActivity {

    static final String FILE_NAME = "database";

    int appWidgetId;

    Button pickDate, pickTime;
    EditText nameET;
    CheckBox showNameCheckBox, showDaysCheckBox, showHoursCheckBox, showMinutesCheckBox;

    Calendar date = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        // If the Activity is dismissed, the widget mustn't update
        setResult(RESULT_CANCELED);

        pickDate = (Button) findViewById(R.id.pick_date_button);
        pickTime = (Button) findViewById(R.id.pick_time_button);
        nameET = (EditText) findViewById(R.id.name_editText);
        showNameCheckBox = (CheckBox) findViewById(R.id.showName_checkBox);
        showDaysCheckBox = (CheckBox) findViewById(R.id.showDays_checkBox);
        showHoursCheckBox = (CheckBox) findViewById(R.id.showHours_checkBox);
        showMinutesCheckBox = (CheckBox) findViewById(R.id.showMinutes_checkBox);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        restoreValues();
    }

    private void restoreValues() {
        SQLiteDatabase db = getDatabase(this);
        Cursor cursor = queryEverything(db, appWidgetId);

        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
            Countdown countdown = fromCursor(cursor);

            nameET.setText(countdown.getName());
            nameET.setSelection(nameET.length());
            date.setTimeInMillis(countdown.getMillis());
            showNameCheckBox.setChecked(countdown.isShowName());
            showDaysCheckBox.setChecked(countdown.isShowDays());
            showHoursCheckBox.setChecked(countdown.isShowHours());
            showMinutesCheckBox.setChecked(countdown.isShowMinutes());
        } else {
            date.setTimeInMillis(System.currentTimeMillis());
        }

        cursor.close();
        db.close();
        updateButtonTexts();
    }

    public void onClickPickDate(View view) {
        new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                date.set(Calendar.YEAR, year);
                date.set(Calendar.MONTH, month);
                date.set(Calendar.DAY_OF_MONTH, day);

                updateButtonTexts();
            }
        }, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    public void onClickPickTime(View view) {
        new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                date.set(Calendar.HOUR_OF_DAY, hour);
                date.set(Calendar.MINUTE, minute);

                updateButtonTexts();
            }
        }, date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE), true)
                .show();
    }

    private void updateButtonTexts() {
        // Date
        int YEAR = date.get(Calendar.YEAR);
        int MONTH = date.get(Calendar.MONTH) + 1;
        int DAY = date.get(Calendar.DAY_OF_MONTH);
        pickDate.setText(DAY + " / " + MONTH + " / " + YEAR);

        // Time
        int HOUR = date.get(Calendar.HOUR_OF_DAY);
        int MINUTE = date.get(Calendar.MINUTE);
        String hourStr = Integer.toString(HOUR);
        String minuteStr = Integer.toString(MINUTE);

        if (hourStr.length() < 2) {
            hourStr = "0" + hourStr;
        }
        if (minuteStr.length() < 2) {
            minuteStr = "0" + minuteStr;
        }

        pickTime.setText(hourStr + ":" + minuteStr);
    }

    public void onClickApply(View view) {
        if (nameET.getText().toString().trim().length() < 1) {
            Toast.makeText(this, getString(R.string.please_provide_a_name), Toast.LENGTH_SHORT).show();
            return;
        }

        final String name = nameET.getText().toString();
        final long millis = date.getTimeInMillis();
        final boolean showName = showNameCheckBox.isChecked();
        final boolean showDays = showDaysCheckBox.isChecked();
        final boolean showHours = showHoursCheckBox.isChecked();
        final boolean showMinutes = showMinutesCheckBox.isChecked();

        apply(name, millis, showName, showDays, showHours, showMinutes);
    }

    private void apply(String name, long millis, boolean showName,
                       boolean showDays, boolean showHours, boolean showMinutes) {
        // ALTER DATABASE
        SQLiteDatabase db = getDatabase(this);
        db.delete("dates", "appwidgetid=" + appWidgetId, null);
        db.insert("dates", null, createContentValues(
                appWidgetId, name, millis, showName, showDays, showHours, showMinutes));
        db.close();

        // UPDATE WIDGET
        setAlarm();

        // FINISH
        Intent result = new Intent();
        result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, result);
        finish();
    }

    private void setAlarm() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        RemoteViews views = CountdownWidgetProvider.updateWidget(this, appWidgetId);
        appWidgetManager.updateAppWidget(appWidgetId, views);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC,
                System.currentTimeMillis() + 60000, 60000, CountdownWidgetProvider.getPendingIntent(this));
    }

    public void onClickCancel(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

    public static SQLiteDatabase getDatabase(Context context) {
        SQLiteDatabase db = context.openOrCreateDatabase(FILE_NAME, 0, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS dates(" +
                "appwidgetid INTEGER, date TEXT, name TEXT, showName INTEGER, " +
                "showDays INTEGER, showHours INTEGER, showMinutes INTEGER);");
        return db;
    }

    public static Cursor queryEverything(SQLiteDatabase db, int appWidgetId) {
        return db.query("dates", null,
                "appwidgetid = " + appWidgetId, null, null, null, null);
    }

    public static Countdown fromCursor(Cursor cursor) {
        try {
            cursor.moveToFirst();

            int dateColumn = cursor.getColumnIndex("date");
            int nameColumn = cursor.getColumnIndex("name");
            int showNameColumn = cursor.getColumnIndex("showName");
            int showDaysColumn = cursor.getColumnIndex("showDays");
            int showHoursColumn = cursor.getColumnIndex("showHours");
            int showMinutesColumn = cursor.getColumnIndex("showMinutes");

            long millis = Long.parseLong(cursor.getString(dateColumn));
            String name = cursor.getString(nameColumn);
            boolean showName = cursor.getInt(showNameColumn) != 0;
            boolean showDays = cursor.getInt(showDaysColumn) != 0;
            boolean showHours = cursor.getInt(showHoursColumn) != 0;
            boolean showMinutes = cursor.getInt(showMinutesColumn) != 0;

            return new Countdown(name, showName, showDays, showHours, showMinutes, millis);
        } catch (CursorIndexOutOfBoundsException e) {
            return null;
        }
    }

    public static ContentValues createContentValues(int appWidgetId, String name, long millis, boolean showName,
                                                    boolean showDays, boolean showHours, boolean showMinutes) {
        ContentValues values = new ContentValues();
        values.put("appwidgetid", appWidgetId);
        values.put("date", Long.toString(millis));
        values.put("name", name);
        values.put("showName", showName ? 1 : 0);
        values.put("showDays", showDays ? 1 : 0);
        values.put("showHours", showHours ? 1 : 0);
        values.put("showMinutes", showMinutes ? 1 : 0);

        return values;
    }
}
