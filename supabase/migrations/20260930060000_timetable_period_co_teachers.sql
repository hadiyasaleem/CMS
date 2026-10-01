-- A timetable slot can be taught by several teachers at once (e.g. a project supervised by more than one
-- teacher). `teacher_email` stays the main teacher, so older app versions keep working; the others are
-- listed in co_teacher_emails (with co_teacher_names alongside, the same snapshot teacher_name is for the
-- main teacher). Every teacher on a slot teaches the class: they see it, take its attendance and enter
-- its marks, and none of them can be double-booked.

alter table timetable_periods
  add column if not exists co_teacher_emails text[] not null default '{}',
  add column if not exists co_teacher_names  text[] not null default '{}';

-- The main teacher and the co-teachers of a period, without blanks.
create or replace function period_teachers_of(p_main text, p_co text[]) returns text[]
language sql immutable set search_path = public as $$
  select array_remove(array[p_main] || coalesce(p_co, '{}'::text[]), null)
$$;

-- ── The caller teaches a session / shift / audience through any period they are on ────────────────────────
create or replace function teaches(p_session text) returns boolean
language sql stable security definer set search_path = public as $$
  select exists (
    select 1 from timetable_periods tp
    left join period_sessions ps on ps.period_id = tp.id
    where current_email() = any(period_teachers_of(tp.teacher_email, tp.co_teacher_emails))
      and (tp.primary_session_id = p_session or ps.session_id = p_session))
$$;

create or replace function teaches_shift(p_session text, p_shift shift) returns boolean
language sql stable security definer set search_path = public as $$
  select exists (
    select 1 from timetable_periods tp
    left join period_sessions ps on ps.period_id = tp.id
    where current_email() = any(period_teachers_of(tp.teacher_email, tp.co_teacher_emails))
      and tp.shift = p_shift
      and (tp.primary_session_id = p_session or ps.session_id = p_session))
$$;

create or replace function teacher_reaches(p_dept text, p_session text, p_shift shift) returns boolean
language sql stable security definer set search_path = public as $$
  select case
    when p_session is not null then exists (
      select 1 from timetable_periods tp
      left join period_sessions ps on ps.period_id = tp.id
      where current_email() = any(period_teachers_of(tp.teacher_email, tp.co_teacher_emails))
        and (tp.primary_session_id = p_session or ps.session_id = p_session)
        and (p_shift is null or tp.shift = p_shift))
    when p_dept is not null then
      exists (select 1 from teachers t where t.email = current_email() and t.dept_id = p_dept)
      or exists (
        select 1 from timetable_periods tp
        left join period_sessions ps on ps.period_id = tp.id
        join academic_sessions a on a.session_id in (tp.primary_session_id, ps.session_id)
        where current_email() = any(period_teachers_of(tp.teacher_email, tp.co_teacher_emails))
          and a.dept_id = p_dept)
    else true
  end
$$;

-- ── No teacher (main or co-) can be on two overlapping lectures ──────────────────────────────────────────
-- Same rules and wording as before (LECTURE only, soft-deleted rows ignored, effective-date ranges must
-- overlap); a clash is now any teacher the two periods have in common.
create or replace function fn_check_timetable_conflict() returns trigger
language plpgsql set search_path = public as $$
declare
  v_other timetable_periods%rowtype;
  v_mine  text[] := period_teachers_of(new.teacher_email, new.co_teacher_emails);
  v_clash text;
begin
  if new.period_type <> 'LECTURE' then return new; end if;

  if cardinality(v_mine) > 0 then
    select p.* into v_other from timetable_periods p
    where p.id <> new.id and p.period_type = 'LECTURE' and not p.is_deleted
      and period_teachers_of(p.teacher_email, p.co_teacher_emails) && v_mine and p.day = new.day
      and p.start_time < new.end_time and new.start_time < p.end_time
      and coalesce(p.effective_from, '-infinity'::date) <= coalesce(new.effective_to, 'infinity'::date)
      and coalesce(new.effective_from, '-infinity'::date) <= coalesce(p.effective_to, 'infinity'::date)
    limit 1;
    if found then
      select t into v_clash from unnest(v_mine) t
      where t = any(period_teachers_of(v_other.teacher_email, v_other.co_teacher_emails)) limit 1;
      raise exception '%', format(
        'Teacher %s already has an overlapping lecture: %s%s for %s on %s %s–%s.',
        msg_teacher_label(v_clash),
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

-- A co-teacher is always one of the college's teachers.
create or replace function fn_check_period_co_teachers() returns trigger
language plpgsql set search_path = public as $$
begin
  if cardinality(new.co_teacher_emails) > 0 then
    if new.teacher_email is null then
      raise exception 'Choose the main teacher before adding other teachers to this period.';
    end if;
    if new.teacher_email = any(new.co_teacher_emails) then
      raise exception 'The main teacher is also listed as an additional teacher on this period.';
    end if;
    if (select count(distinct e) from unnest(new.co_teacher_emails) e) <> cardinality(new.co_teacher_emails) then
      raise exception 'A teacher is listed twice on this period.';
    end if;
    if exists (
      select 1 from unnest(new.co_teacher_emails) e where not exists (select 1 from teachers t where t.email = e)
    ) then
      raise exception 'One of the additional teachers is not a teacher at this college.';
    end if;
  end if;
  return new;
end $$;

drop trigger if exists trg_period_co_teachers on timetable_periods;
create trigger trg_period_co_teachers before insert or update of teacher_email, co_teacher_emails on timetable_periods
  for each row execute function fn_check_period_co_teachers();

revoke all on function period_teachers_of(text, text[]) from public, anon;
grant execute on function period_teachers_of(text, text[]) to authenticated, service_role;
revoke all on function fn_check_period_co_teachers() from public, anon, authenticated;
