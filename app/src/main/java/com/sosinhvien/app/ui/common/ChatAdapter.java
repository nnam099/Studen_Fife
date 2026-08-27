package com.sosinhvien.app.ui.common;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.sosinhvien.app.R;
import com.sosinhvien.app.data.model.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private final List<ChatMessage> items;

    public ChatAdapter(List<ChatMessage> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMessage item = items.get(position);
        holder.message.setText(item.getContent());

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.message.getLayoutParams();
        if (item.isUser()) {
            holder.message.setBackgroundResource(R.drawable.bg_chip_active);
            holder.message.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.on_primary_container));
            params.gravity = Gravity.END;
            holder.container.setGravity(Gravity.END);
        } else {
            holder.message.setBackgroundResource(R.drawable.bg_card);
            holder.message.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.on_surface));
            params.gravity = Gravity.START;
            holder.container.setGravity(Gravity.START);
        }
        holder.message.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout container;
        TextView message;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.bubble_container);
            message = itemView.findViewById(R.id.text_message);
        }
    }
}
