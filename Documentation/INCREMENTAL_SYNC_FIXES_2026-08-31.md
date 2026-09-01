# Incremental synchronization fixes

Status: source changes only. No Gradle builds, test execution, migration application, or runtime checks were run at the user's request.

## 1. Checkpoint ordering

Changed `fetchIncrementalDelta` so it now calls the caller's cache-persistence function before it saves the checkpoint. A failed local cache write now prevents checkpoint advancement, so the next refresh replays the same inclusive timestamp boundary instead of skipping uncached rows.

Updated the active helper callers:

- `SessionFeeRepositoryImpl`: fee and fee-head Room deltas are persisted in the helper callback before their respective checkpoints advance.
- `CurriculumRepositoryImpl`: semester-term cache updates occur in the helper callback before the term checkpoint advances.
- `IncrementalSyncTest`: existing helper calls now supply the persistence callback. Tests were not run.

## Next handoff item

Desktop snapshot updates still need atomic read-merge-write handling. The store's fixed temporary filename and repository-level merge sequences are being addressed separately.
## 2. Desktop temporary-file collision

Changed DesktopBootstrapSnapshotStore to create a unique temporary file for each snapshot write and clean it up if a move fails. Concurrent writers no longer overwrite the same <snapshot>.tmp file. The repository-level atomic merge change remains next.

### Atomic desktop cache primitive

Added `DesktopBootstrapSnapshotStore.updateRows`, which performs one snapshot file's read, transformation, and write under a per-file lock. Repositories must use this method for merge and mutation paths; this conversion is in progress.

### Notification scope and merge

Desktop notification checkpoints are now scoped by role, session, and department, and notification cache merges use the atomic snapshot operation.

### Regression coverage added

Added an unexecuted test proving a failed local cache application leaves the checkpoint unchanged.
Deterministic desktop paging: added stable secondary ordering after updated_at to the changed department, session, and notification delta queries.

## Insights local derivation

Selected by the user: Insights now derives session overviews, at-risk students, and exam statistics from synchronized desktop snapshot files. sync() only recalculates and stores local report snapshots; it performs no reporting-view query.

Mobile Insights now derives the same three report types from existing Room base tables; no Supabase reporting-view reads remain in its sync path.
