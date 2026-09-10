#!/usr/bin/env python3
"""Checksums verified against https://services.gradle.org/distributions/."""
import hashlib
from pathlib import Path
root = Path(__file__).resolve().parents[1]
expected_jar = 'a8451eeda314d0568b5340498b36edf147a8f0d692c5ff58082d477abe9146e4'
expected_zip = '38f66cd6eef217b4c35855bb11ea4e9fbc53594ccccb5fb82dfd317ef8c2c5a3'
props = (root / 'gradle/wrapper/gradle-wrapper.properties').read_text()
valid = hashlib.sha256((root / 'gradle/wrapper/gradle-wrapper.jar').read_bytes()).hexdigest() == expected_jar
valid &= f'distributionSha256Sum={expected_zip}' in props
valid &= 'distributionUrl=https\\://services.gradle.org/distributions/gradle-8.2-bin.zip' in props
if not valid: raise SystemExit('FAIL: Gradle wrapper checksum/configuration')
print('PASS: Gradle 8.2 wrapper JAR and distribution checksum configuration')
