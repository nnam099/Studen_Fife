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
import com.sosinhvien.app.data.model.Transaction;
import com.sosinhvien.app.databinding.FragmentHomeBinding;
import com.sosinhvien.app.ui.common.ReminderAdapter;
import com.sosinhvien.app.ui.finance.AddTransactionActivity;
import com.sosinhvien.app.util.CurrencyFormatter;
import com.sosinhvien.app.util.SessionManager;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        SessionManager session = new SessionManager(requireContext());
        MockDataRepository repo = MockDataRepository.getInstance();
        Budget budget = repo.getCurrentBudget();

        binding.textGreeting.setText("Chào bạn, " + session.getDisplayName() + "!");
        binding.textMonth.setText(budget.getMonthLabel());
        binding.textBalance.setText(CurrencyFormatter.format(budget.getAvailableBalance()));

        int percent = budget.getUsagePercent();
        binding.textBudgetPercent.setText(percent + "%");
        binding.textBudgetPercent.setTextColor(ContextCompat.getColor(requireContext(),
                percent >= 100 ? R.color.danger : percent >= 80 ? R.color.warning : R.color.success));
        binding.textBudgetDetail.setText("Đã chi " + CurrencyFormatter.formatShort(budget.getSpent())
                + " / " + CurrencyFormatter.formatShort(budget.getTotalBudget()));

        binding.recyclerReminders.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerReminders.setAdapter(new ReminderAdapter(repo.getTodayReminders()));

        binding.textUpcomingEvent.setText("14:00 - 16:30 • Học Thể chất - Sân B2");

        binding.btnAddExpense.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddTransactionActivity.class)
                        .putExtra(AddTransactionActivity.EXTRA_TYPE, Transaction.TYPE_EXPENSE)));
        binding.btnAddIncome.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddTransactionActivity.class)
                        .putExtra(AddTransactionActivity.EXTRA_TYPE, Transaction.TYPE_INCOME)));

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
