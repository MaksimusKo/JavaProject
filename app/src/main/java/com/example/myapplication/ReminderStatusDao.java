package com.example.myapplication;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ReminderStatusDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertReminderStatus(ReminderStatus status);

    @Query("SELECT * FROM ReminderStatus WHERE reminderId = :reminderId AND date = :date")
    ReminderStatus getStatusForReminderOnDate(int reminderId, String date);

    @Query("SELECT * FROM ReminderStatus WHERE date = :date")
    List<ReminderStatus> getStatusesForDate(String date);
}
