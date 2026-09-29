-- Human-readable database error messages.
--
-- Every `raise exception` below reaches the apps verbatim (PostgREST P0001) and is shown to the user by
-- ErrorClassifier, so the wording is the user experience. This migration ONLY rewrites message text:
-- every condition, exclusion (e.g. soft-deleted rows), security setting and grant is carried over
-- unchanged from the latest definition of each function. Messages name people, classes, subjects,
-- rooms and times instead of ids/emails, and say what to do next.
--
-- Wording keeps the phrases the app's text-only fallback already recognises
-- ("already has an overlapping", "already booked overlapping", "already booked for an overlapping").
--
-- NOT YET APPLIED to the live project: review, then apply deliberately.
--
-- Functions changed (old -> new wording is noted above each one):
--   fn_check_timetable_conflict, fn_enforce_roster_cap, fn_check_datesheet_conflict,
--   approve_link_request, approve_attendance_edit_request, record_semester_result,
--   fn_check_student_shift, fn_guard_session_shift_change, fn_check_row_shift_allowed,
--   fn_check_period_link_shift, fn_guard_profile_update, fn_guard_exam_paper_review.
-- New helpers (security invoker, so RLS still applies to the lookups they perform):
--   msg_session_label, msg_class_label, msg_teacher_label.
-- Deliberately untouched: roll_block_error (already plain), the one-off "expects empty session tables"
-- guard in session_shift_consolidation (a migration-time check, never seen by users).

-- ── Helpers: names instead of ids ─────────────────────────────────────────────────────────────────────

-- "ENG 2023–2027" (adds "(MA Replacement)" for the 2-year program). Null when the session is unknown.
create or replace function msg_session_label(p_session text) returns text
language sql stable set search_path = public as $$
  select concat_ws(' ',
           coalesce(d.code, a.dept_id) || ' ' || a.start_year::text || '–' || a.end_year::text,
           case when a.program_type = 'MA_REPLACEMENT' then '(MA Replacement)' end)
    from academic_sessions a
    left join departments d on d.dept_id = a.dept_id
   where a.session_id = p_session
$$;

-- "ENG Semester 5 Morning" -- the class a timetable period / datesheet paper belongs to.
create or replace function msg_class_label(p_session text, p_shift shift, p_semester int default null) returns text
language sql stable set search_path = public as $$
  select concat_ws(' ',
           coalesce(d.code, a.dept_id),
           'Semester ' || coalesce(p_semester, a.current_semester)::text,
           initcap(p_shift::text),
           case when a.program_type = 'MA_REPLACEMENT' then '(MA Replacement)' end)
    from academic_sessions a
    left join departments d on d.dept_id = a.dept_id
   where a.session_id = p_session
$$;

-- A teacher's name, falling back to the email when the account has no teacher row.
create or replace function msg_teacher_label(p_email text) returns text
language sql stable set search_path = public as $$
  select coalesce((select t.name from teachers t where t.email = p_email), p_email)
$$;

revoke all on function msg_session_label(text) from public, anon;
revoke all on function msg_class_label(text, shift, int) from public, anon;
revoke all on function msg_teacher_label(text) from public, anon;
grant execute on function msg_session_label(text) to authenticated, service_role;
grant execute on function msg_class_label(text, shift, int) to authenticated, service_role;
grant execute on function msg_teacher_label(text) to authenticated, service_role;

-- ── Timetable: teacher / room double-booking ───────────────────────────────────────────────────────────
-- Old: "Teacher majid@x.pk already has an overlapping lecture on WEDNESDAY at 13:40:00"
--      "Room R#14 is already booked overlapping 13:40:00 on WEDNESDAY"
-- New: "Teacher Majid Bashir already has an overlapping lecture: Romantic & Victorian Poetry (EL-309) for
--       ENG Semester 5 Morning on Wednesday 13:40–15:05."
--      "Room R#14 is already booked overlapping this time: Romantic & Victorian Poetry (EL-309) for ..."
-- Names the OTHER class so an admin can tell a real clash from a mistake. Kept free of trailing advice so the
-- longest realistic message (long teacher + subject + class names) stays under the app's 220-character cap.
-- Logic identical to 20260927150000_conflict_check_excludes_deleted.sql (LECTURE only, soft-deleted rows
-- ignored, effective-date ranges must overlap).
create or replace function fn_check_timetable_conflict() returns trigger
language plpgsql set search_path = public as $$
declare
  v_other timetable_periods%rowtype;
begin
  if new.period_type <> 'LECTURE' then return new; end if;

  if new.teacher_email is not null then
    select p.* into v_other from timetable_periods p
    where p.id <> new.id and p.period_type = 'LECTURE' and not p.is_deleted
      and p.teacher_email = new.teacher_email and p.day = new.day
      and p.start_time < new.end_time and new.start_time < p.end_time
      and coalesce(p.effective_from, '-infinity'::date) <= coalesce(new.effective_to, 'infinity'::date)
      and coalesce(new.effective_from, '-infinity'::date) <= coalesce(p.effective_to, 'infinity'::date)
    limit 1;
    if found then
      raise exception '%', format(
        'Teacher %s already has an overlapping lecture: %s%s for %s on %s %s–%s.',
        msg_teacher_label(new.teacher_email),
        coalesce(nullif(v_other.subject_name, ''), 'another subject'),
        coalesce(' (' || nullif(v_other.course_code, '') || ')', ''),
        coalesce(msg_class_label(v_other.primary_session_id, v_other.shift), 'another class'),
        initcap(lower(v_other.day::text)),
        to_char(v_other.start_time, 'HH24:MI'), to_char(v_other.end_time, 'HH24:MI'));
    end if;
  end if;

  if new.room_no is not null then
    select p.* into v_other from timetable_periods p
    where p.id <> new.id and p.period_type = 'LECTURE' and not p.is_deleted
      and p.room_no = new.room_no and p.day = new.day
      and p.start_time < new.end_time and new.start_time < p.end_time
      and coalesce(p.effective_from, '-infinity'::date) <= coalesce(new.effective_to, 'infinity'::date)
      and coalesce(new.effective_from, '-infinity'::date) <= coalesce(p.effective_to, 'infinity'::date)
    limit 1;
    if found then
      raise exception '%', format(
        'Room %s is already booked overlapping this time: %s%s for %s on %s %s–%s.',
        new.room_no,
        coalesce(nullif(v_other.subject_name, ''), 'another subject'),
        coalesce(' (' || nullif(v_other.course_code, '') || ')', ''),
        coalesce(msg_class_label(v_other.primary_session_id, v_other.shift), 'another class'),
        initcap(lower(v_other.day::text)),
        to_char(v_other.start_time, 'HH24:MI'), to_char(v_other.end_time, 'HH24:MI'));
    end if;
  end if;

  return new;
end $$;

-- ── Roster capacity ────────────────────────────────────────────────────────────────────────────────────
-- Old: "Session isl_2026 is full (50 students max)"
-- New: "ENG 2023–2027 is full (limit 50 students). Increase the session's student limit or remove a student first."
create or replace function fn_enforce_roster_cap() returns trigger
language plpgsql as $$
declare cap int; n int;
begin
  select max_students into cap from academic_sessions where session_id = new.session_id;
  select count(*) into n from session_students where session_id = new.session_id;
  if n >= coalesce(cap, 50) then
    raise exception '%', format(
      '%s is full (limit %s students). Increase the session''s student limit or remove a student first.',
      coalesce(msg_session_label(new.session_id), 'This session'), coalesce(cap, 50));
  end if;
  return new;
end $$;

-- ── Datesheet: room / invigilator double-booking across all datesheets ────────────────────────────────
-- Old: "Room is already booked for an overlapping exam on 2026-05-12"
--      "Invigilator jane@x.pk already has an overlapping exam duty on 2026-05-12"
-- New: "Room R#14 (Main Block) is already booked for an overlapping exam: Data Structures (CS-201) for
--       CS Semester 3 Morning on 12 May 2026, 09:00–12:00."
--      "Invigilator Jane Doe already has an overlapping exam duty: Data Structures (CS-201) for ..."
-- Logic identical to 20260908120000_datesheet_rework.sql.
create or replace function fn_check_datesheet_conflict() returns trigger
language plpgsql set search_path = public as $$
declare
  eff_start time;
  eff_end time;
  v_other record;
  v_room text;
begin
  if new.exam_date is null then
    return new;
  end if;

  select coalesce(new.start_time, d.default_start_time), coalesce(new.end_time, d.default_end_time)
    into eff_start, eff_end
    from datesheets d
    where d.id = new.datesheet_id;

  if eff_start is null or eff_end is null then
    return new;
  end if;

  if new.room_id is not null then
    select s.course_code, s.subject_name, d2.session_id, d2.semester, d2.shift,
           coalesce(s.start_time, d2.default_start_time) as other_start,
           coalesce(s.end_time, d2.default_end_time) as other_end
      into v_other
      from datesheet_slots s
      join datesheets d2 on d2.id = s.datesheet_id
      where s.id <> new.id
        and s.room_id = new.room_id
        and s.exam_date = new.exam_date
        and coalesce(s.start_time, d2.default_start_time) < eff_end
        and eff_start < coalesce(s.end_time, d2.default_end_time)
      limit 1;
    if found then
      select r.room_no || coalesce(' (' || b.name || ')', '') into v_room
        from rooms r left join buildings b on b.building_id = r.building_id
        where r.room_id = new.room_id;
      raise exception '%', format(
        '%s is already booked for an overlapping exam: %s (%s) for %s on %s, %s–%s.',
        coalesce('Room ' || v_room, 'This room'),
        v_other.subject_name, v_other.course_code,
        coalesce(msg_class_label(v_other.session_id, v_other.shift, v_other.semester), 'another class'),
        to_char(new.exam_date, 'FMDD Mon YYYY'),
        to_char(v_other.other_start, 'HH24:MI'), to_char(v_other.other_end, 'HH24:MI'));
    end if;
  end if;

  if new.invigilator_email is not null then
    select s.course_code, s.subject_name, d2.session_id, d2.semester, d2.shift,
           coalesce(s.start_time, d2.default_start_time) as other_start,
           coalesce(s.end_time, d2.default_end_time) as other_end
      into v_other
      from datesheet_slots s
      join datesheets d2 on d2.id = s.datesheet_id
      where s.id <> new.id
        and s.invigilator_email = new.invigilator_email
        and s.exam_date = new.exam_date
        and coalesce(s.start_time, d2.default_start_time) < eff_end
        and eff_start < coalesce(s.end_time, d2.default_end_time)
      limit 1;
    if found then
      raise exception '%', format(
        'Invigilator %s already has an overlapping exam duty: %s (%s) for %s on %s, %s–%s.',
        msg_teacher_label(new.invigilator_email),
        v_other.subject_name, v_other.course_code,
        coalesce(msg_class_label(v_other.session_id, v_other.shift, v_other.semester), 'another class'),
        to_char(new.exam_date, 'FMDD Mon YYYY'),
        to_char(v_other.other_start, 'HH24:MI'), to_char(v_other.other_end, 'HH24:MI'));
    end if;
  end if;

  return new;
end $$;

-- ── Link-request approval ─────────────────────────────────────────────────────────────────────────────
-- Old: "not allowed" / "This request is no longer pending." / "No student X in session Y -- add that student ..."
-- New: says what permission is missing, what state the request is actually in (and who reviewed it),
--      and which roster the roll number is missing from.
create or replace function approve_link_request(p_request_id uuid, p_reviewed_by text)
returns void
language plpgsql security definer set search_path = public as $$
declare
  v_request record;
  v_previous_email text;
  v_status link_status;
  v_reviewer text;
begin
  if not (is_admin() or teacher_can('can_approve_link_requests')) then
    raise exception 'You don''t have permission to approve link requests. Ask an admin.';
  end if;

  select * into v_request from student_link_requests
    where request_id = p_request_id and status = 'PENDING';
  if not found then
    select status, reviewed_by into v_status, v_reviewer from student_link_requests where request_id = p_request_id;
    if v_status is null then
      raise exception 'This link request no longer exists. Refresh the list.';
    end if;
    raise exception '%', format('This link request was already %s%s. Refresh the list.',
      lower(v_status::text), coalesce(' by ' || nullif(v_reviewer, ''), ''));
  end if;

  select linked_email into v_previous_email from session_students
    where session_id = v_request.session_id and roll_number = v_request.roll_number_claimed;
  if not found then
    raise exception '%', format('Roll number %s is not on the %s roster. Add that student to the roster first.',
      v_request.roll_number_claimed, coalesce(msg_session_label(v_request.session_id), 'session'));
  end if;

  -- Relinking: unlink the previous holder's account and downgrade their own (now-stale) APPROVED
  -- request, otherwise their LinkRequestScreen would be stuck showing "approved" forever with no
  -- roster link behind it.
  if v_previous_email is not null and v_previous_email <> '' and v_previous_email <> v_request.requested_by_email then
    update profiles set linked_session_id = null, linked_roll = null where email = v_previous_email;
    update student_link_requests set status = 'REJECTED',
      rejection_reason = 'This roll number was relinked to a different account.',
      reviewed_by = p_reviewed_by, reviewed_at = now()
      where requested_by_email = v_previous_email and status = 'APPROVED';
  end if;

  update session_students set linked_email = v_request.requested_by_email
    where session_id = v_request.session_id and roll_number = v_request.roll_number_claimed;

  update profiles set linked_session_id = v_request.session_id, linked_roll = v_request.roll_number_claimed
    where email = v_request.requested_by_email;

  update student_link_requests set status = 'APPROVED', reviewed_by = p_reviewed_by, reviewed_at = now()
    where request_id = p_request_id;
end $$;

-- ── Attendance edit-request approval ──────────────────────────────────────────────────────────────────
-- Old: "not allowed" / "This request is no longer pending."
-- New: "Only an admin can approve attendance edit requests." /
--      "This attendance edit request was already approved by x. Refresh the list."
create or replace function approve_attendance_edit_request(p_request_id uuid, p_reviewed_by text)
returns void
language plpgsql security definer set search_path = public as $$
declare
  v_request attendance_edit_requests%rowtype;
  v_status text;
  v_reviewer text;
begin
  if not is_admin() then
    raise exception 'Only an admin can approve attendance edit requests.';
  end if;

  select * into v_request from attendance_edit_requests
    where id = p_request_id and status = 'PENDING'
    for update;
  if not found then
    select status::text, reviewed_by into v_status, v_reviewer from attendance_edit_requests where id = p_request_id;
    if v_status is null then
      raise exception 'This attendance edit request no longer exists. Refresh the list.';
    end if;
    raise exception '%', format('This attendance edit request was already %s%s. Refresh the list.',
      lower(v_status), coalesce(' by ' || nullif(v_reviewer, ''), ''));
  end if;

  insert into session_attendance (session_id, semester, course_code, date, roll_number, status, is_late, teacher_email)
  values (v_request.session_id, v_request.semester, v_request.course_code, v_request.date,
          v_request.roll_number, v_request.requested_status, v_request.requested_is_late, v_request.requested_by)
  on conflict (session_id, course_code, date, roll_number) do update
    set status = excluded.status, is_late = excluded.is_late, is_deleted = false;

  update attendance_edit_requests set status = 'APPROVED', reviewed_by = p_reviewed_by, reviewed_at = now()
    where id = p_request_id;
end $$;

-- ── Semester result recording ─────────────────────────────────────────────────────────────────────────
-- Old: "not allowed"
-- New: "Only an admin or a teacher of this student's class can record results."
-- Body identical to 20260926120000_session_shift_consolidation.sql.
create or replace function record_semester_result(
  p_session text, p_roll text, p_semester integer, p_gpa numeric, p_cgpa numeric,
  p_term_label text default null, p_result semester_result default 'PENDING',
  p_class_position integer default null, p_remarks text default null, p_supply text[] default null)
returns void
language plpgsql security definer set search_path = public as $$
begin
  if not (is_admin() or teaches_student(p_session, p_roll)) then
    raise exception 'Only an admin or a teacher of this student''s class can record results.';
  end if;
  insert into student_semester_gpa
    (session_id, roll_number, semester, gpa, cgpa, term_label, result_status,
     class_position, remarks, supply_courses, created_by, updated_by)
  values
    (p_session, p_roll, p_semester, p_gpa, p_cgpa, p_term_label, p_result,
     p_class_position, p_remarks, coalesce(p_supply, '{}'), current_email(), current_email())
  on conflict (session_id, roll_number, semester) do update set
    gpa = excluded.gpa, cgpa = excluded.cgpa, term_label = excluded.term_label,
    result_status = excluded.result_status, class_position = excluded.class_position,
    remarks = excluded.remarks, supply_courses = excluded.supply_courses,
    updated_by = current_email(), updated_at = now();
  update session_students st set gpa = p_gpa, cgpa = p_cgpa, updated_at = now()
  where st.session_id = p_session and st.roll_number = p_roll
    and not exists (select 1 from student_semester_gpa g
                    where g.session_id = p_session and g.roll_number = p_roll
                      and g.semester > p_semester);
end $$;

-- ── Shifts ────────────────────────────────────────────────────────────────────────────────────────────
-- Old: "This session does not run a Morning shift."
-- New: "ENG 2023–2027 does not run a Morning shift. Choose one of the shifts it runs."
create or replace function fn_check_student_shift() returns trigger
language plpgsql set search_path = public as $$
declare
  v_mode shift_mode;
  v_max int;
  v_error text;
begin
  select shift_mode, max_students into v_mode, v_max from academic_sessions where session_id = new.session_id;
  if not found then
    return new;  -- the FK reports the missing session
  end if;
  if not (v_mode = 'BOTH' or v_mode::text = new.shift::text) then
    raise exception '%', format('%s does not run a %s shift. Choose one of the shifts it runs.',
      coalesce(msg_session_label(new.session_id), 'This session'), initcap(new.shift::text));
  end if;
  v_error := roll_block_error(v_mode, v_max, new.shift, new.roll_number);
  if v_error is not null then
    raise exception '%', v_error;
  end if;
  return new;
end $$;

-- Old: "Cannot switch to Morning only: the other shift still has 12 student(s), a fee structure. Move or remove them first."
--      "<roll problem> Adjust the capacity or renumber that student first."
-- New: "ENG 2023–2027 can't switch to Morning only while the other shift still has 12 student(s), a fee structure.
--       Move or remove those first."
--      "<roll problem> Change the student limit or renumber that student before saving this change."
-- Body identical to 20260926120002_fix_session_shift_guard_message.sql.
create or replace function fn_guard_session_shift_change() returns trigger
language plpgsql set search_path = public as $$
declare
  v_students int; v_fees int; v_periods int; v_datesheets int;
  v_parts text[] := '{}';
  v_bad record;
  v_error text;
begin
  if new.shift_mode is distinct from old.shift_mode and new.shift_mode <> 'BOTH' then
    select count(*) into v_students from session_students
      where session_id = new.session_id and not is_deleted and shift::text <> new.shift_mode::text;
    select count(*) into v_fees from session_fees
      where session_id = new.session_id and not is_deleted and shift::text <> new.shift_mode::text;
    select count(*) into v_periods from timetable_periods
      where primary_session_id = new.session_id and not is_deleted and shift::text <> new.shift_mode::text;
    select count(*) into v_datesheets from datesheets
      where session_id = new.session_id and not is_deleted and shift::text <> new.shift_mode::text;
    if v_students > 0 then v_parts := array_append(v_parts, format('%s student(s)', v_students)); end if;
    if v_fees > 0 then v_parts := array_append(v_parts, 'a fee structure'::text); end if;
    if v_periods > 0 then v_parts := array_append(v_parts, format('%s timetable period(s)', v_periods)); end if;
    if v_datesheets > 0 then v_parts := array_append(v_parts, format('%s datesheet(s)', v_datesheets)); end if;
    if cardinality(v_parts) > 0 then
      raise exception '%', format('%s can''t switch to %s only while the other shift still has %s. Move or remove those first.',
        coalesce(msg_session_label(new.session_id), 'This session'),
        initcap(new.shift_mode::text), array_to_string(v_parts, ', '));
    end if;
  end if;

  if new.shift_mode is distinct from old.shift_mode or new.max_students is distinct from old.max_students then
    for v_bad in
      select roll_number, shift from session_students where session_id = new.session_id and not is_deleted
    loop
      v_error := roll_block_error(new.shift_mode, new.max_students, v_bad.shift, v_bad.roll_number);
      if v_error is not null then
        raise exception '%', format('%s Change the student limit or renumber that student before saving this change.', v_error);
      end if;
    end loop;
  end if;
  return new;
end $$;

-- Old: "This session does not run a Morning shift."
-- New: "ENG 2023–2027 does not run a Morning shift, so this timetable period can't be saved for it."
create or replace function fn_check_row_shift_allowed() returns trigger
language plpgsql set search_path = public as $$
declare
  v_row jsonb := to_jsonb(new);
  v_session text := coalesce(v_row ->> 'primary_session_id', v_row ->> 'session_id');
  v_shift shift := (v_row ->> 'shift')::shift;
  v_what text := case tg_table_name
    when 'timetable_periods' then 'timetable period'
    when 'session_fees' then 'fee structure'
    when 'session_fee_heads' then 'fee item'
    when 'datesheets' then 'datesheet'
    else 'record' end;
begin
  if not session_allows_shift(v_session, v_shift) then
    raise exception '%', format('%s does not run a %s shift, so this %s can''t be saved for it.',
      coalesce(msg_session_label(v_session), 'This session'), initcap(v_shift::text), v_what);
  end if;
  return new;
end $$;

-- Old: "Session isl_2026 does not run a Morning shift, so it cannot share this lecture."
-- New: "ENG 2023–2027 does not run a Morning shift, so it can't share this lecture."
create or replace function fn_check_period_link_shift() returns trigger
language plpgsql set search_path = public as $$
declare v_shift shift;
begin
  select shift into v_shift from timetable_periods where id = new.period_id;
  if v_shift is not null and not session_allows_shift(new.session_id, v_shift) then
    raise exception '%', format('%s does not run a %s shift, so it can''t share this lecture.',
      coalesce(msg_session_label(new.session_id), 'This session'), initcap(v_shift::text));
  end if;
  return new;
end $$;

-- ── Account and exam-paper guards ─────────────────────────────────────────────────────────────────────
-- Old: "not allowed to modify privileged profile columns"
-- New: "You can't change an account's role, email, status or class link. Ask an admin."
-- Body identical to 20260715070000_guard_profile_exempt_service_role.sql.
create or replace function fn_guard_profile_update()
returns trigger
language plpgsql
security definer
set search_path to 'public'
as $$
begin
  if coalesce(auth.jwt() ->> 'role', '') = 'service_role' then return new; end if;
  if is_admin() then return new; end if;
  if new.role is distinct from old.role
     or new.email is distinct from old.email
     or new.status is distinct from old.status
     or new.teacher_email is distinct from old.teacher_email
     or new.linked_session_id is distinct from old.linked_session_id
     or new.linked_roll is distinct from old.linked_roll then
    raise exception 'You can''t change an account''s role, email, status or class link. Ask an admin.';
  end if;
  return new;
end
$$;

-- Old: "not allowed to set exam paper review columns on insert" / "not allowed to modify exam paper review columns"
-- New: "Review status, notes and the answer key are set by an admin, not when submitting a paper." /
--      "Only an admin can change an exam paper's review status, notes or answer key."
-- Body identical to 20260902190508_guard_exam_paper_review_columns.sql.
create or replace function fn_guard_exam_paper_review()
returns trigger
language plpgsql
security definer
set search_path to 'public'
as $$
begin
  if coalesce(auth.jwt() ->> 'role', '') = 'service_role' then return new; end if;
  if is_admin() then return new; end if;

  if tg_op = 'INSERT' then
    if new.review_status is distinct from 'SUBMITTED'
       or new.reviewed_by is not null
       or new.reviewed_at is not null
       or new.teacher_notes is not null
       or new.key_storage_path is not null then
      raise exception 'Review status, notes and the answer key are set by an admin, not when submitting a paper.';
    end if;
    return new;
  end if;

  if new.review_status is distinct from old.review_status
     or new.reviewed_by is distinct from old.reviewed_by
     or new.reviewed_at is distinct from old.reviewed_at
     or new.teacher_notes is distinct from old.teacher_notes
     or new.key_storage_path is distinct from old.key_storage_path then
    raise exception 'Only an admin can change an exam paper''s review status, notes or answer key.';
  end if;
  return new;
end
$$;
