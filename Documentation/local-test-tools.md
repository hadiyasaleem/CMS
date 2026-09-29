# Local test tools (scratch Postgres + Deno)

Throwaway tooling for testing the SQL migrations and the Supabase edge functions **without touching a real
Supabase project**. Everything is installed under `<repo>/.testtools/` (gitignored) — delete the folder to remove it.

| Tool | Used for |
|---|---|
| Embedded PostgreSQL 18 (npm `embedded-postgres`, port **54329**) | Replaying `supabase/migrations/*.sql` and provoking real database errors |
| Deno (npm `deno`) | Running the edge functions' handlers against a fake Supabase |

## One-time install

From the repository root:

```bash
npm install --prefix .testtools/node deno embedded-postgres pg
```

(`.testtools/` is in `.gitignore`. Nothing else is installed system-wide.)

## Scratch database: migrations and error messages

```bash
# 1. start Postgres (leave it running; data lives in .testtools/pgdata)
node supabase/tests/local/pg-server.mjs

# 2. in another terminal: recreate cms_test and replay every migration
CONTINUE=1 node supabase/tests/local/apply.mjs

# 3. provoke each failure a user can hit and record what Postgres says
node supabase/tests/local/scenarios.mjs

# 4. run the Kotlin side over the captured errors
./gradlew :core:test --tests "*DatabaseScenarioMessagesTest"
```

- `supabase/tests/local/shim.sql` stands in for the Supabase-managed objects the migrations depend on (`auth.*`,
  `storage.*`, `cron.schedule`, the `anon` / `authenticated` / `service_role` roles).
- `apply.mjs` stops at the first failing migration unless `CONTINUE=1` is set.
- **Known replay failures (pre-existing):** `20260715070000_guard_profile_exempt_service_role.sql` (trigger
  already exists) and `20260729180000_mark_edit_requests_baseline.sql` (`updated_at` column missing). Both are
  backfilled copies of migrations applied live, so a from-scratch replay is not faithful for them; the new
  migrations after them apply cleanly.
- `scenarios.mjs` writes `.testtools/run/scenario-results.json` (and `db-names.txt`, every constraint/index/table
  name). `DatabaseScenarioMessagesTest` reads them and is **skipped** when they are absent, so it costs nothing on a
  machine without the tools. It checks that every provoked error reads as a plain sentence, that raised trigger text
  is shown verbatim, and that every constraint name `ConstraintMessages` words specially still exists.
- Add a scenario to `scenarios.mjs` whenever a new trigger/constraint message is written.
- `node supabase/tests/local/q.mjs "select ..."` runs an ad-hoc query against `cms_test`.

## Edge functions

```bash
export PATH="$PATH:$PWD/.testtools/node/node_modules/.bin"
deno test --allow-net --allow-env --allow-read supabase/functions/_tests/
deno check supabase/functions/*/index.ts supabase/functions/_shared/auth.ts
```

`supabase/functions/_tests/error_contract_test.ts` imports each function's real handler and runs it against an
in-process fake of Supabase auth, PostgREST and storage. It asserts the error contract
`{ "error": "<plain sentence>", "code": "<CODE>" }` for auth failures, validation, missing records, database errors,
auth-service errors, and outages (an outage must be a 500, never "not an admin" or "no such teacher").

To also run the Kotlin parser (`EdgeFunctionErrors`) over the real bodies:

```bash
EDGE_CAPTURE="$PWD/.testtools/run/edge-captures.jsonl" deno test --allow-net --allow-env --allow-read --allow-write supabase/functions/_tests/
./gradlew :core:test --tests "*DatabaseScenarioMessagesTest"
```

## What this does NOT cover

- Row-level-security behaviour beyond the few RPC calls in `scenarios.mjs` (the shim's `auth.uid()` reads the
  `request.jwt.claim.*` settings the script sets).
- Real GoTrue, Storage and PostgREST servers — the fakes only reproduce the response shapes the functions read.
- Anything on the real project: applying the migration and deploying the functions remain separate, deliberate steps.
