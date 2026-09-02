package com.sosinhvien.app.ui.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sosinhvien.app.data.MockDataRepository;
import com.sosinhvien.app.data.database.entity.CalendarEventEntity;
import com.sosinhvien.app.databinding.FragmentCalendarBinding;
import com.sosinhvien.app.ui.calendar.AiScheduleActivity;
import com.sosinhvien.app.ui.common.CalendarEventAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CalendarFragment extends Fragment {

    private FragmentCalendarBinding binding;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    importCalendarEvents();
                } else {
                    Toast.makeText(requireContext(), "Cần cấp quyền đọc lịch để đồng bộ", Toast.LENGTH_SHORT).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false);

        binding.textMonthLabel.setText(MockDataRepository.getInstance().getCurrentMonthLabel());
        binding.recyclerEvents.setLayoutManager(new LinearLayoutManager(requireContext()));

        binding.btnAiSchedule.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AiScheduleActivity.class)));

        binding.btnImportCalendar.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CALENDAR)
                    == PackageManager.PERMISSION_GRANTED) {
                importCalendarEvents();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_CALENDAR);
            }
        });

        return binding.getRoot();
    }

    private void importCalendarEvents() {
        MockDataRepository repo = MockDataRepository.getInstance();
        repo.executeAsync(() -> {
            List<CalendarEventEntity> importedEvents = new ArrayList<>();
            String[] projection = new String[]{
                    CalendarContract.Events.TITLE,
                    CalendarContract.Events.DTSTART,
                    CalendarContract.Events.DTEND
            };

            // Query events from today onwards, up to 30 days
            long now = System.currentTimeMillis();
            long thirtyDaysLater = now + (30L * 24 * 60 * 60 * 1000);
            
            String selection = CalendarContract.Events.DTSTART + " >= ? AND " + CalendarContract.Events.DTSTART + " <= ?";
            String[] selectionArgs = new String[]{String.valueOf(now), String.valueOf(thirtyDaysLater)};

            try (Cursor cursor = requireContext().getContentResolver().query(
                    CalendarContract.Events.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    CalendarContract.Events.DTSTART + " ASC"
            )) {
                if (cursor != null) {
                    int titleIdx = cursor.getColumnIndexOrThrow(CalendarContract.Events.TITLE);
                    int startIdx = cursor.getColumnIndexOrThrow(CalendarContract.Events.DTSTART);
                    int endIdx = cursor.getColumnIndexOrThrow(CalendarContract.Events.DTEND);

                    while (cursor.moveToNext()) {
                        String title = cursor.getString(titleIdx);
                        long start = cursor.getLong(startIdx);
                        long end = cursor.getLong(endIdx);
                        
                        if (end == 0) end = start + 3600000; // default 1 hour if no end time

                        CalendarEventEntity entity = new CalendarEventEntity(
                                UUID.randomUUID().toString(),
                                repo.getCurrentUserId(),
                                title != null ? title : "Sự kiện",
                                start,
                                end,
                                "google_calendar",
                                null, null, null, null, false, null, null, null, false
                        );
                        importedEvents.add(entity);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            repo.importSystemEvents(importedEvents);
            
            repo.runOnMainThread(() -> {
                if (binding != null) {
                    Toast.makeText(requireContext(), "Đã đồng bộ " + importedEvents.size() + " sự kiện", Toast.LENGTH_SHORT).show();
                    refreshEvents();
                }
            });
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshEvents();
    }

    private void refreshEvents() {
        if (binding == null) return;
        MockDataRepository repo = MockDataRepository.getInstance();
        repo.executeAsync(() -> {
            java.util.List<com.sosinhvien.app.data.model.CalendarEvent> events = repo.getTodayEvents();
            repo.runOnMainThread(() -> {
                if (binding == null) return;
                binding.recyclerEvents.setAdapter(new CalendarEventAdapter(events));
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
