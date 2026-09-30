# Remote write audit: updates that can succeed while changing nothing

PostgREST reports a successful `UPDATE`/`DELETE` that matched **zero rows** as success. Row-level security makes that
easy to hit: a row the caller can't update is silently filtered out, not refused. The apps then often update their local
cache and tell the user it worked. This audit reads every remote write in the repositories and checks it against the
**production** policies (read-only queries, 2026-09-30).

**Status (fixed in code, migration still to be applied):** both bugs and the false-success cases below are fixed in the apps.
`supabase/migrations/20260930030000_delete_notification_and_ingest_app_logs.sql` adds `delete_notification` and `ingest_app_logs`; it is
**not yet applied to production** and must be applied *before* the new app versions are released (until then notification delete and log
upload report an out-of-date-app error). Guarded writes use `requireAffected` (`core/.../util/RowsAffected.kt`): the soft-deletes of
calendar events, fines, buildings, rooms, departments, sessions, students, subjects, exam papers, datesheets/papers and timetable periods, the
mark-edit approve/reject (an approval is no longer recorded unless the score changed), attendance-edit reject and link-request reject. A stale
local copy is dropped when the server row is gone, and the user is told "That item was already changed or removed." `linkStudent` and
`unlinkStudent` were deleted.

**Scope:** 58 non-insert writes (44 `update`, 13 `upsert`, 1 `delete`) in `core`, `mobile-shared` and `desktop-shared`.
Plain inserts are excluded: a rejected insert raises an error, so it can't fail silently.

## Result

| Verdict | Count | Meaning |
|---|---|---|
| **BUG** | 1 | Always affects zero rows in production |
| **BUG (latent)** | 1 | Fails under a specific, plausible condition |
| Watch | 3 | Correct today; a concurrent change can produce a false success |
| Low | 2 | Zero rows only if a permission changes mid-session |
| Dead code | 2 | No callers |
| OK | 49 | Admin-only or owner-only actions where zero rows just means "already gone" |

### 1. Notification delete never reaches the server (BUG)
`BaseNotificationRepository.delete` (used by mobile and desktop) sets `is_deleted = true` with an `UPDATE`, but
`notifications` has **no UPDATE policy** in production or in the migrations (only INSERT, SELECT and DELETE), so the row
is never changed. The app then removes it from the local cache and `NotificationsController` shows "Notification
deleted." Everyone else still sees it, and it comes back for the author on a fresh install or cache clear.

Reproduced on a from-scratch replay of the migrations: as an admin, `update notifications set is_deleted = true` reports
`rowCount 0`, while `delete from notifications` reports `rowCount 1`.

**Recommended fix:** do not add a broad UPDATE policy (an author could then rewrite the target audience their own
`INSERT` policy restricts). Use a small definer function so only the soft-delete is possible, and call it with
`postgrest.rpc(...)`:

```sql
create or replace function delete_notification(p_id uuid) returns void
language plpgsql security definer set search_path = public as $$
begin
  update notifications set is_deleted = true
   where id = p_id and (is_admin() or created_by_email = current_email());
  if not found then
    raise exception 'You can only delete notifications you sent, and this one no longer exists.';
  end if;
end $$;
revoke all on function delete_notification(uuid) from public, anon;
grant execute on function delete_notification(uuid) to authenticated, service_role;
```

Keeping the soft delete (instead of switching to the existing DELETE policy) matters: other devices learn about a
deletion from the row's newer `updated_at`, which a hard delete would not produce.

### 2. A re-sent app log is refused (BUG, latent)
`AppLogRepositoryImpl.flush` upserts on `log_id` so that a log re-queued after a failed local delete does not raise a
duplicate-key error. `app_logs` has INSERT and SELECT policies only, so the conflict path fails: on a replay,
inserting an existing `log_id` as a signed-in user gives `42501 new row violates row-level security policy` for both
`DO UPDATE` and `DO NOTHING`. The flush is wrapped in `runCatching`, so the failure is silent and that batch is
retried forever, blocking the logs queued behind it.

**Recommended fix:** an ingest function that is idempotent server-side (again avoids widening the policies):

```sql
create or replace function ingest_app_logs(p_rows jsonb) returns void
language sql security definer set search_path = public as $$
  insert into app_logs (log_id, occurred_at, severity, kind, tag, message, stack_trace, account_email, app_id, app_version, platform, device_info)
  select r.log_id, r.occurred_at, r.severity, r.kind, r.tag, r.message, r.stack_trace, r.account_email, r.app_id, r.app_version, r.platform, r.device_info
    from jsonb_to_recordset(p_rows) as r(log_id text, occurred_at timestamptz, severity text, kind text, tag text, message text,
         stack_trace text, account_email text, app_id text, app_version text, platform text, device_info text)
   where r.account_email is null or r.account_email = current_email()
  on conflict (log_id) do nothing
$$;
revoke all on function ingest_app_logs(jsonb) from public, anon;
grant execute on function ingest_app_logs(jsonb) to authenticated, service_role;
```

### 3. The rest
- **Approving a mark edit** (`MarkEditRequestRepositoryImpl`, `MarkEditRequestRepositoryLocalImpl`): the score row is
  looked up first (this already removed the earlier stale-semester bug), so only a concurrent change reaches zero rows --
  but the request is then still marked APPROVED. Worth a guard.
- **Rejecting a link request** works for an admin or a teacher with `can_approve_link_requests`; if that permission is
  withdrawn while the screen is open, the reject silently does nothing.
- **`linkStudent` / `unlinkStudent`** have no callers; delete them.
- **Everything else** is an admin action on an admin-only table (or an owner-only action on `exam_paper_submissions`),
  where "0 rows" means the row was already removed. Harmless, but the local cache is still updated as if it had worked.

Both functions above were run against a from-scratch replay: an author deletes their own notification, an admin deletes any, a
non-author is refused with the message shown, re-ingesting a stored log is a no-op, and a log claiming another account's email is
not stored.

### A general guard
For the writes where 0 rows is meaningful (soft-deletes, approvals, rejections -- about 14 sites) ask PostgREST to
return the changed rows (`select()` on the update) and treat an empty result as
`CmsException.NotFound("That item was already changed or removed. Refresh and try again.")` before touching the local
cache. Put it in one helper next to `FailureSummary`/`orThrowValidation` so each repository stays a one-liner.

## Every site

| Site | Function | Table | Op | Verdict | Why |
|---|---|---|---|---|---|
| `AppLogRepositoryImpl.kt:30` | `flush` | `app_logs` | upsert | **BUG (latent)** | `app_logs` has INSERT and SELECT policies only, so re-sending an already-stored log (the case this upsert exists for) fails with an RLS error and blocks the rest of that flush batch |
| `BaseNotificationRepository.kt:152` | `delete` | `notifications` | update | **BUG** | No UPDATE policy exists on `notifications`, so this soft-delete matches 0 rows for everyone (admin included); the app then removes it locally and says "Notification deleted." |
| `AcademicSessionRepositoryImpl.kt:182` | `createSession` | `academic_sessions` | upsert | OK | Admin-only screen and admin-only policy; 0 rows only if the row is already gone (stale device), which is harmless |
| `AcademicSessionRepositoryImpl.kt:183` | `createSession` | `academic_sessions` | update | OK | Admin-only screen and admin-only policy; 0 rows only if the row is already gone (stale device), which is harmless |
| `AcademicSessionRepositoryImpl.kt:194` | `updateShiftMode` | `academic_sessions` | update | OK | Admin-only screen and admin-only policy; 0 rows only if the row is already gone (stale device), which is harmless |
| `AcademicSessionRepositoryImpl.kt:220` | `updateSessionDetails` | `academic_sessions` | update | OK | Admin-only screen and admin-only policy; 0 rows only if the row is already gone (stale device), which is harmless |
| `AcademicSessionRepositoryImpl.kt:240` | `deleteSession` | `academic_sessions` | update | OK | Admin-only screen and admin-only policy; 0 rows only if the row is already gone (stale device), which is harmless |
| `AttendanceEditRequestRepositoryImpl.kt:90` | `rejectRequest` | `attendance_edit_requests` | update | OK | Admin-only screen and admin-only policy; 0 rows only if the row is already gone (stale device), which is harmless |
| `BuildingRepositoryImpl.kt:79` | `deleteBuilding` | `buildings` | update | OK | Admin-only screen and admin-only policy; 0 rows only if the row is already gone (stale device), which is harmless |
| `CalendarRepositoryImpl.kt:45` | `deleteEvent` | `calendar_events` | update | OK | Admin-only screen and admin-only policy; 0 rows only if the row is already gone (stale device), which is harmless |
| `RecordsRepositoryImpls.kt:64` | `deleteEvent` | `calendar_events` | update | OK | Admin-only screen and admin-only policy; 0 rows only if the row is already gone (stale device), which is harmless |
| `DatesheetRepositoryLocalImpl.kt:112` | `deleteDatesheet` | `datesheet_slots` | update | OK | Admin-only screen and admin-only policy; 0 rows only if the row is already gone (stale device), which is harmless |
| `DatesheetRepositoryLocalImpl.kt:147` | `updateSlot` | `datesheet_slots` | update | OK | Admin-only screen and admin-only policy; 0 rows only if the row is already gone (stale device), which is harmless |
| `DatesheetRepositoryLocalImpl.kt:183` | `deleteSlot` | `datesheet_slots` | update | OK | Admin-only screen and admin-only policy; 0 rows only if the row is already gone (stale device), which is harmless |
| `DatesheetRepositoryLocalImpl.kt:70` | `updateDatesheet` | `datesheets` | update | OK | Admin-only screen and admin-only policy; 0 rows only if the row is already gone (stale device), which is harmless |
| `DatesheetRepositoryLocalImpl.kt:100` | `setPublished` | `datesheets` | update | OK | Admin-only screen and admin-only policy; 0 rows only if the row is already gone (stale device), which is harmless |
| `DatesheetRepositoryLocalImpl.kt:109` | `deleteDatesheet` | `datesheets` | update | OK | Admin-only screen and admin-only policy; 0 rows only if the row is already gone (stale device), which is harmless |
| `DepartmentRepositoryImpl.kt:79` | `deleteDepartment` | `departments` | update | OK | Admin-only screen and admin-only policy; 0 rows only if the row is already gone (stale device), which is harmless |
| `ExamPaperSubmissionRepositoryImpl.kt:70` | `uploadSubmission` | `exam_paper_submissions` | update | OK | Owner teacher or admin (`upd_papers`); the UI only offers a teacher their own papers |
| `ExamPaperSubmissionRepositoryImpl.kt:109` | `deleteSubmission` | `exam_paper_submissions` | update | OK | Owner teacher or admin (`upd_papers`); the UI only offers a teacher their own papers |
| `FineRepositoryImpl.kt:47` | `deleteFine` | `fines` | delete | OK | Admin-only screen and admin-only policy; 0 rows only if the row is already gone (stale device), which is harmless |
| `RecordsRepositoryImpls.kt:128` | `deleteFine` | `fines` | update | OK | Admin-only screen and admin-only policy; 0 rows only if the row is already gone (stale device), which is harmless |
| `MarkEditRequestRepositoryImpl.kt:71` | `approveRequest` | `mark_edit_requests` | update | OK | Admin-only screen and admin-only policy; 0 rows only if the row is already gone (stale device), which is harmless |
| `MarkEditRequestRepositoryImpl.kt:87` | `approveRequest` | `mark_edit_requests` | update | OK | Admin-only screen and admin-only policy; 0 rows only if the row is already gone (stale device), which is harmless |
| `MarkEditRequestRepositoryImpl.kt:97` | `rejectRequest` | `mark_edit_requests` | update | OK | Admin-only screen and admin-only policy; 0 rows only if the row is already gone (stale device), which is harmless |
| `MarkEditRequestRepositoryLocalImpl.kt:130` | `updateStatus` | `mark_edit_requests` | update | OK | Admin-only screen and admin-only policy; 0 rows only if the row is already gone (stale device), which is harmless |
| `SessionTimetableRepositoryImpl.kt:131` | `removePeriod` | `period_sessions` | update | OK | Admin-only screen and admin-only policy; 0 rows only if the row is already gone (stale device), which is harmless |
| `SessionTimetableRepositoryImpl.kt:158` | `setPeriodLink` | `period_sessions` | upsert | OK | Admin-only screen and admin-only policy; 0 rows only if the row is already gone (stale device), which is harmless |
| `SessionTimetableRepositoryImpl.kt:162` | `setPeriodLink` | `period_sessions` | update | OK | Admin-only screen and admin-only policy; 0 rows only if the row is already gone (stale device), which is harmless |
| `AcademicSessionRepositoryImpl.kt:309` | `delinkStudent` | `profiles` | update | OK | Admin action (`delinkStudent`); policy allows admin |
| `UserRepositoryImpl.kt:50` | `touchLastLogin` | `profiles` | update | OK | Own row (`id = auth.uid()`); 0 rows only if the profile is missing, which is harmless |
| `UserRepositoryImpl.kt:60` | `provisionAdmin` | `profiles` | update | OK | Best-effort and logged (`orLogCritical`); `provisionAdmin` is followed by `resolveRole`, which reports a profile that was not promoted |
| `UserRepositoryImpl.kt:81` | `linkStudent` | `profiles` | update | Dead code | No caller anywhere; a non-admin would be stopped by the profile guard trigger (which raises, so not silent) |
| `UserRepositoryImpl.kt:91` | `unlinkStudent` | `profiles` | update | Dead code | No caller anywhere; a non-admin would be stopped by the profile guard trigger (which raises, so not silent) |
| `UserRepositoryImpl.kt:101` | `deleteUser` | `profiles` | update | OK | Best-effort and logged (`orLogCritical`); `provisionAdmin` is followed by `resolveRole`, which reports a profile that was not promoted |
| `RoomRepositoryImpl.kt:79` | `deleteRoom` | `rooms` | update | OK | Admin-only screen and admin-only policy; 0 rows only if the row is already gone (stale device), which is harmless |
| `CurriculumRepositoryImpl.kt:163` | `saveSemesterTerm` | `semester_terms` | upsert | OK | Admin-only screen and admin-only policy; 0 rows only if the row is already gone (stale device), which is harmless |
| `SessionFeeRepositoryImpl.kt:54` | `saveSessionFee` | `session_fee_heads` | update | OK | Admin-only screen and admin-only policy; 0 rows only if the row is already gone (stale device), which is harmless |
| `SessionFeeRepositoryImpl.kt:76` | `saveSessionFee` | `session_fee_heads` | upsert | OK | Admin-only screen and admin-only policy; 0 rows only if the row is already gone (stale device), which is harmless |
| `SessionFeeRepositoryImpl.kt:52` | `saveSessionFee` | `session_fees` | upsert | OK | Admin-only screen and admin-only policy; 0 rows only if the row is already gone (stale device), which is harmless |
| `MarkEditRequestRepositoryImpl.kt:75` | `approveRequest` | `session_marks` | update | Watch | Admin-only update. The row is resolved first, so only a concurrent change reaches 0 rows -- but the request can then be marked APPROVED with no score changed |
| `MarkEditRequestRepositoryLocalImpl.kt:102` | `approveRequest` | `session_marks` | update | Watch | Admin-only update. The row is resolved first, so only a concurrent change reaches 0 rows -- but the request can then be marked APPROVED with no score changed |
| `SessionMarksRepositoryImpl.kt:149` | `saveScores` | `session_marks` | upsert | Watch | Admin-only update. The row is resolved first, so only a concurrent change reaches 0 rows -- but the request can then be marked APPROVED with no score changed |
| `AcademicSessionRepositoryImpl.kt:264` | `addStudent` | `session_students` | upsert | OK | Admin-only screen and admin-only policy; 0 rows only if the row is already gone (stale device), which is harmless |
| `AcademicSessionRepositoryImpl.kt:283` | `deleteStudent` | `session_students` | update | OK | Admin-only screen and admin-only policy; 0 rows only if the row is already gone (stale device), which is harmless |
| `AcademicSessionRepositoryImpl.kt:330` | `delinkStudent` | `session_students` | update | OK | Admin-only screen and admin-only policy; 0 rows only if the row is already gone (stale device), which is harmless |
| `AcademicSessionRepositoryImpl.kt:394` | `saveStudentProfile` | `session_students` | update | OK | Admin-only screen and admin-only policy; 0 rows only if the row is already gone (stale device), which is harmless |
| `AcademicSessionRepositoryImpl.kt:541` | `uploadStudentPhoto` | `session_students` | update | OK | Admin-only screen and admin-only policy; 0 rows only if the row is already gone (stale device), which is harmless |
| `CurriculumRepositoryImpl.kt:98` | `saveSemesterSubject` | `session_subjects` | upsert | OK | Admin-only screen and admin-only policy; 0 rows only if the row is already gone (stale device), which is harmless |
| `CurriculumRepositoryImpl.kt:109` | `deleteSemesterSubject` | `session_subjects` | update | OK | Admin-only screen and admin-only policy; 0 rows only if the row is already gone (stale device), which is harmless |
| `AcademicSessionRepositoryImpl.kt:318` | `delinkStudent` | `student_link_requests` | update | Low | Admin or a teacher with `can_approve_link_requests`. If that permission is removed mid-session the reject matches 0 rows silently |
| `StudentLinkRequestRepositoryImpl.kt:183` | `rejectRequest` | `student_link_requests` | update | Low | Admin or a teacher with `can_approve_link_requests`. If that permission is removed mid-session the reject matches 0 rows silently |
| `TeacherRepositoryImpl.kt:83` | `syncSelf` | `teachers` | upsert | OK | Admin-only screen and admin-only policy; 0 rows only if the row is already gone (stale device), which is harmless |
| `TeacherRepositoryImpl.kt:92` | `createTeacherAccount` | `teachers` | upsert | OK | Admin-only screen and admin-only policy; 0 rows only if the row is already gone (stale device), which is harmless |
| `TeacherRepositoryImpl.kt:97` | `updateTeacher` | `teachers` | upsert | OK | Admin-only screen and admin-only policy; 0 rows only if the row is already gone (stale device), which is harmless |
| `TeacherRepositoryImpl.kt:127` | `uploadPhoto` | `teachers` | update | OK | Admin-only screen and admin-only policy; 0 rows only if the row is already gone (stale device), which is harmless |
| `SessionTimetableRepositoryImpl.kt:108` | `savePeriod` | `timetable_periods` | upsert | OK | Admin-only screen and admin-only policy; 0 rows only if the row is already gone (stale device), which is harmless |
| `SessionTimetableRepositoryImpl.kt:121` | `removePeriod` | `timetable_periods` | update | OK | Admin-only screen and admin-only policy; 0 rows only if the row is already gone (stale device), which is harmless |
