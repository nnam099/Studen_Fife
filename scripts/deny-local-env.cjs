// CI checks must not read local secret files, even when such files exist locally.
const fs = require('node:fs');
const path = require('node:path');
function guard(file) {
  if (typeof file !== 'string' && !(file instanceof URL) && !Buffer.isBuffer(file)) return;
  const name = path.basename(String(file));
  if ((name === '.env' || name.startsWith('.env.')) && !name.endsWith('.example') && fs.existsSync(file)) {
    process.stderr.write('FAIL: CI attempted to read a local environment file\n');
    process.exit(1);
  }
}
for (const method of ['readFileSync', 'readFile', 'createReadStream']) {
  const original = fs[method];
  fs[method] = function(file, ...args) { guard(file); return original.call(this, file, ...args); };
}
const original = fs.promises.readFile;
fs.promises.readFile = function(file, ...args) { guard(file); return original.call(this, file, ...args); };
