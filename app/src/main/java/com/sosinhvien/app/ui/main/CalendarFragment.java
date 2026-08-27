package com.sosinhvien.app.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sosinhvien.app.data.MockDataRepository;
import com.sosinhvien.app.databinding.FragmentCalendarBinding;
import com.sosinhvien.app.ui.calendar.AiScheduleActivity;
import com.sosinhvien.app.ui.common.CalendarEventAdapter;

public class CalendarFragment extends Fragment {

    private FragmentCalendarBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false);

        binding.textMonthLabel.setText("Tháng 10, 2025");
        binding.recyclerEvents.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerEvents.setAdapter(
                new CalendarEventAdapter(MockDataRepository.getInstance().getTodayEvents()));

        binding.btnAiSchedule.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AiScheduleActivity.class)));

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
