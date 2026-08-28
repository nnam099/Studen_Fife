const express = require('express');
const router = express.Router();
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const db = require('../db');

// Helper to sign JWT
const generateToken = (email) => {
  return jwt.sign({ email }, process.env.JWT_SECRET || 'secret', {
    expiresIn: '30d',
  });
};

// 1. POST /api/auth/register - Đăng ký người dùng
router.post('/register', async (req, res) => {
  const { email, password, displayName } = req.body;

  if (!email || !password || !displayName) {
    return res.status(400).json({ message: 'Vui lòng điền đầy đủ thông tin.' });
  }
  if (password.length < 8) {
    return res.status(400).json({ message: 'Mật khẩu phải chứa ít nhất 8 ký tự.' });
  }

  try {
    // Check if user exists
    const userCheck = await db.query('SELECT * FROM users WHERE email = $1', [email]);
    if (userCheck.rows.length > 0) {
      return res.status(400).json({ message: 'Email đã tồn tại trên hệ thống.' });
    }

    // Hash password
    const salt = await bcrypt.genSalt(10);
    const hash = await bcrypt.hash(password, salt);

    // Insert user
    await db.query(
      'INSERT INTO users (email, password_hash, display_name) VALUES ($1, $2, $3)',
      [email, hash, displayName]
    );

    const now = Date.now();

    // Create default configuration
    await db.query(
      `INSERT INTO user_configs (
        user_email, sleep_start_time, sleep_end_time, buffer_time, min_interval_duration, 
        concentration_threshold, focus_topics, improvement_goals, last_sync_time, 
        ai_scheduling_prompt, budget_alert_enabled, deadline_reminder_hours, 
        event_reminder_minutes, record_reminder_time, budget_reminder_enabled, updated_at
      ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $15, $16)`,
      [
        email, '23:00', '07:00', 15, 30, 90,
        'Đọc sách, Thể thao', 'Học tốt học kỳ này', now,
        '', true, '24,3', 15, '21:00', true, now
      ]
    );

    // Create default categories
    const categories = [
      { id: 'food', name: 'Ăn uống', icon: 'restaurant' },
      { id: 'transport', name: 'Di chuyển', icon: 'directions_car' },
      { id: 'study', name: 'Học tập', icon: 'school' },
      { id: 'entertainment', name: 'Giải trí', icon: 'movie' },
      { id: 'other', name: 'Khác', icon: 'more_horiz' }
    ];

    for (const cat of categories) {
      await db.query(
        'INSERT INTO categories (id, user_email, name, icon_name, is_default, visible, updated_at) VALUES ($1, $2, $3, $4, TRUE, TRUE, $5)',
        [cat.id, email, cat.name, cat.icon, now]
      );
    }

    const token = generateToken(email);
    res.status(201).json({ token, email, displayName });
  } catch (err) {
    console.error(err);
    res.status(500).json({ message: 'Lỗi server khi đăng ký.' });
  }
});

// 2. POST /api/auth/login - Đăng nhập
router.post('/login', async (req, res) => {
  const { email, password } = req.body;

  if (!email || !password) {
    return res.status(400).json({ message: 'Vui lòng cung cấp email và mật khẩu.' });
  }

  try {
    const userResult = await db.query('SELECT * FROM users WHERE email = $1', [email]);
    if (userResult.rows.length === 0) {
      // SV_QD_39: Sai thông tin thì thông báo chung, không tiết lộ email có tồn tại hay không
      return res.status(400).json({ message: 'Email hoặc mật khẩu không chính xác.' });
    }

    const user = userResult.rows[0];
    const now = Date.now();

    // Check account lockout status
    if (user.locked_until && user.locked_until > now) {
      const remainingMinutes = Math.ceil((user.locked_until - now) / 60000);
      return res.status(403).json({
        message: `Tài khoản tạm khóa do đăng nhập sai 5 lần. Thử lại sau ${remainingMinutes} phút.`
      });
    }

    // Match password
    const isMatch = await bcrypt.compare(password, user.password_hash);
    if (!isMatch) {
      // Increment failed attempts
      let attempts = (user.failed_login_attempts || 0) + 1;
      let lockedUntil = 0;

      if (attempts >= 5) {
        lockedUntil = now + 5 * 60 * 1000; // Locked for 5 minutes
      }

      await db.query(
        'UPDATE users SET failed_login_attempts = $1, locked_until = $2 WHERE email = $3',
        [attempts, lockedUntil, email]
      );

      return res.status(400).json({ message: 'Email hoặc mật khẩu không chính xác.' });
    }

    // Success: Reset failure count and lockout time
    await db.query(
      'UPDATE users SET failed_login_attempts = 0, locked_until = 0 WHERE email = $1',
      [email]
    );

    const token = generateToken(email);
    res.json({ token, email, displayName: user.display_name });
  } catch (err) {
    console.error(err);
    res.status(500).json({ message: 'Lỗi server khi đăng nhập.' });
  }
});

module.exports = router;
