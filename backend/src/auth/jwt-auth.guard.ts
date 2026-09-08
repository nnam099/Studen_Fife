import { JWT_CONFIGURATION, JwtConfiguration } from './auth.config';
import {
  CanActivate,
  ExecutionContext,
  Injectable,
  Inject,
  UnauthorizedException,
} from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';

@Injectable()
export class JwtAuthGuard implements CanActivate {
  constructor(
    private readonly jwtService: JwtService,
    @Inject(JWT_CONFIGURATION) private readonly jwtConfig: JwtConfiguration,
  ) {}

  canActivate(context: ExecutionContext): boolean {
    const request = context.switchToHttp().getRequest();
    const authorization = request.headers.authorization as string | undefined;
    const token = authorization?.startsWith('Bearer ')
      ? authorization.slice(7)
      : undefined;

    if (!token) {
      throw new UnauthorizedException('Bearer token is required');
    }

    try {
      request.user = this.jwtService.verify(token, {
        secret: this.jwtConfig.accessSecret,
        algorithms: ['HS256'],
      });
      return true;
    } catch {
      throw new UnauthorizedException('Invalid or expired access token');
    }
  }
}
