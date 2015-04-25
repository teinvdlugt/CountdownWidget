package com.teinproductions.tein.countdownwidget;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

public class ConfigurationActivity extends AppCompatActivity {

    static final String FILE_NAME = "database";
    static final String TABLE_NAME = "dates";
    static final String APPWIDGET_ID = "appwidgetid";
    static final String MILLIS = "millis";
    static final String NAME = "name";
    static final String SHOW_NAME = "showName";
    static final String SHOW_DAYS = "showDays";
    static final String SHOW_HOURS = "showHours";
    static final String SHOW_MINUTES = "showMinutes";
    static final String USE_CAPITALS = "useCapitals";
    static final String TEXT_SIZE = "textSize";

    private int appWidgetId;

    private Button pickDate, pickTime;
    private EditText nameET;
    private CheckBox showNameCheckBox, showDaysCheckBox, showHoursCheckBox, showMinutesCheckBox, capitalsCheckBox;
    private TextView textSizeTextView;
    private SeekBar textSizeSeekBar;

    private final Calendar date = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        // If the Activity is dismissed, the widget mustn't update
        setResult(RESULT_CANCELED);

        initViews();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        restoreValues();
    }

    private void initViews() {
        pickDate = (Button) findViewById(R.id.pick_date_button);
        pickTime = (Button) findViewById(R.id.pick_time_button);
        nameET = (EditText) findViewById(R.id.name_editText);
        showNameCheckBox = (CheckBox) findViewById(R.id.showName_checkBox);
        showDaysCheckBox = (CheckBox) findViewById(R.id.showDays_checkBox);
        showHoursCheckBox = (CheckBox) findViewById(R.id.showHours_checkBox);
        showMinutesCheckBox = (CheckBox) findViewById(R.id.showMinutes_checkBox);
        capitalsCheckBox = (CheckBox) findViewById(R.id.capitals_checkBox);
        textSizeSeekBar = (SeekBar) findViewById(R.id.textSize_seekBar);
        textSizeTextView = (TextView) findViewById(R.id.textSize_textView);

        if (Build.VERSION.SDK_INT < 16) {
            textSizeTextView.setVisibility(View.GONE);
            textSizeSeekBar.setVisibility(View.GONE);
        } else {
            textSizeSeekBar.setMax(64); // Max text size is 76sp
            textSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                    int textSize = progress + 12;
                    textSizeTextView.setText(getString(R.string.text_size) + ": " + textSize);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
        }
    }

    private void restoreValues() {
        SQLiteDatabase db = getDatabase(this);
        Cursor cursor = queryEverything(db, appWidgetId);

        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
            Countdown countdown = Countdown.fromCursor(cursor);
            loadCountdown(countdown, true);
        } else {
            date.setTimeInMillis(System.currentTimeMillis());
            textSizeTextView.setText(getString(R.string.text_size) + ": 56");
            updateButtonTexts();
        }

        cursor.close();
        db.close();
    }

    /**
     * @param countdown The Countdown object to load onto the views
     * @param changeFontSize If false, the font size will not be changed
     */
    private void loadCountdown(Countdown countdown, boolean changeFontSize) {
        nameET.setText(countdown.getName());
        nameET.setSelection(nameET.length());
        date.setTimeInMillis(countdown.getMillis());
        updateButtonTexts();
        showNameCheckBox.setChecked(countdown.isShowName());
        showDaysCheckBox.setChecked(countdown.isShowDays());
        showHoursCheckBox.setChecked(countdown.isShowHours());
        showMinutesCheckBox.setChecked(countdown.isShowMinutes());
        capitalsCheckBox.setChecked(countdown.isUseCapitals());

        if (changeFontSize) {
            textSizeSeekBar.setProgress(countdown.getTextSize() - 12);
            textSizeTextView.setText(getString(R.string.text_size) + ": " + (textSizeSeekBar.getProgress() + 12));
        }
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

        final long millis = date.getTimeInMillis();
        final String name = nameET.getText().toString();
        final boolean showName = showNameCheckBox.isChecked();
        final boolean showDays = showDaysCheckBox.isChecked();
        final boolean showHours = showHoursCheckBox.isChecked();
        final boolean showMinutes = showMinutesCheckBox.isChecked();
        final boolean useCapitals = capitalsCheckBox.isChecked();
        final int textSize = textSizeSeekBar.getProgress() + 12;

        apply(new Countdown(millis, name, showName, showDays, showHours, showMinutes, useCapitals, textSize));
    }

    private void apply(Countdown countdown) {
        // ALTER DATABASE
        SQLiteDatabase db = getDatabase(this);
        db.delete(TABLE_NAME, APPWIDGET_ID + "=" + appWidgetId, null);
        db.insert(TABLE_NAME, null, countdown.toContentValues(appWidgetId));
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

    public void onClickChooseFromExisting(View view) {
        final Countdown[] countdowns = Countdown.allCountdowns(this);
        String[] names = new String[countdowns.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = countdowns[i].getName();
        }

        new AlertDialog.Builder(this)
                .setAdapter(new CountdownAdapter(this, countdowns), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int position) {
                        loadCountdown(countdowns[position], false);
                    }
                }).create().show();
    }

    public static SQLiteDatabase getDatabase(Context context) {
        SQLiteDatabase db = context.openOrCreateDatabase(FILE_NAME, 0, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" +
                APPWIDGET_ID + " INTEGER," + MILLIS + " TEXT," + NAME + " TEXT," + SHOW_NAME + " INTEGER," +
                SHOW_DAYS + " INTEGER," + SHOW_HOURS + " INTEGER," + SHOW_MINUTES + " INTEGER," +
                USE_CAPITALS + " INTEGER," + TEXT_SIZE + " INTEGER);");
        return db;
    }

    public static Cursor queryEverything(SQLiteDatabase db, int appWidgetId) {
        return db.query(TABLE_NAME, null,
                APPWIDGET_ID + "=" + appWidgetId, null, null, null, null);
    }

    private static class CountdownAdapter extends ArrayAdapter {

        Countdown[] countdowns;

        public CountdownAdapter(Context context, Countdown[] countdowns) {
            super(context, R.layout.countdown_list_item);
            this.countdowns = countdowns;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.countdown_list_item, parent, false);

            ((TextView) view.findViewById(R.id.name_textView)).setText(countdowns[position].getName());

            return view;
        }

        @Override
        public int getCount() {
            return countdowns.length;
        }
    }
}
