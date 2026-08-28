package com.sosinhvien.app.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sosinhvien.app.R;
import com.sosinhvien.app.data.MockDataRepository;
import com.sosinhvien.app.data.model.Budget;
import com.sosinhvien.app.databinding.FragmentFinanceBinding;
import com.sosinhvien.app.ui.common.TransactionAdapter;
import com.sosinhvien.app.ui.finance.BudgetSetupActivity;
import com.sosinhvien.app.ui.finance.CategoriesActivity;
import com.sosinhvien.app.ui.finance.StatisticsActivity;
import com.sosinhvien.app.util.CurrencyFormatter;

public class FinanceFragment extends Fragment {

    private FragmentFinanceBinding binding;
    private TransactionAdapter adapter;
    private String currentFilter = "Tất cả";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentFinanceBinding.inflate(inflater, container, false);

        binding.recyclerTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));

        binding.chipAll.setOnClickListener(v -> applyFilter("Tất cả"));
        binding.chipIncome.setOnClickListener(v -> applyFilter("Thu"));
        binding.chipExpense.setOnClickListener(v -> applyFilter("Chi"));

        binding.btnStatistics.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), StatisticsActivity.class)));
        binding.btnCategories.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CategoriesActivity.class)));
        binding.btnBudgetSetup.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), BudgetSetupActivity.class)));

        binding.cardBudget.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), BudgetSetupActivity.class)));

        return binding.getRoot();
    }

    private void applyFilter(String filter) {
        currentFilter = filter;
        adapter = new TransactionAdapter(MockDataRepository.getInstance().getTransactionsFiltered(filter));
        binding.recyclerTransactions.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    private void refreshData() {
        if (binding == null) return;
        MockDataRepository repo = MockDataRepository.getInstance();
        Budget budget = repo.getCurrentBudget();

        binding.textRemaining.setText(CurrencyFormatter.format(budget.getRemaining()));
        binding.textBudgetSub.setText("Còn lại của " + CurrencyFormatter.format(budget.getTotalBudget()));
        binding.textSpent.setText("Đã chi " + CurrencyFormatter.format(budget.getSpent())
                + " • " + budget.getUsagePercent() + "%");
        binding.progressBudget.setProgress(budget.getUsagePercent());
        binding.progressBudget.setIndicatorColor(ContextCompat.getColor(requireContext(),
                budget.getUsagePercent() >= 100 ? R.color.danger
                        : budget.getUsagePercent() >= 80 ? R.color.warning : R.color.primary_container));

        adapter = new TransactionAdapter(repo.getTransactionsFiltered(currentFilter));
        binding.recyclerTransactions.setAdapter(adapter);
    }
}
