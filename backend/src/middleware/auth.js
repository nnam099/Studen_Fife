// Legacy entry point: build NestJS first; reuse its validated JWT contract.
const { loadJwtConfig } = require('../../dist/auth/auth.config');
const jwtConfig = loadJwtConfig(process.env);
const jwt = require('jsonwebtoken');

module.exports = function (req, res, next) {
  // Get token from header
  const authHeader = req.header('Authorization');

  if (!authHeader) {
    return res.status(401).json({ message: 'Không có token, quyền truy cập bị từ chối.' });
  }

  // Check Bearer format
  const parts = authHeader.split(' ');
  if (parts.length !== 2 || parts[0] !== 'Bearer') {
    return res.status(401).json({ message: 'Token sai định dạng Bearer.' });
  }

  const token = parts[1];

  try {
    const decoded = jwt.verify(token, jwtConfig.accessSecret, { algorithms: ['HS256'] });
    req.user = decoded; // Attach user payload containing { email }
    next();
  } catch (err) {
    res.status(401).json({ message: 'Token không hợp lệ hoặc đã hết hạn.' });
  }
};
