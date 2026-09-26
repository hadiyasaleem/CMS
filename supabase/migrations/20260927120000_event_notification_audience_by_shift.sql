-- Task 9: event and notification targeting by Department -> Session -> Shift.
--
-- A null level means "no restriction at that level":
--   nothing set            -> college-wide
--   department             -> every session and both shifts of it
--   department + session   -> both shifts of that session
--   ... + shift            -> that shift only
--
-- Students were already scoped in 20260926120000. This migration narrows teachers: a teacher sees an item
-- when it is college-wide, targets their home department or a department they teach in, or targets a
-- session (and shift) they teach. Authors always see what they sent. Delivery stays in-app.

-- The caller (a teacher) is reached by a (dept, session, shift) target.
create or replace function teacher_reaches(p_dept text, p_session text, p_shift shift) returns boolean
language sql stable security definer set search_path = public as $$
  select case
    when p_session is not null then exists (
      select 1 from timetable_periods tp
      left join period_sessions ps on ps.period_id = tp.id
      where tp.teacher_email = current_email()
        and (tp.primary_session_id = p_session or ps.session_id = p_session)
        and (p_shift is null or tp.shift = p_shift))
    when p_dept is not null then
      exists (select 1 from teachers t where t.email = current_email() and t.dept_id = p_dept)
      or exists (
        select 1 from timetable_periods tp
        left join period_sessions ps on ps.period_id = tp.id
        join academic_sessions a on a.session_id in (tp.primary_session_id, ps.session_id)
        where tp.teacher_email = current_email() and a.dept_id = p_dept)
    else true
  end
$$;

revoke all on function teacher_reaches(text, text, shift) from public;
revoke all on function teacher_reaches(text, text, shift) from anon;
grant execute on function teacher_reaches(text, text, shift) to authenticated;

-- ── Calendar: teachers see events for what they teach (or wider) ─────────────
drop policy sel_calendar on calendar_events;
create policy sel_calendar on calendar_events for select to authenticated
  using (is_admin()
         or (is_active_teacher() and teacher_reaches(dept_id, session_id, shift))
         or ((dept_id is null or dept_id = my_dept())
             and (session_id is null or session_id = my_session())
             and (shift is null or shift = my_shift())));

-- ── Notifications: same rule; authors keep seeing their own notices ──────────
drop policy sel_notifications on notifications;
create policy sel_notifications on notifications for select to authenticated
  using ((expires_at is null or expires_at > now()) and (
    is_admin()
    or created_by_email = current_email()
    or ((target_role is null or target_role in ('ALL', 'TEACHER')) and is_active_teacher()
        and teacher_reaches(target_dept_id, target_session_id, target_shift))
    or ((target_role is null or target_role in ('ALL', 'STUDENT')) and my_session() is not null
        and (target_dept_id is null or target_dept_id = my_dept())
        and (target_session_id is null or target_session_id = my_session())
        and (target_shift is null or target_shift = my_shift()))
    or ((target_role is null or target_role = 'ALL')
        and target_dept_id is null and target_session_id is null)));

-- A teacher may only notify classes they teach (admins are unrestricted).
drop policy ins_notifications on notifications;
create policy ins_notifications on notifications for insert to authenticated
  with check (created_by_email = current_email() and (
    is_admin()
    or (teacher_can('can_send_notifications') and target_session_id is not null
        and teacher_reaches(target_dept_id, target_session_id, target_shift))));
