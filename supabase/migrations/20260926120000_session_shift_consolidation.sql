-- Session & Shift Consolidation (Task 1 of 10) -- see the requirements doc
-- "CMS-Session-Shift-Consolidation-Requirements.md" v3.
--
-- One academic session per intake (dept + start year) now serves Morning, Evening or both:
--   * academic_sessions: session_id = "{deptId}_{startYear}", shift -> shift_mode (MORNING/EVENING/BOTH)
--   * shared by both shifts (unchanged): session_subjects, semester_terms, current_semester
--   * shift-specific (new `shift` column): session_students, timetable_periods, session_fees (+ heads),
--     datesheets
--   * optional shift targeting: calendar_events.shift, notifications.target_shift
--   * student-scoped history (attendance, marks, GPA, fines, fee_overrides, edit requests) keeps its
--     (session_id, roll_number) key -- the student row carries the shift. fee_overrides needs no shift of
--     its own: an override's label refers to the heads of the student's own shift.
--
-- Roll-number blocks (decision 1): a student's serial is the trailing number of the roll number
-- (IT-22-09 -> 9). Morning uses serials 1..morningCapacity, Evening uses serials above it, where
-- morningCapacity = max_students for a single-shift session and max_students / 2 for BOTH. Enforced HERE
-- by trigger so every writer (both admin apps, SQL, future tools) obeys it; the apps pre-validate with the
-- same rule (Task 2 helper) to show friendly messages and suggest the next roll number.
--
-- Teachers are scoped to the shifts they teach: roster / attendance / marks / GPA / fines / edit-request
-- access moves from teaches(session) to teaches_student(session, roll), i.e. the teacher has a period in
-- that student's shift (merged lectures via period_sessions count, using the period's shift).
--
-- Precondition: every session-related table was verified EMPTY before this migration was written
-- (26 Sep 2026), so no data is merged or re-keyed here. The guard below aborts if that ever changes.

do $$
begin
  if exists (select 1 from academic_sessions) or exists (select 1 from session_students)
     or exists (select 1 from timetable_periods) or exists (select 1 from session_fees)
     or exists (select 1 from datesheets) then
    raise exception 'session_shift_consolidation expects empty session tables; migrate existing data first';
  end if;
end $$;

-- ── Sessions ─────────────────────────────────────────────────────────────────
create type shift_mode as enum ('MORNING', 'EVENING', 'BOTH');

alter table academic_sessions drop column shift;  -- also drops unique (dept_id, start_year, shift)
alter table academic_sessions add column shift_mode shift_mode not null default 'MORNING';
alter table academic_sessions alter column shift_mode drop default;  -- the app always chooses explicitly
alter table academic_sessions add constraint academic_sessions_dept_id_start_year_key unique (dept_id, start_year);
alter table academic_sessions add constraint academic_sessions_session_id_format
  check (session_id = dept_id || '_' || start_year::text);
alter table academic_sessions add constraint academic_sessions_max_students_range
  check (max_students between 1 and 200);
comment on column academic_sessions.shift_mode is
  'Which shifts this intake runs. Students, periods, fees and datesheets carry their own shift.';
comment on column academic_sessions.max_students is
  'Total capacity across both shifts (app default: 50 single shift, 100 BOTH). Morning roll block = this (single) or half (BOTH).';

-- True when the session runs p_shift. A missing session returns true so the FK reports it instead.
create or replace function session_allows_shift(p_session text, p_shift shift) returns boolean
language sql stable set search_path = public as $$
  select coalesce(
    (select a.shift_mode = 'BOTH' or a.shift_mode::text = p_shift::text
       from academic_sessions a where a.session_id = p_session),
    true)
$$;

-- Null when p_roll fits p_shift's serial block for a session with (p_mode, p_max); otherwise the message.
create or replace function roll_block_error(p_mode shift_mode, p_max int, p_shift shift, p_roll text) returns text
language plpgsql immutable set search_path = public as $$
declare
  cap int := case when p_mode = 'BOTH' then p_max / 2 else p_max end;
  digits text := substring(p_roll from '([0-9]+)\s*$');
  serial int;
begin
  if digits is null or length(digits) > 6 then
    return format('Roll number %s must end with a serial number, e.g. IT-22-09.', p_roll);
  end if;
  serial := digits::int;
  if p_shift = 'MORNING' and (serial < 1 or serial > cap) then
    return format('Morning roll numbers use serials 1-%s; %s is outside that range.', cap, p_roll);
  end if;
  if p_shift = 'EVENING' and serial <= cap then
    return format('Evening roll numbers start after serial %s; %s is in the Morning range.', cap, p_roll);
  end if;
  return null;
end $$;

-- ── Students ─────────────────────────────────────────────────────────────────
alter table session_students add column shift shift not null;
drop index one_cr_per_session;
drop index one_gr_per_session;
create unique index one_cr_per_session_shift on session_students(session_id, shift) where is_cr;
create unique index one_gr_per_session_shift on session_students(session_id, shift) where is_gr;
create index idx_students_session_shift on session_students(session_id, shift);

-- Shift must be one the session runs, and the roll number must sit in that shift's serial block.
-- (Capacity stays in fn_enforce_roster_cap, which already counts the whole session.)
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
    raise exception 'This session does not run a % shift.', initcap(new.shift::text);
  end if;
  v_error := roll_block_error(v_mode, v_max, new.shift, new.roll_number);
  if v_error is not null then
    raise exception '%', v_error;
  end if;
  return new;
end $$;
create trigger trg_student_shift before insert or update of session_id, roll_number, shift on session_students
  for each row execute function fn_check_student_shift();

-- Changing a session's shifts or capacity must not strand existing data:
--   * BOTH -> single (or MORNING <-> EVENING) is blocked while the dropped shift still has live students,
--     a fee structure, timetable periods or a datesheet (decision 4);
--   * every live student must still fit the (possibly moved) roll-number blocks.
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
    if v_students > 0 then v_parts := v_parts || format('%s student(s)', v_students); end if;
    if v_fees > 0 then v_parts := v_parts || 'a fee structure'; end if;
    if v_periods > 0 then v_parts := v_parts || format('%s timetable period(s)', v_periods); end if;
    if v_datesheets > 0 then v_parts := v_parts || format('%s datesheet(s)', v_datesheets); end if;
    if cardinality(v_parts) > 0 then
      raise exception 'Cannot switch to % only: the other shift still has %. Move or remove them first.',
        initcap(new.shift_mode::text), array_to_string(v_parts, ', ');
    end if;
  end if;

  if new.shift_mode is distinct from old.shift_mode or new.max_students is distinct from old.max_students then
    for v_bad in
      select roll_number, shift from session_students where session_id = new.session_id and not is_deleted
    loop
      v_error := roll_block_error(new.shift_mode, new.max_students, v_bad.shift, v_bad.roll_number);
      if v_error is not null then
        raise exception '% Adjust the capacity or renumber that student first.', v_error;
      end if;
    end loop;
  end if;
  return new;
end $$;
create trigger trg_session_shift_change before update of shift_mode, max_students on academic_sessions
  for each row execute function fn_guard_session_shift_change();

-- Generic "this row's shift must be one its session runs" check for periods, fees and datesheets.
create or replace function fn_check_row_shift_allowed() returns trigger
language plpgsql set search_path = public as $$
declare
  v_row jsonb := to_jsonb(new);
  v_session text := coalesce(v_row ->> 'primary_session_id', v_row ->> 'session_id');
  v_shift shift := (v_row ->> 'shift')::shift;
begin
  if not session_allows_shift(v_session, v_shift) then
    raise exception 'This session does not run a % shift.', initcap(v_shift::text);
  end if;
  return new;
end $$;

-- ── Timetable ────────────────────────────────────────────────────────────────
alter table timetable_periods add column shift shift not null;
drop index uq_session_slot;
create unique index uq_session_slot on timetable_periods(primary_session_id, shift, day, start_time);
-- fn_check_timetable_conflict is unchanged: teacher/room clashes are checked across all sessions and shifts.
create trigger trg_period_shift before insert or update of primary_session_id, shift on timetable_periods
  for each row execute function fn_check_row_shift_allowed();

-- A merged lecture serves other sessions in the period's shift, so each linked session must run it.
create or replace function fn_check_period_link_shift() returns trigger
language plpgsql set search_path = public as $$
declare v_shift shift;
begin
  select shift into v_shift from timetable_periods where id = new.period_id;
  if v_shift is not null and not session_allows_shift(new.session_id, v_shift) then
    raise exception 'Session % does not run a % shift, so it cannot share this lecture.',
      new.session_id, initcap(v_shift::text);
  end if;
  return new;
end $$;
create trigger trg_period_link_shift before insert or update on period_sessions
  for each row execute function fn_check_period_link_shift();

-- ── Fees (one structure per session + shift; cadence per shift) ──────────────
alter table session_fee_heads drop constraint session_fee_heads_session_id_fkey;
alter table session_fee_heads drop constraint session_fee_heads_pkey;
alter table session_fees drop constraint session_fees_pkey;

alter table session_fees add column shift shift not null;
alter table session_fees add primary key (session_id, shift);
comment on column session_fees.cadence is 'ANNUAL or SEMESTER, chosen per shift (app default: Morning annual, Evening per semester).';

alter table session_fee_heads add column shift shift not null;
alter table session_fee_heads add primary key (session_id, shift, label);
alter table session_fee_heads add constraint session_fee_heads_session_shift_fkey
  foreign key (session_id, shift) references session_fees(session_id, shift) on delete cascade;

create trigger trg_session_fees_shift before insert or update of session_id, shift on session_fees
  for each row execute function fn_check_row_shift_allowed();

-- ── Datesheets (shift specific, decision 3) ──────────────────────────────────
alter table datesheets add column shift shift not null;
drop index uq_datesheet_session_semester;
create unique index uq_datesheet_session_semester_shift on datesheets(session_id, semester, shift);
create trigger trg_datesheet_shift before insert or update of session_id, shift on datesheets
  for each row execute function fn_check_row_shift_allowed();

-- ── Event / notification targeting (null = no restriction at that level) ─────
alter table calendar_events add column shift shift;
alter table calendar_events add constraint calendar_events_shift_needs_session
  check (shift is null or session_id is not null);
alter table notifications add column target_shift shift;
alter table notifications add constraint notifications_target_shift_needs_session
  check (target_shift is null or target_session_id is not null);

-- ── Helpers ──────────────────────────────────────────────────────────────────
create or replace function my_shift() returns shift
language sql stable security definer set search_path = public as $$
  select st.shift from profiles p
  join session_students st on st.session_id = p.linked_session_id and st.roll_number = p.linked_roll
  where p.id = auth.uid()
$$;

create or replace function my_dept() returns text
language sql stable security definer set search_path = public as $$
  select a.dept_id from profiles p join academic_sessions a on a.session_id = p.linked_session_id
  where p.id = auth.uid()
$$;

-- The caller teaches p_session in p_shift (own grid or a merged lecture linked to it).
create or replace function teaches_shift(p_session text, p_shift shift) returns boolean
language sql stable security definer set search_path = public as $$
  select exists (
    select 1 from timetable_periods tp
    left join period_sessions ps on ps.period_id = tp.id
    where tp.teacher_email = current_email()
      and tp.shift = p_shift
      and (tp.primary_session_id = p_session or ps.session_id = p_session))
$$;

-- The caller teaches this student's shift of this session.
create or replace function teaches_student(p_session text, p_roll text) returns boolean
language sql stable security definer set search_path = public as $$
  select exists (
    select 1 from session_students st
    where st.session_id = p_session and st.roll_number = p_roll
      and teaches_shift(p_session, st.shift))
$$;

-- ── RLS: teachers see/write only the shifts they teach ───────────────────────
drop policy sel_students on session_students;
create policy sel_students on session_students for select to authenticated
  using (is_admin() or teaches_student(session_id, roll_number)
         or (session_id = my_session() and roll_number = my_roll()));

drop policy sel_attendance on session_attendance;
create policy sel_attendance on session_attendance for select to authenticated
  using (is_admin() or teaches_student(session_id, roll_number)
         or (session_id = my_session() and roll_number = my_roll()));
drop policy ins_attendance on session_attendance;
create policy ins_attendance on session_attendance for insert to authenticated
  with check (is_active_teacher() and teaches_student(session_id, roll_number) and teacher_email = current_email());
drop policy upd_attendance on session_attendance;
create policy upd_attendance on session_attendance for update to authenticated
  using (is_active_teacher() and teaches_student(session_id, roll_number))
  with check (is_active_teacher() and teaches_student(session_id, roll_number));

drop policy sel_marks on session_marks;
create policy sel_marks on session_marks for select to authenticated
  using (is_admin() or teaches_student(session_id, roll_number)
         or (session_id = my_session() and roll_number = my_roll()));
drop policy ins_marks on session_marks;
create policy ins_marks on session_marks for insert to authenticated
  with check (is_admin() or (is_active_teacher() and teaches_student(session_id, roll_number)));

drop policy sel_gpa on student_semester_gpa;
create policy sel_gpa on student_semester_gpa for select to authenticated
  using (is_admin() or teaches_student(session_id, roll_number)
         or (session_id = my_session() and roll_number = my_roll()));

drop policy sel_fines on fines;
create policy sel_fines on fines for select to authenticated
  using (is_admin() or teaches_student(session_id, roll_number)
         or (session_id = my_session() and roll_number = my_roll()));

drop policy sel_attendance_edit_requests on attendance_edit_requests;
create policy sel_attendance_edit_requests on attendance_edit_requests for select to authenticated
  using (is_admin() or teaches_student(session_id, roll_number));
drop policy ins_attendance_edit_requests on attendance_edit_requests;
create policy ins_attendance_edit_requests on attendance_edit_requests for insert to authenticated
  with check (is_active_teacher() and teaches_student(session_id, roll_number) and requested_by = current_email());

drop policy sel_mark_edit_requests on mark_edit_requests;
create policy sel_mark_edit_requests on mark_edit_requests for select to authenticated
  using (is_admin() or teaches_student(session_id, roll_number));
drop policy ins_mark_edit_requests on mark_edit_requests;
create policy ins_mark_edit_requests on mark_edit_requests for insert to authenticated
  with check (is_active_teacher() and teaches_student(session_id, roll_number));

-- ── RLS: students see only their own shift's fee structure ───────────────────
drop policy sel_fees on session_fees;
create policy sel_fees on session_fees for select to authenticated
  using (is_admin() or is_active_teacher() or (session_id = my_session() and shift = my_shift()));
drop policy sel_fee_heads on session_fee_heads;
create policy sel_fee_heads on session_fee_heads for select to authenticated
  using (is_admin() or is_active_teacher() or (session_id = my_session() and shift = my_shift()));

-- ── RLS: progressive dept -> session -> shift scope for students ─────────────
-- Admins and active teachers keep seeing every event (teacher targeting is refined in Task 9).
drop policy sel_calendar on calendar_events;
create policy sel_calendar on calendar_events for select to authenticated
  using (is_admin() or is_active_teacher()
         or ((dept_id is null or dept_id = my_dept())
             and (session_id is null or session_id = my_session())
             and (shift is null or shift = my_shift())));

drop policy sel_notifications on notifications;
create policy sel_notifications on notifications for select to authenticated
  using ((expires_at is null or expires_at > now()) and (
    is_admin()
    or ((target_role is null or target_role in ('ALL', 'TEACHER')) and is_active_teacher())
    or ((target_role is null or target_role in ('ALL', 'STUDENT')) and my_session() is not null
        and (target_dept_id is null or target_dept_id = my_dept())
        and (target_session_id is null or target_session_id = my_session())
        and (target_shift is null or target_shift = my_shift()))
    or ((target_role is null or target_role = 'ALL')
        and target_dept_id is null and target_session_id is null)));

-- ── RPCs ─────────────────────────────────────────────────────────────────────
-- Results may only be recorded by an admin or a teacher of that student's shift.
create or replace function record_semester_result(
  p_session text, p_roll text, p_semester integer, p_gpa numeric, p_cgpa numeric,
  p_term_label text default null, p_result semester_result default 'PENDING',
  p_class_position integer default null, p_remarks text default null, p_supply text[] default null)
returns void
language plpgsql security definer set search_path = public as $$
begin
  if not (is_admin() or teaches_student(p_session, p_roll)) then raise exception 'not allowed'; end if;
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

-- The account-linking picker now also shows each roll number's shift. (Return type changes, so drop first.)
drop function available_roll_numbers(text);
create function available_roll_numbers(p_session text) returns table(roll_number text, shift shift)
language sql stable security definer set search_path = public as $$
  select st.roll_number, st.shift from session_students st
  where st.session_id = p_session and st.linked_email = '' and st.enrollment_status = 'ACTIVE'
    and not st.is_deleted
  order by st.shift, st.roll_number
$$;
-- approve_link_request and approve_attendance_edit_request are unchanged: both work on (session, roll).

-- ── Grants ───────────────────────────────────────────────────────────────────
-- Trigger functions: never an API endpoint.
revoke all on function fn_check_student_shift() from public, anon, authenticated;
revoke all on function fn_guard_session_shift_change() from public, anon, authenticated;
revoke all on function fn_check_row_shift_allowed() from public, anon, authenticated;
revoke all on function fn_check_period_link_shift() from public, anon, authenticated;

-- RLS helpers / RPCs: authenticated only.
do $$
declare fn text;
begin
  foreach fn in array array[
    'session_allows_shift(text, shift)', 'roll_block_error(shift_mode, int, shift, text)',
    'my_shift()', 'my_dept()', 'teaches_shift(text, shift)', 'teaches_student(text, text)',
    'available_roll_numbers(text)',
    'record_semester_result(text, text, integer, numeric, numeric, text, semester_result, integer, text, text[])'
  ] loop
    execute format('revoke all on function %s from public', fn);
    execute format('revoke all on function %s from anon', fn);
    execute format('grant execute on function %s to authenticated', fn);
  end loop;
end $$;
