#!/usr/bin/env python3
"""Value-safe checks over the Git index or an explicit clean-room manifest."""
import argparse
import json
import re
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def is_environment(name):
    return name == '.env' or name.startswith('.env.')

def is_template(name):
    return name == '.env.example' or (name.startswith('.env.') and name.endswith('.example'))

def source_files():
    if (ROOT / '.git').exists():
        return subprocess.check_output(['git', 'ls-files', '-z'], cwd=ROOT).decode().split('\0')[:-1]
    manifest = ROOT / '.ci-source-manifest.json'
    if not manifest.is_file():
        raise RuntimeError('Missing Git index or clean-room source manifest')
    return json.loads(manifest.read_text())

def check():
    errors = []
    files = source_files()
    for name in files:
        base = Path(name).name
        if is_environment(base) and not is_template(base):
            errors.append(f'{name}: tracked environment file')
    template = ROOT / 'backend/.env.example'
    if not template.is_file() or 'backend/.env.example' not in files:
        errors.append('backend/.env.example: required tracked template missing')
    else:
        for line in template.read_text().splitlines():
            match = re.match(r'^(JWT_SECRET|JWT_REFRESH_SECRET|DB_PASSWORD)=(.*)$', line)
            if match and match[2].strip():
                errors.append(f'backend/.env.example: {match[1]} must be empty')
        for key in ['JWT_SECRET', 'JWT_REFRESH_SECRET', 'DB_PASSWORD']:
            if not re.search(rf'^{key}=$', template.read_text(), re.M):
                errors.append(f'backend/.env.example: missing empty {key} placeholder')
    patterns = [
        (r'(?:JWT_SECRET|JWT_REFRESH_SECRET)\s*(?:\|\||\?\?)\s*[\'"`]', 'JWT fallback'),
        (r'(?:secret|accessSecret|refreshSecret)\s*:\s*[\'"`][^\'"`]+[\'"`]', 'literal JWT secret'),
        (r'-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----', 'private key'),
    ]
    for path in sorted((ROOT / 'backend/src').rglob('*')):
        if path.suffix not in ['.ts', '.js', '.cjs']: continue
        for number, line in enumerate(path.read_text().splitlines(), 1):
            for pattern, kind in patterns:
                if re.search(pattern, line): errors.append(f'{path.relative_to(ROOT)}:{number}: {kind}')
    for error in errors: print(error)
    if errors: return 1
    print('PASS: repository environment tracking, template and runtime JWT guards')
    return 0

if __name__ == '__main__':
    try: raise SystemExit(check())
    except (RuntimeError, subprocess.CalledProcessError):
        print('FAIL: repository inventory unavailable'); raise SystemExit(1)
