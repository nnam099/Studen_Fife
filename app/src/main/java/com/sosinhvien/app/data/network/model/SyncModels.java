package com.sosinhvien.app.data.network.model;

import com.sosinhvien.app.data.database.entity.BudgetEntity;
import com.sosinhvien.app.data.database.entity.CalendarEventEntity;
import com.sosinhvien.app.data.database.entity.CategoryBudgetEntity;
import com.sosinhvien.app.data.database.entity.CategoryEntity;
import com.sosinhvien.app.data.database.entity.TaskEntity;
import com.sosinhvien.app.data.database.entity.TransactionEntity;
import com.sosinhvien.app.data.database.entity.UserConfigEntity;

import java.util.List;

public class SyncModels {
    public static class PullRequest {
        public long lastSyncTime;

        public PullRequest(long lastSyncTime) {
            this.lastSyncTime = lastSyncTime;
        }
    }

    public static class PullResponse {
        public long serverTime;
        public List<CategoryEntity> categories;
        public List<BudgetEntity> budgets;
        public List<CategoryBudgetEntity> categoryBudgets;
        public List<TransactionEntity> transactions;
        public List<TaskEntity> tasks;
        public List<CalendarEventEntity> calendarEvents;
        public UserConfigEntity userConfig;
    }

    public static class PushRequest {
        public List<CategoryEntity> categories;
        public List<BudgetEntity> budgets;
        public List<CategoryBudgetEntity> categoryBudgets;
        public List<TransactionEntity> transactions;
        public List<TaskEntity> tasks;
        public List<CalendarEventEntity> calendarEvents;
        public UserConfigEntity userConfig;
    }

    public static class PushResponse {
        public boolean success;
        public long serverTime;
    }
}
