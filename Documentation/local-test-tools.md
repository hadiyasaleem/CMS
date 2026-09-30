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
- **Replay is clean.** All 41 migrations apply from scratch with no failures. Two backfilled copies of
  live-applied migrations used to fail and were made replayable with statements that are no-ops on the live
  database: `20260715070000_guard_profile_exempt_service_role.sql` (`drop trigger if exists` before creating
  `trg_guard_profile`) and `20260729180000_mark_edit_requests_baseline.sql` (`add column if not exists updated_at`,
  `drop trigger if exists`, `create index if not exists`). A replayed `mark_edit_requests` has the same columns,
  indexes and triggers as production. If `apply.mjs` prints a `FAIL` line, a new migration is not replayable.
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

## Known drift from production

`supabase/tests/local/drift.sql` fingerprints every public function, trigger, policy, index, table (columns and
constraints), enum, view and RLS flag. Run it on a scratch replay and on production and diff the outputs.

Last comparison (read-only queries against project `ygmvyvjhdkxddkqxtrdw`, after the humanize-errors migration): the
repo replay matched production for all columns, constraints, enums, views and RLS flags, and for every function,
trigger, policy and index **except** the four items below, which existed only on production and are backfilled by
`20260930010000_backfill_live_only_drift.sql` (a no-op on production):

| Object | Production | Repo replay (before the backfill) |
|---|---|---|
| `roll_block_error` | live migration `fix_single_shift_roll_block`: single-shift sessions only need serial 1..max; the Morning/Evening ranges apply to `BOTH` only | older body that applied the shift ranges to every session |
| policy `session_marks.upd_marks` | **admin only** (locked marks; teachers correct through a mark edit request) | admin **or** the class's teacher — a fresh database let teachers overwrite saved marks |
| policy `mark_edit_requests.adm_mark_edit_requests` | not limited to `authenticated` (same effect) | `to authenticated` |
| indexes `idx_session_attendance_updated_entity`, `ux_session_attendance_entity_id` | present (pagination tie-breaker on `entity_id`) | missing |

Production also has a data-only migration, `bump_english_session_capacity` (`update academic_sessions set max_students
= 100 where session_id in ('eng_2023','eng_2024')`), which is deliberately not replayed.

### Production snapshot vs. what the migrations intend (grants, storage, cron, auth)

The scratch database can't reproduce these (it lacks Supabase's default privileges and storage/cron internals), so
they were checked by reading production directly (read-only queries) and comparing with
`20260714000003_storage_cron.sql`, `20260714000004_harden_functions.sql` and the later revoke migrations.

| Area | Result |
|---|---|
| Storage buckets | `documents` (10 MB, PDF+JSON), `exam-papers` (5 MB, PDF), `photos` (1 MB, JPEG/PNG/WebP), all private — **match** |
| Storage policies (`storage.objects`) | All 8 exist and match the migration (papers ×4, photos ×2, documents ×2) — **match** |
| `pg_cron` | `purge-expired-notifications` (02:15) and `flag-ended-sessions` (02:30), both active — **match** |
| Trigger on `auth.users` | `trg_on_auth_user_created` → `fn_handle_new_user()` — **match** |
| Row-level security | enabled on all 27 public tables — **match** |
| Table grants | every public table grants Supabase's default privileges (including `TRUNCATE`) to `anon` and `authenticated`. Access is therefore governed entirely by RLS (which is on everywhere); PostgREST does not expose `TRUNCATE` — **standard Supabase, no action** |
| Function grants | `approve_*`, `record_semester_result`, `available_roll_numbers`, the `my_*`/`teach*` RLS helpers: `authenticated` + `service_role` only, `anon` revoked — **match** (the advisor's "SECURITY DEFINER executable" warnings for these are intentional: policies call them and the `approve_*`/`record_*` functions check the caller inside) |

Deviations from the migrations' *intent* (none is an exploitable exposure; nothing was changed on production):

| Function | Intent | Production | Why it is harmless |
|---|---|---|---|
| `fn_touch_updated_at()`, `fn_enforce_roster_cap()`, `fn_check_timetable_conflict()` | revoke from `public, anon, authenticated` (0004) | still executable by `anon` and `authenticated` (they were re-created later and picked up Supabase's default grants) | trigger functions cannot be called through the API, and `EXECUTE` is only checked when a trigger is *created* |
| `bootstrap_admin_email()` | revoke from `public, anon` | executable by `anon` and `authenticated` | returns the constant `admin@example.com` |
| `audit_actor()`, `fn_cms_audit_row()` | (created in the audit-metadata migration without a revoke) | executable by `PUBLIC` | `audit_actor()` returns the caller's own JWT email; `fn_cms_audit_row()` is a trigger function. Do **not** revoke `audit_actor()` from `authenticated`: the audit trigger runs as the caller and needs it |

If you want the intent enforced, this is safe to review and apply (it does not touch `audit_actor`):

```sql
revoke all on function fn_touch_updated_at(), fn_enforce_roster_cap(), fn_check_timetable_conflict() from public, anon, authenticated;
revoke all on function bootstrap_admin_email() from public, anon;
```

**Bootstrap admin.** `is_admin()`, `requireAdmin` in the edge functions and the storage policies all treat the account
`admin@example.com` as a permanent admin. That account exists and its email is confirmed, so nobody can register it
again. `example.com` is a placeholder domain with no mailbox, so a forgotten password can't be reset by email — keep its
password in a password manager, or replace the bootstrap address with a real one (it appears in
`bootstrap_admin_email()` and in `supabase/functions/_shared/auth.ts`).

**Migration versions.** Repo file names and the versions recorded on production differ for every migration (production
uses the time it was applied, e.g. the humanize-errors migration is `20260929…` there and `20260930000000` here). Do not
run `supabase db push` blindly: it would treat the repo files as unapplied. Apply reviewed SQL deliberately instead.

## What this does NOT cover

- Row-level-security behaviour beyond the few RPC calls in `scenarios.mjs` (the shim's `auth.uid()` reads the
  `request.jwt.claim.*` settings the script sets).
- Real GoTrue, Storage and PostgREST servers — the fakes only reproduce the response shapes the functions read.
- Anything on the real project: applying the migration and deploying the functions remain separate, deliberate steps.
