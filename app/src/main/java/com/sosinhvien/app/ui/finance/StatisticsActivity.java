package com.sosinhvien.app.ui.finance;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.sosinhvien.app.R;
import com.sosinhvien.app.data.MockDataRepository;
import com.sosinhvien.app.databinding.ActivityStatisticsBinding;
import com.sosinhvien.app.util.CurrencyFormatter;

import java.util.List;

public class StatisticsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityStatisticsBinding binding = ActivityStatisticsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thống kê chi tiêu");
        }

        // Setup Charts UI placeholders
        if (binding.chartCategory != null) {
            binding.chartCategory.setNoDataText("Đang tải dữ liệu biểu đồ...");
            binding.chartCategory.setNoDataTextColor(ContextCompat.getColor(this, R.color.primary));
            binding.chartCategory.getDescription().setEnabled(false);
            binding.chartCategory.getLegend().setEnabled(false);
            binding.chartCategory.setDrawHoleEnabled(true);
            binding.chartCategory.setHoleColor(Color.TRANSPARENT);
        }

        if (binding.chartTopSpent != null) {
            binding.chartTopSpent.setNoDataText("Đang tải dữ liệu biểu đồ...");
            binding.chartTopSpent.setNoDataTextColor(ContextCompat.getColor(this, R.color.primary));
            binding.chartTopSpent.getDescription().setEnabled(false);
            binding.chartTopSpent.getLegend().setEnabled(false);
        }

        MockDataRepository repo = MockDataRepository.getInstance();

        repo.executeAsync(() -> {
            String monthLabel = repo.getCurrentMonthLabel();
            List<MockDataRepository.CategoryStat> catStats = repo.getCategorySpentRatios();
            List<MockDataRepository.ProductStat> prodStats = repo.getTopPurchasedProducts();
            List<MockDataRepository.ProductStat> spentStats = repo.getTopSpentProducts();

            repo.runOnMainThread(() -> {
                binding.textCategoryTitle.setText("Chi theo danh mục (" + monthLabel + ")");

                // 1. Category stats (fallback text)
                binding.layoutCategoryStats.removeAllViews();
                if (catStats.isEmpty()) {
                    TextView tv = new TextView(this);
                    tv.setText("Chưa có giao dịch chi tiêu nào trong tháng.");
                    binding.layoutCategoryStats.addView(tv);
                    if (binding.chartCategory != null) binding.chartCategory.setNoDataText("Chưa có dữ liệu");
                } else {
                    for (MockDataRepository.CategoryStat stat : catStats) {
                        TextView tv = new TextView(this);
                        tv.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT));
                        tv.setPadding(0, 8, 0, 8);
                        tv.setText(stat.categoryName + " — " + stat.percent + "% (" + CurrencyFormatter.format(stat.spent) + ")");
                        binding.layoutCategoryStats.addView(tv);
                    }
                    if (binding.chartCategory != null) binding.chartCategory.setNoDataText("Biểu đồ danh mục (sắp ra mắt)");
                }

                // 2. Top products
                binding.layoutTopProducts.removeAllViews();
                if (prodStats.isEmpty()) {
                    TextView tv = new TextView(this);
                    tv.setText("Chưa có đủ dữ liệu sản phẩm mua thường xuyên (tần suất >= 3 lần).");
                    binding.layoutTopProducts.addView(tv);
                } else {
                    int rank = 1;
                    for (MockDataRepository.ProductStat stat : prodStats) {
                        TextView tv = new TextView(this);
                        tv.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT));
                        tv.setPadding(0, 8, 0, 8);
                        tv.setText(rank + ". " + stat.productName + " — " + stat.count + " lần (" + CurrencyFormatter.format(stat.totalSpent) + ")");
                        binding.layoutTopProducts.addView(tv);
                        rank++;
                    }
                }

                // 3. Top spent products
                binding.layoutTopSpentProducts.removeAllViews();
                if (spentStats.isEmpty()) {
                    TextView tv = new TextView(this);
                    tv.setText("Chưa có đủ dữ liệu.");
                    binding.layoutTopSpentProducts.addView(tv);
                    if (binding.chartTopSpent != null) binding.chartTopSpent.setNoDataText("Chưa có dữ liệu");
                } else {
                    int rankSpent = 1;
                    for (MockDataRepository.ProductStat stat : spentStats) {
                        TextView tv = new TextView(this);
                        tv.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT));
                        tv.setPadding(0, 8, 0, 8);
                        tv.setText(rankSpent + ". " + stat.productName + " (" + CurrencyFormatter.format(stat.totalSpent) + ")");
                        binding.layoutTopSpentProducts.addView(tv);
                        rankSpent++;
                        if (rankSpent > 10) break;
                    }
                    if (binding.chartTopSpent != null) binding.chartTopSpent.setNoDataText("Biểu đồ sản phẩm (sắp ra mắt)");
                }
            });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
