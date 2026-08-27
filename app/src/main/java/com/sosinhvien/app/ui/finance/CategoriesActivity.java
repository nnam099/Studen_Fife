package com.sosinhvien.app.ui.finance;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sosinhvien.app.data.MockDataRepository;
import com.sosinhvien.app.databinding.ActivityCategoriesBinding;
import com.sosinhvien.app.ui.common.CategoryAdapter;

public class CategoriesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCategoriesBinding binding = ActivityCategoriesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản lý danh mục");
        }

        binding.recyclerCategories.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerCategories.setAdapter(
                new CategoryAdapter(MockDataRepository.getInstance().getCategories()));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
