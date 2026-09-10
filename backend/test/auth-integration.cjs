// Starts its own temporary PostgreSQL cluster. Never connects to the project DB.
const { mkdtempSync, writeFileSync, rmSync, symlinkSync } = require('node:fs');
const { tmpdir } = require('node:os');
const path = require('node:path');
const { randomBytes } = require('node:crypto');
const { spawn, spawnSync } = require('node:child_process');
const net = require('node:net');
const assert = require('node:assert/strict');
const { once } = require('node:events');

const root = path.resolve(__dirname, '..');
const temp = mkdtempSync(path.join(tmpdir(), 'student-life-auth-test-'));
const pgBin = process.env.PG_BIN || '/usr/lib/postgresql/17/bin';
const main = path.join(root, 'dist/main.js');
let app, pgStarted = false, phase = 'initialization';
const logs = [];
const secret = () => randomBytes(48).toString('base64url');
const env = { ...process.env, NODE_ENV: 'production', JWT_SECRET: secret(), JWT_REFRESH_SECRET: secret(), JWT_ACCESS_TTL: '2m', JWT_REFRESH_TTL: '1h', DB_PASSWORD: secret(), DB_USERNAME: 'auth_test', DB_NAME: 'postgres', DB_HOST: '127.0.0.1' };
function command(executable, args, options = {}) {
  const result = spawnSync(executable, args, { cwd: temp, encoding: 'utf8', timeout: 20000, ...options });
  if (result.status !== 0) throw new Error('Command failed');
}
async function freePort() {
  const server = net.createServer();
  server.listen(0, '127.0.0.1'); await once(server, 'listening');
  const port = server.address().port; await new Promise(resolve => server.close(resolve)); return port;
}
async function run() {
  phase = 'startup validation without environment files';
  for (const key of ['JWT_SECRET', 'JWT_REFRESH_SECRET']) {
    for (const value of [undefined, '', '   ']) {
      const invalid = { ...env }; if (value === undefined) delete invalid[key]; else invalid[key] = value;
      const result = spawnSync(process.execPath, [main], { cwd: temp, env: invalid, encoding: 'utf8', timeout: 10000 });
      const output = (result.stdout || '') + (result.stderr || '');
      assert.ok(result.status !== null && result.status !== 0);
      assert.ok(output.includes('Missing required configuration: ' + key));
      assert.ok(![env.JWT_SECRET, env.JWT_REFRESH_SECRET, env.DB_PASSWORD].some(value => output.includes(value)));
      assert.ok(!output.includes('Unable to connect to the database'));
    }
  }
  phase = 'isolated PostgreSQL startup';
  const passwordFile = path.join(temp, 'pg-password');
  writeFileSync(passwordFile, env.DB_PASSWORD, { mode: 0o600 });
  command(path.join(pgBin, 'initdb'), ['-D', path.join(temp, 'data'), '-U', env.DB_USERNAME, '--pwfile=' + passwordFile, '--auth=scram-sha-256', '--no-instructions']);
  env.DB_PORT = String(await freePort()); env.PORT = String(await freePort());
  command(path.join(pgBin, 'pg_ctl'), ['-D', path.join(temp, 'data'), '-l', path.join(temp, 'postgres.log'), '-o', `-k ${temp} -h 127.0.0.1 -p ${env.DB_PORT}`, '-w', 'start']);
  pgStarted = true;
  command(path.join(pgBin, 'pg_isready'), ['-h', '127.0.0.1', '-p', env.DB_PORT, '-U', env.DB_USERNAME, '-d', env.DB_NAME]);
  phase = 'backend startup and migrations';
  symlinkSync(path.join(root, 'dist'), path.join(temp, 'dist'), 'dir');
  app = spawn(process.execPath, [main], { cwd: temp, env, stdio: ['ignore', 'pipe', 'pipe'] });
  app.stdout.on('data', chunk => logs.push(chunk.toString())); app.stderr.on('data', chunk => logs.push(chunk.toString()));
  // AppModule's migrations path is relative to cwd. Link only compiled migration
  // assets into this empty directory; project .env files cannot be read here.
  const base = `http://127.0.0.1:${env.PORT}/api/v1`;
  let ready = false;
  for (let attempt = 0; attempt < 100; attempt++) {
    if (app.exitCode !== null) break;
    try { ready = (await fetch(base + '/health')).ok; } catch {}
    if (ready) break;
    await new Promise(resolve => setTimeout(resolve, 100));
  }
  assert.ok(ready);
  phase = 'register/login/protected endpoint/refresh';
  async function request(method, route, body, token, expected) {
    const response = await fetch(base + route, {
      method, headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: 'Bearer ' + token } : {}) },
      ...(body ? { body: JSON.stringify(body) } : {}), signal: AbortSignal.timeout(5000),
    });
    assert.equal(response.status, expected);
    return response.json();
  }
  const credentials = { email: 'integration@example.invalid', password: secret() };
  const registered = await request('POST', '/auth/register', { ...credentials, fullName: 'Integration' }, null, 201);
  await request('GET', '/academic-terms', null, registered.accessToken, 200);
  const loggedIn = await request('POST', '/auth/login', credentials, null, 200);
  assert.deepEqual(await request('GET', '/academic-terms', null, loggedIn.accessToken, 200), []);
  const refreshed = await request('POST', '/auth/refresh', { refreshToken: loggedIn.refreshToken }, null, 200);
  await request('GET', '/academic-terms', null, refreshed.accessToken, 200);
  await request('GET', '/academic-terms', null, loggedIn.refreshToken, 401);
  await request('POST', '/auth/refresh', { refreshToken: loggedIn.accessToken }, null, 401);
  await request('GET', '/academic-terms', null, null, 401);
  phase = 'log redaction';
  const sensitive = [env.JWT_SECRET, env.JWT_REFRESH_SECRET, env.DB_PASSWORD, credentials.password, registered.accessToken, registered.refreshToken];
  assert.ok(!sensitive.some(value => logs.join('').includes(value)));
  console.log('PASS: missing/blank config fails before DB; fresh migrations; HTTP register/login/protected/refresh; token separation; no secret/token/password in captured logs.');
}
let cleaning = false;
async function cleanup() {
  if (cleaning) return;
  cleaning = true;
  if (app && app.exitCode === null) { const exited = once(app, 'exit'); app.kill('SIGTERM'); await exited; }
  if (pgStarted) {
    try { command(path.join(pgBin, 'pg_ctl'), ['-D', path.join(temp, 'data'), '-m', 'fast', '-w', 'stop']); }
    catch { console.error('Temporary PostgreSQL cleanup failed; inspect temporary cluster manually.'); process.exitCode = 1; return; }
  }
  rmSync(temp, { recursive: true, force: true });
}
for (const signal of ['SIGINT', 'SIGTERM']) {
  process.once(signal, async () => { await cleanup(); process.exit(1); });
}
run().catch(() => { console.error(`FAIL: ${phase} (details redacted)`); process.exitCode = 1; }).finally(cleanup);
