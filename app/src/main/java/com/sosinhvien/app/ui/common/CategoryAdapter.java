package com.sosinhvien.app.ui.common;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sosinhvien.app.R;
import com.sosinhvien.app.data.model.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    public interface OnCategoryActionListener {
        void onRenameClick(Category category);
        void onHideClick(Category category);
    }

    private final List<Category> items;
    private final OnCategoryActionListener listener;

    public CategoryAdapter(List<Category> items, OnCategoryActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category item = items.get(position);
        holder.name.setText(item.getName());
        holder.badge.setVisibility(item.isDefault() ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(v.getContext());
            builder.setTitle("Tùy chọn danh mục");
            String[] options = {"Đổi tên", "Ẩn danh mục"};
            builder.setItems(options, (dialog, which) -> {
                if (which == 0) {
                    listener.onRenameClick(item);
                } else {
                    listener.onHideClick(item);
                }
            });
            builder.show();
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView badge;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text_category_name);
            badge = itemView.findViewById(R.id.text_badge);
        }
    }
}
