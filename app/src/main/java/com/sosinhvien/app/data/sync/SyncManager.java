package com.sosinhvien.app.data.sync;

import android.content.Context;
import android.util.Log;

import com.sosinhvien.app.data.database.AppDatabase;
import com.sosinhvien.app.data.database.dao.SyncDao;
import com.sosinhvien.app.data.database.entity.BudgetEntity;
import com.sosinhvien.app.data.database.entity.CalendarEventEntity;
import com.sosinhvien.app.data.database.entity.CategoryBudgetEntity;
import com.sosinhvien.app.data.database.entity.CategoryEntity;
import com.sosinhvien.app.data.database.entity.TaskEntity;
import com.sosinhvien.app.data.database.entity.TransactionEntity;
import com.sosinhvien.app.data.database.entity.UserConfigEntity;
import com.sosinhvien.app.data.network.ApiClient;
import com.sosinhvien.app.data.network.ApiService;
import com.sosinhvien.app.data.network.model.SyncModels;
import com.sosinhvien.app.util.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;

public class SyncManager {
    private static final String TAG = "SyncManager";
    private static SyncManager instance;
    private final Context context;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private SyncManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized SyncManager getInstance(Context context) {
        if (instance == null) {
            instance = new SyncManager(context);
        }
        return instance;
    }

    public interface SyncCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public void triggerSync(SyncCallback callback) {
        executor.execute(() -> {
            try {
                SessionManager session = new SessionManager(context);
                if (!session.isLoggedIn()) {
                    if (callback != null) callback.onFailure("Người dùng chưa đăng nhập.");
                    return;
                }

                String userId = session.getUserId();
                AppDatabase db = AppDatabase.getInstance(context);
                SyncDao syncDao = db.syncDao();
                ApiService api = ApiClient.getApiService(context);

                // --- 1. PUSH LOCAL CHANGES TO SERVER ---
                SyncModels.PushRequest pushReq = new SyncModels.PushRequest();
                pushReq.categories = syncDao.getUnsyncedCategories(userId);
                pushReq.budgets = syncDao.getUnsyncedBudgets(userId);
                pushReq.categoryBudgets = syncDao.getUnsyncedCategoryBudgets(userId);
                pushReq.transactions = syncDao.getUnsyncedTransactions(userId);
                pushReq.tasks = syncDao.getUnsyncedTasks(userId);
                pushReq.calendarEvents = syncDao.getUnsyncedCalendarEvents(userId);
                pushReq.userConfig = syncDao.getUnsyncedUserConfig(userId);

                boolean hasDataToPush = !pushReq.categories.isEmpty() ||
                        !pushReq.budgets.isEmpty() ||
                        !pushReq.categoryBudgets.isEmpty() ||
                        !pushReq.transactions.isEmpty() ||
                        !pushReq.tasks.isEmpty() ||
                        !pushReq.calendarEvents.isEmpty() ||
                        pushReq.userConfig != null;

                if (hasDataToPush) {
                    Log.d(TAG, "Đang push dữ liệu lên server...");
                    Response<SyncModels.PushResponse> pushRes = api.push(pushReq).execute();
                    if (pushRes.isSuccessful() && pushRes.body() != null && pushRes.body().success) {
                        // Mark local items as synced
                        if (!pushReq.categories.isEmpty()) {
                            List<String> ids = new ArrayList<>();
                            for (CategoryEntity c : pushReq.categories) ids.add(c.id);
                            syncDao.markCategoriesSynced(userId, ids);
                        }
                        if (!pushReq.budgets.isEmpty()) {
                            List<String> labels = new ArrayList<>();
                            for (BudgetEntity b : pushReq.budgets) labels.add(b.monthLabel);
                            syncDao.markBudgetsSynced(userId, labels);
                        }
                        if (!pushReq.transactions.isEmpty()) {
                            List<String> ids = new ArrayList<>();
                            for (TransactionEntity t : pushReq.transactions) ids.add(t.id);
                            syncDao.markTransactionsSynced(userId, ids);
                        }
                        if (!pushReq.tasks.isEmpty()) {
                            List<String> ids = new ArrayList<>();
                            for (TaskEntity t : pushReq.tasks) ids.add(t.id);
                            syncDao.markTasksSynced(userId, ids);
                        }
                        if (!pushReq.calendarEvents.isEmpty()) {
                            List<String> ids = new ArrayList<>();
                            for (CalendarEventEntity e : pushReq.calendarEvents) ids.add(e.id);
                            syncDao.markCalendarEventsSynced(userId, ids);
                        }
                        if (pushReq.userConfig != null) {
                            syncDao.markUserConfigSynced(userId);
                        }
                        Log.d(TAG, "Push hoàn thành.");
                    } else {
                        if (callback != null) callback.onFailure("Lỗi push dữ liệu: " + pushRes.message());
                        return;
                    }
                }

                // --- 2. PULL SERVER CHANGES ---
                long lastSync = session.getLastSyncTime();
                Log.d(TAG, "Đang pull dữ liệu mới từ: " + lastSync);
                Response<SyncModels.PullResponse> pullRes = api.pull(new SyncModels.PullRequest(lastSync)).execute();
                if (pullRes.isSuccessful() && pullRes.body() != null) {
                    SyncModels.PullResponse body = pullRes.body();

                    // Merge pulled items into Room
                    if (body.categories != null && !body.categories.isEmpty()) {
                        for (CategoryEntity c : body.categories) c.isSynced = 1;
                        syncDao.insertCategories(body.categories);
                    }
                    if (body.budgets != null && !body.budgets.isEmpty()) {
                        for (BudgetEntity b : body.budgets) b.isSynced = 1;
                        syncDao.insertBudgets(body.budgets);
                    }
                    if (body.categoryBudgets != null && !body.categoryBudgets.isEmpty()) {
                        for (CategoryBudgetEntity cb : body.categoryBudgets) cb.isSynced = 1;
                        syncDao.insertCategoryBudgets(body.categoryBudgets);
                    }
                    if (body.transactions != null && !body.transactions.isEmpty()) {
                        for (TransactionEntity t : body.transactions) {
                            t.isSynced = 1;
                            t.deleted = t.isDeleted == 1;
                        }
                        syncDao.insertTransactions(body.transactions);
                    }
                    if (body.tasks != null && !body.tasks.isEmpty()) {
                        for (TaskEntity tk : body.tasks) {
                            tk.isSynced = 1;
                            tk.deleted = tk.isDeleted == 1;
                        }
                        syncDao.insertTasks(body.tasks);
                    }
                    if (body.calendarEvents != null && !body.calendarEvents.isEmpty()) {
                        for (CalendarEventEntity e : body.calendarEvents) {
                            e.isSynced = 1;
                            e.deleted = e.isDeleted == 1;
                        }
                        syncDao.insertCalendarEvents(body.calendarEvents);
                    }
                    if (body.userConfig != null) {
                        body.userConfig.isSynced = 1;
                        syncDao.insertUserConfig(body.userConfig);
                    }

                    // Save new last sync timestamp
                    session.saveLastSyncTime(body.serverTime);
                    Log.d(TAG, "Pull hoàn thành. Thời gian server mới: " + body.serverTime);

                    if (callback != null) callback.onSuccess();
                } else {
                    if (callback != null) callback.onFailure("Lỗi pull dữ liệu: " + pullRes.message());
                }

            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi đồng bộ: ", e);
                if (callback != null) callback.onFailure(e.getMessage());
            }
        });
    }
}
