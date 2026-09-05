-- archived_at on departments/academic_sessions/session_students was never written by any edge
-- function or client code path (verified: 0 non-null rows across all three, live). Every read site
-- combined it with is_active via AND, making it a permanent no-op. Dropped here; kept on teachers,
-- where set-teacher-status actually writes it as a soft-delete timestamp.
alter table departments        drop column archived_at;
alter table academic_sessions  drop column archived_at;
alter table session_students   drop column archived_at;
