// Generate local-only credentials directly into an exclusive, owner-only file.
// Existing .env is never read, replaced or printed.
const { readFileSync, writeFileSync } = require('node:fs');
const { randomBytes } = require('node:crypto');
const path = require('node:path');
const target = path.resolve(__dirname, '../.env');
try {
  let template = readFileSync(path.resolve(__dirname, '../.env.example'), 'utf8');
  for (const name of ['DB_PASSWORD', 'JWT_SECRET', 'JWT_REFRESH_SECRET']) {
    template = template.replace(new RegExp(`^${name}=$`, 'm'), `${name}=${randomBytes(48).toString('base64url')}`);
  }
  writeFileSync(target, template, { flag: 'wx', mode: 0o600 });
  console.log('Created backend/.env with local-only generated credentials. No values printed.');
} catch (error) {
  console.error(error.code === 'EEXIST' ? 'backend/.env already exists; kept unchanged.' : 'Could not create backend/.env; no values printed.');
  process.exitCode = 1;
}
