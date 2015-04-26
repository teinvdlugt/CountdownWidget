package com.teinproductions.tein.countdownwidget;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
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
import android.widget.ImageButton;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

public class ConfigurationActivity extends AppCompatActivity {

    private AppWidget appWidget;
    private Countdown countdown;

    private Button pickDate, pickTime;
    private EditText nameET;
    private CheckBox showNameCheckBox, showDaysCheckBox, showHoursCheckBox, showMinutesCheckBox, capitalsCheckBox;
    private TextView textSizeTextView;
    private SeekBar textSizeSeekBar;

    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        // If the Activity is dismissed, the widget mustn't update
        setResult(RESULT_CANCELED);

        initViews();
        initAppWidget();
        initCountdown();
        loadCountdownValues();
        loadAppWidgetValues();
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

    private int restoreAppWidgetId() {
        int appWidgetId;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
                finish();
                return -1;
            }
        } else {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            finish();
            return -1;
        }

        return appWidgetId;
    }

    private void initAppWidget() {
        int appWidgetId = restoreAppWidgetId();
        appWidget = AppWidget.fromId(this, appWidgetId);

        if (appWidget == null) {
            appWidget = new AppWidget(appWidgetId);
        }
    }

    private void initCountdown() {
        countdown = Countdown.fromId(this, appWidget.getCountdownId());

        if (countdown == null) {
            countdown = new Countdown();
        }
    }

    private void loadCountdownValues() {
        nameET.setText(countdown.getName());
        nameET.setSelection(nameET.length());
        updateButtonTexts();
        showNameCheckBox.setChecked(countdown.isShowName());
        showDaysCheckBox.setChecked(countdown.isShowDays());
        showHoursCheckBox.setChecked(countdown.isShowHours());
        showMinutesCheckBox.setChecked(countdown.isShowMinutes());
    }

    private void loadAppWidgetValues() {
        capitalsCheckBox.setChecked(appWidget.isUseCapitals());
        textSizeSeekBar.setProgress(appWidget.getTextSize() - 12);
        textSizeTextView.setText(getString(R.string.text_size) + ": " + (textSizeSeekBar.getProgress() + 12));
    }

    public void onClickPickDate(View view) {
        new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                countdown.date.set(Calendar.YEAR, year);
                countdown.date.set(Calendar.MONTH, month);
                countdown.date.set(Calendar.DAY_OF_MONTH, day);

                updateButtonTexts();
            }
        }, countdown.date.get(Calendar.YEAR), countdown.date.get(Calendar.MONTH), countdown.date.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    public void onClickPickTime(View view) {
        new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                countdown.date.set(Calendar.HOUR_OF_DAY, hour);
                countdown.date.set(Calendar.MINUTE, minute);

                updateButtonTexts();
            }
        }, countdown.date.get(Calendar.HOUR_OF_DAY), countdown.date.get(Calendar.MINUTE), true)
                .show();
    }

    private void updateButtonTexts() {
        // Date
        int YEAR = countdown.date.get(Calendar.YEAR);
        int MONTH = countdown.date.get(Calendar.MONTH) + 1;
        int DAY = countdown.date.get(Calendar.DAY_OF_MONTH);
        pickDate.setText(DAY + " / " + MONTH + " / " + YEAR);

        // Time
        int HOUR = countdown.date.get(Calendar.HOUR_OF_DAY);
        int MINUTE = countdown.date.get(Calendar.MINUTE);
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

        countdown.setName(nameET.getText().toString());
        countdown.setShowName(showNameCheckBox.isChecked());
        countdown.setShowDays(showDaysCheckBox.isChecked());
        countdown.setShowHours(showHoursCheckBox.isChecked());
        countdown.setShowMinutes(showMinutesCheckBox.isChecked());

        appWidget.setUseCapitals(capitalsCheckBox.isChecked());
        appWidget.setTextSize(textSizeSeekBar.getProgress() + 12);

        apply();
    }

    private void apply() {
        // ALTER DATABASE
        SQLiteDatabase db = Countdown.getDatabase(this);

        // Delete the stored Countdowns with the same name as the current one
        db.delete(Countdown.COUNTDOWN_TABLE, Countdown.NAME + "=\"" + countdown.getName() + "\"", null);
        db.insert(Countdown.COUNTDOWN_TABLE, Countdown.NAME, countdown.toContentValues());

        // The Countdown object must have an id, so it must be first stored and after that the
        // id must be obtained.
        Countdown tempCountdown = Countdown.fromName(this, countdown.getName());
        int countdownId = tempCountdown.getId();
        appWidget.setCountdownId(countdownId);

        // I don't use db.update() because maybe the appwidget with this id doesn't yet exist
        db.delete(AppWidget.APPWIDGET_TABLE, AppWidget.APPWIDGET_ID + "=" + appWidget.appWidgetId, null);
        db.insert(AppWidget.APPWIDGET_TABLE, null, appWidget.toContentValues());

        db.close();

        // UPDATE WIDGET
        setAlarm();

        // FINISH
        Intent result = new Intent();
        result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidget.appWidgetId);
        setResult(RESULT_OK, result);
        finish();
    }

    private void setAlarm() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        RemoteViews views = CountdownWidgetProvider.updateWidget(this, appWidget.appWidgetId);
        appWidgetManager.updateAppWidget(appWidget.appWidgetId, views);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC,
                System.currentTimeMillis() + 60000, 60000, CountdownWidgetProvider.getPendingIntent(this));
    }

    public void onClickCancel(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void onClickChooseFromExisting(View view) {
        final Countdown[] countdowns = Countdown.allAvailableCountdowns(this);

        if (countdowns.length == 0) {
            Toast.makeText(this, "There is nothing to choose from", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setAdapter(new CountdownAdapter(this, countdowns), null);
        dialog = builder.create();
        dialog.show();
    }

    private class CountdownAdapter extends ArrayAdapter {

        ArrayList<Countdown> countdowns = new ArrayList<>();

        public CountdownAdapter(Context context, Countdown[] countdowns) {
            super(context, R.layout.countdown_list_item);
            Collections.addAll(this.countdowns, countdowns);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final View view = LayoutInflater.from(getContext()).inflate(R.layout.countdown_list_item, parent, false);

            final TextView nameTextView = (TextView) view.findViewById(R.id.name_textView);
            final ImageButton deleteButton = (ImageButton) view.findViewById(R.id.deleteButton);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    countdown = countdowns.get(position);
                    appWidget.setCountdownId(countdown.getId());
                    loadCountdownValues();
                    dialog.dismiss();
                }
            });

            nameTextView.setText(countdowns.get(position).getName());

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int id = countdowns.remove(position).getId();

                    SQLiteDatabase db = Countdown.getDatabase(ConfigurationActivity.this);
                    db.delete(Countdown.COUNTDOWN_TABLE, Countdown.ID + "=" + id, null);

                    notifyDataSetChanged();
                    /*deleteButton.setEnabled(false);
                    nameTextView.setTextColor(getResources().getColor(android.R.color.darker_gray));
                    view.setClickable(false);*/
                }
            });

            return view;
        }

        @Override
        public int getCount() {
            return countdowns.size();
        }
    }
}
