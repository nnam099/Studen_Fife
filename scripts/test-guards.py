#!/usr/bin/env python3
"""Regression checks for the value-safe CI guards using disposable fixtures."""
import json
from pathlib import Path
import shutil
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]

class Guards(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory(prefix='student-life-guards-')
        self.root = Path(self.temp.name)
        (self.root / 'scripts').mkdir()
        (self.root / 'backend/src').mkdir(parents=True)
        shutil.copy(ROOT / 'scripts/repository_guard.py', self.root / 'scripts/repository_guard.py')
        (self.root / 'backend/.env.example').write_text('JWT_SECRET=\nJWT_REFRESH_SECRET=\nDB_PASSWORD=\n')
        self.files = ['backend/.env.example']
    def tearDown(self):
        self.temp.cleanup()
    def run_guard(self):
        (self.root / '.ci-source-manifest.json').write_text(json.dumps(self.files))
        return subprocess.run(['python3', str(self.root / 'scripts/repository_guard.py')], capture_output=True, text=True)
    def test_templates_allowed(self):
        self.files.append('backend/.env.production.example')
        self.assertEqual(self.run_guard().returncode, 0)
    def test_tracked_environment_rejected_without_values(self):
        for name in ['backend/.env', 'nested/.env.local', 'backend/.env.production']:
            self.files = ['backend/.env.example', name]
            result = self.run_guard()
            self.assertNotEqual(result.returncode, 0)
            self.assertIn(name, result.stdout)
    def test_template_required(self):
        (self.root / 'backend/.env.example').unlink()
        self.assertNotEqual(self.run_guard().returncode, 0)
    def test_fallback_and_literal_key_rejected_without_values(self):
        import secrets
        value = secrets.token_urlsafe(40)
        for content in [f"const key = process.env.JWT_SECRET || '{value}';", f"const key = process.env.JWT_REFRESH_SECRET ?? '{value}';", f"const config = {{ secret: '{value}' }};"]:
            (self.root / 'backend/src/example.ts').write_text(content)
            result = self.run_guard()
            self.assertNotEqual(result.returncode, 0)
            self.assertFalse(value in result.stdout + result.stderr)
    def test_env_read_hook(self):
        local = self.root / '.env.local'
        local.write_text('')
        hook = ROOT / 'scripts/deny-local-env.cjs'
        for method in ['readFileSync', 'createReadStream']:
            code = f"require('node:fs').{method}(process.argv[1])"
            result = subprocess.run(['node', '--require', str(hook), '-e', code, str(local)], capture_output=True)
            self.assertNotEqual(result.returncode, 0)
        result = subprocess.run(['node', '--require', str(hook), '-e', "require('node:fs').readFileSync(process.argv[1])", str(self.root / 'backend/.env.example')], capture_output=True)
        self.assertEqual(result.returncode, 0)

if __name__ == '__main__': unittest.main()
