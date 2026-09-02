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

        // Setup category dropdown
        MockDataRepository.getInstance().executeAsync(() -> {
            java.util.List<com.sosinhvien.app.data.model.Category> categories = MockDataRepository.getInstance().getAllCategories();
            MockDataRepository.getInstance().runOnMainThread(() -> {
                java.util.List<String> categoryNames = new java.util.ArrayList<>();
                java.util.Map<String, String> nameToIdMap = new java.util.HashMap<>();
                for (com.sosinhvien.app.data.model.Category cat : categories) {
                    if (cat.isVisible()) {
                        categoryNames.add(cat.getName());
                        nameToIdMap.put(cat.getName(), cat.getId());
                    }
                }
                android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categoryNames);
                binding.editCategory.setAdapter(adapter);
                if (!categoryNames.isEmpty()) {
                    binding.editCategory.setText(categoryNames.get(0), false);
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

                    if (amount > 999_999_999) {
                        Toast.makeText(this, "Số tiền tối đa là 999.999.999 đ", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String selectedType = binding.btnIncome.isChecked() ? Transaction.TYPE_INCOME : Transaction.TYPE_EXPENSE;
                    String catName = binding.editCategory.getText() != null ? binding.editCategory.getText().toString().trim() : "";
                    String categoryId = nameToIdMap.getOrDefault(catName, null);

                    if (selectedType.equals(Transaction.TYPE_EXPENSE) && categoryId == null) {
                        categoryId = "other"; // fallback if somehow not selected
                    }
                    
                    final String finalCategoryId = categoryId;
                    final long finalAmount = amount;
                    MockDataRepository.getInstance().executeAsync(() -> {
                        MockDataRepository.getInstance().addTransaction(name, finalCategoryId, finalAmount, selectedType, Transaction.SOURCE_MANUAL);
                        MockDataRepository.getInstance().runOnMainThread(() -> {
                            Toast.makeText(this, "Đã lưu giao dịch thành công", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    });
                });
            });
        });


    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
