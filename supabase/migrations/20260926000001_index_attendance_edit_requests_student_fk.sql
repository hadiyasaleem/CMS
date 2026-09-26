-- Covering index for the (session_id, roll_number) -> session_students foreign key (advisor 0001).
create index idx_attendance_edit_requests_student on attendance_edit_requests(session_id, roll_number);
