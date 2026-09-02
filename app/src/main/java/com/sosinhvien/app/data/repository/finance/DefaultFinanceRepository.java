package com.sosinhvien.app.data.repository.finance;

import androidx.annotation.NonNull;

import com.sosinhvien.app.data.database.entity.BudgetEntity;
import com.sosinhvien.app.data.database.entity.CategoryBudgetEntity;
import com.sosinhvien.app.data.database.entity.CategoryEntity;
import com.sosinhvien.app.data.database.entity.TransactionEntity;
import com.sosinhvien.app.data.network.model.SyncModels;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DefaultFinanceRepository implements FinanceRepository {

    private final FinanceLocalDataSource localDataSource;
    private final FinanceRemoteDataSource remoteDataSource;

    @Inject
    public DefaultFinanceRepository(
            @NonNull FinanceLocalDataSource localDataSource,
            @NonNull FinanceRemoteDataSource remoteDataSource) {
        this.localDataSource = localDataSource;
        this.remoteDataSource = remoteDataSource;
    }

    @Override
    public List<CategoryEntity> getVisibleCategories(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return localDataSource.getVisibleCategories(userId);
    }

    @Override
    public List<CategoryEntity> getAllCategories(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return localDataSource.getAllCategories(userId);
    }

    @Override
    public CategoryEntity getCategoryById(String userId, String categoryId) {
        if (userId == null || userId.trim().isEmpty() || categoryId == null || categoryId.trim().isEmpty()) {
            return null;
        }
        return localDataSource.getCategoryById(userId, categoryId);
    }

    @Override
    public void insertOrUpdateCategory(String userId, CategoryEntity category) {
        if (userId == null || userId.trim().isEmpty() || category == null) {
            return;
        }
        category.userId = userId;
        localDataSource.insertOrUpdateCategory(category);
    }

    @Override
    public void hideCategory(String userId, String categoryId) {
        if (userId == null || userId.trim().isEmpty() || categoryId == null || categoryId.trim().isEmpty()) {
            return;
        }
        localDataSource.hideCategory(userId, categoryId);
    }

    @Override
    public BudgetEntity getBudget(String userId, String monthLabel) {
        if (userId == null || userId.trim().isEmpty() || monthLabel == null || monthLabel.trim().isEmpty()) {
            return null;
        }
        return localDataSource.getBudget(userId, monthLabel);
    }

    @Override
    public void saveBudget(String userId, BudgetEntity budget) {
        if (userId == null || userId.trim().isEmpty() || budget == null) {
            return;
        }
        budget.userId = userId;
        localDataSource.saveBudget(budget);
    }

    @Override
    public List<CategoryBudgetEntity> getCategoryBudgets(String userId, String monthLabel) {
        if (userId == null || userId.trim().isEmpty() || monthLabel == null || monthLabel.trim().isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return localDataSource.getCategoryBudgets(userId, monthLabel);
    }

    @Override
    public long getTotalAllocatedBudget(String userId, String monthLabel) {
        if (userId == null || userId.trim().isEmpty() || monthLabel == null || monthLabel.trim().isEmpty()) {
            return 0L;
        }
        return localDataSource.getTotalAllocatedBudget(userId, monthLabel);
    }

    @Override
    public void saveCategoryBudgets(String userId, List<CategoryBudgetEntity> categoryBudgets) {
        if (userId == null || userId.trim().isEmpty() || categoryBudgets == null || categoryBudgets.isEmpty()) {
            return;
        }
        for (CategoryBudgetEntity categoryBudget : categoryBudgets) {
            categoryBudget.userId = userId;
        }
        localDataSource.saveCategoryBudgets(categoryBudgets);
    }

    @Override
    public List<TransactionEntity> getTransactions(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return localDataSource.getTransactions(userId);
    }

    @Override
    public List<TransactionEntity> getTransactionsByType(String userId, String type) {
        if (userId == null || userId.trim().isEmpty() || type == null || type.trim().isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return localDataSource.getTransactionsByType(userId, type);
    }

    @Override
    public TransactionEntity getTransactionById(String transactionId) {
        if (transactionId == null || transactionId.trim().isEmpty()) {
            return null;
        }
        return localDataSource.getTransactionById(transactionId);
    }

    @Override
    public void addTransaction(String userId, TransactionEntity transaction) {
        if (userId == null || userId.trim().isEmpty() || transaction == null) {
            return;
        }
        transaction.userId = userId;
        localDataSource.addTransaction(transaction);
    }

    @Override
    public void updateTransaction(String userId, TransactionEntity transaction) {
        if (userId == null || userId.trim().isEmpty() || transaction == null) {
            return;
        }
        transaction.userId = userId;
        localDataSource.updateTransaction(transaction);
    }

    @Override
    public void deleteTransaction(String userId, String transactionId) {
        if (userId == null || userId.trim().isEmpty() || transactionId == null || transactionId.trim().isEmpty()) {
            return;
        }
        localDataSource.deleteTransaction(userId, transactionId);
    }

    @Override
    public long sumSpentByMonth(String userId, long startTime, long endTime) {
        if (userId == null || userId.trim().isEmpty()) {
            return 0L;
        }
        return localDataSource.sumSpentByMonth(userId, startTime, endTime);
    }

    @Override
    public long sumIncomeByMonth(String userId, long startTime, long endTime) {
        if (userId == null || userId.trim().isEmpty()) {
            return 0L;
        }
        return localDataSource.sumIncomeByMonth(userId, startTime, endTime);
    }

    @Override
    public long sumSpentByCategoryMonth(String userId, String categoryId, long startTime, long endTime) {
        if (userId == null || userId.trim().isEmpty() || categoryId == null || categoryId.trim().isEmpty()) {
            return 0L;
        }
        return localDataSource.sumSpentByCategoryMonth(userId, categoryId, startTime, endTime);
    }

    @Override
    public void syncPendingChanges(String userId, long lastSyncTime) {
        if (userId == null || userId.trim().isEmpty()) {
            return;
        }

        SyncModels.PullResponse response = remoteDataSource.pullChanges(userId, lastSyncTime);
        if (response == null) {
            return;
        }

        if (response.categories != null && !response.categories.isEmpty()) {
            for (CategoryEntity category : response.categories) {
                if (category != null) {
                    category.userId = userId;
                    localDataSource.insertOrUpdateCategory(category);
                }
            }
        }

        if (response.budgets != null && !response.budgets.isEmpty()) {
            for (BudgetEntity budget : response.budgets) {
                if (budget != null) {
                    budget.userId = userId;
                    localDataSource.saveBudget(budget);
                }
            }
        }

        if (response.categoryBudgets != null && !response.categoryBudgets.isEmpty()) {
            for (CategoryBudgetEntity categoryBudget : response.categoryBudgets) {
                if (categoryBudget != null) {
                    categoryBudget.userId = userId;
                }
            }
            localDataSource.saveCategoryBudgets(response.categoryBudgets);
        }

        if (response.transactions != null && !response.transactions.isEmpty()) {
            for (TransactionEntity transaction : response.transactions) {
                if (transaction != null) {
                    transaction.userId = userId;
                    localDataSource.addTransaction(transaction);
                }
            }
        }
    }

    @Override
    public SyncModels.PushResponse pushPendingChanges(String userId, SyncModels.PushRequest request) {
        if (userId == null || userId.trim().isEmpty() || request == null) {
            return new SyncModels.PushResponse();
        }

        if (request.categories != null) {
            for (CategoryEntity category : request.categories) {
                if (category != null) {
                    category.userId = userId;
                }
            }
        }

        if (request.budgets != null) {
            for (BudgetEntity budget : request.budgets) {
                if (budget != null) {
                    budget.userId = userId;
                }
            }
        }

        if (request.categoryBudgets != null) {
            for (CategoryBudgetEntity categoryBudget : request.categoryBudgets) {
                if (categoryBudget != null) {
                    categoryBudget.userId = userId;
                }
            }
        }

        if (request.transactions != null) {
            for (TransactionEntity transaction : request.transactions) {
                if (transaction != null) {
                    transaction.userId = userId;
                }
            }
        }

        return remoteDataSource.pushChanges(userId, request);
    }
}
