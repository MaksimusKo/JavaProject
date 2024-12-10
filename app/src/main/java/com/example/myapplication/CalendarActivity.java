// CalendarActivity.java

package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.Toast;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarActivity extends AppCompatActivity {

    private static final String TAG = "CalendarActivity";

    private CalendarView calendarView;
    private ListView remindersListView;
    private Button backButton;

    private String selectedDate;
    private List<Reminder> remindersList;
    private RemindersAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        calendarView = findViewById(R.id.calendarView);
        remindersListView = findViewById(R.id.remindersListView);
        backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> finish());

        selectedDate = getCurrentDate();
        Log.d(TAG, "Selected date: " + selectedDate);
        updateRemindersList(selectedDate);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth);
            Log.d(TAG, "Date changed to: " + selectedDate);
            updateRemindersList(selectedDate);
        });

        remindersListView.setOnItemClickListener((parent, view, position, id) -> {
            if (remindersList != null && position >= 0 && position < remindersList.size()) {
                Reminder reminder = remindersList.get(position);
                boolean newTakenStatus = !reminder.taken;

                Log.d(TAG, "Toggling 'taken' status for reminder ID " + reminder.id + " to " + newTakenStatus);

                String currentDate = selectedDate;

                ReminderStatus status = new ReminderStatus(reminder.id, currentDate, newTakenStatus);

                new Thread(() -> {
                    try {
                        ReminderDatabase db = ReminderDatabase.getInstance(this);
                        db.reminderStatusDao().insertReminderStatus(status);
                        runOnUiThread(() -> {
                            reminder.taken = newTakenStatus;
                            adapter.notifyDataSetChanged();
                            Toast.makeText(this, "Reminder marked as " + (newTakenStatus ? "Taken" : "Not Taken"), Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Updated 'taken' status for reminder ID " + reminder.id + " to " + newTakenStatus);
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Failed to update reminder status.", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Error updating reminder status: ", e);
                        });
                    }
                }).start();
            } else {
                Toast.makeText(this, "Invalid reminder selection.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Attempted to access reminder at invalid position: " + position);
            }
        });
    }

    private String getCurrentDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return formatter.format(new Date());
    }

    private void updateRemindersList(String date) {
        Log.d(TAG, "Updating reminders for date: " + date);
        new Thread(() -> {
            try {
                ReminderDao reminderDao = ReminderDatabase.getInstance(this).reminderDao();
                ReminderStatusDao statusDao = ReminderDatabase.getInstance(this).reminderStatusDao();

                List<Reminder> reminders = reminderDao.getRemindersByDate(date);
                Log.d(TAG, "Fetched " + reminders.size() + " reminders.");

                List<ReminderStatus> statuses = statusDao.getStatusesForDate(date);
                Log.d(TAG, "Fetched " + statuses.size() + " reminder statuses.");

                Map<Integer, Boolean> takenMap = new HashMap<>();
                for (ReminderStatus status : statuses) {
                    takenMap.put(status.reminderId, status.taken);
                }

                for (Reminder reminder : reminders) {
                    Boolean taken = takenMap.get(reminder.id);
                    reminder.taken = (taken != null) ? taken : false;
                }

                remindersList = reminders;
                Log.d(TAG, "Reminders list initialized with " + (remindersList != null ? remindersList.size() : "null") + " items.");

                runOnUiThread(() -> {
                    if (adapter == null) {
                        adapter = new RemindersAdapter(this, remindersList);
                        remindersListView.setAdapter(adapter);
                        Log.d(TAG, "Adapter initialized.");
                    } else {
                        adapter.updateData(remindersList);
                        adapter.notifyDataSetChanged();
                        Log.d(TAG, "Adapter data updated.");
                    }

                    if (remindersList == null || remindersList.isEmpty()) {
                        Toast.makeText(this, "No reminders for this date.", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "No reminders found for date: " + date);
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Failed to load reminders.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error loading reminders: ", e);
                });
            }
        }).start();
    }
}
