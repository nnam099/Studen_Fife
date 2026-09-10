#!/usr/bin/env python3
"""Export proposed tracked + untracked source into an EXISTING empty mktemp dir."""
import json
from pathlib import Path
import shutil
import subprocess
import sys
sys.dont_write_bytecode = True
from repository_guard import is_environment, is_template
root = Path(__file__).resolve().parents[1]
target = Path(sys.argv[1]).resolve()
if not target.is_dir() or any(target.iterdir()) or target == root or root in target.parents:
    raise SystemExit('Target must be an existing empty temporary directory outside repository')
files = subprocess.check_output(['git', 'ls-files', '--cached', '--others', '--exclude-standard', '-z'], cwd=root).decode().split('\0')[:-1]
excluded = {'.git', '.idea', '.vscode', '.gradle', 'build', 'dist', 'node_modules', 'postgres_data', '__pycache__', '.codex', '.agents'}
exported = []
for name in sorted(set(files)):
    rel = Path(name)
    if any(part in excluded for part in rel.parts): continue
    if rel.name in ['local.properties', '.npmrc', '.DS_Store']: continue
    if is_environment(rel.name) and not is_template(rel.name): continue
    source = root / rel
    if not source.is_file(): continue
    if source.is_symlink(): raise SystemExit('Source symlinks require explicit review: ' + name)
    dest = target / rel
    dest.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(source, dest)
    exported.append(name)
(target / '.ci-source-manifest.json').write_text(json.dumps(exported, indent=2) + '\n')
print(f'Exported {len(exported)} source files; no Git metadata, local env or build/dependency directories')
