package com.sosinhvien.app.data;

import android.content.Context;

import com.sosinhvien.app.data.database.AppDatabase;
import com.sosinhvien.app.data.database.entity.BudgetEntity;
import com.sosinhvien.app.data.database.entity.BudgetWarningEntity;
import com.sosinhvien.app.data.database.entity.CalendarEventEntity;
import com.sosinhvien.app.data.database.entity.CategoryBudgetEntity;
import com.sosinhvien.app.data.database.entity.CategoryEntity;
import com.sosinhvien.app.data.database.entity.NotificationLogEntity;
import com.sosinhvien.app.data.database.entity.TaskEntity;
import com.sosinhvien.app.data.database.entity.TransactionEntity;
import com.sosinhvien.app.data.database.entity.UserConfigEntity;
import com.sosinhvien.app.data.database.entity.UserEntity;
import com.sosinhvien.app.data.model.Budget;
import com.sosinhvien.app.data.model.CalendarEvent;
import com.sosinhvien.app.data.model.Category;
import com.sosinhvien.app.data.model.ChatMessage;
import com.sosinhvien.app.data.model.Reminder;
import com.sosinhvien.app.data.model.Transaction;
import com.sosinhvien.app.data.model.User;
import com.sosinhvien.app.util.SessionManager;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class MockDataRepository {

    private static MockDataRepository instance;

    private final AppDatabase db;
    private final Context context;

    private MockDataRepository(Context context) {
        this.context = context.getApplicationContext();
        this.db = AppDatabase.getInstance(this.context);
        prePopulateIfNeeded();
    }

    public static synchronized void initialize(Context context) {
        if (instance == null) {
            instance = new MockDataRepository(context);
        }
    }

    public static synchronized MockDataRepository getInstance() {
        if (instance == null) {
            throw new IllegalStateException("MockDataRepository must be initialized first in Application.onCreate()");
        }
        return instance;
    }

    public String getCurrentUserEmail() {
        if (context != null) {
            return new SessionManager(context).getEmail();
        }
        return "demo@truong.edu.vn";
    }

    public String getCurrentMonthLabel() {
        SimpleDateFormat sdf = new SimpleDateFormat("'Tháng' MM/yyyy", Locale.getDefault());
        return sdf.format(new Date());
    }

    public User getCurrentUser() {
        String email = getCurrentUserEmail();
        UserEntity entity = db.userDao().getUserByEmail(email);
        if (entity != null) {
            return new User(entity.email, entity.displayName);
        }
        return new User(email, "Sinh viên");
    }

    public String getUserDisplayName(String email) {
        UserEntity entity = db.userDao().getUserByEmail(email);
        return entity != null ? entity.displayName : "Sinh viên";
    }

    public Budget getCurrentBudget() {
        String email = getCurrentUserEmail();
        String monthLabel = getCurrentMonthLabel();
        BudgetEntity entity = db.financeDao().getBudget(email, monthLabel);
        if (entity == null) {
            // Create a default if not found
            entity = new BudgetEntity(monthLabel, email, 6_500_000, 2_000_000);
            db.financeDao().insertBudget(entity);
        }
        
        long[] range = getMonthRange(monthLabel);
        long spent = db.financeDao().sumSpentByMonth(email, range[0], range[1]);
        
        return new Budget(entity.monthLabel, entity.totalBudget, entity.openingBalance, spent);
    }

    public void saveBudget(long totalBudget, long openingBalance) {
        String email = getCurrentUserEmail();
        String monthLabel = getCurrentMonthLabel();
        BudgetEntity entity = new BudgetEntity(monthLabel, email, totalBudget, openingBalance);
        db.financeDao().insertBudget(entity);
        
        // Reset warnings since budget changed
        BudgetWarningEntity warning = db.financeDao().getWarningFlag(email, monthLabel, "total");
        if (warning != null) {
            warning.alerted80 = false;
            warning.alerted100 = false;
            db.financeDao().insertWarningFlag(warning);
        }
    }

    public List<Category> getCategories() {
        String email = getCurrentUserEmail();
        List<CategoryEntity> entities = db.financeDao().getVisibleCategories(email);
        List<Category> list = new ArrayList<>();
        for (CategoryEntity entity : entities) {
            list.add(new Category(entity.id, entity.name, entity.iconName, entity.isDefault, entity.visible));
        }
        return list;
    }

    public Category getCategoryById(String id) {
        String email = getCurrentUserEmail();
        CategoryEntity entity = db.financeDao().getCategoryById(email, id);
        if (entity != null) {
            return new Category(entity.id, entity.name, entity.iconName, entity.isDefault, entity.visible);
        }
        return new Category("other", "Khác", "more_horiz", true, true);
    }

    public List<Transaction> getTransactions() {
        String email = getCurrentUserEmail();
        List<TransactionEntity> entities = db.financeDao().getActiveTransactions(email);
        List<Transaction> list = new ArrayList<>();
        for (TransactionEntity entity : entities) {
            list.add(new Transaction(entity.id, entity.name, entity.categoryId, entity.amount,
                    entity.type, entity.source, entity.timeLabel));
        }
        return list;
    }

    public List<Transaction> getTransactionsFiltered(String filter) {
        String email = getCurrentUserEmail();
        List<TransactionEntity> entities;
        if ("Thu".equals(filter)) {
            entities = db.financeDao().getActiveTransactionsByType(email, "income");
        } else if ("Chi".equals(filter)) {
            entities = db.financeDao().getActiveTransactionsByType(email, "expense");
        } else {
            entities = db.financeDao().getActiveTransactions(email);
        }
        List<Transaction> list = new ArrayList<>();
        for (TransactionEntity entity : entities) {
            list.add(new Transaction(entity.id, entity.name, entity.categoryId, entity.amount,
                    entity.type, entity.source, entity.timeLabel));
        }
        return list;
    }

    public void addTransaction(String name, String categoryId, long amount, String type, String source) {
        String email = getCurrentUserEmail();
        String id = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String timeLabel = sdf.format(new Date());

        TransactionEntity transaction = new TransactionEntity(id, email, name, categoryId, amount, type, source, timestamp, timeLabel, "", false);
        db.financeDao().insertTransaction(transaction);

        // Check budget warnings
        checkBudgetThresholds(email, categoryId, amount, type);
    }

    public List<Reminder> getTodayReminders() {
        String email = getCurrentUserEmail();
        List<NotificationLogEntity> entities = db.notificationDao().getNotifications(email);
        List<Reminder> list = new ArrayList<>();
        for (NotificationLogEntity entity : entities) {
            list.add(new Reminder(entity.title, entity.subtitle, entity.accentColor));
        }
        return list;
    }

    public List<CalendarEvent> getTodayEvents() {
        String email = getCurrentUserEmail();
        List<CalendarEventEntity> entities = db.timeDao().getEventsInRange(email, 0, Long.MAX_VALUE);
        List<CalendarEvent> list = new ArrayList<>();
        for (CalendarEventEntity entity : entities) {
            String timeRange = formatTimeRange(entity.startTime, entity.endTime, entity.type);
            boolean completed = "Đã hoàn thành".equals(entity.sessionStatus);
            list.add(new CalendarEvent(entity.id, entity.title, timeRange, entity.type, entity.priority, completed));
        }
        return list;
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
        UserEntity user = db.userDao().getUserByEmail(email);
        if (user == null) {
            return false;
        }
        long now = System.currentTimeMillis();
        if (user.lockedUntil > now) {
            return false; // Locked out
        }
        String hash = hashPassword(password);
        if (user.passwordHash.equals(hash)) {
            user.failedLoginAttempts = 0;
            user.lockedUntil = 0;
            db.userDao().updateUser(user);
            return true;
        } else {
            user.failedLoginAttempts++;
            if (user.failedLoginAttempts >= 5) {
                user.lockedUntil = now + 5 * 60 * 1000; // 5 min lockout
            }
            db.userDao().updateUser(user);
            return false;
        }
    }

    public boolean registerUser(String email, String password, String displayName) {
        UserEntity existing = db.userDao().getUserByEmail(email);
        if (existing != null) {
            return false;
        }
        String hash = hashPassword(password);
        UserEntity user = new UserEntity(email, hash, displayName);
        db.userDao().insertUser(user);

        // Create default configurations
        UserConfigEntity config = new UserConfigEntity(
                email, "23:00", "07:00", 15, 30, 90,
                "Đọc sách, Thể thao", "Học tốt học kỳ này", "", "",
                true, "24,3", 15, "21:00", true
        );
        db.userDao().insertConfig(config);

        // Create default categories
        db.financeDao().insertCategory(new CategoryEntity("food", email, "Ăn uống", "restaurant", true, true));
        db.financeDao().insertCategory(new CategoryEntity("transport", email, "Di chuyển", "directions_car", true, true));
        db.financeDao().insertCategory(new CategoryEntity("study", email, "Học tập", "school", true, true));
        db.financeDao().insertCategory(new CategoryEntity("entertainment", email, "Giải trí", "movie", true, true));
        db.financeDao().insertCategory(new CategoryEntity("other", email, "Khác", "more_horiz", true, true));

        // Create default budget
        String monthLabel = getCurrentMonthLabel();
        BudgetEntity budget = new BudgetEntity(monthLabel, email, 6_500_000, 2_000_000);
        db.financeDao().insertBudget(budget);

        // Create default category budgets
        db.financeDao().insertCategoryBudget(new CategoryBudgetEntity(monthLabel, "food", email, 2_000_000));
        db.financeDao().insertCategoryBudget(new CategoryBudgetEntity(monthLabel, "transport", email, 500_000));
        db.financeDao().insertCategoryBudget(new CategoryBudgetEntity(monthLabel, "study", email, 1_000_000));
        db.financeDao().insertCategoryBudget(new CategoryBudgetEntity(monthLabel, "entertainment", email, 1_000_000));

        return true;
    }

    public void saveUserLocally(String email, String displayName) {
        UserEntity existing = db.userDao().getUserByEmail(email);
        if (existing == null) {
            UserEntity user = new UserEntity(email, "", displayName);
            db.userDao().insertUser(user);
        }
    }

    private void checkBudgetThresholds(String email, String categoryId, long amount, String type) {
        if (!"expense".equals(type)) return;
        String monthLabel = getCurrentMonthLabel();
        BudgetEntity budget = db.financeDao().getBudget(email, monthLabel);
        if (budget == null) return;

        long[] range = getMonthRange(monthLabel);
        long totalSpent = db.financeDao().sumSpentByMonth(email, range[0], range[1]);

        checkAndRaiseWarning(email, "total", monthLabel, totalSpent, budget.totalBudget, "Ngân sách tổng");

        if (categoryId != null) {
            List<CategoryBudgetEntity> cbList = db.financeDao().getCategoryBudgets(email, monthLabel);
            CategoryBudgetEntity targetCb = null;
            for (CategoryBudgetEntity cb : cbList) {
                if (cb.categoryId.equals(categoryId)) {
                    targetCb = cb;
                    break;
                }
            }
            if (targetCb != null) {
                long catSpent = db.financeDao().sumSpentByCategoryMonth(email, categoryId, range[0], range[1]);
                CategoryEntity cat = db.financeDao().getCategoryById(email, categoryId);
                String catName = cat != null ? cat.name : "Danh mục";
                checkAndRaiseWarning(email, categoryId, monthLabel, catSpent, targetCb.amount, "Ngân sách " + catName);
            }
        }
    }

    private void checkAndRaiseWarning(String email, String categoryId, String monthLabel, long spent, long limit, String name) {
        if (limit <= 0) return;
        double percent = (double) spent / limit;

        BudgetWarningEntity flag = db.financeDao().getWarningFlag(email, monthLabel, categoryId);
        if (flag == null) {
            flag = new BudgetWarningEntity(monthLabel, categoryId, email, false, false);
        }

        boolean updated = false;
        long now = System.currentTimeMillis();

        if (percent >= 1.0 && !flag.alerted100) {
            flag.alerted100 = true;
            updated = true;
            NotificationLogEntity notif = new NotificationLogEntity(
                    UUID.randomUUID().toString(),
                    email,
                    "Vượt hạn mức chi tiêu",
                    name + " đã vượt quá 100% hạn mức (Đã chi " + formatVnd(spent) + " / " + formatVnd(limit) + ")",
                    "budget_100",
                    "danger",
                    now,
                    false,
                    categoryId
            );
            db.notificationDao().insertNotification(notif);
        } else if (percent >= 0.8 && percent < 1.0 && !flag.alerted80) {
            flag.alerted80 = true;
            updated = true;
            NotificationLogEntity notif = new NotificationLogEntity(
                    UUID.randomUUID().toString(),
                    email,
                    "Cảnh báo hạn mức chi tiêu",
                    name + " đã tiêu dùng hơn 80% hạn mức (Đã chi " + formatVnd(spent) + " / " + formatVnd(limit) + ")",
                    "budget_80",
                    "warning",
                    now,
                    false,
                    categoryId
            );
            db.notificationDao().insertNotification(notif);
        }

        if (updated) {
            db.financeDao().insertWarningFlag(flag);
        }
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private long[] getMonthRange(String monthLabel) {
        int month = 10;
        int year = 2025;
        try {
            String clean = monthLabel.replace("Tháng ", "").trim();
            String[] parts = clean.split("/");
            if (parts.length == 2) {
                month = Integer.parseInt(parts[0]);
                year = Integer.parseInt(parts[1]);
            } else {
                parts = clean.split("-");
                if (parts.length == 2) {
                    year = Integer.parseInt(parts[0]);
                    month = Integer.parseInt(parts[1]);
                }
            }
        } catch (Exception ignored) {}

        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long start = cal.getTimeInMillis();

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        long end = cal.getTimeInMillis();

        return new long[]{start, end};
    }

    private String formatTimeRange(long startTime, long endTime, String type) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        if ("sleep".equals(type)) {
            return sdf.format(new Date(startTime)) + " - " + sdf.format(new Date(endTime)) + " (Hôm sau)";
        }
        return sdf.format(new Date(startTime)) + " - " + sdf.format(new Date(endTime));
    }

    private void prePopulateIfNeeded() {
        UserEntity demo = db.userDao().getUserByEmail("demo@truong.edu.vn");
        if (demo == null) {
            registerUser("demo@truong.edu.vn", "demo1234", "Minh");

            String email = "demo@truong.edu.vn";
            Calendar cal = Calendar.getInstance();
            int currentYear = cal.get(Calendar.YEAR);
            int currentMonth = cal.get(Calendar.MONTH);
            int currentDay = cal.get(Calendar.DAY_OF_MONTH);

            // Today, 12:30
            cal.set(currentYear, currentMonth, currentDay, 12, 30, 0);
            long t1Time = cal.getTimeInMillis();
            db.financeDao().insertTransaction(new TransactionEntity("t1", email, "Cơm trưa", "food", 35000, "expense", "Thủ công", t1Time, "12:30", "Ăn trưa ở căng tin", false));

            // Yesterday, 10:00
            cal.set(currentYear, currentMonth, currentDay, 10, 0, 0);
            cal.add(Calendar.DAY_OF_MONTH, -1);
            long t2Time = cal.getTimeInMillis();
            // Reset cal to today
            cal.setTimeInMillis(System.currentTimeMillis());
            db.financeDao().insertTransaction(new TransactionEntity("t2", email, "Tiền tiêu vặt tháng 10", "other", 2000000, "income", "Thủ công", t2Time, "Hôm qua", "Bố mẹ gửi", false));

            // Today, 08:00
            cal.set(currentYear, currentMonth, currentDay, 8, 0, 0);
            long t3Time = cal.getTimeInMillis();
            db.financeDao().insertTransaction(new TransactionEntity("t3", email, "Grab", "transport", 20000, "expense", "OCR", t3Time, "08:00", "Đi học xe ôm", false));

            // Today, 15:20
            cal.set(currentYear, currentMonth, currentDay, 15, 20, 0);
            long t4Time = cal.getTimeInMillis();
            db.financeDao().insertTransaction(new TransactionEntity("t4", email, "Cà phê", "food", 45000, "expense", "Thủ công", t4Time, "15:20", "Uống cà phê học nhóm", false));

            // 2 days ago, 17:00
            cal.set(currentYear, currentMonth, currentDay, 17, 0, 0);
            cal.add(Calendar.DAY_OF_MONTH, -2);
            long t5Time = cal.getTimeInMillis();
            // Reset cal to today
            cal.setTimeInMillis(System.currentTimeMillis());
            db.financeDao().insertTransaction(new TransactionEntity("t5", email, "Part-time tuần 3", "other", 800000, "income", "Trợ lý", t5Time, "2 ngày trước", "Làm gia sư", false));

            // Today, 09:15
            cal.set(currentYear, currentMonth, currentDay, 9, 15, 0);
            long t6Time = cal.getTimeInMillis();
            db.financeDao().insertTransaction(new TransactionEntity("t6", email, "Photocopy", "study", 12000, "expense", "Thủ công", t6Time, "09:15", "Tài liệu học tập", false));

            // Default task
            cal.set(currentYear, currentMonth, currentDay, 14, 0, 0);
            db.timeDao().insertTask(new TaskEntity("task_java", email, "Nộp bài tập Java", cal.getTimeInMillis(), "Cao", 120, "Chưa thực hiện", null, false));

            // Default events
            cal.set(currentYear, currentMonth, currentDay, 23, 0, 0);
            long e0Start = cal.getTimeInMillis();
            cal.add(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR_OF_DAY, 7);
            cal.set(Calendar.MINUTE, 0);
            long e0End = cal.getTimeInMillis();
            // Reset cal to today
            cal.setTimeInMillis(System.currentTimeMillis());
            db.timeDao().insertEvent(new CalendarEventEntity("e0", email, "Giờ ngủ", e0Start, e0End, "sleep", null, null, null, null, false, null, null, null, false));

            cal.set(currentYear, currentMonth, currentDay, 8, 0, 0);
            long e1Start = cal.getTimeInMillis();
            cal.set(currentYear, currentMonth, currentDay, 10, 0, 0);
            long e1End = cal.getTimeInMillis();
            db.timeDao().insertEvent(new CalendarEventEntity("e1", email, "Học nhóm", e1Start, e1End, "event", null, null, null, null, false, null, null, null, false));

            cal.set(currentYear, currentMonth, currentDay, 14, 0, 0);
            long e2Time = cal.getTimeInMillis();
            db.timeDao().insertEvent(new CalendarEventEntity("e2", email, "Nộp bài tập Java", e2Time, e2Time, "task", "Cao", "task_java", "Đã lên lịch", null, false, null, null, null, false));

            cal.set(currentYear, currentMonth, currentDay, 14, 0, 0);
            long e3Start = cal.getTimeInMillis();
            cal.set(currentYear, currentMonth, currentDay, 16, 30, 0);
            long e3End = cal.getTimeInMillis();
            db.timeDao().insertEvent(new CalendarEventEntity("e3", email, "Học Thể chất - Sân B2", e3Start, e3End, "event", null, null, null, null, false, null, null, null, false));

            cal.set(currentYear, currentMonth, currentDay, 19, 0, 0);
            long e4Start = cal.getTimeInMillis();
            cal.set(currentYear, currentMonth, currentDay, 21, 0, 0);
            long e4End = cal.getTimeInMillis();
            db.timeDao().insertEvent(new CalendarEventEntity("e4", email, "Tập Gym", e4Start, e4End, "event", null, null, null, null, false, null, null, null, false));

            // Default notifications
            db.notificationDao().insertNotification(new NotificationLogEntity("r1", email, "Hạn nộp học phí", "Còn 2 ngày", "task_deadline", "primary", System.currentTimeMillis() - 3600000, false, ""));
            db.notificationDao().insertNotification(new NotificationLogEntity("r2", email, "Cảnh báo ngân sách", "Ngân sách Ăn uống sắp hết (Còn 150k)", "budget_80", "warning", System.currentTimeMillis() - 7200000, false, "food"));
            db.notificationDao().insertNotification(new NotificationLogEntity("r3", email, "Nộp bài tập Java", "Hạn 14:00 hôm nay", "task_deadline", "danger", System.currentTimeMillis() - 10800000, false, "task_java"));
        }
    }
}
