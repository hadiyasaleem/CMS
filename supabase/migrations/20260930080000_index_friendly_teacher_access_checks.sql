-- 20260930060000_timetable_period_co_teachers.sql made the "does this teacher teach here" checks read
-- `current_email() = any(period_teachers_of(...))`. That expression cannot use the index on teacher_email, so
-- every row a teacher's query touched (session_students, attendance, marks, ... all go through teaches_shift)
-- scanned the whole timetable: loading one teacher's students took ~7.4 s against an 8 s statement limit and
-- the teacher app reported "Couldn't refresh students (timed out)".
--
-- Same rules, written so Postgres can use an index again: the main teacher through the existing
-- idx_periods_teacher, co-teachers through a new GIN index on co_teacher_emails.
create index if not exists idx_periods_co_teachers on timetable_periods using gin (co_teacher_emails);

create or replace function teaches(p_session text) returns boolean
language sql stable security definer set search_path = public as $$
  select exists (
    select 1 from timetable_periods tp
    left join period_sessions ps on ps.period_id = tp.id
    where (tp.teacher_email = current_email() or tp.co_teacher_emails @> array[current_email()])
      and (tp.primary_session_id = p_session or ps.session_id = p_session))
$$;

create or replace function teaches_shift(p_session text, p_shift shift) returns boolean
language sql stable security definer set search_path = public as $$
  select exists (
    select 1 from timetable_periods tp
    left join period_sessions ps on ps.period_id = tp.id
    where (tp.teacher_email = current_email() or tp.co_teacher_emails @> array[current_email()])
      and tp.shift = p_shift
      and (tp.primary_session_id = p_session or ps.session_id = p_session))
$$;

create or replace function teacher_reaches(p_dept text, p_session text, p_shift shift) returns boolean
language sql stable security definer set search_path = public as $$
  select case
    when p_session is not null then exists (
      select 1 from timetable_periods tp
      left join period_sessions ps on ps.period_id = tp.id
      where (tp.teacher_email = current_email() or tp.co_teacher_emails @> array[current_email()])
        and (tp.primary_session_id = p_session or ps.session_id = p_session)
        and (p_shift is null or tp.shift = p_shift))
    when p_dept is not null then
      exists (select 1 from teachers t where t.email = current_email() and t.dept_id = p_dept)
      or exists (
        select 1 from timetable_periods tp
        left join period_sessions ps on ps.period_id = tp.id
        join academic_sessions a on a.session_id in (tp.primary_session_id, ps.session_id)
        where (tp.teacher_email = current_email() or tp.co_teacher_emails @> array[current_email()])
          and a.dept_id = p_dept)
    else true
  end
$$;
