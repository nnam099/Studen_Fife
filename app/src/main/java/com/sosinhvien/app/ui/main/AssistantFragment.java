package com.sosinhvien.app.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.button.MaterialButton;
import com.sosinhvien.app.data.MockDataRepository;
import com.sosinhvien.app.data.model.ChatMessage;
import com.sosinhvien.app.databinding.FragmentAssistantBinding;
import com.sosinhvien.app.ui.common.ChatAdapter;

import java.util.ArrayList;
import java.util.List;

public class AssistantFragment extends Fragment {

    private FragmentAssistantBinding binding;
    private final List<ChatMessage> messages = new ArrayList<>();
    private ChatAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAssistantBinding.inflate(inflater, container, false);

        MockDataRepository.getInstance().executeAsync(() -> {
            java.util.List<com.sosinhvien.app.data.model.ChatMessage> initialMsgs = MockDataRepository.getInstance().getInitialChatMessages();
            MockDataRepository.getInstance().runOnMainThread(() -> {
                if (binding == null) return;
                messages.addAll(initialMsgs);
                adapter = new ChatAdapter(messages);
                binding.recyclerChat.setLayoutManager(new LinearLayoutManager(requireContext()));
                binding.recyclerChat.setAdapter(adapter);
            });
        });

        String[] prompts = {
                "Tháng này tôi còn bao nhiêu?",
                "Xếp lịch ôn thi cuối kỳ",
                "Thêm khoản chi 50k ăn trưa"
        };
        for (String prompt : prompts) {
            MaterialButton chip = new MaterialButton(requireContext(), null,
                    com.google.android.material.R.style.Widget_Material3_Button_OutlinedButton);
            chip.setText(prompt);
            chip.setOnClickListener(v -> sendMessage(prompt));
            binding.layoutPromptChips.addView(chip);
        }

        binding.btnSend.setOnClickListener(v -> {
            String text = binding.editMessage.getText() != null
                    ? binding.editMessage.getText().toString().trim() : "";
            if (!text.isEmpty()) {
                sendMessage(text);
                binding.editMessage.setText("");
            }
        });

        return binding.getRoot();
    }

    private void sendMessage(String text) {
        messages.add(new ChatMessage(ChatMessage.ROLE_USER, text));
        if (adapter != null) {
            adapter.notifyDataSetChanged();
            binding.recyclerChat.scrollToPosition(messages.size() - 1);
        }

        MockDataRepository.getInstance().executeAsync(() -> {
            String reply = MockDataRepository.getInstance().getMockAssistantReply(text);
            MockDataRepository.getInstance().runOnMainThread(() -> {
                if (binding == null) return;
                messages.add(new ChatMessage(ChatMessage.ROLE_ASSISTANT, reply));
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                    binding.recyclerChat.scrollToPosition(messages.size() - 1);
                }
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
