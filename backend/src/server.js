const express = require('express');
const cors = require('cors');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 5000;

// Middleware
app.use(cors());
app.use(express.json());

// Routes
app.use('/api/auth', require('./routes/auth'));
app.use('/api/sync', require('./routes/sync'));

// Basic health check route
app.get('/health', (req, res) => {
  res.json({ status: 'ok', time: Date.now() });
});

// Error handling middleware
app.use((err, req, res, next) => {
  console.error(err.stack);
  res.status(500).json({ message: 'Đã xảy ra lỗi hệ thống!' });
});

app.listen(PORT, () => {
  console.log(`Server đang chạy trên port ${PORT}`);
});
