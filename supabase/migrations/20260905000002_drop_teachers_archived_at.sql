-- teachers.archived_at was the only one of the four archived_at columns actually written
-- (set-teacher-status stamps it on delete), but nothing ever reads it — is_active + status
-- already do the entire lifecycle job (RLS's is_active_teacher(), auth ban/unban, UI). Dropping
-- it for consistency with the other three tables cleaned up in 20260905000001.
alter table teachers drop column archived_at;
