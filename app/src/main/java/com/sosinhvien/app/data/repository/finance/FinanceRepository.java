package com.sosinhvien.app.data.repository.finance;

import com.sosinhvien.app.data.database.entity.BudgetEntity;
import com.sosinhvien.app.data.database.entity.CategoryBudgetEntity;
import com.sosinhvien.app.data.database.entity.CategoryEntity;
import com.sosinhvien.app.data.database.entity.TransactionEntity;
import com.sosinhvien.app.data.network.model.SyncModels;

import java.util.List;

public interface FinanceRepository {

    List<CategoryEntity> getVisibleCategories(String userId);

    List<CategoryEntity> getAllCategories(String userId);

    CategoryEntity getCategoryById(String userId, String categoryId);

    void insertOrUpdateCategory(String userId, CategoryEntity category);

    void hideCategory(String userId, String categoryId);

    BudgetEntity getBudget(String userId, String monthLabel);

    void saveBudget(String userId, BudgetEntity budget);

    List<CategoryBudgetEntity> getCategoryBudgets(String userId, String monthLabel);

    long getTotalAllocatedBudget(String userId, String monthLabel);

    void saveCategoryBudgets(String userId, List<CategoryBudgetEntity> categoryBudgets);

    List<TransactionEntity> getTransactions(String userId);

    List<TransactionEntity> getTransactionsByType(String userId, String type);

    TransactionEntity getTransactionById(String transactionId);

    void addTransaction(String userId, TransactionEntity transaction);

    void updateTransaction(String userId, TransactionEntity transaction);

    void deleteTransaction(String userId, String transactionId);

    long sumSpentByMonth(String userId, long startTime, long endTime);

    long sumIncomeByMonth(String userId, long startTime, long endTime);

    long sumSpentByCategoryMonth(String userId, String categoryId, long startTime, long endTime);

    void syncPendingChanges(String userId, long lastSyncTime);

    SyncModels.PushResponse pushPendingChanges(String userId, SyncModels.PushRequest request);
}
