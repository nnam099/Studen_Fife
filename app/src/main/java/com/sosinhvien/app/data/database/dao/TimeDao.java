package com.sosinhvien.app.data.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.sosinhvien.app.data.database.entity.CalendarEventEntity;
import com.sosinhvien.app.data.database.entity.TaskEntity;

import java.util.List;

@Dao
public interface TimeDao {
    // Tasks
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTask(TaskEntity task);

    @Update
    void updateTask(TaskEntity task);

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    TaskEntity getTaskById(String id);

    @Query("SELECT * FROM tasks WHERE user_email = :email AND deleted = 0 ORDER BY deadline ASC")
    List<TaskEntity> getActiveTasks(String email);

    @Query("SELECT * FROM tasks WHERE user_email = :email AND status = :status AND deleted = 0 ORDER BY deadline ASC")
    List<TaskEntity> getActiveTasksByStatus(String email, String status);

    // Calendar Events
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertEvent(CalendarEventEntity event);

    @Update
    void updateEvent(CalendarEventEntity event);

    @Query("SELECT * FROM calendar_events WHERE id = :id LIMIT 1")
    CalendarEventEntity getEventById(String id);

    @Query("SELECT * FROM calendar_events WHERE user_email = :email AND deleted = 0 AND start_time >= :startTime AND end_time <= :endTime ORDER BY start_time ASC")
    List<CalendarEventEntity> getEventsInRange(String email, long startTime, long endTime);

    @Query("SELECT * FROM calendar_events WHERE task_id = :taskId AND deleted = 0")
    List<CalendarEventEntity> getSessionsForTask(String taskId);

    @Query("SELECT COALESCE(SUM(session_actual_duration), 0) FROM calendar_events WHERE task_id = :taskId AND session_status = 'Đã hoàn thành' AND deleted = 0")
    int sumCompletedSessionsDuration(String taskId);

    @Query("SELECT * FROM calendar_events WHERE parent_event_id = :parentEventId AND deleted = 0")
    List<CalendarEventEntity> getRecurringExceptions(String parentEventId);
}
