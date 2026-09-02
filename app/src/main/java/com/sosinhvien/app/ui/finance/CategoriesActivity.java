package com.sosinhvien.app.ui.finance;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sosinhvien.app.data.MockDataRepository;
import com.sosinhvien.app.databinding.ActivityCategoriesBinding;
import com.sosinhvien.app.ui.common.CategoryAdapter;

public class CategoriesActivity extends AppCompatActivity implements CategoryAdapter.OnCategoryActionListener {

    private ActivityCategoriesBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategoriesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản lý danh mục");
        }

        binding.recyclerCategories.setLayoutManager(new LinearLayoutManager(this));
        refreshList();

        binding.fabAddCategory.setOnClickListener(v -> showAddCategoryDialog());
    }

    private void refreshList() {
        MockDataRepository.getInstance().executeAsync(() -> {
            java.util.List<com.sosinhvien.app.data.model.Category> categories = MockDataRepository.getInstance().getAllCategories();
            MockDataRepository.getInstance().runOnMainThread(() -> {
                binding.recyclerCategories.setAdapter(new CategoryAdapter(categories, this));
            });
        });
    }

    private void showAddCategoryDialog() {
        android.widget.EditText input = new android.widget.EditText(this);
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Thêm danh mục mới")
                .setView(input)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String name = input.getText().toString();
                    if (!name.trim().isEmpty()) {
                        MockDataRepository.getInstance().executeAsync(() -> {
                            boolean success = MockDataRepository.getInstance().addCategory(name);
                            MockDataRepository.getInstance().runOnMainThread(() -> {
                                if (success) {
                                    refreshList();
                                } else {
                                    android.widget.Toast.makeText(this, "Danh mục đã tồn tại", android.widget.Toast.LENGTH_SHORT).show();
                                }
                            });
                        });
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onRenameClick(com.sosinhvien.app.data.model.Category category) {
        if (category.isDefault()) {
            android.widget.Toast.makeText(this, "Không thể đổi tên danh mục mặc định", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        android.widget.EditText input = new android.widget.EditText(this);
        input.setText(category.getName());
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Đổi tên danh mục")
                .setView(input)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String name = input.getText().toString();
                    if (!name.trim().isEmpty()) {
                        MockDataRepository.getInstance().executeAsync(() -> {
                            boolean success = MockDataRepository.getInstance().renameCategory(category.getId(), name);
                            MockDataRepository.getInstance().runOnMainThread(() -> {
                                if (success) {
                                    refreshList();
                                } else {
                                    android.widget.Toast.makeText(this, "Tên danh mục đã tồn tại", android.widget.Toast.LENGTH_SHORT).show();
                                }
                            });
                        });
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onHideClick(com.sosinhvien.app.data.model.Category category) {
        if (category.isDefault()) {
            android.widget.Toast.makeText(this, "Không thể ẩn danh mục mặc định", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Ẩn danh mục")
                .setMessage("Bạn có chắc muốn ẩn danh mục này?")
                .setPositiveButton("Ẩn", (dialog, which) -> {
                    MockDataRepository.getInstance().executeAsync(() -> {
                        MockDataRepository.getInstance().hideCategory(category.getId());
                        MockDataRepository.getInstance().runOnMainThread(this::refreshList);
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
