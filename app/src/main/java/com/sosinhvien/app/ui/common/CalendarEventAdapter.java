package com.sosinhvien.app.ui.common;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sosinhvien.app.R;
import com.sosinhvien.app.data.model.CalendarEvent;

import java.util.List;

public class CalendarEventAdapter extends RecyclerView.Adapter<CalendarEventAdapter.ViewHolder> {

    private final List<CalendarEvent> items;

    public CalendarEventAdapter(List<CalendarEvent> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CalendarEvent item = items.get(position);
        holder.time.setText(item.getTimeRange());
        holder.title.setText(item.getTitle());

        String typeLabel;
        switch (item.getType()) {
            case CalendarEvent.TYPE_SLEEP:
                typeLabel = "Giờ ngủ";
                break;
            case CalendarEvent.TYPE_TASK:
                typeLabel = "Công việc" + (item.getPriority() != null ? " • " + item.getPriority() : "");
                break;
            default:
                typeLabel = "Sự kiện";
                break;
        }
        holder.type.setText(typeLabel);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView time;
        TextView title;
        TextView type;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            time = itemView.findViewById(R.id.text_time);
            title = itemView.findViewById(R.id.text_title);
            type = itemView.findViewById(R.id.text_type);
        }
    }
}
