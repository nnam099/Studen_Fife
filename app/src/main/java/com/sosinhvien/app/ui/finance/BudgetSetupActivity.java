package com.sosinhvien.app.ui.finance;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.sosinhvien.app.data.MockDataRepository;
import com.sosinhvien.app.data.model.Budget;
import com.sosinhvien.app.databinding.ActivityBudgetSetupBinding;
import com.sosinhvien.app.util.CurrencyFormatter;

public class BudgetSetupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityBudgetSetupBinding binding = ActivityBudgetSetupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thiết lập ngân sách");
        }

        Budget budget = MockDataRepository.getInstance().getCurrentBudget();
        binding.editMonth.setText(budget.getMonthLabel());
        binding.editTotalBudget.setText(String.valueOf(budget.getTotalBudget()));
        binding.editOpeningBalance.setText(String.valueOf(budget.getOpeningBalance()));
        binding.textAllocationSummary.setText(
                "Tổng phân bổ: " + CurrencyFormatter.format(budget.getSpent())
                        + " / " + CurrencyFormatter.format(budget.getTotalBudget()));

        binding.btnSave.setOnClickListener(v ->
                Toast.makeText(this, "Đã lưu ngân sách (demo mock)", Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
