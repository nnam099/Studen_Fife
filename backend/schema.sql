-- PostgreSQL Schema for Student Management Backend Database

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    email VARCHAR(255) PRIMARY KEY,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    failed_login_attempts INTEGER DEFAULT 0,
    locked_until BIGINT DEFAULT 0
);

-- 2. Categories Table
CREATE TABLE IF NOT EXISTS categories (
    id VARCHAR(50) NOT NULL,
    user_email VARCHAR(255) NOT NULL REFERENCES users(email) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    icon_name VARCHAR(50) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    visible BOOLEAN DEFAULT TRUE,
    updated_at BIGINT NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (id, user_email)
);

-- 3. Budgets Table
CREATE TABLE IF NOT EXISTS budgets (
    month_label VARCHAR(10) NOT NULL,
    user_email VARCHAR(255) NOT NULL REFERENCES users(email) ON DELETE CASCADE,
    total_budget BIGINT NOT NULL CHECK (total_budget > 0),
    opening_balance BIGINT NOT NULL DEFAULT 0 CHECK (opening_balance >= 0),
    updated_at BIGINT NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (month_label, user_email)
);

-- 4. Category Budgets Table
CREATE TABLE IF NOT EXISTS category_budgets (
    month_label VARCHAR(10) NOT NULL,
    category_id VARCHAR(50) NOT NULL,
    user_email VARCHAR(255) NOT NULL,
    amount BIGINT NOT NULL CHECK (amount >= 0),
    updated_at BIGINT NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (month_label, category_id, user_email),
    FOREIGN KEY (month_label, user_email) REFERENCES budgets(month_label, user_email) ON DELETE CASCADE
);

-- 5. Transactions Table
CREATE TABLE IF NOT EXISTS transactions (
    id VARCHAR(50) NOT NULL,
    user_email VARCHAR(255) NOT NULL REFERENCES users(email) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    category_id VARCHAR(50),
    amount BIGINT NOT NULL CHECK (amount >= 0),
    type VARCHAR(10) NOT NULL,
    source VARCHAR(50) NOT NULL,
    timestamp BIGINT NOT NULL,
    time_label VARCHAR(50),
    note VARCHAR(255),
    updated_at BIGINT NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (id, user_email)
);

-- 6. Tasks Table
CREATE TABLE IF NOT EXISTS tasks (
    id VARCHAR(50) NOT NULL,
    user_email VARCHAR(255) NOT NULL REFERENCES users(email) ON DELETE CASCADE,
    title VARCHAR(100) NOT NULL,
    deadline BIGINT NOT NULL,
    priority VARCHAR(20),
    duration_minutes INTEGER CHECK (duration_minutes >= 0),
    status VARCHAR(20),
    actual_completed_time BIGINT,
    updated_at BIGINT NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (id, user_email)
);

-- 7. Calendar Events Table
CREATE TABLE IF NOT EXISTS calendar_events (
    id VARCHAR(50) NOT NULL,
    user_email VARCHAR(255) NOT NULL REFERENCES users(email) ON DELETE CASCADE,
    title VARCHAR(100) NOT NULL,
    start_time BIGINT NOT NULL,
    end_time BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    priority VARCHAR(20),
    task_id VARCHAR(50),
    session_status VARCHAR(20),
    recurrence_rule VARCHAR(100),
    is_recurrence_exception BOOLEAN DEFAULT FALSE,
    recurrence_exception_date BIGINT,
    original_event_id VARCHAR(50),
    category_id VARCHAR(50),
    updated_at BIGINT NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (id, user_email)
);

-- 8. User Configurations Table
CREATE TABLE IF NOT EXISTS user_configs (
    user_email VARCHAR(255) PRIMARY KEY REFERENCES users(email) ON DELETE CASCADE,
    sleep_start_time VARCHAR(5) NOT NULL,
    sleep_end_time VARCHAR(5) NOT NULL,
    buffer_time INTEGER DEFAULT 15,
    min_interval_duration INTEGER DEFAULT 30,
    concentration_threshold INTEGER DEFAULT 90,
    focus_topics VARCHAR(255),
    improvement_goals VARCHAR(255),
    last_sync_time BIGINT,
    ai_scheduling_prompt VARCHAR(500),
    budget_alert_enabled BOOLEAN DEFAULT TRUE,
    deadline_reminder_hours VARCHAR(50) DEFAULT '24,3',
    event_reminder_minutes INTEGER DEFAULT 15,
    record_reminder_time VARCHAR(5) DEFAULT '21:00',
    budget_reminder_enabled BOOLEAN DEFAULT TRUE,
    updated_at BIGINT NOT NULL
);
