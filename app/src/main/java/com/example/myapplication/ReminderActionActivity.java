// ReminderActionActivity.java
package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class ReminderActionActivity extends AppCompatActivity {

    private static final String TAG = "ReminderActionActivity";

    private Button markTakenButton;
    private int reminderId;
    private String currentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_action);

        markTakenButton = findViewById(R.id.takenButton);

        // Получение данных из Intent
        if (getIntent() != null) {
            reminderId = getIntent().getIntExtra(Constants.EXTRA_REMINDER_ID, -1);
            currentDate = getIntent().getStringExtra(Constants.EXTRA_CURRENT_DATE);
        }

        if (reminderId == -1 || currentDate == null) {
            Toast.makeText(this, "Некорректные данные напоминания.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Недостаточно данных для обновления статуса.");
            finish();
        }

        markTakenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                markReminderAsTaken(reminderId, currentDate);
            }
        });
    }

    /**
     * Метод для отметки напоминания как "taken"
     *
     * @param reminderId идентификатор напоминания
     * @param date       текущая дата
     */
    private void markReminderAsTaken(final int reminderId, final String date) {
        final ReminderStatus status = new ReminderStatus(reminderId, date, true);
        Log.d(TAG, "Отмечаем напоминание ID " + reminderId + " как принятое на дату " + date);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ReminderDatabase db = ReminderDatabase.getInstance(getApplicationContext());
                    db.reminderStatusDao().insertReminderStatus(status);
                    Log.d(TAG, "Статус напоминания обновлен успешно.");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ReminderActionActivity.this, "Напоминание отмечено как принято.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Ошибка при обновлении статуса напоминания: ", e);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ReminderActionActivity.this, "Не удалось обновить статус напоминания.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }
}
