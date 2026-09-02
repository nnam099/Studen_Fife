package com.sosinhvien.app.data.repository.calendar;

import com.sosinhvien.app.data.database.entity.CalendarEventEntity;
import com.sosinhvien.app.data.database.entity.TaskEntity;

import java.util.List;

public interface CalendarRepository {

    List<TaskEntity> getTasks(String userId);

    TaskEntity getTaskById(String taskId);

    void saveTask(String userId, TaskEntity task);

    List<CalendarEventEntity> getEventsForRange(String userId, long startTime, long endTime);

    void saveEvent(String userId, CalendarEventEntity event);

    void deleteEvent(String userId, String eventId);
}
