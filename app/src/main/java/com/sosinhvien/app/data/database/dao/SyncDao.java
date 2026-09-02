package com.sosinhvien.app.data.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.sosinhvien.app.data.database.entity.BudgetEntity;
import com.sosinhvien.app.data.database.entity.CalendarEventEntity;
import com.sosinhvien.app.data.database.entity.CategoryBudgetEntity;
import com.sosinhvien.app.data.database.entity.CategoryEntity;
import com.sosinhvien.app.data.database.entity.TaskEntity;
import com.sosinhvien.app.data.database.entity.TransactionEntity;
import com.sosinhvien.app.data.database.entity.UserConfigEntity;

import java.util.List;

@Dao
public interface SyncDao {
    // Queries to fetch unsynced local data
    @Query("SELECT * FROM categories WHERE user_id = :userId AND is_synced = 0")
    List<CategoryEntity> getUnsyncedCategories(String userId);

    @Query("SELECT * FROM budgets WHERE user_id = :userId AND is_synced = 0")
    List<BudgetEntity> getUnsyncedBudgets(String userId);

    @Query("SELECT * FROM category_budgets WHERE user_id = :userId AND is_synced = 0")
    List<CategoryBudgetEntity> getUnsyncedCategoryBudgets(String userId);

    @Query("SELECT * FROM transactions WHERE user_id = :userId AND is_synced = 0")
    List<TransactionEntity> getUnsyncedTransactions(String userId);

    @Query("SELECT * FROM tasks WHERE user_id = :userId AND is_synced = 0")
    List<TaskEntity> getUnsyncedTasks(String userId);

    @Query("SELECT * FROM calendar_events WHERE user_id = :userId AND is_synced = 0")
    List<CalendarEventEntity> getUnsyncedCalendarEvents(String userId);

    @Query("SELECT * FROM user_configs WHERE user_id = :userId AND is_synced = 0 LIMIT 1")
    UserConfigEntity getUnsyncedUserConfig(String userId);

    // Bulk inserts/upserts for pulling data from server
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCategories(List<CategoryEntity> categories);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBudgets(List<BudgetEntity> budgets);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCategoryBudgets(List<CategoryBudgetEntity> categoryBudgets);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTransactions(List<TransactionEntity> transactions);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTasks(List<TaskEntity> tasks);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCalendarEvents(List<CalendarEventEntity> calendarEvents);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUserConfig(UserConfigEntity userConfig);

    // Queries to mark items as synced
    @Query("UPDATE categories SET is_synced = 1 WHERE user_id = :userId AND id IN (:ids)")
    void markCategoriesSynced(String userId, List<String> ids);

    @Query("UPDATE budgets SET is_synced = 1 WHERE user_id = :userId AND month_label IN (:monthLabels)")
    void markBudgetsSynced(String userId, List<String> monthLabels);

    @Query("UPDATE transactions SET is_synced = 1 WHERE user_id = :userId AND id IN (:ids)")
    void markTransactionsSynced(String userId, List<String> ids);

    @Query("UPDATE tasks SET is_synced = 1 WHERE user_id = :userId AND id IN (:ids)")
    void markTasksSynced(String userId, List<String> ids);

    @Query("UPDATE calendar_events SET is_synced = 1 WHERE user_id = :userId AND id IN (:ids)")
    void markCalendarEventsSynced(String userId, List<String> ids);

    @Query("UPDATE user_configs SET is_synced = 1 WHERE user_id = :userId")
    void markUserConfigSynced(String userId);
}
