import {
  Injectable,
  UnauthorizedException,
  ConflictException,
} from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { User } from '../users/entities/user.entity';
import { RegisterDto } from './dto/register.dto';
import { LoginDto } from './dto/login.dto';
import { hashPassword, comparePassword } from '../common/utils/password.util';

@Injectable()
export class AuthService {
  constructor(
    @InjectRepository(User)
    private readonly userRepository: Repository<User>,
    private readonly jwtService: JwtService,
  ) {}

  async register(dto: RegisterDto) {
    const existing = await this.userRepository.findOne({
      where: { email: dto.email },
    });

    if (existing) {
      throw new ConflictException('Email already exists');
    }

    const hashedPassword = await hashPassword(dto.password);
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

  async login(dto: LoginDto) {
    const user = await this.userRepository.findOne({
      where: { email: dto.email },
    });

    if (!user) {
      throw new UnauthorizedException('Invalid credentials');
    }

    const isValidPassword = await comparePassword(dto.password, user.passwordHash);
    if (!isValidPassword) {
      throw new UnauthorizedException('Invalid credentials');
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

  async refresh(refreshToken: string) {
    try {
      const payload = this.jwtService.verify(refreshToken, {
        secret: process.env.JWT_REFRESH_SECRET || 'dev-refresh-secret',
      });

      const user = await this.userRepository.findOne({
        where: { id: payload.sub },
      });

      if (!user) {
        throw new UnauthorizedException('Invalid refresh token');
      }

      const tokens = await this.createTokens(user.id, user.email);
      return tokens;
    } catch (error) {
      throw new UnauthorizedException('Invalid refresh token');
    }
  }

  private async createTokens(userId: string, email: string) {
    const accessToken = this.jwtService.sign(
      { sub: userId, email },
      {
        secret: process.env.JWT_SECRET || 'dev-secret',
        expiresIn: '15m' as const,
      },
    );

    const refreshToken = this.jwtService.sign(
      { sub: userId, email },
      {
        secret: process.env.JWT_REFRESH_SECRET || 'dev-refresh-secret',
        expiresIn: '7d' as const,
      },
    );

    return {
      accessToken,
      refreshToken,
    };
  }
}
