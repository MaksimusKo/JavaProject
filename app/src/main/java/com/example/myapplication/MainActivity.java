package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.room.Room;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final int NOTIFICATION_PERMISSION_CODE = 1001;

    private EditText medNameEditText;
    private TextView timeTextView;
    private TextView endDateTextView;
    private Button selectTimeButton, setReminderButton, openCalendarButton, selectEndDateButton;
    private CheckBox indefiniteCheckBox;

    private int selectedHour = -1;
    private int selectedMinute = -1;
    private String selectedEndDate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        medNameEditText = findViewById(R.id.medNameEditText);
        timeTextView = findViewById(R.id.timeTextView);
        endDateTextView = findViewById(R.id.endDateTextView);
        selectTimeButton = findViewById(R.id.selectTimeButton);
        setReminderButton = findViewById(R.id.setReminderButton);
        openCalendarButton = findViewById(R.id.openCalendarButton);
        selectEndDateButton = findViewById(R.id.selectEndDateButton);
        indefiniteCheckBox = findViewById(R.id.indefiniteCheckBox);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            }
        }

        selectTimeButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this,
                    (view, hourOfDay, minute1) -> {
                        selectedHour = hourOfDay;
                        selectedMinute = minute1;
                        timeTextView.setText(String.format("Selected Time: %02d:%02d", selectedHour, selectedMinute));
                    }, hour, minute, true);
            timePickerDialog.show();
        });

        selectEndDateButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(year, month, dayOfMonth);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        selectedEndDate = sdf.format(selectedDate.getTime());
                        endDateTextView.setText("End Date: " + selectedEndDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        indefiniteCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedEndDate = null;
                endDateTextView.setText("End Date: Indefinite");
                selectEndDateButton.setEnabled(false);
            } else {
                selectEndDateButton.setEnabled(true);
                endDateTextView.setText("Select End Date");
            }
        });

        setReminderButton.setOnClickListener(v -> {
            String medName = medNameEditText.getText().toString().trim();
            if (medName.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter medication name", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedHour == -1 || selectedMinute == -1) {
                Toast.makeText(MainActivity.this, "Please select a time", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!indefiniteCheckBox.isChecked() && selectedEndDate == null) {
                Toast.makeText(MainActivity.this, "Please select an end date or choose indefinite", Toast.LENGTH_SHORT).show();
                return;
            }

            String startDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(new Date());

            Reminder reminder = new Reminder(medName, selectedHour, selectedMinute, startDate, selectedEndDate);

            new Thread(() -> {
                ReminderDatabase db = ReminderDatabase.getInstance(MainActivity.this);
                long newId = db.reminderDao().insertReminder(reminder);
                reminder.id = (int) newId;

                scheduleNotification(reminder);

                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Reminder set for " + medName, Toast.LENGTH_SHORT).show();
                    medNameEditText.setText("");
                    timeTextView.setText("Selected Time: ");
                    endDateTextView.setText("Select End Date");
                    selectedHour = -1;
                    selectedMinute = -1;
                    selectedEndDate = null;
                    indefiniteCheckBox.setChecked(false);
                });
            }).start();
        });

        openCalendarButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
            startActivity(intent);
        });
    }

    private void scheduleNotification(Reminder reminder) {
        Intent intent = new Intent(MainActivity.this, ReminderReceiver.class);
        intent.putExtra("medName", reminder.medicationName);
        intent.putExtra("time", String.format("%02d:%02d", reminder.hour, reminder.minute));
        intent.putExtra("reminderId", reminder.id);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                MainActivity.this,
                reminder.id, // Используем reminder.id как requestCode
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, reminder.hour);
        calendar.set(Calendar.MINUTE, reminder.minute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        long triggerAtMillis = calendar.getTimeInMillis();

        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    scheduleExactAlarm(alarmManager, triggerAtMillis, pendingIntent);
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Please allow exact alarms for proper reminders.", Toast.LENGTH_LONG).show();
                        Intent intentSettings = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                        startActivity(intentSettings);
                    });
                }
            } else {
                scheduleExactAlarm(alarmManager, triggerAtMillis, pendingIntent);
            }
        }
    }


    private void scheduleExactAlarm(AlarmManager alarmManager, long triggerAtMillis, PendingIntent pendingIntent) {
        try {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
            );
        } catch (SecurityException e) {
            runOnUiThread(() -> {
                Toast.makeText(this, "Cannot schedule exact alarm: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        }
    }

    // Handle notification permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission denied. Reminders may not work.", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
