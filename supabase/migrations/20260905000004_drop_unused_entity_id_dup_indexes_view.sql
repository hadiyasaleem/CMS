-- Three independent cleanups, all verified against live usage stats / pg_depend before writing:
--
-- 1) entity_id (bigint generated always as identity) exists on 23 tables, added by a migration
--    not tracked in this repo (see complete_public_audit_metadata in the live migration history —
--    flagged separately for reconciliation). pg_stat_user_indexes shows every entity_id-related
--    index at 0 scans except on session_attendance, where the client genuinely uses it as a
--    pagination tie-breaker (order("updated_at") then order("entity_id"), for rows sharing a
--    timestamp within one bulk attendance save). Dropping the column cascades to drop its owned
--    identity sequence and both indexes automatically. Kept on session_attendance.
--
-- 2) Seven tables ended up with two separate btree indexes on the same updated_at column — the
--    original schema's index plus a same-column index added later by the incremental-sync
--    migration (which used `create index if not exists` with a different name instead of reusing
--    the existing one). Dropping the older/less-used duplicate on each.
--
-- 3) student_gpa_progression view has zero dependents (verified via pg_depend) and is never
--    queried by any client code. The other views that looked orphaned at a glance
--    (session_attendance_summary) turned out to be depended on by session_overview/at_risk_students
--    and are NOT touched here.

alter table academic_sessions        drop column entity_id;
alter table calendar_events          drop column entity_id;
alter table datesheet_slots          drop column entity_id;
alter table datesheets               drop column entity_id;
alter table departments              drop column entity_id;
alter table exam_paper_submissions   drop column entity_id;
alter table fee_overrides            drop column entity_id;
alter table fines                    drop column entity_id;
alter table mark_edit_requests       drop column entity_id;
alter table notifications            drop column entity_id;
alter table period_sessions          drop column entity_id;
alter table profiles                 drop column entity_id;
alter table semester_terms           drop column entity_id;
alter table session_fee_heads        drop column entity_id;
alter table session_fees             drop column entity_id;
alter table session_marks            drop column entity_id;
alter table session_students         drop column entity_id;
alter table session_subjects         drop column entity_id;
alter table student_link_requests    drop column entity_id;
alter table student_semester_gpa     drop column entity_id;
alter table teachers                 drop column entity_id;
alter table timetable_periods        drop column entity_id;

drop index if exists idx_academic_sessions_updated_at;
drop index if exists idx_attendance_updated;
drop index if exists idx_marks_updated;
drop index if exists idx_subjects_updated;
drop index if exists idx_gpa_updated;
drop index if exists idx_teachers_updated;
drop index if exists idx_periods_updated;

drop view if exists student_gpa_progression;
