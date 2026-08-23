-- Richer link-request claim: the student self-identifies (name + DOB) alongside the existing
-- session_id / roll_number_claimed / cnic_bform_claimed, so admin/teacher can verify identity.
alter table student_link_requests
  add column if not exists name_claimed text,
  add column if not exists dob_claimed  date;
