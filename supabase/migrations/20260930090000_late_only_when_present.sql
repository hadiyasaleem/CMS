-- Late only means anything for a student who actually showed up: an absent/leave row has no "arrived late".
alter table session_attendance
  add constraint session_attendance_late_only_present check (not is_late or status = 'PRESENT');

alter table attendance_edit_requests
  add constraint attendance_edit_requests_late_only_present check (not requested_is_late or requested_status = 'PRESENT');
