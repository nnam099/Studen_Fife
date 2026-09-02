package com.sosinhvien.app.data.repository.finance;

import androidx.annotation.NonNull;

import com.sosinhvien.app.data.database.AppDatabase;
import com.sosinhvien.app.data.database.entity.BudgetEntity;
import com.sosinhvien.app.data.database.entity.BudgetWarningEntity;
import com.sosinhvien.app.data.database.entity.CategoryBudgetEntity;
import com.sosinhvien.app.data.database.entity.CategoryEntity;
import com.sosinhvien.app.data.database.entity.TransactionEntity;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class RoomFinanceLocalDataSource implements FinanceLocalDataSource {

    private final AppDatabase database;

    @Inject
    public RoomFinanceLocalDataSource(@NonNull AppDatabase database) {
        this.database = database;
    }

    @Override
    public List<CategoryEntity> getVisibleCategories(String userId) {
        return database.financeDao().getVisibleCategories(userId);
    }

    @Override
    public List<CategoryEntity> getAllCategories(String userId) {
        return database.financeDao().getAllCategories(userId);
    }

    @Override
    public CategoryEntity getCategoryById(String userId, String categoryId) {
        return database.financeDao().getCategoryById(userId, categoryId);
    }

    @Override
    public void insertOrUpdateCategory(CategoryEntity category) {
        if (category == null) {
            return;
        }
        database.financeDao().insertCategory(category);
    }

    @Override
    public void hideCategory(String userId, String categoryId) {
        CategoryEntity category = database.financeDao().getCategoryById(userId, categoryId);
        if (category == null) {
            return;
        }
        category.visible = false;
        database.financeDao().updateCategory(category);
    }

    @Override
    public BudgetEntity getBudget(String userId, String monthLabel) {
        return database.financeDao().getBudget(userId, monthLabel);
    }

    @Override
    public void saveBudget(BudgetEntity budget) {
        if (budget == null) {
            return;
        }
        database.financeDao().insertBudget(budget);
    }

    @Override
    public List<CategoryBudgetEntity> getCategoryBudgets(String userId, String monthLabel) {
        return database.financeDao().getCategoryBudgets(userId, monthLabel);
    }

    @Override
    public long getTotalAllocatedBudget(String userId, String monthLabel) {
        return database.financeDao().getTotalAllocatedBudget(userId, monthLabel);
    }

    @Override
    public void saveCategoryBudgets(List<CategoryBudgetEntity> categoryBudgets) {
        if (categoryBudgets == null || categoryBudgets.isEmpty()) {
            return;
        }
        for (CategoryBudgetEntity categoryBudget : categoryBudgets) {
            database.financeDao().insertCategoryBudget(categoryBudget);
        }
    }

    @Override
    public List<TransactionEntity> getTransactions(String userId) {
        return database.financeDao().getActiveTransactions(userId);
    }

    @Override
    public List<TransactionEntity> getTransactionsByType(String userId, String type) {
        return database.financeDao().getActiveTransactionsByType(userId, type);
    }

    @Override
    public TransactionEntity getTransactionById(String transactionId) {
        return database.financeDao().getTransactionById(transactionId);
    }

    @Override
    public void addTransaction(TransactionEntity transaction) {
        if (transaction == null) {
            return;
        }
        database.financeDao().insertTransaction(transaction);
    }

    @Override
    public void updateTransaction(TransactionEntity transaction) {
        if (transaction == null) {
            return;
        }
        database.financeDao().updateTransaction(transaction);
    }

    @Override
    public void deleteTransaction(String userId, String transactionId) {
        TransactionEntity transaction = database.financeDao().getTransactionById(transactionId);
        if (transaction == null) {
            return;
        }
        transaction.deleted = true;
        transaction.isDeleted = 1;
        transaction.updatedAt = System.currentTimeMillis();
        database.financeDao().updateTransaction(transaction);
    }

    @Override
    public long sumSpentByMonth(String userId, long startTime, long endTime) {
        return database.financeDao().sumSpentByMonth(userId, startTime, endTime);
    }

    @Override
    public long sumIncomeByMonth(String userId, long startTime, long endTime) {
        return database.financeDao().sumIncomeByMonth(userId, startTime, endTime);
    }

    @Override
    public long sumSpentByCategoryMonth(String userId, String categoryId, long startTime, long endTime) {
        return database.financeDao().sumSpentByCategoryMonth(userId, categoryId, startTime, endTime);
    }
}
