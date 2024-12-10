package com.example.myapplication;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ReminderDao {

    @Insert
    long insertReminder(Reminder reminder);

    @Query("SELECT * FROM Reminder")
    List<Reminder> getAllReminders();

    @Query("SELECT * FROM Reminder WHERE startDate <= :date AND (endDate >= :date OR endDate IS NULL)")
    List<Reminder> getRemindersByDate(String date);

    @Update
    void updateReminder(Reminder reminder);

    @Query("SELECT * FROM Reminder WHERE id = :id")
    Reminder getReminderById(int id);
}
