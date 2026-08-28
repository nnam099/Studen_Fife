package com.sosinhvien.app.ui.finance;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.sosinhvien.app.data.MockDataRepository;
import com.sosinhvien.app.data.model.Transaction;
import com.sosinhvien.app.databinding.ActivityAddTransactionBinding;

public class AddTransactionActivity extends AppCompatActivity {

    public static final String EXTRA_TYPE = "extra_type";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityAddTransactionBinding binding = ActivityAddTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thêm giao dịch");
        }

        String type = getIntent().getStringExtra(EXTRA_TYPE);
        if (Transaction.TYPE_INCOME.equals(type)) {
            binding.btnIncome.setChecked(true);
        } else {
            binding.btnExpense.setChecked(true);
        }

        binding.btnSave.setOnClickListener(v -> {
            String name = binding.editName.getText() != null ? binding.editName.getText().toString().trim() : "";
            String amountStr = binding.editAmount.getText() != null ? binding.editAmount.getText().toString().trim() : "0";
            long amount = 0;
            try {
                amount = Long.parseLong(amountStr);
            } catch (NumberFormatException ignored) {}

            if (name.isEmpty() || amount <= 0) {
                Toast.makeText(this, "Vui lòng nhập tên và số tiền hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedType = binding.btnIncome.isChecked() ? Transaction.TYPE_INCOME : Transaction.TYPE_EXPENSE;
            String catName = binding.editCategory.getText() != null ? binding.editCategory.getText().toString().trim() : "";
            String categoryId = null;

            if (selectedType.equals(Transaction.TYPE_EXPENSE)) {
                categoryId = "other";
                if (catName.equalsIgnoreCase("Ăn uống") || catName.toLowerCase().contains("ăn")) {
                    categoryId = "food";
                } else if (catName.equalsIgnoreCase("Di chuyển") || catName.toLowerCase().contains("chuyển") || catName.toLowerCase().contains("đi")) {
                    categoryId = "transport";
                } else if (catName.equalsIgnoreCase("Học tập") || catName.toLowerCase().contains("học")) {
                    categoryId = "study";
                } else if (catName.equalsIgnoreCase("Giải trí") || catName.toLowerCase().contains("chơi") || catName.toLowerCase().contains("trí")) {
                    categoryId = "entertainment";
                }
            }

            MockDataRepository.getInstance().addTransaction(name, categoryId, amount, selectedType, Transaction.SOURCE_MANUAL);
            Toast.makeText(this, "Đã lưu giao dịch thành công", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
