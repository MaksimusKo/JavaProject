package com.example.myapplication;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
        foreignKeys = @ForeignKey(
                entity = Reminder.class,
                parentColumns = "id",
                childColumns = "reminderId",
                onDelete = ForeignKey.CASCADE
        )
)
public class ReminderStatus {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int reminderId;

    public String date;

    public boolean taken;

    public ReminderStatus(int reminderId, String date, boolean taken) {
        this.reminderId = reminderId;
        this.date = date;
        this.taken = taken;
    }
}
