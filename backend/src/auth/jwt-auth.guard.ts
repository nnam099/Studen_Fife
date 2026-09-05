import {
  CanActivate,
  ExecutionContext,
  Injectable,
  UnauthorizedException,
} from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';

@Injectable()
export class JwtAuthGuard implements CanActivate {
  constructor(private readonly jwtService: JwtService) {}

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
        secret: process.env.JWT_SECRET || 'student-finance-dev-secret',
      });
      return true;
    } catch {
      throw new UnauthorizedException('Invalid or expired access token');
    }
  }
}
