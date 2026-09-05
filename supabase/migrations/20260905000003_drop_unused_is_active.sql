-- departments.is_active and session_students.is_active audited: 0 non-true rows on either table
-- (live). departments.is_active was filtered on locally but never written false by any feature
-- (no "deactivate department" action exists). session_students.is_active was never displayed or
-- toggled at all — enrollment_status (ACTIVE/PROMOTED/REPEATED/WITHDRAWN/GRADUATED) is the real
-- student lifecycle field. Kept on teachers (RLS-critical) and academic_sessions (drives the
-- graduation feature).
alter table departments      drop column is_active;
alter table session_students drop column is_active;
