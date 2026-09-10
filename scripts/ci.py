#!/usr/bin/env python3
"""Same fail-fast entry point for local and GitHub checks. No Docker required."""
import argparse
import json
import os
from pathlib import Path
import subprocess
import sys
root = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser()
parser.add_argument('target', choices=['guard', 'backend', 'android', 'all'])
args = parser.parse_args()
env = dict(os.environ)
for key in list(env):
    if key.startswith(('JWT_', 'DB_', 'PGPASSWORD', 'DATABASE_URL')):
        del env[key]
env['NODE_OPTIONS'] = '--require=' + json.dumps(str(root / 'scripts/deny-local-env.cjs'))

def run(command, cwd=root):
    print('+ ' + ' '.join(command), flush=True)
    subprocess.run(command, cwd=cwd, env=env, check=True)

try:
    run([sys.executable, 'scripts/repository_guard.py'])
    run([sys.executable, 'scripts/test-guards.py'])
    if args.target in ['backend', 'all']:
        actual = subprocess.check_output(['node', '--version'], env=env, text=True).strip().lstrip('v')
        if actual != (root / '.node-version').read_text().strip():
            raise RuntimeError('Node version differs from .node-version')
        run(['npm', 'ci', '--no-audit', '--no-fund'], root / 'backend')
        run(['npm', 'run', 'typecheck'], root / 'backend')
        # npm test pretest builds the actual code used by JWT tests.
        run(['npm', 'test', '--', '--runInBand'], root / 'backend')
        run(['npm', 'run', 'test:auth:integration'], root / 'backend')
    if args.target in ['android', 'all']:
        run([sys.executable, 'scripts/verify_wrapper.py'])
        java_home = env.get('JAVA_HOME')
        if not java_home: raise RuntimeError('Set JAVA_HOME to JDK 17')
        version = subprocess.check_output([str(Path(java_home) / 'bin/java'), '-version'], stderr=subprocess.STDOUT, text=True)
        if 'version "17.' not in version: raise RuntimeError('JAVA_HOME must select JDK 17')
        run(['bash', 'gradlew', '--no-daemon', '-Dorg.gradle.java.home=' + java_home,
             '-PbackendBaseUrl=http://10.0.2.2:3000/api/v1/', 'testDebugUnitTest', 'assembleDebug'])
except subprocess.CalledProcessError as error:
    raise SystemExit(error.returncode or 1)
except RuntimeError as error:
    raise SystemExit(str(error))
