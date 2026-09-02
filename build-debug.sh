#!/usr/bin/env sh
set -eu

exec ./gradlew assembleDebug \
  -Pkotlin.compiler.execution.strategy=in-process \
  -Pkotlin.incremental=false \
  "$@"