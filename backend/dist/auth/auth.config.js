"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.jwtConfig = void 0;
exports.jwtConfig = {
    secret: process.env.JWT_SECRET || 'student-finance-dev-secret',
    refreshSecret: process.env.JWT_REFRESH_SECRET || 'student-finance-refresh-dev-secret',
    accessTtl: process.env.JWT_ACCESS_TTL || '15m',
    refreshTtl: process.env.JWT_REFRESH_TTL || '7d',
};
//# sourceMappingURL=auth.config.js.map