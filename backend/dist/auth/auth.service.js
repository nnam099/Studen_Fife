"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
var __param = (this && this.__param) || function (paramIndex, decorator) {
    return function (target, key) { decorator(target, key, paramIndex); }
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.AuthService = void 0;
const common_1 = require("@nestjs/common");
const jwt_1 = require("@nestjs/jwt");
const typeorm_1 = require("@nestjs/typeorm");
const typeorm_2 = require("typeorm");
const user_entity_1 = require("../users/entities/user.entity");
const password_util_1 = require("../common/utils/password.util");
let AuthService = class AuthService {
    constructor(userRepository, jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }
    async register(dto) {
        const existing = await this.userRepository.findOne({
            where: { email: dto.email },
        });
        if (existing) {
            throw new common_1.ConflictException('Email already exists');
        }
        const hashedPassword = await (0, password_util_1.hashPassword)(dto.password);
        const user = this.userRepository.create({
            email: dto.email,
            passwordHash: hashedPassword,
            fullName: dto.fullName,
            isActive: true,
        });
        const savedUser = await this.userRepository.save(user);
        const tokens = await this.createTokens(savedUser.id, savedUser.email);
        return {
            user: {
                id: savedUser.id,
                email: savedUser.email,
                fullName: savedUser.fullName,
            },
            ...tokens,
        };
    }
    async login(dto) {
        const user = await this.userRepository.findOne({
            where: { email: dto.email },
        });
        if (!user) {
            throw new common_1.UnauthorizedException('Invalid credentials');
        }
        const isValidPassword = await (0, password_util_1.comparePassword)(dto.password, user.passwordHash);
        if (!isValidPassword) {
            throw new common_1.UnauthorizedException('Invalid credentials');
        }
        const tokens = await this.createTokens(user.id, user.email);
        return {
            user: {
                id: user.id,
                email: user.email,
                fullName: user.fullName,
            },
            ...tokens,
        };
    }
    async refresh(refreshToken) {
        try {
            const payload = this.jwtService.verify(refreshToken, {
                secret: process.env.JWT_REFRESH_SECRET || 'dev-refresh-secret',
            });
            const user = await this.userRepository.findOne({
                where: { id: payload.sub },
            });
            if (!user) {
                throw new common_1.UnauthorizedException('Invalid refresh token');
            }
            const tokens = await this.createTokens(user.id, user.email);
            return tokens;
        }
        catch (error) {
            throw new common_1.UnauthorizedException('Invalid refresh token');
        }
    }
    async createTokens(userId, email) {
        const accessToken = this.jwtService.sign({ sub: userId, email }, {
            secret: process.env.JWT_SECRET || 'dev-secret',
            expiresIn: '15m',
        });
        const refreshToken = this.jwtService.sign({ sub: userId, email }, {
            secret: process.env.JWT_REFRESH_SECRET || 'dev-refresh-secret',
            expiresIn: '7d',
        });
        return {
            accessToken,
            refreshToken,
        };
    }
};
exports.AuthService = AuthService;
exports.AuthService = AuthService = __decorate([
    (0, common_1.Injectable)(),
    __param(0, (0, typeorm_1.InjectRepository)(user_entity_1.User)),
    __metadata("design:paramtypes", [typeorm_2.Repository,
        jwt_1.JwtService])
], AuthService);
//# sourceMappingURL=auth.service.js.map