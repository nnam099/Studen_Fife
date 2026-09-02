package com.sosinhvien.app.ui.finance;

import androidx.lifecycle.ViewModel;

import com.sosinhvien.app.data.database.entity.CategoryEntity;
import com.sosinhvien.app.data.repository.finance.FinanceRepository;

import java.util.List;

import javax.inject.Inject;

public class FinanceViewModel extends ViewModel {

    private final FinanceRepository financeRepository;

    @Inject
    public FinanceViewModel(FinanceRepository financeRepository) {
        this.financeRepository = financeRepository;
    }

    public List<CategoryEntity> getVisibleCategories(String userId) {
        return financeRepository.getVisibleCategories(userId);
    }

    public long getTotalAllocatedBudget(String userId, String monthLabel) {
        return financeRepository.getTotalAllocatedBudget(userId, monthLabel);
    }
}
