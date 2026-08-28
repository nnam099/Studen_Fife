package com.sosinhvien.app.data.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.sosinhvien.app.data.database.entity.NotificationLogEntity;

import java.util.List;

@Dao
public interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNotification(NotificationLogEntity log);

    @Query("SELECT * FROM notification_logs WHERE user_email = :email ORDER BY timestamp DESC")
    List<NotificationLogEntity> getNotifications(String email);

    @Query("UPDATE notification_logs SET is_read = 1 WHERE id = :id")
    void markAsRead(String id);

    @Query("SELECT COUNT(*) FROM notification_logs WHERE user_email = :email AND is_read = 0")
    int getUnreadCount(String email);
}
