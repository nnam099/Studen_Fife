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
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public final class MockDataRepository {

    private static MockDataRepository instance;

    public interface DataCallback<T> { void onDataLoaded(T data); }
    public interface ActionCallback { void onComplete(); }
    public interface ActionCallbackBool { void onComplete(boolean success); }

    private final java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(4);
    private final android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());

    public void executeAsync(Runnable backgroundTask) {
        executor.execute(backgroundTask);
    }

    public void runOnMainThread(Runnable mainThreadTask) {
        mainHandler.post(mainThreadTask);
    }

    private final AppDatabase db;
    private final Context context;

    private MockDataRepository(Context context) {
        this.context = context.getApplicationContext();
        this.db = AppDatabase.getInstance(this.context);
        executor.execute(this::prePopulateIfNeeded);
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

    public String getCurrentUserId() {
        if (context != null) {
            return new SessionManager(context).getUserId();
        }
        return "default_user_id";
    }

    public String getCurrentMonthLabel() {
        SimpleDateFormat sdf = new SimpleDateFormat("'Tháng' MM/yyyy", Locale.getDefault());
        return sdf.format(new Date());
    }

    public User getCurrentUser() {
        String userId = getCurrentUserId();
        UserEntity entity = db.userDao().getUserById(userId);
        if (entity != null) {
            return new User(entity.email, entity.displayName);
        }
        return new User("unknown@example.com", "Sinh viên");
    }

    public String getUserDisplayName(String email) {
        UserEntity entity = db.userDao().getUserByEmail(email);
        return entity != null ? entity.displayName : "Sinh viên";
    }

    public String getUserIdByEmail(String email) {
        UserEntity entity = db.userDao().getUserByEmail(email);
        return entity != null ? entity.id : "default_user_id";
    }

    public Budget getCurrentBudget() {
        String userId = getCurrentUserId();
        String monthLabel = getCurrentMonthLabel();
        BudgetEntity entity = db.financeDao().getBudget(userId, monthLabel);
        if (entity == null) {
            return null;
        }
        
        long[] range = getMonthRange(monthLabel);
        long maxTime = Math.min(range[1], System.currentTimeMillis());
        long spent = db.financeDao().sumSpentByMonth(userId, range[0], maxTime);
        long income = db.financeDao().sumIncomeByMonth(userId, range[0], maxTime);
        
        return new Budget(entity.monthLabel, entity.totalBudget, entity.openingBalance, spent, income);
    }

    public boolean hasBudget() {
        String userId = getCurrentUserId();
        String monthLabel = getCurrentMonthLabel();
        return db.financeDao().getBudget(userId, monthLabel) != null;
    }

    public void saveBudget(long totalBudget, long openingBalance) {
        String userId = getCurrentUserId();
        String monthLabel = getCurrentMonthLabel();
        
        BudgetEntity entity = new BudgetEntity(monthLabel, userId, totalBudget, openingBalance);
        db.financeDao().insertBudget(entity);
        
        // Reset warnings since budget changed
        BudgetWarningEntity warning = db.financeDao().getWarningFlag(userId, monthLabel, "total");
        if (warning != null) {
            warning.alerted80 = false;
            warning.alerted100 = false;
            db.financeDao().insertWarningFlag(warning);
        }
    }

    public List<Category> getCategories() {
        String userId = getCurrentUserId();
        List<CategoryEntity> entities = db.financeDao().getVisibleCategories(userId);
        List<Category> list = new ArrayList<>();
        for (CategoryEntity entity : entities) {
            list.add(new Category(entity.id, entity.name, entity.iconName, entity.isDefault, entity.visible));
        }
        return list;
    }

    public Category getCategoryById(String id) {
        String userId = getCurrentUserId();
        CategoryEntity entity = db.financeDao().getCategoryById(userId, id);
        if (entity != null) {
            return new Category(entity.id, entity.name, entity.iconName, entity.isDefault, entity.visible);
        }
        return new Category("other", "Khác", "more_horiz", true, true);
    }

    public List<Category> getAllCategories() {
        String userId = getCurrentUserId();
        List<CategoryEntity> entities = db.financeDao().getAllCategories(userId);
        List<Category> list = new ArrayList<>();
        for (CategoryEntity entity : entities) {
            list.add(new Category(entity.id, entity.name, entity.iconName, entity.isDefault, entity.visible));
        }
        return list;
    }

    public boolean addCategory(String name) {
        String userId = getCurrentUserId();
        String stdName = standardizeProductName(name);
        List<CategoryEntity> all = db.financeDao().getAllCategories(userId);
        for (CategoryEntity cat : all) {
            if (standardizeProductName(cat.name).equals(stdName)) {
                return false; // Duplicate after standardization
            }
        }
        String id = UUID.randomUUID().toString();
        db.financeDao().insertCategory(new CategoryEntity(id, userId, name.trim(), "label", false, true));
        return true;
    }

    public boolean renameCategory(String categoryId, String newName) {
        String userId = getCurrentUserId();
        String stdName = standardizeProductName(newName);
        List<CategoryEntity> all = db.financeDao().getAllCategories(userId);
        for (CategoryEntity cat : all) {
            if (!cat.id.equals(categoryId) && standardizeProductName(cat.name).equals(stdName)) {
                return false; // Duplicate
            }
        }
        CategoryEntity entity = db.financeDao().getCategoryById(userId, categoryId);
        if (entity != null) {
            entity.name = newName.trim();
            db.financeDao().updateCategory(entity);
            return true;
        }
        return false;
    }

    public void hideCategory(String categoryId) {
        String userId = getCurrentUserId();
        CategoryEntity entity = db.financeDao().getCategoryById(userId, categoryId);
        if (entity != null) {
            entity.visible = false;
            db.financeDao().updateCategory(entity);
        }
    }

    public List<Transaction> getTransactions() {
        String userId = getCurrentUserId();
        List<TransactionEntity> entities = db.financeDao().getActiveTransactions(userId);
        List<Transaction> list = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        for (TransactionEntity entity : entities) {
            String timeLabel = sdf.format(new Date(entity.timestamp));
            list.add(new Transaction(entity.id, entity.name, entity.categoryId, entity.amount,
                    entity.type, entity.source, timeLabel));
        }
        return list;
    }

    public List<Transaction> getTransactionsFiltered(String filter) {
        String userId = getCurrentUserId();
        List<TransactionEntity> entities;
        if ("Thu".equals(filter)) {
            entities = db.financeDao().getActiveTransactionsByType(userId, "income");
        } else if ("Chi".equals(filter)) {
            entities = db.financeDao().getActiveTransactionsByType(userId, "expense");
        } else {
            entities = db.financeDao().getActiveTransactions(userId);
        }
        List<Transaction> list = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        for (TransactionEntity entity : entities) {
            String timeLabel = sdf.format(new Date(entity.timestamp));
            list.add(new Transaction(entity.id, entity.name, entity.categoryId, entity.amount,
                    entity.type, entity.source, timeLabel));
        }
        return list;
    }

    public void addTransaction(String name, String categoryId, long amount, String type, String source) {
        String userId = getCurrentUserId();
        String id = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();

        TransactionEntity transaction = new TransactionEntity(id, userId, name, categoryId, amount, type, source, timestamp, "", false);
        db.financeDao().insertTransaction(transaction);
        
        com.sosinhvien.app.data.database.entity.AuditLogEntity audit = new com.sosinhvien.app.data.database.entity.AuditLogEntity(
                UUID.randomUUID().toString(), id, userId, "INSERT", amount, timestamp);
        db.financeDao().insertAuditLog(audit);

        // Check budget warnings
        checkBudgetThresholds(userId, categoryId, amount, type);
    }

    public List<Reminder> getTodayReminders() {
        String userId = getCurrentUserId();
        List<NotificationLogEntity> entities = db.notificationDao().getNotifications(userId);
        List<Reminder> list = new ArrayList<>();
        for (NotificationLogEntity entity : entities) {
            list.add(new Reminder(entity.title, entity.subtitle, entity.accentColor));
        }
        return list;
    }

    public List<CalendarEvent> getTodayEvents() {
        String userId = getCurrentUserId();
        List<CalendarEventEntity> entities = db.timeDao().getEventsInRange(userId, 0, Long.MAX_VALUE);
        List<CalendarEvent> list = new ArrayList<>();
        for (CalendarEventEntity entity : entities) {
            String timeRange = formatTimeRange(entity.startTime, entity.endTime, entity.type);
            boolean completed = "Đã hoàn thành".equals(entity.sessionStatus);
            boolean overdue = false;
            if (CalendarEvent.TYPE_TASK.equals(entity.type) && entity.taskId != null) {
                com.sosinhvien.app.data.database.entity.TaskEntity task = db.timeDao().getTaskById(entity.taskId);
                if (task != null) {
                    overdue = task.isOverdue();
                }
            }
            list.add(new CalendarEvent(entity.id, entity.title, timeRange, entity.type, entity.priority, completed, overdue));
        }
        return list;
    }

    public void importSystemEvents(List<CalendarEventEntity> importedEvents) {
        if (importedEvents == null || importedEvents.isEmpty()) return;
        String userId = getCurrentUserId();
        
        for (CalendarEventEntity event : importedEvents) {
            // Check for duplicates by title and start time
            List<CalendarEventEntity> existing = db.timeDao().getEventsInRange(userId, event.startTime, event.endTime);
            boolean isDuplicate = false;
            for (CalendarEventEntity e : existing) {
                if (e.title != null && e.title.equals(event.title) && e.startTime == event.startTime) {
                    isDuplicate = true;
                    break;
                }
            }
            if (!isDuplicate) {
                db.timeDao().insertEvent(event);
            }
        }
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
        String userId = UUID.randomUUID().toString();
        UserEntity user = new UserEntity(userId, email, hash, displayName);
        db.userDao().insertUser(user);

        // Create default configurations
        UserConfigEntity config = new UserConfigEntity(
                userId, "23:00", "07:00", 15, 30, 90,
                "Đọc sách, Thể thao", "Học tốt học kỳ này", "", "",
                true, "24,3", 15, "21:00", true
        );
        db.userDao().insertConfig(config);

        // Create default categories
        db.financeDao().insertCategory(new CategoryEntity("food", userId, "Ăn uống", "restaurant", true, true));
        db.financeDao().insertCategory(new CategoryEntity("transport", userId, "Di chuyển", "directions_car", true, true));
        db.financeDao().insertCategory(new CategoryEntity("study", userId, "Học tập", "school", true, true));
        db.financeDao().insertCategory(new CategoryEntity("entertainment", userId, "Giải trí", "movie", true, true));
        db.financeDao().insertCategory(new CategoryEntity("other", userId, "Khác", "more_horiz", true, true));

        // Create default budget
        String monthLabel = getCurrentMonthLabel();
        BudgetEntity budget = new BudgetEntity(monthLabel, userId, 6_500_000, 2_000_000);
        db.financeDao().insertBudget(budget);

        // Create default category budgets
        db.financeDao().insertCategoryBudget(new CategoryBudgetEntity(monthLabel, "food", userId, 2_000_000));
        db.financeDao().insertCategoryBudget(new CategoryBudgetEntity(monthLabel, "transport", userId, 500_000));
        db.financeDao().insertCategoryBudget(new CategoryBudgetEntity(monthLabel, "study", userId, 1_000_000));
        db.financeDao().insertCategoryBudget(new CategoryBudgetEntity(monthLabel, "entertainment", userId, 1_000_000));

        return true;
    }

    public void saveUserLocally(String email, String displayName) {
        UserEntity existing = db.userDao().getUserByEmail(email);
        if (existing == null) {
            String userId = UUID.randomUUID().toString();
            UserEntity user = new UserEntity(userId, email, "", displayName);
            db.userDao().insertUser(user);
        }
    }

    private void checkBudgetThresholds(String userId, String categoryId, long amount, String type) {
        if (!"expense".equals(type)) return;
        String monthLabel = getCurrentMonthLabel();
        BudgetEntity budget = db.financeDao().getBudget(userId, monthLabel);
        if (budget == null) return;

        long[] range = getMonthRange(monthLabel);
        long maxTime = Math.min(range[1], System.currentTimeMillis());
        long totalSpent = db.financeDao().sumSpentByMonth(userId, range[0], maxTime);

        checkAndRaiseWarning(userId, "total", monthLabel, totalSpent, budget.totalBudget, "Ngân sách tổng");

        if (categoryId != null) {
            List<CategoryBudgetEntity> cbList = db.financeDao().getCategoryBudgets(userId, monthLabel);
            CategoryBudgetEntity targetCb = null;
            for (CategoryBudgetEntity cb : cbList) {
                if (cb.categoryId.equals(categoryId)) {
                    targetCb = cb;
                    break;
                }
            }
            if (targetCb != null) {
                long catSpent = db.financeDao().sumSpentByCategoryMonth(userId, categoryId, range[0], maxTime);
                CategoryEntity cat = db.financeDao().getCategoryById(userId, categoryId);
                String catName = cat != null ? cat.name : "Danh mục";
                checkAndRaiseWarning(userId, categoryId, monthLabel, catSpent, targetCb.amount, "Ngân sách " + catName);
            }
        }
    }

    private void checkAndRaiseWarning(String userId, String categoryId, String monthLabel, long spent, long limit, String name) {
        if (limit <= 0) return;
        double percent = (double) spent / limit;

        BudgetWarningEntity flag = db.financeDao().getWarningFlag(userId, monthLabel, categoryId);
        if (flag == null) {
            flag = new BudgetWarningEntity(monthLabel, categoryId, userId, false, false);
        }

        boolean updated = false;
        long now = System.currentTimeMillis();

        // Reset flags when ratio drops below threshold (mục 6.4)
        if (percent < 0.8 && flag.alerted80) {
            flag.alerted80 = false;
            updated = true;
        }
        if (percent < 1.0 && flag.alerted100) {
            flag.alerted100 = false;
            updated = true;
        }

        if (percent >= 1.0 && !flag.alerted100) {
            flag.alerted100 = true;
            updated = true;
            NotificationLogEntity notif = new NotificationLogEntity(
                    UUID.randomUUID().toString(),
                    userId,
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
                    userId,
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

    // Re-evaluate all warning flags after transaction edit/delete (mục 6.4)
    public void reEvaluateWarningFlags() {
        String userId = getCurrentUserId();
        String monthLabel = getCurrentMonthLabel();
        BudgetEntity budget = db.financeDao().getBudget(userId, monthLabel);
        if (budget == null) return;

        long[] range = getMonthRange(monthLabel);
        long maxTime = Math.min(range[1], System.currentTimeMillis());
        long totalSpent = db.financeDao().sumSpentByMonth(userId, range[0], maxTime);

        checkAndRaiseWarning(userId, "total", monthLabel, totalSpent, budget.totalBudget, "Ngân sách tổng");

        List<CategoryBudgetEntity> cbList = db.financeDao().getCategoryBudgets(userId, monthLabel);
        for (CategoryBudgetEntity cb : cbList) {
            long catSpent = db.financeDao().sumSpentByCategoryMonth(userId, cb.categoryId, range[0], maxTime);
            CategoryEntity cat = db.financeDao().getCategoryById(userId, cb.categoryId);
            String catName = cat != null ? cat.name : "Danh mục";
            checkAndRaiseWarning(userId, cb.categoryId, monthLabel, catSpent, cb.amount, "Ngân sách " + catName);
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
            demo = db.userDao().getUserByEmail("demo@truong.edu.vn");
            if (demo == null) return; // Should not happen
            String userId = demo.id;

            Calendar cal = Calendar.getInstance();
            int currentYear = cal.get(Calendar.YEAR);
            int currentMonth = cal.get(Calendar.MONTH);
            int currentDay = cal.get(Calendar.DAY_OF_MONTH);

            // Today, 12:30
            cal.set(currentYear, currentMonth, currentDay, 12, 30, 0);
            long t1Time = cal.getTimeInMillis();
            db.financeDao().insertTransaction(new TransactionEntity("t1", userId, "Cơm trưa", "food", 35000, "expense", "Thủ công", t1Time, "", false));

            // Yesterday, 10:00
            cal.set(currentYear, currentMonth, currentDay, 10, 0, 0);
            cal.add(Calendar.DAY_OF_MONTH, -1);
            long t2Time = cal.getTimeInMillis();
            // Reset cal to today
            cal.setTimeInMillis(System.currentTimeMillis());
            db.financeDao().insertTransaction(new TransactionEntity("t2", userId, "Tiền tiêu vặt tháng 10", "other", 2000000, "income", "Thủ công", t2Time, "", false));

            // Today, 08:00
            cal.set(currentYear, currentMonth, currentDay, 8, 0, 0);
            long t3Time = cal.getTimeInMillis();
            db.financeDao().insertTransaction(new TransactionEntity("t3", userId, "Grab", "transport", 20000, "expense", "OCR", t3Time, "", false));

            // Today, 15:20
            cal.set(currentYear, currentMonth, currentDay, 15, 20, 0);
            long t4Time = cal.getTimeInMillis();
            db.financeDao().insertTransaction(new TransactionEntity("t4", userId, "Cà phê", "food", 45000, "expense", "Thủ công", t4Time, "", false));

            // 2 days ago, 17:00
            cal.set(currentYear, currentMonth, currentDay, 17, 0, 0);
            cal.add(Calendar.DAY_OF_MONTH, -2);
            long t5Time = cal.getTimeInMillis();
            // Reset cal to today
            cal.setTimeInMillis(System.currentTimeMillis());
            db.financeDao().insertTransaction(new TransactionEntity("t5", userId, "Part-time tuần 3", "other", 800000, "income", "Trợ lý", t5Time, "", false));

            // Today, 09:15
            cal.set(currentYear, currentMonth, currentDay, 9, 15, 0);
            long t6Time = cal.getTimeInMillis();
            db.financeDao().insertTransaction(new TransactionEntity("t6", userId, "Photocopy", "study", 12000, "expense", "Thủ công", t6Time, "", false));

            // Default task
            cal.set(currentYear, currentMonth, currentDay, 14, 0, 0);
            db.timeDao().insertTask(new TaskEntity("task_java", userId, "Nộp bài tập Java", cal.getTimeInMillis(), "Cao", 120, "Chưa thực hiện", null, false));

            // Default events
            cal.set(currentYear, currentMonth, currentDay, 23, 0, 0);
            long e0Start = cal.getTimeInMillis();
            cal.add(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR_OF_DAY, 7);
            cal.set(Calendar.MINUTE, 0);
            long e0End = cal.getTimeInMillis();
            // Reset cal to today
            cal.setTimeInMillis(System.currentTimeMillis());
            db.timeDao().insertEvent(new CalendarEventEntity("e0", userId, "Giờ ngủ", e0Start, e0End, "sleep", null, null, null, null, false, null, null, null, false));

            cal.set(currentYear, currentMonth, currentDay, 8, 0, 0);
            long e1Start = cal.getTimeInMillis();
            cal.set(currentYear, currentMonth, currentDay, 10, 0, 0);
            long e1End = cal.getTimeInMillis();
            db.timeDao().insertEvent(new CalendarEventEntity("e1", userId, "Học nhóm", e1Start, e1End, "event", null, null, null, null, false, null, null, null, false));

            cal.set(currentYear, currentMonth, currentDay, 14, 0, 0);
            long e2Time = cal.getTimeInMillis();
            db.timeDao().insertEvent(new CalendarEventEntity("e2", userId, "Nộp bài tập Java", e2Time, e2Time, "task", "Cao", "task_java", "Đã lên lịch", null, false, null, null, null, false));

            cal.set(currentYear, currentMonth, currentDay, 14, 0, 0);
            long e3Start = cal.getTimeInMillis();
            cal.set(currentYear, currentMonth, currentDay, 16, 30, 0);
            long e3End = cal.getTimeInMillis();
            db.timeDao().insertEvent(new CalendarEventEntity("e3", userId, "Học Thể chất - Sân B2", e3Start, e3End, "event", null, null, null, null, false, null, null, null, false));

            cal.set(currentYear, currentMonth, currentDay, 19, 0, 0);
            long e4Start = cal.getTimeInMillis();
            cal.set(currentYear, currentMonth, currentDay, 21, 0, 0);
            long e4End = cal.getTimeInMillis();
            db.timeDao().insertEvent(new CalendarEventEntity("e4", userId, "Tập Gym", e4Start, e4End, "event", null, null, null, null, false, null, null, null, false));

            // Default notifications
            db.notificationDao().insertNotification(new NotificationLogEntity("r1", userId, "Hạn nộp học phí", "Còn 2 ngày", "task_deadline", "primary", System.currentTimeMillis() - 3600000, false, ""));
            db.notificationDao().insertNotification(new NotificationLogEntity("r2", userId, "Cảnh báo ngân sách", "Ngân sách Ăn uống sắp hết (Còn 150k)", "budget_80", "warning", System.currentTimeMillis() - 7200000, false, "food"));
            db.notificationDao().insertNotification(new NotificationLogEntity("r3", userId, "Nộp bài tập Java", "Hạn 14:00 hôm nay", "task_deadline", "danger", System.currentTimeMillis() - 10800000, false, "task_java"));
        }
    }

    // --- Dynamic Statistics Models ---
    public static class CategoryStat {
        public final String categoryName;
        public final long spent;
        public final int percent;

        public CategoryStat(String categoryName, long spent, int percent) {
            this.categoryName = categoryName;
            this.spent = spent;
            this.percent = percent;
        }
    }

    public static class ProductStat {
        public final String productName;
        public final int count;
        public final long totalSpent;

        public ProductStat(String productName, int count, long totalSpent) {
            this.productName = productName;
            this.count = count;
            this.totalSpent = totalSpent;
        }
    }

    // --- Category Budgets Management ---
    public List<CategoryBudgetEntity> getCategoryBudgets(String monthLabel) {
        String userId = getCurrentUserId();
        return db.financeDao().getCategoryBudgets(userId, monthLabel);
    }

    public long getTotalAllocatedBudget(String monthLabel) {
        String userId = getCurrentUserId();
        return db.financeDao().getTotalAllocatedBudget(userId, monthLabel);
    }

    public void saveCategoryBudgets(List<CategoryBudgetEntity> list) {
        for (CategoryBudgetEntity cb : list) {
            db.financeDao().insertCategoryBudget(cb);
        }
    }

    public void scaleCategoryBudgetsProportionally(long newTotalBudget) {
        String userId = getCurrentUserId();
        String monthLabel = getCurrentMonthLabel();
        long totalAllocated = db.financeDao().getTotalAllocatedBudget(userId, monthLabel);
        if (totalAllocated <= 0) return;

        double ratio = (double) newTotalBudget / totalAllocated;
        List<CategoryBudgetEntity> allocatedList = db.financeDao().getCategoryBudgets(userId, monthLabel);
        long newAllocatedSum = 0;

        for (int i = 0; i < allocatedList.size(); i++) {
            CategoryBudgetEntity cb = allocatedList.get(i);
            long newAmount = Math.round(cb.amount * ratio);
            newAllocatedSum += newAmount;
            cb.amount = newAmount;
            db.financeDao().insertCategoryBudget(cb);
        }

        // If due to rounding, newAllocatedSum exceeds newTotalBudget, adjust the largest one
        if (newAllocatedSum > newTotalBudget) {
            long diff = newAllocatedSum - newTotalBudget;
            CategoryBudgetEntity maxCb = null;
            for (CategoryBudgetEntity cb : allocatedList) {
                if (maxCb == null || cb.amount > maxCb.amount) {
                    maxCb = cb;
                }
            }
            if (maxCb != null && maxCb.amount >= diff) {
                maxCb.amount -= diff;
                db.financeDao().insertCategoryBudget(maxCb);
            }
        }
    }

    // --- Product Standardization (TT_11) ---
    public static String removeAccents(String src) {
        if (src == null) return "";
        String temp = Normalizer.normalize(src, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("")
                .replaceAll("đ", "d")
                .replaceAll("Đ", "D");
    }

    public static String standardizeProductName(String name) {
        if (name == null) return "";
        String normalized = removeAccents(name);
        normalized = normalized.trim().replaceAll("\\s+", " ").toLowerCase(Locale.getDefault());
        return normalized;
    }

    // --- Category Stats Calculation (SV_QD_11) ---
    public List<CategoryStat> getCategorySpentRatios() {
        String userId = getCurrentUserId();
        String monthLabel = getCurrentMonthLabel();
        long[] range = getMonthRange(monthLabel);
        long maxTime = Math.min(range[1], System.currentTimeMillis());
        
        long totalSpent = db.financeDao().sumSpentByMonth(userId, range[0], maxTime);
        List<CategoryEntity> categories = db.financeDao().getVisibleCategories(userId);
        List<CategoryStat> stats = new ArrayList<>();
        
        if (totalSpent <= 0) {
            for (CategoryEntity cat : categories) {
                stats.add(new CategoryStat(cat.name, 0, 0));
            }
            return stats;
        }

        long calculatedSpent = 0;
        for (CategoryEntity cat : categories) {
            long spent = db.financeDao().sumSpentByCategoryMonth(userId, cat.id, range[0], maxTime);
            if (spent > 0) {
                int percent = (int) Math.round((double) spent * 100.0 / totalSpent);
                stats.add(new CategoryStat(cat.name, spent, percent));
                calculatedSpent += spent;
            }
        }
        
        // Handle unallocated expenses (TT_06)
        long unallocated = totalSpent - calculatedSpent;
        if (unallocated > 0) {
            int percent = (int) Math.round((double) unallocated * 100.0 / totalSpent);
            stats.add(new CategoryStat("Khác / Chưa phân loại", unallocated, percent));
        }

        // Sort by spent descending
        Collections.sort(stats, (o1, o2) -> Long.compare(o2.spent, o1.spent));
        return stats;
    }

    // --- Top Purchased Products Calculation (SV_QD_12, TT_12) ---
    public List<ProductStat> getTopPurchasedProducts() {
        String userId = getCurrentUserId();
        String monthLabel = getCurrentMonthLabel();
        long[] range = getMonthRange(monthLabel);
        long maxTime = Math.min(range[1], System.currentTimeMillis());

        // Get active transactions
        List<TransactionEntity> transactions = db.financeDao().getActiveTransactions(userId);
        
        // Group by standardized name
        Map<String, List<TransactionEntity>> grouped = new HashMap<>();
        Map<String, String> displayNames = new HashMap<>();

        for (TransactionEntity t : transactions) {
            if (!"expense".equals(t.type)) continue;
            if (t.timestamp < range[0] || t.timestamp > maxTime) continue;

            String std = standardizeProductName(t.name);
            if (std.isEmpty()) continue;

            if (!grouped.containsKey(std)) {
                grouped.put(std, new ArrayList<>());
                displayNames.put(std, t.name);
            }
            grouped.get(std).add(t);
        }

        List<ProductStat> stats = new ArrayList<>();
        for (Map.Entry<String, List<TransactionEntity>> entry : grouped.entrySet()) {
            List<TransactionEntity> list = entry.getValue();
            int count = list.size();
            
            // Rule TT_12: Frequency >= 3
            if (count >= 3) {
                long totalSpent = 0;
                for (TransactionEntity t : list) {
                    totalSpent += t.amount;
                }
                String originalName = displayNames.get(entry.getKey());
                stats.add(new ProductStat(originalName, count, totalSpent));
            }
        }

        // Sort by count descending, then by totalSpent descending
        Collections.sort(stats, (o1, o2) -> {
            int comp = Integer.compare(o2.count, o1.count);
            if (comp != 0) return comp;
            return Long.compare(o2.totalSpent, o1.totalSpent);
        });

        return stats;
    }

    public List<ProductStat> getTopSpentProducts() {
        String userId = getCurrentUserId();
        String monthLabel = getCurrentMonthLabel();
        long[] range = getMonthRange(monthLabel);
        long maxTime = Math.min(range[1], System.currentTimeMillis());

        List<TransactionEntity> transactions = db.financeDao().getActiveTransactions(userId);
        Map<String, List<TransactionEntity>> grouped = new HashMap<>();
        Map<String, String> displayNames = new HashMap<>();

        for (TransactionEntity t : transactions) {
            if (!"expense".equals(t.type)) continue;
            if (t.timestamp < range[0] || t.timestamp > maxTime) continue;

            String std = standardizeProductName(t.name);
            if (std.isEmpty()) continue;

            if (!grouped.containsKey(std)) {
                grouped.put(std, new ArrayList<>());
                displayNames.put(std, t.name);
            }
            grouped.get(std).add(t);
        }

        List<ProductStat> stats = new ArrayList<>();
        for (Map.Entry<String, List<TransactionEntity>> entry : grouped.entrySet()) {
            List<TransactionEntity> list = entry.getValue();
            long totalSpent = 0;
            for (TransactionEntity t : list) {
                totalSpent += t.amount;
            }
            String originalName = displayNames.get(entry.getKey());
            stats.add(new ProductStat(originalName, list.size(), totalSpent));
        }

        Collections.sort(stats, (o1, o2) -> Long.compare(o2.totalSpent, o1.totalSpent));
        return stats;
    }
}
