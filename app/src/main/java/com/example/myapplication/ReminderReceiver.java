package com.example.myapplication;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ReminderReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        String medName = intent.getStringExtra("medName");
        String time = intent.getStringExtra("time");
        int reminderId = intent.getIntExtra("reminderId", -1);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "ReminderChannel",
                    "Medication Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = sdf.format(new Date());

        Intent actionIntent = new Intent(context, ReminderActionActivity.class);
        actionIntent.putExtra(Constants.EXTRA_MED_NAME, medName);
        actionIntent.putExtra(Constants.EXTRA_CURRENT_DATE, currentDate); // Передаём текущую дату
        actionIntent.putExtra(Constants.EXTRA_REMINDER_ID, reminderId);
        actionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent actionPendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            actionPendingIntent = PendingIntent.getActivity(
                    context,
                    reminderId,
                    actionIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
        } else {
            actionPendingIntent = PendingIntent.getActivity(
                    context,
                    reminderId,
                    actionIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "ReminderChannel")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Time to take: " + medName)
                .setContentText("Scheduled time: " + time)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(actionPendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(reminderId, builder.build());

        new Thread(() -> {
            ReminderDatabase db = ReminderDatabase.getInstance(context);
            Reminder reminder = db.reminderDao().getReminderById(reminderId);

            if (reminder != null && (reminder.endDate == null || !isDateAfterEndDate(reminder.endDate))) {
                scheduleNextReminder(context, reminder);
            }
        }).start();
    }


    private boolean isDateAfterEndDate(String endDateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date endDate = sdf.parse(endDateStr);
            Date today = new Date();
            return today.after(endDate);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void scheduleNextReminder(Context context, Reminder reminder) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("medName", reminder.medicationName);
        intent.putExtra("time", String.format("%02d:%02d", reminder.hour, reminder.minute));
        intent.putExtra("reminderId", reminder.id);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                reminder.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, reminder.hour);
        calendar.set(Calendar.MINUTE, reminder.minute);
        calendar.set(Calendar.SECOND, 0);

        long triggerAtMillis = calendar.getTimeInMillis();

        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerAtMillis,
                            pendingIntent
                    );
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                );
            }
        }
    }
}
