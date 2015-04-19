package com.teinproductions.tein.countdownwidget;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.appwidget.AppWidgetManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RemoteViews;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ConfigurationActivity extends ActionBarActivity {

    static final String FILE_NAME = "dates";

    int appWidgetId;
    Button pickDate, pickTime;

    Calendar date = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);

        // If the Activity is dismissed, the widget mustn't update
        setResult(RESULT_CANCELED);

        pickDate = (Button) findViewById(R.id.pick_date_button);
        pickTime = (Button) findViewById(R.id.pick_time_button);

        date.setTimeInMillis(System.currentTimeMillis());

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }
    }

    public void onClickPickDate(View view) {
        new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                date.set(Calendar.YEAR, year);
                date.set(Calendar.MONTH, month);
                date.set(Calendar.DAY_OF_MONTH, day);

                pickDate.setText(new SimpleDateFormat("dd / mm / yyyy").format(date.getTime()));
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

                pickTime.setText(new SimpleDateFormat("HH:mm").format(date.getTime()));
            }
        }, date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE), true)
                .show();
    }

    public void onClickApply(View view) {
        SQLiteDatabase db = openOrCreateDatabase(FILE_NAME, 0, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS dates(" +
                "appwidgetid INTEGER, date TEXT);");
        ContentValues values = new ContentValues();
        values.put("appwidgetid", appWidgetId);
        values.put("date", Long.toString(date.getTimeInMillis()));
        db.insert("dates", null, values);
        db.close();

        Log.d("WIDGET", "onClickApply: " + date.getTimeInMillis());

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        RemoteViews views = CountdownWidgetProvider.updateWidget(this, appWidgetId);
        appWidgetManager.updateAppWidget(appWidgetId, views);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC,
                System.currentTimeMillis() + 60000, 60000, CountdownWidgetProvider.getPendingIntent(this));

        Intent result = new Intent();
        result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, result);
        finish();
    }
}
