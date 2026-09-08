/** The only JWT environment contract. Never include values in validation errors. */
export const JWT_CONFIGURATION = Symbol('JWT_CONFIGURATION');

export interface JwtConfiguration {
  readonly accessSecret: string;
  readonly refreshSecret: string;
  readonly accessTtl: number;
  readonly refreshTtl: number;
}

type Environment = Record<string, unknown>;

function secret(env: Environment, name: string): string {
  const value = env[name];
  if (typeof value !== 'string' || !value.trim()) {
    throw new Error(`Missing required configuration: ${name}`);
  }
  if (value !== value.trim() || value.length < 32) {
    throw new Error(`Invalid configuration: ${name}`);
  }
  return value;
}

// Convert an explicit seconds/minutes/hours/days duration to JWT numeric seconds.
function ttl(env: Environment, name: string, fallback: string): number {
  const value = env[name] === undefined ? fallback : env[name];
  const match = typeof value === 'string' ? /^([1-9][0-9]*)(s|m|h|d)$/.exec(value) : null;
  if (!match) throw new Error(`Invalid configuration: ${name}`);
  const seconds = Number(match[1]) * { s: 1, m: 60, h: 3600, d: 86400 }[match[2]]!;
  if (!Number.isSafeInteger(seconds) || seconds > 2147483647) {
    throw new Error(`Invalid configuration: ${name}`);
  }
  return seconds;
}

export function loadJwtConfig(env: Environment): JwtConfiguration {
  const accessSecret = secret(env, 'JWT_SECRET');
  const refreshSecret = secret(env, 'JWT_REFRESH_SECRET');
  if (accessSecret === refreshSecret) {
    throw new Error('Invalid configuration: JWT_SECRET, JWT_REFRESH_SECRET');
  }
  return Object.freeze({
    accessSecret,
    refreshSecret,
    accessTtl: ttl(env, 'JWT_ACCESS_TTL', '15m'),
    refreshTtl: ttl(env, 'JWT_REFRESH_TTL', '7d'),
  });
}

export function validateEnvironment(env: Environment): Environment {
  const jwt = loadJwtConfig(env);
  if (typeof env.DB_PASSWORD !== 'string' || !env.DB_PASSWORD.trim()) {
    throw new Error('Missing required configuration: DB_PASSWORD');
  }
  return { ...env, validatedJwt: jwt };
}
