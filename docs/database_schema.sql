-- ============================================================================
-- SQLITE DATABASE SCHEMA - STUDENT MANAGEMENT APP (QUẢN LÝ SINH VIÊN)
-- ============================================================================

-- Enable Foreign Key support in SQLite (Must be executed on database connection open)
PRAGMA foreign_keys = ON;

-- 1. Table: users (Tài khoản người dùng)
-- Mapped to SV_QD_38, SV_QD_39
CREATE TABLE IF NOT EXISTS users (
    email VARCHAR(255) PRIMARY KEY,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    failed_login_attempts INTEGER DEFAULT 0,
    locked_until INTEGER DEFAULT 0 -- timestamp in milliseconds when login lock expires
);

-- 2. Table: categories (Danh mục chi tiêu)
-- Mapped to SV_QD_37, DB_02
CREATE TABLE IF NOT EXISTS categories (
    id VARCHAR(50) NOT NULL,
    user_email VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    icon_name VARCHAR(50) NOT NULL,
    is_default INTEGER DEFAULT 0, -- 0 for false, 1 for true
    visible INTEGER DEFAULT 1,     -- 0 for hidden (soft deleted if transactions exist), 1 for visible
    PRIMARY KEY (id, user_email),
    FOREIGN KEY (user_email) REFERENCES users(email) ON DELETE CASCADE
);

-- 3. Table: budgets (Ngân sách tháng)
-- Mapped to SV_QD_01, SV_QD_03, SV_QD_04, DB_01
CREATE TABLE IF NOT EXISTS budgets (
    month_label VARCHAR(10) NOT NULL, -- Format: YYYY-MM
    user_email VARCHAR(255) NOT NULL,
    total_budget INTEGER NOT NULL CHECK (total_budget > 0),
    opening_balance INTEGER NOT NULL DEFAULT 0 CHECK (opening_balance >= 0),
    PRIMARY KEY (month_label, user_email),
    FOREIGN KEY (user_email) REFERENCES users(email) ON DELETE CASCADE
);

-- 4. Table: category_budgets (Phân bổ ngân sách theo danh mục)
-- Mapped to SV_QD_02, DB_01
CREATE TABLE IF NOT EXISTS category_budgets (
    month_label VARCHAR(10) NOT NULL,
    category_id VARCHAR(50) NOT NULL,
    user_email VARCHAR(255) NOT NULL,
    amount INTEGER NOT NULL CHECK (amount >= 0),
    PRIMARY KEY (month_label, category_id, user_email),
    FOREIGN KEY (month_label, user_email) REFERENCES budgets(month_label, user_email) ON DELETE CASCADE,
    FOREIGN KEY (category_id, user_email) REFERENCES categories(id, user_email) ON DELETE CASCADE
);

-- 5. Table: transactions (Khoản thu / chi)
-- Mapped to SV_QD_05, SV_QD_06, SV_QD_07, SV_QD_08, DB_06, DB_08
CREATE TABLE IF NOT EXISTS transactions (
    id VARCHAR(50) PRIMARY KEY,
    user_email VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    category_id VARCHAR(50),
    amount INTEGER NOT NULL CHECK (amount > 0 AND amount <= 999999999),
    type VARCHAR(10) NOT NULL CHECK (type IN ('income', 'expense')),
    source VARCHAR(20) NOT NULL CHECK (source IN ('Thủ công', 'OCR', 'Trợ lý')),
    timestamp INTEGER NOT NULL,      -- UTC timestamp in milliseconds
    time_label VARCHAR(50) NOT NULL,  -- Local formatted time/date label
    notes TEXT,
    deleted INTEGER DEFAULT 0,        -- Soft-deleted flag: 0 = false, 1 = true
    FOREIGN KEY (user_email) REFERENCES users(email) ON DELETE CASCADE,
    FOREIGN KEY (category_id, user_email) REFERENCES categories(id, user_email) ON DELETE SET NULL
);

-- 6. Table: tasks (Công việc)
-- Mapped to SV_QD_20, SV_QD_21, SV_QD_22, SV_QD_23, SV_QD_24, DB_03
CREATE TABLE IF NOT EXISTS tasks (
    id VARCHAR(50) PRIMARY KEY,
    user_email VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    deadline INTEGER NOT NULL, -- deadline timestamp in milliseconds
    priority VARCHAR(10) NOT NULL CHECK (priority IN ('Thấp', 'Trung bình', 'Cao')),
    estimated_duration INTEGER NOT NULL CHECK (estimated_duration > 0), -- in minutes
    status VARCHAR(20) NOT NULL DEFAULT 'Chưa thực hiện' CHECK (status IN ('Chưa thực hiện', 'Đang thực hiện', 'Đã hoàn thành')),
    completed_time INTEGER, -- timestamp in milliseconds when status changed to Completed
    deleted INTEGER DEFAULT 0, -- Soft-deleted flag: 0 = false, 1 = true
    FOREIGN KEY (user_email) REFERENCES users(email) ON DELETE CASCADE
);

-- 7. Table: calendar_events (Sự kiện lịch và Phiên công việc)
-- Mapped to SV_QD_15, SV_QD_16, SV_QD_17, SV_QD_18, SV_QD_19, SV_QD_36, DB_04, DB_05
CREATE TABLE IF NOT EXISTS calendar_events (
    id VARCHAR(50) PRIMARY KEY,
    user_email VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    start_time INTEGER NOT NULL, -- timestamp in milliseconds
    end_time INTEGER NOT NULL CHECK (end_time > start_time), -- timestamp in milliseconds
    type VARCHAR(10) NOT NULL CHECK (type IN ('event', 'task', 'sleep')),
    priority VARCHAR(10) CHECK (priority IN ('Thấp', 'Trung bình', 'Cao')), -- only for task type sessions
    task_id VARCHAR(50), -- nullable, links event to a task to form a Session
    session_status VARCHAR(20) CHECK (session_status IN ('Đã lên lịch', 'Đã hoàn thành', 'Đã bỏ')), -- only if task_id not null
    session_actual_duration INTEGER, -- actual minutes spent (optional)
    is_recurring INTEGER DEFAULT 0, -- 0 = false, 1 = true
    recurrence_rule VARCHAR(100),   -- e.g., 'weekly'
    recurrence_end_date INTEGER,    -- timestamp in milliseconds
    parent_event_id VARCHAR(50),    -- links exception to parent event series
    deleted INTEGER DEFAULT 0,      -- Soft-deleted flag: 0 = false, 1 = true
    FOREIGN KEY (user_email) REFERENCES users(email) ON DELETE CASCADE,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE SET NULL,
    FOREIGN KEY (parent_event_id) REFERENCES calendar_events(id) ON DELETE CASCADE
);

-- 8. Table: user_configs (Cấu hình sở thích, thời gian cá nhân và nhắc nhở)
-- Mapped to SV_QD_34, SV_QD_43, SV_BM_14, SV_BM_22
CREATE TABLE IF NOT EXISTS user_configs (
    user_email VARCHAR(255) PRIMARY KEY,
    -- Personal Time Configurations
    sleep_start_time VARCHAR(5) DEFAULT '23:00', -- Format: HH:MM
    sleep_end_time VARCHAR(5) DEFAULT '07:00',   -- Format: HH:MM
    buffer_time INTEGER DEFAULT 15,              -- buffer minutes, default 15
    min_interval_duration INTEGER DEFAULT 30,    -- min interval minutes, default 30
    concentration_threshold INTEGER DEFAULT 90,  -- focus threshold minutes, default 90
    activity_interests TEXT,                     -- JSON/text array of interests
    personal_goals TEXT,                         -- user-entered text goals
    unavailable_hours TEXT,                      -- JSON representation of unavailable slots
    preferred_hours TEXT,                        -- JSON representation of preferred slots
    -- Reminder Configurations
    budget_alert_enabled INTEGER DEFAULT 1,      -- 0 = disabled, 1 = enabled
    deadline_reminder_hours VARCHAR(50) DEFAULT '24,3', -- comma-separated values (e.g. "24,3" means 24h and 3h before)
    event_reminder_minutes INTEGER DEFAULT 15,   -- minutes before event, default 15
    record_reminder_time VARCHAR(5) DEFAULT '21:00', -- Format: HH:MM
    budget_reminder_enabled INTEGER DEFAULT 1,   -- 0 = disabled, 1 = enabled
    FOREIGN KEY (user_email) REFERENCES users(email) ON DELETE CASCADE
);

-- 9. Table: notification_logs (Lịch sử nhắc nhở trong ứng dụng)
-- Conforming to Android 13 permissions fallback (NT_01 to NT_06)
CREATE TABLE IF NOT EXISTS notification_logs (
    id VARCHAR(50) PRIMARY KEY,
    user_email VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    subtitle VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('budget_80', 'budget_100', 'task_deadline', 'event_upcoming', 'daily_record', 'monthly_budget')),
    accent_color VARCHAR(10) NOT NULL CHECK (accent_color IN ('primary', 'warning', 'danger')),
    timestamp INTEGER NOT NULL, -- timestamp in milliseconds when generated
    is_read INTEGER DEFAULT 0,  -- 0 = unread, 1 = read
    entity_id VARCHAR(50),      -- ID of the related object (transaction, event, task, budget)
    FOREIGN KEY (user_email) REFERENCES users(email) ON DELETE CASCADE
);

-- 10. Table: budget_warning_flags (Cờ đã gửi cảnh báo ngân sách)
-- Mapped to 6.4 (Prevent duplicate alerts; resets on month change or when transaction changes)
CREATE TABLE IF NOT EXISTS budget_warning_flags (
    month_label VARCHAR(10) NOT NULL,
    category_id VARCHAR(50) NOT NULL, -- Use 'total' for total budget warning
    user_email VARCHAR(255) NOT NULL,
    alerted_80 INTEGER DEFAULT 0,
    alerted_100 INTEGER DEFAULT 0,
    PRIMARY KEY (month_label, category_id, user_email),
    FOREIGN KEY (user_email) REFERENCES users(email) ON DELETE CASCADE
);

-- ============================================================================
-- INDEXES FOR PERFORMANCE OPTIMIZATION
-- ============================================================================

-- Index on Transactions for quick chronological queries and filters
CREATE INDEX IF NOT EXISTS idx_transactions_user_time ON transactions (user_email, timestamp, deleted);
CREATE INDEX IF NOT EXISTS idx_transactions_category ON transactions (user_email, category_id, deleted);

-- Index on Calendar Events for quick timeline loading
CREATE INDEX IF NOT EXISTS idx_calendar_events_user_time ON calendar_events (user_email, start_time, end_time, deleted);

-- Index on Tasks for deadline checking and ordering
CREATE INDEX IF NOT EXISTS idx_tasks_user_deadline ON tasks (user_email, deadline, status, deleted);
