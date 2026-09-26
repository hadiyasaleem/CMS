-- Advisor follow-up to the session/shift consolidation: cover every foreign key flagged as unindexed
-- (several now sit on the dept -> session -> shift scoping paths), drop the duplicate updated_at index on
-- session_students, and drop idx_students_session_shift (the (session_id, roll_number) primary key already
-- serves per-session lookups, including teaches_student()).

create index if not exists idx_calendar_events_dept on calendar_events(dept_id);
create index if not exists idx_calendar_events_session on calendar_events(session_id);
create index if not exists idx_notifications_target_dept on notifications(target_dept_id);
create index if not exists idx_notifications_target_session on notifications(target_session_id);
create index if not exists idx_attendance_student on session_attendance(session_id, roll_number);
create index if not exists idx_marks_student on session_marks(session_id, roll_number);
create index if not exists idx_link_requests_session on student_link_requests(session_id);
create index if not exists idx_profiles_linked_student on profiles(linked_session_id, linked_roll);
create index if not exists idx_profiles_teacher_email on profiles(teacher_email);
create index if not exists idx_sessions_incharge on academic_sessions(incharge_email);
create index if not exists idx_departments_hod on departments(hod_email);
create index if not exists idx_datesheets_default_building on datesheets(default_building_id);
create index if not exists idx_ds_slots_building on datesheet_slots(building_id);
create index if not exists idx_ds_slots_invigilator on datesheet_slots(invigilator_email);
create index if not exists idx_ds_slots_room on datesheet_slots(room_id);

drop index if exists idx_students_updated;         -- duplicate of idx_session_students_updated_at
drop index if exists idx_students_session_shift;   -- covered by the primary key prefix
