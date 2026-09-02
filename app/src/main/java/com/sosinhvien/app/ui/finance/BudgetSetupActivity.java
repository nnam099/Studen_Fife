package com.sosinhvien.app.ui.finance;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.sosinhvien.app.data.MockDataRepository;
import com.sosinhvien.app.data.database.entity.CategoryBudgetEntity;
import com.sosinhvien.app.data.model.Budget;
import com.sosinhvien.app.databinding.ActivityBudgetSetupBinding;
import com.sosinhvien.app.util.CurrencyFormatter;

import java.util.ArrayList;
import java.util.List;

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

        MockDataRepository repo = MockDataRepository.getInstance();
        repo.executeAsync(() -> {
            Budget budget = repo.getCurrentBudget();
            String currentMonth = repo.getCurrentMonthLabel();
            List<CategoryBudgetEntity> categoryBudgets = repo.getCategoryBudgets(budget != null ? budget.getMonthLabel() : currentMonth);
            if (categoryBudgets == null) {
                categoryBudgets = new ArrayList<>();
            }
            final List<CategoryBudgetEntity> finalCategoryBudgets = categoryBudgets;

            // Resolve category names
            List<String> catNames = new ArrayList<>();
            for (CategoryBudgetEntity cb : finalCategoryBudgets) {
                catNames.add(repo.getCategoryById(cb.categoryId).getName());
            }

            long totalAllocatedInit = budget != null ? repo.getTotalAllocatedBudget(budget.getMonthLabel()) : 0;

            repo.runOnMainThread(() -> {
                if (budget != null) {
                    binding.editMonth.setText(budget.getMonthLabel());
                    binding.editTotalBudget.setText(String.valueOf(budget.getTotalBudget()));
                    binding.editOpeningBalance.setText(String.valueOf(budget.getOpeningBalance()));
                    binding.textAllocationSummary.setText(
                            "Tổng phân bổ: " + CurrencyFormatter.format(totalAllocatedInit)
                                    + " / " + CurrencyFormatter.format(budget.getTotalBudget()));
                } else {
                    binding.editMonth.setText(currentMonth);
                    binding.editTotalBudget.setText("");
                    binding.editOpeningBalance.setText("0");
                    binding.textAllocationSummary.setText("Chưa có ngân sách — hãy thiết lập mới");
                }

                // Dynamically load category budgets
                binding.layoutCategoryAllocations.removeAllViews();
                final List<TextInputEditText> editTexts = new ArrayList<>();

                for (int i = 0; i < finalCategoryBudgets.size(); i++) {
                    CategoryBudgetEntity cb = finalCategoryBudgets.get(i);
                    String catName = catNames.get(i);

                    TextInputLayout inputLayout = new TextInputLayout(this);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 8, 0, 8);
                    inputLayout.setLayoutParams(params);
                    inputLayout.setHint("Hạn mức: " + catName);

                    TextInputEditText editText = new TextInputEditText(this);
                    editText.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));
                    editText.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
                    editText.setText(String.valueOf(cb.amount));

                    inputLayout.addView(editText);
                    binding.layoutCategoryAllocations.addView(inputLayout);
                    editTexts.add(editText);
                }

                binding.btnSave.setOnClickListener(v -> {
                    String totalStr = binding.editTotalBudget.getText() != null ? binding.editTotalBudget.getText().toString().trim() : "0";
                    String openingStr = binding.editOpeningBalance.getText() != null ? binding.editOpeningBalance.getText().toString().trim() : "0";
                    long total = 0;
                    long opening = 0;
                    try {
                        total = Long.parseLong(totalStr);
                        opening = Long.parseLong(openingStr);
                    } catch (NumberFormatException ignored) {}

                    if (total <= 0) {
                        Toast.makeText(this, "Tổng ngân sách phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (total > 999_999_999) {
                        Toast.makeText(this, "Tổng ngân sách tối đa là 999.999.999 đ", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (opening < 0) {
                        Toast.makeText(this, "Số dư đầu kỳ phải ≥ 0", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Gather category allocations
                    long totalAllocated = 0;
                    List<CategoryBudgetEntity> updatedAllocations = new ArrayList<>();
                    for (int i = 0; i < finalCategoryBudgets.size(); i++) {
                        CategoryBudgetEntity cb = finalCategoryBudgets.get(i);
                        String amtStr = editTexts.get(i).getText() != null ? editTexts.get(i).getText().toString().trim() : "0";
                        long amt = 0;
                        try {
                            amt = Long.parseLong(amtStr);
                        } catch (NumberFormatException ignored) {}
                        cb.amount = amt;
                        totalAllocated += amt;
                        updatedAllocations.add(cb);
                    }

                    final long finalTotal = total;
                    final long finalOpening = opening;

                    if (totalAllocated > total) {
                        // Show choice Dialog
                        new androidx.appcompat.app.AlertDialog.Builder(this)
                                .setTitle("Cảnh báo ngân sách")
                                .setMessage("Tổng phân bổ các danh mục (" + CurrencyFormatter.format(totalAllocated) 
                                        + ") lớn hơn Tổng ngân sách mới (" + CurrencyFormatter.format(total) + ").\n\nBạn có muốn:")
                                .setPositiveButton("Giảm tỷ lệ đều", (dialog, which) -> {
                                    repo.executeAsync(() -> {
                                        repo.saveBudget(finalTotal, finalOpening);
                                        repo.saveCategoryBudgets(updatedAllocations);
                                        repo.scaleCategoryBudgetsProportionally(finalTotal);
                                        repo.runOnMainThread(() -> {
                                            Toast.makeText(this, "Đã tự động giảm tỷ lệ đều các danh mục và lưu thành công", Toast.LENGTH_SHORT).show();
                                            finish();
                                        });
                                    });
                                })
                                .setNeutralButton("Tự điều chỉnh", (dialog, which) -> dialog.dismiss())
                                .setNegativeButton("Hủy thao tác", (dialog, which) -> finish())
                                .show();
                    } else {
                        repo.executeAsync(() -> {
                            repo.saveBudget(finalTotal, finalOpening);
                            repo.saveCategoryBudgets(updatedAllocations);
                            repo.runOnMainThread(() -> {
                                Toast.makeText(this, "Đã lưu ngân sách và phân bổ thành công", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                        });
                    }
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
