const express = require('express');
const router = express.Router();
const db = require('../db');
const auth = require('../middleware/auth');

// 1. POST /api/sync/pull - Lấy các thay đổi từ server
router.post('/pull', auth, async (req, res) => {
  const email = req.user.email;
  const { lastSyncTime } = req.body;
  const since = lastSyncTime ? parseInt(lastSyncTime) : 0;

  try {
    const serverTime = Date.now();

    // Pull Categories
    const categoriesRes = await db.query(
      'SELECT id, name, icon_name, is_default, visible, updated_at, is_deleted FROM categories WHERE user_email = $1 AND updated_at > $2',
      [email, since]
    );

    // Pull Budgets
    const budgetsRes = await db.query(
      'SELECT month_label, total_budget, opening_balance, updated_at, is_deleted FROM budgets WHERE user_email = $1 AND updated_at > $2',
      [email, since]
    );

    // Pull Category Budgets
    const catBudgetsRes = await db.query(
      'SELECT month_label, category_id, amount, updated_at, is_deleted FROM category_budgets WHERE user_email = $1 AND updated_at > $2',
      [email, since]
    );

    // Pull Transactions
    const transactionsRes = await db.query(
      'SELECT id, name, category_id, amount, type, source, timestamp, time_label, note, updated_at, is_deleted FROM transactions WHERE user_email = $1 AND updated_at > $2',
      [email, since]
    );

    // Pull Tasks
    const tasksRes = await db.query(
      'SELECT id, title, deadline, priority, duration_minutes, status, actual_completed_time, updated_at, is_deleted FROM tasks WHERE user_email = $1 AND updated_at > $2',
      [email, since]
    );

    // Pull Calendar Events
    const eventsRes = await db.query(
      'SELECT id, title, start_time, end_time, type, priority, task_id, session_status, recurrence_rule, is_recurrence_exception, recurrence_exception_date, original_event_id, category_id, updated_at, is_deleted FROM calendar_events WHERE user_email = $1 AND updated_at > $2',
      [email, since]
    );

    // Pull User Config
    const configRes = await db.query(
      'SELECT sleep_start_time, sleep_end_time, buffer_time, min_interval_duration, concentration_threshold, focus_topics, improvement_goals, last_sync_time, ai_scheduling_prompt, budget_alert_enabled, deadline_reminder_hours, event_reminder_minutes, record_reminder_time, budget_reminder_enabled, updated_at FROM user_configs WHERE user_email = $1 AND updated_at > $2',
      [email, since]
    );

    res.json({
      serverTime,
      categories: categoriesRes.rows,
      budgets: budgetsRes.rows,
      categoryBudgets: catBudgetsRes.rows,
      transactions: transactionsRes.rows,
      tasks: tasksRes.rows,
      calendarEvents: eventsRes.rows,
      userConfig: configRes.rows.length > 0 ? configRes.rows[0] : null
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({ message: 'Lỗi server khi đồng bộ dữ liệu (pull).' });
  }
});

// 2. POST /api/sync/push - Đồng bộ dữ liệu cục bộ lên server
router.post('/push', auth, async (req, res) => {
  const email = req.user.email;
  const { categories, budgets, categoryBudgets, transactions, tasks, calendarEvents, userConfig } = req.body;
  const serverTime = Date.now();

  try {
    // Start transactional processing
    await db.query('BEGIN');

    // 1. Sync Categories
    if (categories && Array.isArray(categories)) {
      for (const cat of categories) {
        await db.query(
          `INSERT INTO categories (id, user_email, name, icon_name, is_default, visible, updated_at, is_deleted) 
           VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
           ON CONFLICT (id, user_email) 
           DO UPDATE SET name = EXCLUDED.name, icon_name = EXCLUDED.icon_name, is_default = EXCLUDED.is_default, 
                         visible = EXCLUDED.visible, updated_at = EXCLUDED.updated_at, is_deleted = EXCLUDED.is_deleted
           WHERE EXCLUDED.updated_at >= categories.updated_at`,
          [cat.id, email, cat.name, cat.icon_name, cat.is_default, cat.visible, cat.updated_at, cat.is_deleted]
        );
      }
    }

    // 2. Sync Budgets
    if (budgets && Array.isArray(budgets)) {
      for (const b of budgets) {
        await db.query(
          `INSERT INTO budgets (month_label, user_email, total_budget, opening_balance, updated_at, is_deleted) 
           VALUES ($1, $2, $3, $4, $5, $6)
           ON CONFLICT (month_label, user_email) 
           DO UPDATE SET total_budget = EXCLUDED.total_budget, opening_balance = EXCLUDED.opening_balance, 
                         updated_at = EXCLUDED.updated_at, is_deleted = EXCLUDED.is_deleted
           WHERE EXCLUDED.updated_at >= budgets.updated_at`,
          [b.month_label, email, b.total_budget, b.opening_balance, b.updated_at, b.is_deleted]
        );
      }
    }

    // 3. Sync Category Budgets
    if (categoryBudgets && Array.isArray(categoryBudgets)) {
      for (const cb of categoryBudgets) {
        await db.query(
          `INSERT INTO category_budgets (month_label, category_id, user_email, amount, updated_at, is_deleted) 
           VALUES ($1, $2, $3, $4, $5, $6)
           ON CONFLICT (month_label, category_id, user_email) 
           DO UPDATE SET amount = EXCLUDED.amount, updated_at = EXCLUDED.updated_at, is_deleted = EXCLUDED.is_deleted
           WHERE EXCLUDED.updated_at >= category_budgets.updated_at`,
          [cb.month_label, cb.category_id, email, cb.amount, cb.updated_at, cb.is_deleted]
        );
      }
    }

    // 4. Sync Transactions
    if (transactions && Array.isArray(transactions)) {
      for (const t of transactions) {
        await db.query(
          `INSERT INTO transactions (id, user_email, name, category_id, amount, type, source, timestamp, time_label, note, updated_at, is_deleted) 
           VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12)
           ON CONFLICT (id, user_email) 
           DO UPDATE SET name = EXCLUDED.name, category_id = EXCLUDED.category_id, amount = EXCLUDED.amount, 
                         type = EXCLUDED.type, source = EXCLUDED.source, timestamp = EXCLUDED.timestamp, 
                         time_label = EXCLUDED.time_label, note = EXCLUDED.note, updated_at = EXCLUDED.updated_at, 
                         is_deleted = EXCLUDED.is_deleted
           WHERE EXCLUDED.updated_at >= transactions.updated_at`,
          [t.id, email, t.name, t.category_id, t.amount, t.type, t.source, t.timestamp, t.time_label, t.note, t.updated_at, t.is_deleted]
        );
      }
    }

    // 5. Sync Tasks
    if (tasks && Array.isArray(tasks)) {
      for (const tk of tasks) {
        await db.query(
          `INSERT INTO tasks (id, user_email, title, deadline, priority, duration_minutes, status, actual_completed_time, updated_at, is_deleted) 
           VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)
           ON CONFLICT (id, user_email) 
           DO UPDATE SET title = EXCLUDED.title, deadline = EXCLUDED.deadline, priority = EXCLUDED.priority, 
                         duration_minutes = EXCLUDED.duration_minutes, status = EXCLUDED.status, 
                         actual_completed_time = EXCLUDED.actual_completed_time, updated_at = EXCLUDED.updated_at, 
                         is_deleted = EXCLUDED.is_deleted
           WHERE EXCLUDED.updated_at >= tasks.updated_at`,
          [tk.id, email, tk.title, tk.deadline, tk.priority, tk.duration_minutes, tk.status, tk.actual_completed_time, tk.updated_at, tk.is_deleted]
        );
      }
    }

    // 6. Sync Calendar Events
    if (calendarEvents && Array.isArray(calendarEvents)) {
      for (const e of calendarEvents) {
        await db.query(
          `INSERT INTO calendar_events (id, user_email, title, start_time, end_time, type, priority, task_id, session_status, recurrence_rule, is_recurrence_exception, recurrence_exception_date, original_event_id, category_id, updated_at, is_deleted) 
           VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $15, $16)
           ON CONFLICT (id, user_email) 
           DO UPDATE SET title = EXCLUDED.title, start_time = EXCLUDED.start_time, end_time = EXCLUDED.end_time, 
                         type = EXCLUDED.type, priority = EXCLUDED.priority, task_id = EXCLUDED.task_id, 
                         session_status = EXCLUDED.session_status, recurrence_rule = EXCLUDED.recurrence_rule, 
                         is_recurrence_exception = EXCLUDED.is_recurrence_exception, 
                         recurrence_exception_date = EXCLUDED.recurrence_exception_date, 
                         original_event_id = EXCLUDED.original_event_id, category_id = EXCLUDED.category_id, 
                         updated_at = EXCLUDED.updated_at, is_deleted = EXCLUDED.is_deleted
           WHERE EXCLUDED.updated_at >= calendar_events.updated_at`,
          [e.id, email, e.title, e.start_time, e.end_time, e.type, e.priority, e.task_id, e.session_status, e.recurrence_rule, e.is_recurrence_exception, e.recurrence_exception_date, e.original_event_id, e.category_id, e.updated_at, e.is_deleted]
        );
      }
    }

    // 7. Sync User Config
    if (userConfig) {
      await db.query(
        `INSERT INTO user_configs (
          user_email, sleep_start_time, sleep_end_time, buffer_time, min_interval_duration, 
          concentration_threshold, focus_topics, improvement_goals, last_sync_time, 
          ai_scheduling_prompt, budget_alert_enabled, deadline_reminder_hours, 
          event_reminder_minutes, record_reminder_time, budget_reminder_enabled, updated_at
        ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $15, $16)
        ON CONFLICT (user_email) 
        DO UPDATE SET sleep_start_time = EXCLUDED.sleep_start_time, sleep_end_time = EXCLUDED.sleep_end_time, 
                      buffer_time = EXCLUDED.buffer_time, min_interval_duration = EXCLUDED.min_interval_duration, 
                      concentration_threshold = EXCLUDED.concentration_threshold, focus_topics = EXCLUDED.focus_topics, 
                      improvement_goals = EXCLUDED.improvement_goals, last_sync_time = EXCLUDED.last_sync_time, 
                      ai_scheduling_prompt = EXCLUDED.ai_scheduling_prompt, budget_alert_enabled = EXCLUDED.budget_alert_enabled, 
                      deadline_reminder_hours = EXCLUDED.deadline_reminder_hours, event_reminder_minutes = EXCLUDED.event_reminder_minutes, 
                      record_reminder_time = EXCLUDED.record_reminder_time, budget_reminder_enabled = EXCLUDED.budget_reminder_enabled, 
                      updated_at = EXCLUDED.updated_at
        WHERE EXCLUDED.updated_at >= user_configs.updated_at`,
        [
          email, userConfig.sleep_start_time, userConfig.sleep_end_time, userConfig.buffer_time, userConfig.min_interval_duration,
          userConfig.concentration_threshold, userConfig.focus_topics, userConfig.improvement_goals, serverTime,
          userConfig.ai_scheduling_prompt, userConfig.budget_alert_enabled, userConfig.deadline_reminder_hours,
          userConfig.event_reminder_minutes, userConfig.record_reminder_time, userConfig.budget_reminder_enabled, userConfig.updated_at
        ]
      );
    }

    // Update main config sync time
    await db.query('UPDATE user_configs SET last_sync_time = $1 WHERE user_email = $2', [serverTime, email]);

    await db.query('COMMIT');
    res.json({ success: true, serverTime });
  } catch (err) {
    await db.query('ROLLBACK');
    console.error(err);
    res.status(500).json({ message: 'Lỗi server khi đồng bộ dữ liệu (push).' });
  }
});

module.exports = router;
