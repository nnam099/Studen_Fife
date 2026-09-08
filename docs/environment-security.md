# Environment setup and credential rotation (P0/S0)

## Supported entry point and prerequisites

Use NestJS (`backend/src/main.ts`, `npm start` after build). Node 22.23.2 was
verified for the installed NestJS 12 packages; PostgreSQL 17 and npm are required.
The Express files under `backend/src/routes`, `middleware`, `server.js`, `db.js`
are legacy, not the Android API. Their JWT fallback keys were also removed;
they reuse the compiled central contract and require a build before use.
Do not run the legacy SQL schema against the NestJS database.

## Required configuration

`ConfigModule` reads shell/service environment first, then `backend/.env.local`,
then `backend/.env` when the backend is started from `backend/`.
Production should inject environment values through its deployment secret store;
do not copy development environment files into the deployment image.

| Variable | Contract |
| --- | --- |
| `JWT_SECRET` | Required access signing secret; independent random value, at least 32 characters, no surrounding whitespace |
| `JWT_REFRESH_SECRET` | Required refresh signing secret; same requirements, must differ from access secret |
| `JWT_ACCESS_TTL` | Optional; positive integer followed by `s`, `m`, `h` or `d`; default `15m` |
| `JWT_REFRESH_TTL` | Optional; same format; default `7d` |
| `DB_PASSWORD` | Required, nonblank; no password fallback |
| `DB_USERNAME`, `DB_NAME` | Required by local Compose; explicitly configure in production |
| `DB_HOST`, `DB_PORT` | Backend database host/port; template targets localhost port 55432 |
| `PORT` | Backend port, default 3000 |
| `NODE_ENV` | `development` locally; `production` on deployed service |

JWT validation runs before database initialization. Error messages identify only
variable names. Both token types use HS256; TTLs are converted centrally to
numeric seconds. Existing Android request/response fields are unchanged.
The minimum length and distinctness checks are not a substitute for randomness.

## Fresh local setup

From repository root:

```bash
node backend/scripts/init-local-env.cjs
docker compose --env-file backend/.env up -d postgres
cd backend
npm ci
npm run build
npm start
```

The generator creates `backend/.env` with exclusive creation and mode 0600.
Random credentials go directly to the file, never stdout, command arguments,
shell history or documentation. It refuses to overwrite an existing file.
The template has empty secret placeholders; never fill the tracked template.
If a local file already exists, preserve it and update it deliberately through
a local editor or password manager. Do not paste its contents into logs/chat.

Compose is **local-development only**: database port binds to 127.0.0.1.
Use the same `DB_USERNAME`, `DB_PASSWORD`, `DB_NAME` and `DB_PORT` for Compose
and backend. If using `.env.local`, point Compose at the corresponding complete
file too; Compose does not merge the two files in the same way as ConfigModule.
Do not run `docker compose config` without `--quiet` in shared logs: expanded
configuration contains secrets. Use `docker compose --env-file backend/.env config --quiet`.

An existing PostgreSQL volume keeps its existing credentials. Editing environment
variables does NOT change its database password. Do not remove the volume or
change a running database as part of setup. Coordinate any rotation separately.

Production must use separate generated keys/DB credentials, a private database
endpoint, HTTPS ingress and an appropriate database TLS/backup configuration.
This local Compose file is not a production deployment recipe.

## Verification

From `backend/`:

```bash
./node_modules/.bin/tsc --noEmit --incremental false -p tsconfig.json
npm test -- --runInBand
npm run test:auth:integration
```

`npm test` builds first. Jest launches isolated Node processes against the actual
CommonJS build because Jest 29 cannot directly load NestJS 12 ESM dependencies.
There are no JWT cryptography mocks. Rerun `npm test` after source changes;
watching only test files does not rebuild production source. Jest's coverage
report does not measure child-process production code; do not present it as
application coverage.

The integration runner requires PostgreSQL server binaries and a non-root user.
Its default `PG_BIN` is `/usr/lib/postgresql/17/bin`; set `PG_BIN` to the installed
server binary directory if different. It creates a temporary cluster with random
credentials, chooses localhost ports, runs migrations and HTTP auth requests,
then stops/removes only its own temporary cluster. It never uses `backend/.env`
or the project database. It also launches the compiled backend from a directory
without environment files and verifies missing/blank JWT variables cause a
nonzero exit before database initialization, without leaking values.
A restricted sandbox may require permission for process spawning/local sockets.

Do not test fail-fast by renaming/deleting the real local environment file.

## Manual rotation checklist — cần chủ dự án thực hiện

Untracking `.env` does not revoke credentials. Previously committed values remain
in Git history and existing clones. No deployed credentials were rotated by P0.

- [ ] Inventory every local, demo, CI, staging and production environment that
      consumed committed values; record only environment/owner and variable names.
- [ ] Generate and install a new independent `JWT_SECRET` in each affected
      environment. All access tokens signed by the old key become invalid.
- [ ] Generate and install a new `JWT_REFRESH_SECRET`. All refresh tokens signed
      by the old key become invalid; users must log in again after both rotations.
- [ ] Coordinate restarting all backend replicas so signing and verification use
      the same new configuration; never log old/new values or token bodies.
- [ ] If committed database credentials were used outside local development,
      rotate the database role credential through the database administrator,
      update deployment secret stores and connection pools, and verify service
      connectivity. Changing Compose variables alone does not rotate PostgreSQL.
- [ ] Verify an old access token returns 401 on a protected endpoint and an old
      refresh token returns 401 at `/auth/refresh`; verify new login/access/refresh
      succeed. Handle test tokens privately; record status codes only.
- [ ] Verify the old database credential fails on a NEW authenticated connection;
      verify the new one succeeds. Existing connections can survive rotation and
      are not evidence the old password remains valid.
- [ ] Check deployment configs, CI secrets and logs for obsolete references.
- [ ] Handle Git history cleanup as a separate coordinated task after backups;
      agree with all contributors before rewriting history or replacing clones.

## Follow-up backlog (not implemented)

P1: refresh session store with hashed tokens, rotation/reuse detection, revocation,
server-side logout and disabled-account checks. Current refresh tokens remain
stateless until expiry or a global key rotation.
