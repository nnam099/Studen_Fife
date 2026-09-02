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

        binding.recyclerReminders.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Setup dynamic upcoming event
        MockDataRepository.getInstance().executeAsync(() -> {
            java.util.List<com.sosinhvien.app.data.model.CalendarEvent> events = MockDataRepository.getInstance().getTodayEvents();
            MockDataRepository.getInstance().runOnMainThread(() -> {
                if (binding == null) return;
                boolean found = false;
                for (com.sosinhvien.app.data.model.CalendarEvent e : events) {
                    if (!e.isCompleted()) {
                        binding.textUpcomingEvent.setText(e.getTimeRange() + " • " + e.getTitle());
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    binding.textUpcomingEvent.setText("Không có sự kiện sắp tới");
                }
            });
        });

        binding.btnAddExpense.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddTransactionActivity.class)
                        .putExtra(AddTransactionActivity.EXTRA_TYPE, Transaction.TYPE_EXPENSE)));
        binding.btnAddIncome.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddTransactionActivity.class)
                        .putExtra(AddTransactionActivity.EXTRA_TYPE, Transaction.TYPE_INCOME)));
                        
        if (binding.chartSparkline != null) {
            binding.chartSparkline.getDescription().setEnabled(false);
            binding.chartSparkline.getLegend().setEnabled(false);
            binding.chartSparkline.getXAxis().setEnabled(false);
            binding.chartSparkline.getAxisLeft().setEnabled(false);
            binding.chartSparkline.getAxisRight().setEnabled(false);
            binding.chartSparkline.setTouchEnabled(false);
            binding.chartSparkline.setNoDataText("Chưa có dữ liệu chi tiêu 7 ngày");
            binding.chartSparkline.setNoDataTextColor(ContextCompat.getColor(requireContext(), R.color.on_primary));
        }

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshHomeData();
    }

    private void refreshHomeData() {
        if (binding == null) return;
        SessionManager session = new SessionManager(requireContext());
        MockDataRepository repo = MockDataRepository.getInstance();

        repo.executeAsync(() -> {
            Budget budget = repo.getCurrentBudget();
            java.util.List<com.sosinhvien.app.data.model.Reminder> reminders = repo.getTodayReminders();
            String monthLabel = repo.getCurrentMonthLabel();

            repo.runOnMainThread(() -> {
                if (binding == null) return;
                binding.textGreeting.setText("Chào bạn, " + session.getDisplayName() + "!");

                if (budget != null) {
                    binding.textMonth.setText(budget.getMonthLabel());
                    binding.textBalance.setText(CurrencyFormatter.format(budget.getAvailableBalance()));

                    int percent = budget.getUsagePercent();
                    binding.textBudgetPercent.setText(percent + "%");
                    binding.textBudgetPercent.setTextColor(ContextCompat.getColor(requireContext(),
                            percent >= 100 ? R.color.danger : percent >= 80 ? R.color.warning : R.color.success));
                            
                    // Cập nhật CircularProgressIndicator
                    if (binding.progressBudget != null) {
                        binding.progressBudget.setProgress(Math.min(percent, 100));
                        binding.progressBudget.setIndicatorColor(ContextCompat.getColor(requireContext(),
                            percent >= 100 ? R.color.danger : percent >= 80 ? R.color.warning : R.color.primary));
                    }
                    
                    binding.textBudgetDetail.setText("Đã chi " + CurrencyFormatter.formatShort(budget.getSpent())
                            + " / " + CurrencyFormatter.formatShort(budget.getTotalBudget()));
                } else {
                    binding.textMonth.setText(monthLabel);
                    binding.textBalance.setText("Chưa thiết lập");
                    binding.textBudgetPercent.setText("—");
                    binding.textBudgetPercent.setTextColor(ContextCompat.getColor(requireContext(), R.color.on_surface_subtle));
                    
                    if (binding.progressBudget != null) {
                        binding.progressBudget.setProgress(0);
                    }
                    
                    binding.textBudgetDetail.setText("Hãy thiết lập ngân sách tháng này");
                }

                // Xử lý Empty State cho Nhắc nhở
                if (reminders.isEmpty()) {
                    binding.recyclerReminders.setVisibility(View.GONE);
                    if (binding.layoutEmptyReminders != null) {
                        binding.layoutEmptyReminders.setVisibility(View.VISIBLE);
                    }
                } else {
                    binding.recyclerReminders.setVisibility(View.VISIBLE);
                    if (binding.layoutEmptyReminders != null) {
                        binding.layoutEmptyReminders.setVisibility(View.GONE);
                    }
                    binding.recyclerReminders.setAdapter(new ReminderAdapter(reminders));
                }
            });
        });
    }
}
