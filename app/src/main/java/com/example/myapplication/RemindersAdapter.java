// RemindersAdapter.java

package com.example.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

public class RemindersAdapter extends BaseAdapter {

    private Context context;
    private List<Reminder> reminders;
    private LayoutInflater inflater;

    public RemindersAdapter(Context context, List<Reminder> reminders) {
        this.context = context;
        this.reminders = reminders;
        this.inflater = LayoutInflater.from(context);
    }

    public void updateData(List<Reminder> newReminders) {
        this.reminders = newReminders;
    }

    @Override
    public int getCount() {
        return reminders != null ? reminders.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return reminders != null ? reminders.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return reminders != null && position < reminders.size() ? reminders.get(position).id : 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.reminder_list_item, parent, false);
        }

        TextView medNameTextView = convertView.findViewById(R.id.medNameTextView);
        TextView timeTextView = convertView.findViewById(R.id.timeTextView);
        TextView statusTextView = convertView.findViewById(R.id.statusTextView);

        if (reminders != null && position < reminders.size()) {
            Reminder reminder = reminders.get(position);

            medNameTextView.setText(reminder.medicationName);
            timeTextView.setText(String.format(Locale.getDefault(), "%02d:%02d", reminder.hour, reminder.minute));
            statusTextView.setText(reminder.taken ? "Taken" : "Not Taken");
            statusTextView.setTextColor(reminder.taken ? Color.GREEN : Color.RED);
        }

        return convertView;
    }
}
