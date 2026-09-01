# CMS incremental sync: handoff for Claude

Updated: 2026-08-31  
Workspace: `D:\CMS`  
Status: Partially implemented in the working tree. Not compiled or runtime-verified.

## User request and scope

> Upon login, necessary data must be downloaded to populate screens. Screens must not refetch data from Supabase every time they are opened. Apps should fetch data only when the Refresh button is clicked. Refresh should download only newly created or updated data since the last fetch. Implement this on both desktop and mobile.

Login preload is the explicit exception to refresh-only fetching. Navigation, filter changes, screen recreation, and returning to a screen must read local data without triggering a download.

Scope is all six apps: Admin, Teacher, and Student on desktop and mobile. The user explicitly authorized mobile changes for this task.

The latest user request was to produce this Markdown handoff. Implementation was paused for that request. Continue from the existing working tree when asked; do not restart or assume the feature is finished.

## Protect the current workspace

There are many unrelated existing edits, including UI work, assets, reports, and documentation. Preserve them. There is no clean implementation-only commit.

Start with:

```powershell
git -c safe.directory=D:/CMS status --short
git -c safe.directory=D:/CMS diff --stat
git -c safe.directory=D:/CMS diff --check
```

Use focused diffs before changing files. Do not reset the repository, restore entire directories, remove the existing `tmp/` directory, or blanket-ignore Markdown/PDF documentation.

Keep Supabase credentials out of logs. No live migration or deployment was completed during this work.

## Required data flow

1. Resolve authentication and role, then preload data permitted for that account.
2. Persist data locally. Screens query or observe the cache.
3. Explicit Refresh runs synchronization and updates the local state.
4. Keep checkpoints per account, table, and query scope. One session's cursor must not suppress another session's first download.
5. Query with an inclusive `updated_at >= lastUpdatedAt` boundary. Equal-timestamp replay is intentional and must be idempotent.
6. Merge by stable keys, applying both updates and soft-delete tombstones. Preserve rows outside the refreshed scope.
7. Save the delta successfully before advancing the checkpoint.
8. Update cache after a successful mutation using submitted data or its server response, without an unrelated full-table refetch.

Authentication maintenance is separate from screen-data fetching. Returning a row from an INSERT/UPDATE response is not the same as issuing a separate SELECT on screen opening.

## Source changes already made

These are implementation changes, not confirmed runtime behavior.

### Shared code

- Added `core/src/main/kotlin/com/mbd/cmscommon/data/sync/IncrementalSync.kt`: paginated delta fetching, inclusive boundaries, stable-key merging, and tombstone removal. Default page size is 500.
- Extended repository contracts with explicit synchronization methods where needed.
- Added `core/src/main/kotlin/com/mbd/cmscommon/data/mapper/StudentProfileMapper.kt`.
- Changed several shared controllers to use cache-only initial loading and explicit remote refresh.
- Added `core/src/test/kotlin/com/mbd/cmscommon/data/sync/IncrementalSyncTest.kt`. Tests cover pagination, equal-timestamp boundaries, failed-page checkpoint retention, and merging. They have not executed.

Important: the shared fetch helper currently advances the checkpoint after downloading all pages, before the caller persists those rows. Fix this before claiming reliable incremental synchronization.

### Mobile

Room database version is now 33:

- `MIGRATION_31_32` creates local `table_sync_state`.
- `MIGRATION_32_33` adds nullable `profileJson TEXT` to `session_students`.
- All three mobile database modules use the migration list.
- Full student profile JSON is cached, with a basic-profile fallback for old rows.

Cache-only reads and incremental synchronization were added or adapted for calendar events, datesheets/slots, fines, mark-edit requests, fees/heads, attendance, marks/GPA, timetables, sessions/students, curriculum, notifications, student-link requests, and exam papers. Several mappers were corrected to carry tombstone fields.

The comprehensive `AdminDataBootstrapper.refreshAll()` was wired into role startup paths. Its name is historical; it is now used beyond Admin. Mobile Admin has an account/process guard against repeated bootstrap from normal recomposition.

Mobile semester terms currently use an in-memory map. This remains unfinished persistence work.

### Desktop

`DesktopBootstrapSnapshotStore` now supplies durable JSON snapshots and implements `SyncCheckpointStore`. It stores checkpoints in `sync-checkpoints.json`, normally under `APPDATA/CMSDesktop/<appId>/cache`.

Active dependency bindings were changed to cache-first repositories for the main data areas. The administrator repository was also converted. Desktop role startup paths use the comprehensive bootstrapper.

Student's top-level Refresh now synchronizes before recreating displayed state; previously it only delayed and refreshed the UI.

This is JSON persistence, not desktop SQLite/SQLDelight. Historical planning notes from August 20 recorded a preference for full desktop SQLite persistence using SQLDelight. That older preference was not re-confirmed during this continuation. Flag the difference instead of claiming the earlier storage decision was implemented or silently changing architecture again.

### Navigation and mutation cleanup

Removed remote sync calls from these initial-load/selection paths:

- Shared marks-entry assignment/exam-type selection and exam-paper selection.
- Mobile datesheet subject loading.
- Desktop attendance-history roster loading and attendance-records department/session selection.
- Desktop Teacher/Admin notification startup effects.
- Desktop Admin profile, Departments, unlinked-student, and Teacher profile initial refresh effects.

Several mutations now update cache directly: administrator creation, session edits, attendance writes, fees/heads, datesheets/slots, teacher status, timetable edits, and GPA recording.

The last applied patch was checked in source:

- Mobile mark-edit request sync now includes all statuses, allowing remotely reviewed requests to leave the pending list.
- Mobile timetable upsert caches the server-returned row, including its real ID.
- Desktop timetable keys use session/day/normalized start time rather than switching between synthetic keys and server IDs.

These last changes have not compiled or been exercised at runtime. A final audit of remaining screen and mutation paths is still required.

## Supabase migration: prepared, not applied

File:

`supabase/migrations/20260830000001_incremental_sync_updated_at_triggers.sql`

It adds missing audit/tombstone columns, updated-time indexes, and timestamp touch triggers on existing tables. It reuses `public.fn_touch_updated_at()`.

Covered tables:

```text
profiles, departments, teachers, academic_sessions, session_subjects,
semester_terms, session_students, timetable_periods, period_sessions,
session_attendance, session_marks, mark_edit_requests, student_semester_gpa,
session_fees, session_fee_heads, fee_overrides, fines, datesheets,
datesheet_slots, calendar_events, exam_paper_submissions,
student_link_requests, notifications
```

Review it against the actual schema, existing policies, and deletion paths before applying. Adding audit columns does not make physical deletes emit tombstones. Do not weaken RLS to make bootstrap succeed.

Supabase guidance informed the filters and migration work. References consulted: [select](https://supabase.com/docs/reference/kotlin/select), [filters](https://supabase.com/docs/reference/kotlin/using-filters), [gte](https://supabase.com/docs/reference/kotlin/gte), and [order](https://supabase.com/docs/reference/kotlin/order). Check the installed library version when changing API calls.

## Remaining work, in priority order

### 1. Commit checkpoints after cache persistence

In `fetchIncrementalDelta()`, checkpoint advancement precedes the caller's Room/JSON write. A failed local write can therefore cause future refreshes to skip uncached rows.

Move checkpoint advancement after successful persistence, for example through an apply-delta callback or explicit commit operation. Update every helper caller and its tests. Some mobile repositories use independent paginated loops that already write pages first; inspect them separately.

Partial cache application after a failed refresh is acceptable if retry remains safe. Duplicate replay is preferable to missing rows.

### 2. Prevent desktop merge races

Desktop bootstrap runs parallel downloads for departments, sessions, student fines, course papers, and datesheet slots. Different scopes often write the same JSON file.

`DesktopBootstrapSnapshotStore.writeTextSafely()` uses a fixed `<filename>.tmp` path. The checkpoint lock does not protect repository read/merge/write sequences. Concurrent writes can collide or overwrite another scope's rows.

Protect the full read/merge/write operation. Locking only the rename is insufficient. Serializing same-repository bootstrap work helps, but also examine explicit refresh and mutation concurrency.

Desktop bootstrap also calls notification sync for ADMIN, TEACHER, and STUDENT in parallel. Desktop notification sync currently uses one global authorized scope, making those calls redundant and subject to the same race.

### 3. Verify deletion, transitions, and stable keys

Test the final mark-edit and timetable fixes above. Also audit:

- APPROVED/REJECTED requests arriving from another client.
- Tombstones reaching the cache.
- Physical deletes, particularly fee-head replacement. Timestamp deltas cannot discover a disappeared row without a tombstone/change log.
- The local marks cache after approving an edit request; changing remote marks and removing the pending request must not leave displayed marks stale.
- Queries whose status or visibility filters exclude rows after they change, leaving stale cached entries.
- Timetable save/refresh/remove behavior, time normalization, and IDs across app restart.

### 4. Make paging deterministic

Several queries order only by `updated_at`. Add an appropriate stable secondary key where needed, using the actual table schema.

Test more than 500 rows sharing timestamps. Consider concurrent updates during offset pagination too; deterministic ordering alone does not provide a fixed database snapshot.

### 5. Verify login gating, failures, and account isolation

Audit startup for all six apps, especially mobile Teacher and Student. Earlier source inspection suggested a cached role can set `authChecked` before bootstrap completes. Check fresh sign-in when the root ViewModel survives the login screen.

Do not open empty screens prematurely or rerun preload on ordinary navigation. Preserve usable cached data after bootstrap failure and expose an honest retry/error state. Inspect callers that ignore the Boolean result from `refreshAll()`.

Checkpoints are account-scoped, but desktop row snapshots are app-scoped. Verify logout/account switching so one account cannot see another account's cached content. Keep local visibility consistent with role and RLS scope.

### 6. Resolve Insights and semester-term exceptions

Insights still downloads whole reporting-view snapshots during login/explicit Refresh because the views lack a suitable update cursor. Navigation is cache-only, but this still violates the changed-rows-only refresh requirement.

Consider deriving reports from synchronized local base tables or defining a suitable incremental reporting source. Do not hide the exception.

Mobile semester terms live in an in-memory map. A persisted checkpoint with non-persisted rows can leave the cache incomplete after restart. Make row persistence and checkpoint lifetime consistent.

### 7. Complete the remote-read audit

Inspect UI, ViewModels, controllers, and repositories for direct reads, sync in startup/navigation effects, getters that fetch remotely, and mutation-triggered full refreshes.

```powershell
rg -n 'fetchIncrementalDelta|checkpointStore\.upsert' core desktop-shared mobile-shared
rg -n 'LaunchedEffect|init\s*\{|sync\(|syncSession|syncStudents|refresh\(' core desktop-shared mobile-shared mobile-admin mobile-teacher mobile-student
rg -n 'postgrest|SupabaseTables|\.select\s*\{' core desktop-shared mobile-shared
```

Classify matches before changing them: login preload, explicit Refresh, authentication, mutation response, or unintended screen-data fetch.

## Where to look

Paths are relative to `D:\CMS`.

| Area | Location |
| --- | --- |
| Shared sync | `core/src/main/kotlin/com/mbd/cmscommon/data/sync/` |
| Shared controllers | `core/src/main/kotlin/com/mbd/cmscommon/controller/` |
| Shared tests | `core/src/test/kotlin/com/mbd/cmscommon/data/sync/IncrementalSyncTest.kt` |
| Desktop cache | `desktop-shared/src/main/kotlin/com/mbd/cmsdesktop/data/cache/DesktopBootstrapSnapshotStore.kt` |
| Desktop bootstrap | `desktop-shared/src/main/kotlin/com/mbd/cmscommon/data/sync/AdminDataBootstrapper.kt` |
| Desktop repositories | `desktop-shared/src/main/kotlin/com/mbd/cmsdesktop/data/repository/` and `desktop-shared/src/main/kotlin/com/mbd/cmscommon/data/repository/` |
| Mobile bootstrap | `mobile-shared/src/main/java/com/mbd/cmscommon/data/sync/AdminDataBootstrapper.kt` |
| Mobile repositories | `mobile-shared/src/main/java/com/mbd/cmscommon/data/repository/` |
| Mobile Room | `mobile-shared/src/main/java/com/mbd/cmscommon/data/local/` |
| Migration | `supabase/migrations/20260830000001_incremental_sync_updated_at_triggers.sql` |

Check active dependency-injection bindings. Updating an unused repository does not change the app.

## Verification status

Completed at earlier source checkpoints:

- Repeated `git diff --check` runs passed, with line-ending warnings.
- Focused searches found no direct Supabase reads in the inspected UI files.
- Several shared initial-load paths were confirmed to use cache or `fetchRemote = false`.

These are static checks, not build or behavior tests. Rerun them after edits.

Attempted:

```powershell
.\gradlew.bat :core:test --offline --no-daemon --stacktrace
```

Gradle failed before Kotlin compilation:

```text
java.io.IOException: Unable to establish loopback connection
Caused by: java.net.SocketException: Invalid argument: connect
```

Matching `JAVA_OPTS` and `GRADLE_OPTS` to the configured JVM settings did not resolve it. The configured JDK was Eclipse Adoptium 21 at `C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot`.

No new tests have executed. Kotlin/Room compilation, desktop/Android builds, database migration execution, device/emulator behavior, live desktop behavior, and network request counts remain unverified.

## Acceptance checks

Run relevant builds/tests, then exercise Admin, Teacher, and Student on both platforms.

- Login preloads the complete permitted data needed by screens.
- Repeated navigation and filter changes issue no extra screen-data SELECTs.
- Cache and checkpoints remain coherent after restart.
- Refresh brings in new/edited rows without duplicates and applies tombstones/status transitions.
- No-change refresh does not download full tables; inclusive boundary replay is expected.
- More than one page of changes, including equal timestamps, is retained.
- Later-page network failure and local-write failure both leave retry-safe checkpoints.
- Concurrent refreshes cannot lose rows, regress checkpoints, or corrupt files.
- Successful writes update displayed local data without a full-table refetch.
- Scoped refresh preserves other sessions' rows.
- Logout/account switching does not expose previous-account data.
- Insights and semester terms satisfy the same refresh/persistence promise, or the user explicitly accepts an exception.
- Existing mobile databases upgrade through versions 31, 32, and 33 without deleting user data.

## Temporary files and tool failures

Root-level `.codex-desired-*.kt`, `.codex-fix-*.kt`, and `.codex-*.patch` files were generated by the patch fallback workflow. They are not application source. Inspect exact targets before removing confirmed temporary artifacts; preserve unrelated documents and `tmp/`.

The normal edit helper repeatedly failed with:

```text
windows sandbox failed: helper_unknown_error: setup refresh had errors
```

Checked Git patches were used as a fallback. Shell commands also ran slowly. Do not mistake an environment failure for a successful compile.

## Suggested continuation

Fix checkpoint ordering and desktop merge races first. Then verify the final consistency patches, login/account behavior, and remaining reporting/persistence exceptions. Review the migration against the schema, get builds running, and execute the acceptance checks.

Report implementation status, migration status, tests that actually passed, and untested behavior separately.
