package com.sosinhvien.app.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.sosinhvien.app.data.database.dao.FinanceDao;
import com.sosinhvien.app.data.database.dao.NotificationDao;
import com.sosinhvien.app.data.database.dao.SyncDao;
import com.sosinhvien.app.data.database.dao.TimeDao;
import com.sosinhvien.app.data.database.dao.UserDao;
import com.sosinhvien.app.data.database.entity.BudgetEntity;
import com.sosinhvien.app.data.database.entity.BudgetWarningEntity;
import com.sosinhvien.app.data.database.entity.CategoryBudgetEntity;
import com.sosinhvien.app.data.database.entity.CategoryEntity;
import com.sosinhvien.app.data.database.entity.CalendarEventEntity;
import com.sosinhvien.app.data.database.entity.NotificationLogEntity;
import com.sosinhvien.app.data.database.entity.TaskEntity;
import com.sosinhvien.app.data.database.entity.UserConfigEntity;
import com.sosinhvien.app.data.database.entity.UserEntity;
import com.sosinhvien.app.data.database.entity.TransactionEntity;

@Database(entities = {
        UserEntity.class,
        CategoryEntity.class,
        BudgetEntity.class,
        CategoryBudgetEntity.class,
        TransactionEntity.class,
        TaskEntity.class,
        CalendarEventEntity.class,
        UserConfigEntity.class,
        NotificationLogEntity.class,
        BudgetWarningEntity.class
}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;

    public abstract UserDao userDao();
    public abstract FinanceDao financeDao();
    public abstract TimeDao timeDao();
    public abstract NotificationDao notificationDao();
    public abstract SyncDao syncDao();

    public static AppDatabase getInstance(final Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "so_sinh_vien_db")
                            .allowMainThreadQueries() // Simple for demo and testing, can be optimized later
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}
