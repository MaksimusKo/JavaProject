package com.example.myapplication;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class ReminderListActivity extends AppCompatActivity {

    private TextView remindersTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_list);

        remindersTextView = findViewById(R.id.remindersTextView);

        List<Reminder> reminders = ReminderDatabase.getInstance(this).reminderDao().getAllReminders();
        StringBuilder builder = new StringBuilder();
        for (Reminder reminder : reminders) {
            builder.append("Medication: ").append(reminder.medicationName)
                    .append(", Time: ").append(String.format("%02d:%02d", reminder.hour, reminder.minute))
                    .append("\n");
        }
        remindersTextView.setText(builder.toString());
    }
}
