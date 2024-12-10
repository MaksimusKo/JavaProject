// Reminder.java
package com.example.myapplication;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

@Entity
public class Reminder {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String medicationName;
    public int hour;
    public int minute;
    public String startDate;
    public String endDate;

    @Ignore
    public boolean taken;

    public Reminder(String medicationName, int hour, int minute, String startDate, String endDate) {
        this.medicationName = medicationName;
        this.hour = hour;
        this.minute = minute;
        this.startDate = startDate;
        this.endDate = endDate;
        this.taken = false;
    }

    @Ignore
    public Reminder() {}
}
