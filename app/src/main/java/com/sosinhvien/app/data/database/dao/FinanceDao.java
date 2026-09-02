package com.sosinhvien.app.data.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.sosinhvien.app.data.database.entity.AuditLogEntity;
import com.sosinhvien.app.data.database.entity.BudgetEntity;
import com.sosinhvien.app.data.database.entity.BudgetWarningEntity;
import com.sosinhvien.app.data.database.entity.CategoryBudgetEntity;
import com.sosinhvien.app.data.database.entity.CategoryEntity;
import com.sosinhvien.app.data.database.entity.TransactionEntity;

import java.util.List;

@Dao
public interface FinanceDao {
    // Categories
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCategory(CategoryEntity category);

    @Query("SELECT * FROM categories WHERE user_id = :userId AND visible = 1")
    List<CategoryEntity> getVisibleCategories(String userId);

    @Query("SELECT * FROM categories WHERE user_id = :userId")
    List<CategoryEntity> getAllCategories(String userId);

    @Query("SELECT * FROM categories WHERE user_id = :userId AND id = :id LIMIT 1")
    CategoryEntity getCategoryById(String userId, String id);

    @Update
    void updateCategory(CategoryEntity category);

    // Budgets
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBudget(BudgetEntity budget);

    @Query("SELECT * FROM budgets WHERE user_id = :userId AND month_label = :monthLabel LIMIT 1")
    BudgetEntity getBudget(String userId, String monthLabel);

    @Update
    void updateBudget(BudgetEntity budget);

    // Category Budgets
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCategoryBudget(CategoryBudgetEntity categoryBudget);

    @Query("SELECT * FROM category_budgets WHERE user_id = :userId AND month_label = :monthLabel")
    List<CategoryBudgetEntity> getCategoryBudgets(String userId, String monthLabel);

    @Query("SELECT SUM(amount) FROM category_budgets WHERE user_id = :userId AND month_label = :monthLabel")
    long getTotalAllocatedBudget(String userId, String monthLabel);

    @Delete
    void deleteCategoryBudget(CategoryBudgetEntity categoryBudget);

    // Transactions
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTransaction(TransactionEntity transaction);

    @Update
    void updateTransaction(TransactionEntity transaction);

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    TransactionEntity getTransactionById(String id);

    @Query("SELECT * FROM transactions WHERE user_id = :userId AND deleted = 0 ORDER BY timestamp DESC")
    List<TransactionEntity> getActiveTransactions(String userId);

    @Query("SELECT * FROM transactions WHERE user_id = :userId AND type = :type AND deleted = 0 ORDER BY timestamp DESC")
    List<TransactionEntity> getActiveTransactionsByType(String userId, String type);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE user_id = :userId AND type = 'expense' AND deleted = 0 AND timestamp >= :startTime AND timestamp <= :endTime")
    long sumSpentByMonth(String userId, long startTime, long endTime);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE user_id = :userId AND type = 'expense' AND category_id = :categoryId AND deleted = 0 AND timestamp >= :startTime AND timestamp <= :endTime")
    long sumSpentByCategoryMonth(String userId, String categoryId, long startTime, long endTime);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE user_id = :userId AND type = 'income' AND deleted = 0 AND timestamp >= :startTime AND timestamp <= :endTime")
    long sumIncomeByMonth(String userId, long startTime, long endTime);

    // Warning Flags
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertWarningFlag(BudgetWarningEntity flag);

    @Query("SELECT * FROM budget_warning_flags WHERE user_id = :userId AND month_label = :monthLabel AND category_id = :categoryId LIMIT 1")
    BudgetWarningEntity getWarningFlag(String userId, String monthLabel, String categoryId);

    // Audit Log
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAuditLog(AuditLogEntity log);
}
