import { ConfigModule, ConfigService } from '@nestjs/config';
import { JWT_CONFIGURATION, JwtConfiguration } from './auth.config';
import { Module } from '@nestjs/common';
import { JwtModule } from '@nestjs/jwt';
import { TypeOrmModule } from '@nestjs/typeorm';
import { User } from '../users/entities/user.entity';
import { UsersModule } from '../users/users.module';
import { AuthController } from './auth.controller';
import { AuthService } from './auth.service';
import { JwtAuthGuard } from './jwt-auth.guard';

@Module({
  imports: [
    ConfigModule,
    UsersModule,
    TypeOrmModule.forFeature([User]),
    JwtModule.register({}),
  ],
  controllers: [AuthController],
  providers: [
    {
      provide: JWT_CONFIGURATION,
      inject: [ConfigService],
      useFactory: (config: ConfigService): JwtConfiguration => config.getOrThrow<JwtConfiguration>('validatedJwt'),
    },
    AuthService, JwtAuthGuard,
  ],
  exports: [AuthService, JwtModule, JwtAuthGuard, JWT_CONFIGURATION],
})
export class AuthModule {}
