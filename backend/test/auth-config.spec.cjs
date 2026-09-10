const { execFileSync } = require('node:child_process');
const path = require('node:path');

// Jest 29 cannot require Nest 12's ESM dependencies. Execute the actual build
// under the same Node runtime as production; no JWT/crypto mocks or transforms.
function check(body) {
  const source = `
    const assert = require('node:assert/strict');
    const { randomBytes } = require('node:crypto');
    const { loadJwtConfig, validateEnvironment } = require('./dist/auth/auth.config');
    const environment = () => ({ JWT_SECRET: randomBytes(48).toString('base64url'), JWT_REFRESH_SECRET: randomBytes(48).toString('base64url'), DB_PASSWORD: randomBytes(24).toString('hex') });
    (async () => { ${body} })().catch(() => { process.stderr.write('JWT assertion failed (details redacted)'); process.exitCode = 1; });
  `;
  execFileSync(process.execPath, ['-e', source], {
    cwd: path.resolve(__dirname, '..'), timeout: 15000, stdio: 'pipe',
  });
}

describe('JWT configuration', () => {
  test.each(['JWT_SECRET', 'JWT_REFRESH_SECRET'])('requires %s', key => check(`
    const env = environment(); delete env['${key}'];
    assert.throws(() => loadJwtConfig(env), { message: 'Missing required configuration: ${key}' });
  `));
  test.each(['JWT_SECRET', 'JWT_REFRESH_SECRET'])('rejects empty/whitespace %s', key => check(`
    for (const value of ['', '  ']) assert.throws(() => loadJwtConfig({ ...environment(), ['${key}']: value }), { message: 'Missing required configuration: ${key}' });
  `));
  test('rejects short or identical secrets', () => check(`
    const env = environment();
    assert.throws(() => loadJwtConfig({ ...env, JWT_REFRESH_SECRET: env.JWT_SECRET }), { message: 'Invalid configuration: JWT_SECRET, JWT_REFRESH_SECRET' });
    assert.throws(() => loadJwtConfig({ ...env, JWT_SECRET: randomBytes(8).toString('hex') }), { message: 'Invalid configuration: JWT_SECRET' });
  `));
  test('centralizes and validates TTLs', () => check(`
    const config = loadJwtConfig(environment());
    assert.equal(config.accessTtl, 900); assert.equal(config.refreshTtl, 604800);
    const custom = loadJwtConfig({ ...environment(), JWT_ACCESS_TTL: '2m', JWT_REFRESH_TTL: '3h' });
    assert.equal(custom.accessTtl, 120); assert.equal(custom.refreshTtl, 10800);
    for (const key of ['JWT_ACCESS_TTL', 'JWT_REFRESH_TTL']) for (const value of ['', '0s', '-1m', '100', 'invalid', '999999999999999d']) {
      assert.throws(() => loadJwtConfig({ ...environment(), [key]: value }), { message: 'Invalid configuration: ' + key });
    }
  `));
  test('provides validated configuration and requires database password', () => check(`
    const env = environment();
    assert.deepEqual(validateEnvironment(env).validatedJwt, loadJwtConfig(env));
    assert.throws(() => validateEnvironment({ ...env, DB_PASSWORD: '' }), { message: 'Missing required configuration: DB_PASSWORD' });
  `));
  test('validation errors and console logs do not disclose values', () => check(`
    const env = environment(); const logs = [];
    console.log = console.error = console.warn = (...args) => logs.push(args.join(' '));
    let message;
    try { loadJwtConfig({ ...env, JWT_ACCESS_TTL: env.JWT_SECRET }); } catch (error) { message = error.message; }
    assert.equal(message, 'Invalid configuration: JWT_ACCESS_TTL');
    assert.equal(Object.values(env).some(value => message.includes(value)), false);
    assert.equal(logs.length, 0);
  `));
});

test('real AuthService register/login/access/refresh, TTL and wrong/cross-key rejection', () => check(`
  const { JwtService } = require('@nestjs/jwt');
  const { AuthService } = require('./dist/auth/auth.service');
  const { JwtAuthGuard } = require('./dist/auth/jwt-auth.guard');
  const config = loadJwtConfig({ ...environment(), JWT_ACCESS_TTL: '2m', JWT_REFRESH_TTL: '3h' });
  let user;
  const repo = {
    findOne: async ({ where }) => user && (where.id === user.id || where.email === user.email) ? user : null,
    create: value => value,
    save: async value => (user = { ...value, id: 'test-user' }),
  };
  const jwt = new JwtService(); const service = new AuthService(repo, jwt, config); const guard = new JwtAuthGuard(jwt, config);
  const context = token => ({ switchToHttp: () => ({ getRequest: () => ({ headers: { authorization: 'Bearer ' + token } }) }) });
  const password = randomBytes(24).toString('base64url');
  const credentials = { email: 'unit@example.invalid', password };
  const registered = await service.register({ ...credentials, fullName: 'Unit Test' });
  assert.deepEqual(Object.keys(registered).sort(), ['accessToken', 'refreshToken', 'user']);
  assert.equal(guard.canActivate(context(registered.accessToken)), true);
  const loggedIn = await service.login(credentials);
  assert.equal(guard.canActivate(context(loggedIn.accessToken)), true);
  const access = jwt.verify(loggedIn.accessToken, { secret: config.accessSecret, algorithms: ['HS256'] });
  const refresh = jwt.verify(loggedIn.refreshToken, { secret: config.refreshSecret, algorithms: ['HS256'] });
  assert.equal(access.exp - access.iat, 120); assert.equal(refresh.exp - refresh.iat, 10800);
  const refreshed = await service.refresh(loggedIn.refreshToken);
  assert.deepEqual(Object.keys(refreshed).sort(), ['accessToken', 'refreshToken']);
  assert.equal(guard.canActivate(context(refreshed.accessToken)), true);
  assert.throws(() => guard.canActivate(context(loggedIn.refreshToken)));
  await assert.rejects(service.refresh(loggedIn.accessToken), { message: 'Invalid refresh token' });
  const wrong = jwt.sign({ sub: user.id }, { secret: randomBytes(48).toString('hex') });
  assert.throws(() => guard.canActivate(context(wrong)));
  await assert.rejects(service.refresh(wrong), { message: 'Invalid refresh token' });
  const expired = jwt.sign({ sub: user.id }, { secret: config.accessSecret, expiresIn: -1 });
  assert.throws(() => guard.canActivate(context(expired)));
  await assert.rejects(service.login({ ...credentials, password: 'incorrect' }));
  await assert.rejects(service.register({ ...credentials, fullName: 'Duplicate' }));
`));
