
package com.example.myapplication;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;


@Database(entities = {Reminder.class, ReminderStatus.class}, version = 4, exportSchema = false)
public abstract class ReminderDatabase extends RoomDatabase {

    private static volatile ReminderDatabase instance;

    public abstract ReminderDao reminderDao();
    public abstract ReminderStatusDao reminderStatusDao();

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `ReminderStatus` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`reminderId` INTEGER NOT NULL, " +
                    "`date` TEXT NOT NULL, " +
                    "`taken` INTEGER NOT NULL, " +
                    "FOREIGN KEY(`reminderId`) REFERENCES `Reminder`(`id`) ON DELETE CASCADE )");
        }
    };

    public static ReminderDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (ReminderDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    ReminderDatabase.class, "reminder_database")
                            .addMigrations(MIGRATION_3_4)
                            .build();
                }
            }
        }
        return instance;
    }
}
