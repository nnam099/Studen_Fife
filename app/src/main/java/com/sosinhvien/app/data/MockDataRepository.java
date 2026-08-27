package com.sosinhvien.app.data;

import com.sosinhvien.app.data.model.Budget;
import com.sosinhvien.app.data.model.CalendarEvent;
import com.sosinhvien.app.data.model.Category;
import com.sosinhvien.app.data.model.ChatMessage;
import com.sosinhvien.app.data.model.Reminder;
import com.sosinhvien.app.data.model.Transaction;
import com.sosinhvien.app.data.model.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class MockDataRepository {

    private static MockDataRepository instance;

    private final User demoUser = new User("demo@truong.edu.vn", "Minh");
    private final Budget budget = new Budget("Tháng 10/2025", 6_500_000, 2_000_000, 2_250_000);

    private MockDataRepository() {
    }

    public static synchronized MockDataRepository getInstance() {
        if (instance == null) {
            instance = new MockDataRepository();
        }
        return instance;
    }

    public User getCurrentUser() {
        return demoUser;
    }

    public Budget getCurrentBudget() {
        return budget;
    }

    public List<Category> getCategories() {
        return Arrays.asList(
                new Category("food", "Ăn uống", "restaurant", true, true),
                new Category("transport", "Di chuyển", "directions_car", true, true),
                new Category("study", "Học tập", "school", true, true),
                new Category("entertainment", "Giải trí", "movie", true, true),
                new Category("other", "Khác", "more_horiz", true, true)
        );
    }

    public Category getCategoryById(String id) {
        for (Category c : getCategories()) {
            if (c.getId().equals(id)) return c;
        }
        return getCategories().get(getCategories().size() - 1);
    }

    public List<Transaction> getTransactions() {
        return Arrays.asList(
                new Transaction("t1", "Cơm trưa", "food", 35_000,
                        Transaction.TYPE_EXPENSE, Transaction.SOURCE_MANUAL, "12:30"),
                new Transaction("t2", "Tiền tiêu vặt tháng 10", "other", 2_000_000,
                        Transaction.TYPE_INCOME, Transaction.SOURCE_MANUAL, "Hôm qua"),
                new Transaction("t3", "Grab", "transport", 20_000,
                        Transaction.TYPE_EXPENSE, Transaction.SOURCE_OCR, "08:00"),
                new Transaction("t4", "Cà phê", "food", 45_000,
                        Transaction.TYPE_EXPENSE, Transaction.SOURCE_MANUAL, "15:20"),
                new Transaction("t5", "Part-time tuần 3", "other", 800_000,
                        Transaction.TYPE_INCOME, Transaction.SOURCE_ASSISTANT, "2 ngày trước"),
                new Transaction("t6", "Photocopy", "study", 12_000,
                        Transaction.TYPE_EXPENSE, Transaction.SOURCE_MANUAL, "09:15")
        );
    }

    public List<Transaction> getTransactionsFiltered(String filter) {
        List<Transaction> all = getTransactions();
        if ("Thu".equals(filter)) {
            List<Transaction> result = new ArrayList<>();
            for (Transaction t : all) {
                if (!t.isExpense()) result.add(t);
            }
            return result;
        }
        if ("Chi".equals(filter)) {
            List<Transaction> result = new ArrayList<>();
            for (Transaction t : all) {
                if (t.isExpense()) result.add(t);
            }
            return result;
        }
        return all;
    }

    public List<Reminder> getTodayReminders() {
        return Arrays.asList(
                new Reminder("Hạn nộp học phí", "Còn 2 ngày", "primary"),
                new Reminder("Cảnh báo ngân sách", "Ngân sách Ăn uống sắp hết (Còn 150k)", "warning"),
                new Reminder("Nộp bài tập Java", "Hạn 14:00 hôm nay", "danger")
        );
    }

    public List<CalendarEvent> getTodayEvents() {
        return Arrays.asList(
                new CalendarEvent("e0", "Giờ ngủ", "23:00 - 07:00 (Hôm sau)",
                        CalendarEvent.TYPE_SLEEP, null, false),
                new CalendarEvent("e1", "Học nhóm", "08:00 - 10:00",
                        CalendarEvent.TYPE_EVENT, null, false),
                new CalendarEvent("e2", "Nộp bài tập Java", "14:00",
                        CalendarEvent.TYPE_TASK, "Cao", false),
                new CalendarEvent("e3", "Học Thể chất - Sân B2", "14:00 - 16:30",
                        CalendarEvent.TYPE_EVENT, null, false),
                new CalendarEvent("e4", "Tập Gym", "19:00 - 21:00",
                        CalendarEvent.TYPE_EVENT, null, false)
        );
    }

    public List<ChatMessage> getInitialChatMessages() {
        return Arrays.asList(
                new ChatMessage(ChatMessage.ROLE_ASSISTANT,
                        "Xin chào! Tôi là trợ lý Sổ Sinh Viên. Bạn có thể hỏi về chi tiêu, lịch học hoặc nhờ tôi ghi khoản chi."),
                new ChatMessage(ChatMessage.ROLE_ASSISTANT,
                        "Ví dụ: \"Tháng này tôi còn bao nhiêu?\" hoặc \"Thêm khoản chi 50k ăn trưa\".")
        );
    }

    public String getMockAssistantReply(String userMessage) {
        String lower = userMessage.toLowerCase();
        if (lower.contains("còn bao nhiêu") || lower.contains("số dư")) {
            Budget b = getCurrentBudget();
            return "Tháng này bạn còn " + formatVnd(b.getAvailableBalance())
                    + " khả dụng. Đã chi " + formatVnd(b.getSpent())
                    + " / ngân sách " + formatVnd(b.getTotalBudget()) + ".";
        }
        if (lower.contains("thêm") && lower.contains("chi")) {
            return "Tôi đề xuất tạo khoản chi mới. Vui lòng xác nhận trước khi lưu (demo).";
        }
        if (lower.contains("lịch") || lower.contains("xếp")) {
            return "Tôi tìm thấy 2 khung giờ trống phù hợp để ôn thi. Mở màn Đề xuất lịch AI để xem chi tiết.";
        }
        return "Đây là phản hồi demo. Trong bản production, trợ lý sẽ truy vấn dữ liệu thực của bạn.";
    }

    private String formatVnd(long amount) {
        return String.format("%,d đ", amount).replace(',', '.');
    }

    public boolean validateLogin(String email, String password) {
        return "demo@truong.edu.vn".equalsIgnoreCase(email) && "demo1234".equals(password);
    }
}
