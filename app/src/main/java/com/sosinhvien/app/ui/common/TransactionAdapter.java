package com.sosinhvien.app.ui.common;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.sosinhvien.app.R;
import com.sosinhvien.app.data.MockDataRepository;
import com.sosinhvien.app.data.model.Transaction;
import com.sosinhvien.app.util.CurrencyFormatter;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private final List<Transaction> items;

    public TransactionAdapter(List<Transaction> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction item = items.get(position);
        holder.name.setText(item.getName());
        holder.time.setText(item.getTimeLabel());
        holder.source.setText(item.getSource());

        String prefix = item.isExpense() ? "-" : "+";
        holder.amount.setText(prefix + CurrencyFormatter.format(item.getAmount()));

        int color = item.isExpense()
                ? ContextCompat.getColor(holder.itemView.getContext(), R.color.danger)
                : ContextCompat.getColor(holder.itemView.getContext(), R.color.success);
        holder.amount.setTextColor(color);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView time;
        TextView source;
        TextView amount;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text_name);
            time = itemView.findViewById(R.id.text_time);
            source = itemView.findViewById(R.id.text_source);
            amount = itemView.findViewById(R.id.text_amount);
        }
    }
}
