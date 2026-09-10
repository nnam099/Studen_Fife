# S1: local checks and GitHub Actions

## Toolchain

- Node **22.23.2**, pinned in `.node-version` and `backend/package.json`/lockfile.
  Nest core's installed lockfile requires Node >=20; CLI requires >=20.11.
  This exact Node version was already used by S0 and has an official distribution.
- npm 10.9.8 was used locally. `npm ci` uses the committed lockfile. The existing
  TypeScript 5.9.3 / Nest schematics 12 peer warning is visible, not suppressed;
  build and tests pass. Dependency upgrades are outside S1.
- JDK **17**: local verification used OpenJDK 17.0.15-ea; CI selects Temurin 17.
  Set `JAVA_HOME`; the entry point overrides the machine-specific
  `org.gradle.java.home` property on the command line without editing Android.
- Gradle 8.2 wrapper, AGP 8.2.2, Kotlin 1.9.22, SDK platform 34/build-tools 34.0.0.
- Python 3, PostgreSQL server binaries and a non-root account for integration.
  Local validation uses PostgreSQL 17.11. Ubuntu 24.04 CI installs PostgreSQL 16
  from its distribution repository and explicitly selects `PG_BIN`; this CI
  version will be verified when the workflow runs. No production DB is used.

Actions are pinned to verified tag commit SHAs, with source version comments in
`.github/workflows/ci.yml`. Wrapper JAR and distribution checksums are pinned from
https://services.gradle.org/distributions/gradle-8.2-wrapper.jar.sha256 and
https://services.gradle.org/distributions/gradle-8.2-bin.zip.sha256.
Upgrade version and checksum together, after reviewing the official source.

## Local commands (repository root)

```bash
python3 scripts/ci.py guard
python3 scripts/ci.py backend
JAVA_HOME=/path/to/jdk-17 ANDROID_HOME=/path/to/android-sdk python3 scripts/ci.py android
```

`all` runs backend then Android and stops on the first failure. For the backend,
select Node from `.node-version`, e.g. `nvm install "$(cat .node-version)"` and
`nvm use "$(cat .node-version)"`. For standalone integration after installing deps:

```bash
cd backend
PG_BIN=/path/to/postgresql/bin npm run test:auth:integration
```

The runner performs `npm ci`, typecheck, a build via `npm test`'s pretest hook,
Jest unit tests, then a separately built auth integration suite. Android runs
wrapper verification, `testDebugUnitTest` and `assembleDebug`. Exit codes are
preserved and failures are not ignored. No source, Git index or local `.env`
is changed. Dependency/build outputs are expected to change.

No Docker or Podman daemon is needed: integration creates a fresh PostgreSQL
cluster under a unique temporary directory, uses generated credentials and local
ports, waits for PostgreSQL readiness, migrates a fresh database, then cleans up.
`PG_BIN` allows installed PostgreSQL distributions; the binary directory must
contain `initdb`, `pg_ctl` and `pg_isready`. SIGINT/SIGTERM request cleanup; a
forcibly killed process/host cannot guarantee cleanup. Hosted CI runners are
fresh each run and no DB data is cached. The S0 Docker/Podman local database
configuration is unrelated to the test database.

## Environment independence and guards

Checks do not need JWT or DB credentials from the developer. The entry point
removes JWT/DB credential environment overrides and preloads a Node guard that
fails if checks try to read an existing non-template `.env` file. Integration
runs the backend in an empty temporary working directory with generated test
configuration and only compiled assets linked in.

Repository guards inspect the Git index, reject tracked `.env`/`.env.*` except
`*.example` templates, require the tracked `backend/.env.example`, require empty
secret placeholders, and reject known JWT fallback/literal-key patterns in
runtime source. Reports contain paths, line numbers and violation kinds, never
matching source lines or values. Five disposable-fixture regression tests cover
allowed templates, forbidden environment names, missing template, fallback keys
and the local-env read guard. This is a targeted guard, not a general guarantee
against all secret formats or secret material already in Git history.

## GitHub workflow

Triggers: pull requests targeting `main`, pushes to `main`, manual dispatch.
Backend and Android are independent jobs with 20/30-minute timeouts. Permissions
are `contents: read`; checkout does not persist credentials; newer runs cancel
older ones on the same PR/ref. Only npm dependency cache is enabled. No APK,
logs, DB files or environment artifacts are uploaded; no deploy/publish steps.
The Android job installs SDK 34 and checks the same commands as local.

Not covered: emulator/instrumentation, UI rendering/accessibility, live production
connectivity, release signing/deployment, refresh revocation, comprehensive
business API regression or application-wide coverage. The JWT Jest suite uses
Node subprocesses, so Jest coverage is not source coverage of those subprocesses.
The security/CI commit includes the existing auth serialization test only.
The developer working tree additionally has 3 finance helper tests and UI edits;
these are excluded from the security/CI commit. `FinanceUiTest.kt` and
`StudentTheme.kt` require a separate reviewed Android commit. Working-tree test
counts must not be presented as committed-source or GitHub test counts.

## Clean-room export validation (not a clean checkout)

From the root, create a new empty directory and export proposed source:

```bash
ci_export_dir=$(mktemp -d /tmp/student-life-source.XXXXXX)
python3 scripts/export-source.py "$ci_export_dir"
cd "$ci_export_dir"
python3 scripts/ci.py backend
JAVA_HOME=/path/to/jdk-17 ANDROID_HOME=/path/to/android-sdk python3 scripts/ci.py android
```

Export includes working copies of tracked files and nonignored new source.
It excludes Git metadata, local env, dependency/build directories, IDE state,
local.properties, npm config and DB data. A generated source manifest lets the
guard operate without `.git`; it is NOT a GitHub index check and must never be
presented as one. Review export inventory if new personal files are added.
Dependencies may download again or use shared dependency caches; no existing
build output is copied. Existing SDK/JDK installations are external toolchains.
Do not broadly delete `/tmp`; remove only the exact directory you created after
reviewing its path. No cleanup command is automatically run against user paths.

A successful export run is **clean-room export validation**, not proof that
GitHub checkout passed. Only a reviewed commit pushed to GitHub provides that
final evidence. Include every new P0/S1 file from the commit inventory.

## Updating existing local JWT configuration safely

The current local `.env` is intentionally untouched and may fail the stronger
S0 validation. CI does not depend on fixing it. To update it yourself:

1. Make a timestamped, permission-0600 backup OUTSIDE the repository, using a
   local file manager/password manager; verify the backup exists before editing.
2. Keep DB settings for an existing DB unchanged unless coordinating its rotation.
3. Generate two independent random keys directly into a private local file or
   password manager and transfer them with a local editor into `JWT_SECRET` and
   `JWT_REFRESH_SECRET`. Never put key literals in command arguments/history.
4. `backend/scripts/init-local-env.cjs` is for a NEW environment and refuses to
   overwrite `.env`. Do not delete or replace the existing file just to run it:
   generate in a separate disposable copy of the template/script if needed, then
   copy ONLY the two JWT fields locally after backup. Its new DB password does
   not rotate or match an existing database automatically.
5. Keep the file ignored; do not paste keys into chat, commit or shared logs.
   Coordinate deployed key rotation using `docs/environment-security.md`.

The security/CI baseline must be reviewed independently of unfinished Android
changes. GitHub Actions results apply only to the specific committed source.
