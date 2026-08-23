-- ═══════════════════════════════════════════════════════════════════════════
-- RLS — translates firestore.rules RBAC + the v2 rules.
-- Identity = normalized email in the JWT (matches SessionManager.accountKey).
-- Helpers are SECURITY DEFINER (owner bypasses RLS internally → no recursion).
-- Key invariants:
--   • admin can READ attendance but has NO write policy on it (cannot forge)
--   • students see only their own session's rows — directly and through views
--     (all views are security_invoker, see schema migration)
--   • teacher GPA writes go through the record_semester_result RPC only
-- ═══════════════════════════════════════════════════════════════════════════

-- ── Helper functions ────────────────────────────────────────────────────────
create or replace function current_email() returns text
language sql stable security definer set search_path = public as
$$ select lower(coalesce(auth.jwt() ->> 'email', '')) $$;

-- The single designated bootstrap admin — mirrored in cmsadmin LoginViewModel.
create or replace function bootstrap_admin_email() returns text
language sql immutable as $$ select 'admin@example.com' $$;

create or replace function is_admin() returns boolean
language sql stable security definer set search_path = public as $$
  select current_email() = bootstrap_admin_email()
      or exists (select 1 from profiles p
                 where p.id = auth.uid() and p.role = 'ADMIN' and p.status = 'ACTIVE')
      or exists (select 1 from teachers t
                 where t.email = current_email() and t.is_admin
                   and t.status = 'ACTIVE' and t.is_active)
$$;

create or replace function is_active_teacher() returns boolean
language sql stable security definer set search_path = public as $$
  select exists (select 1 from teachers t
                 where t.email = current_email()
                   and t.status = 'ACTIVE' and t.is_active)
$$;

create or replace function teacher_can(flag text) returns boolean
language plpgsql stable security definer set search_path = public as $$
declare ok boolean;
begin
  execute format(
    'select exists (select 1 from teachers t where t.email = $1
       and t.status = ''ACTIVE'' and t.is_active and t.%I)', flag)
  into ok using current_email();
  return coalesce(ok, false);
end $$;

-- Teacher teaches this session: any LECTURE of theirs on its grid (incl. merged links).
create or replace function teaches(p_session text) returns boolean
language sql stable security definer set search_path = public as $$
  select exists (
    select 1 from timetable_periods tp
    left join period_sessions ps on ps.period_id = tp.id
    where tp.teacher_email = current_email()
      and (tp.primary_session_id = p_session or ps.session_id = p_session))
$$;

create or replace function my_session() returns text
language sql stable security definer set search_path = public as
$$ select linked_session_id from profiles where id = auth.uid() $$;

create or replace function my_roll() returns text
language sql stable security definer set search_path = public as
$$ select linked_roll from profiles where id = auth.uid() $$;

-- ── Atomic GPA write path (teachers/admin) — column-safe alternative to a raw
--    UPDATE on session_students (RLS cannot restrict columns).
create or replace function record_semester_result(
  p_session text, p_roll text, p_semester int,
  p_gpa numeric, p_cgpa numeric,
  p_term_label text default null,
  p_result semester_result default 'PENDING',
  p_class_position int default null,
  p_remarks text default null
) returns void
language plpgsql security definer set search_path = public as $$
begin
  if not (is_admin() or teaches(p_session)) then
    raise exception 'not allowed';
  end if;
  insert into student_semester_gpa
    (session_id, roll_number, semester, gpa, cgpa, term_label, result_status,
     class_position, remarks, created_by, updated_by)
  values
    (p_session, p_roll, p_semester, p_gpa, p_cgpa, p_term_label, p_result,
     p_class_position, p_remarks, current_email(), current_email())
  on conflict (session_id, roll_number, semester) do update set
    gpa = excluded.gpa, cgpa = excluded.cgpa, term_label = excluded.term_label,
    result_status = excluded.result_status, class_position = excluded.class_position,
    remarks = excluded.remarks, updated_by = current_email(), updated_at = now();
  -- refresh the current snapshot only when this is the latest recorded semester
  update session_students st set gpa = p_gpa, cgpa = p_cgpa, updated_at = now()
  where st.session_id = p_session and st.roll_number = p_roll
    and not exists (select 1 from student_semester_gpa g
                    where g.session_id = p_session and g.roll_number = p_roll
                      and g.semester > p_semester);
end $$;

grant execute on function current_email, is_admin, is_active_teacher, my_session, my_roll to authenticated;
grant execute on function teacher_can(text) to authenticated;
grant execute on function teaches(text) to authenticated;
grant execute on function record_semester_result(text, text, int, numeric, numeric, text, semester_result, int, text) to authenticated;

-- ── Enable RLS everywhere (no anon policies anywhere → anon sees nothing) ───
do $$
declare t text;
begin
  foreach t in array array[
    'departments','teachers','profiles','academic_sessions','session_subjects',
    'session_students','timetable_periods','period_sessions','session_attendance',
    'session_marks','student_semester_gpa','session_fees','session_fee_heads',
    'fee_overrides','fines','datesheets','datesheet_slots','calendar_events',
    'documents','exam_paper_submissions','student_link_requests','notifications'
  ] loop
    execute format('alter table %I enable row level security', t);
  end loop;
end $$;

-- ── Reference data: any signed-in user reads; admin writes ──────────────────
create policy sel_departments on departments for select to authenticated using (true);
create policy adm_departments on departments for all to authenticated
  using (is_admin()) with check (is_admin());

create policy sel_teachers on teachers for select to authenticated using (true);
create policy adm_teachers on teachers for all to authenticated
  using (is_admin()) with check (is_admin());

create policy sel_sessions on academic_sessions for select to authenticated using (true);
create policy adm_sessions on academic_sessions for all to authenticated
  using (is_admin()) with check (is_admin());

create policy sel_subjects on session_subjects for select to authenticated using (true);
create policy adm_subjects on session_subjects for all to authenticated
  using (is_admin()) with check (is_admin());

-- ── profiles: own row + admin ───────────────────────────────────────────────
-- NB: column-level GRANTs can't limit self-service here because admins share the
-- `authenticated` Postgres role — a column grant would fence admins in too. So a
-- BEFORE-UPDATE trigger guards the privileged columns for non-admins instead.
create policy sel_profiles on profiles for select to authenticated
  using (id = auth.uid() or is_admin());
create policy upd_profiles_own on profiles for update to authenticated
  using (id = auth.uid()) with check (id = auth.uid());
create policy adm_profiles on profiles for all to authenticated
  using (is_admin()) with check (is_admin());

create or replace function fn_guard_profile_update() returns trigger
language plpgsql security definer set search_path = public as $$
begin
  -- Trusted backend (Edge Functions using the service-role key) may set privileged columns:
  -- admin-create-user promotes a new profile to TEACHER/ADMIN, set-teacher-status flips status,
  -- revoke-student-link clears the link. This is a TRIGGER (fires even for service-role writes,
  -- which bypass RLS but not triggers) and is_admin() is false without an admin JWT, so exempt
  -- the service role explicitly.
  if coalesce(auth.jwt() ->> 'role', '') = 'service_role' then return new; end if;
  if is_admin() then return new; end if;
  -- A non-admin may only change last_login_at / notification_prefs of their own row.
  if new.role is distinct from old.role
     or new.email is distinct from old.email
     or new.status is distinct from old.status
     or new.teacher_email is distinct from old.teacher_email
     or new.linked_session_id is distinct from old.linked_session_id
     or new.linked_roll is distinct from old.linked_roll then
    raise exception 'not allowed to modify privileged profile columns';
  end if;
  return new;
end $$;
create trigger trg_guard_profile before update on profiles
  for each row execute function fn_guard_profile_update();

-- ── Roster: admin + teacher-of-session + the student's own row ──────────────
create policy sel_students on session_students for select to authenticated
  using (is_admin() or teaches(session_id)
         or (session_id = my_session() and roll_number = my_roll()));
create policy adm_students on session_students for all to authenticated
  using (is_admin()) with check (is_admin());
-- (teacher gpa/cgpa writes ONLY via record_semester_result — no direct policy)

-- ── Timetable: everyone signed-in reads; admin or permitted teacher writes ──
create policy sel_periods on timetable_periods for select to authenticated using (true);
create policy wr_periods on timetable_periods for all to authenticated
  using (is_admin() or teacher_can('can_edit_timetable'))
  with check (is_admin() or teacher_can('can_edit_timetable'));
create policy sel_period_sessions on period_sessions for select to authenticated using (true);
create policy wr_period_sessions on period_sessions for all to authenticated
  using (is_admin() or teacher_can('can_edit_timetable'))
  with check (is_admin() or teacher_can('can_edit_timetable'));

-- ── Attendance: teacher-of-session writes; admin READS ONLY; student own ────
create policy sel_attendance on session_attendance for select to authenticated
  using (is_admin() or teaches(session_id)
         or (session_id = my_session() and roll_number = my_roll()));
create policy ins_attendance on session_attendance for insert to authenticated
  with check (is_active_teacher() and teaches(session_id)
              and teacher_email = current_email());
create policy upd_attendance on session_attendance for update to authenticated
  using (is_active_teacher() and teaches(session_id))
  with check (is_active_teacher() and teaches(session_id));
-- no delete policy, no admin write policy: attendance cannot be forged or erased

-- ── Marks: teacher-of-session writes; admin + student read ──────────────────
create policy sel_marks on session_marks for select to authenticated
  using (is_admin() or teaches(session_id)
         or (session_id = my_session() and roll_number = my_roll()));
create policy ins_marks on session_marks for insert to authenticated
  with check ((is_admin() or (is_active_teacher() and teaches(session_id))));
create policy upd_marks on session_marks for update to authenticated
  using (is_admin() or (is_active_teacher() and teaches(session_id)))
  with check (is_admin() or (is_active_teacher() and teaches(session_id)));

-- ── GPA history: read tiers; writes via RPC (definer) or admin directly ─────
create policy sel_gpa on student_semester_gpa for select to authenticated
  using (is_admin() or teaches(session_id)
         or (session_id = my_session() and roll_number = my_roll()));
create policy adm_gpa on student_semester_gpa for all to authenticated
  using (is_admin()) with check (is_admin());

-- ── Fees & fines: student sees own session/rows; admin writes ───────────────
create policy sel_fees on session_fees for select to authenticated
  using (is_admin() or is_active_teacher() or session_id = my_session());
create policy adm_fees on session_fees for all to authenticated
  using (is_admin()) with check (is_admin());
create policy sel_fee_heads on session_fee_heads for select to authenticated
  using (is_admin() or is_active_teacher() or session_id = my_session());
create policy adm_fee_heads on session_fee_heads for all to authenticated
  using (is_admin()) with check (is_admin());
create policy sel_overrides on fee_overrides for select to authenticated
  using (is_admin() or (session_id = my_session() and roll_number = my_roll()));
create policy adm_overrides on fee_overrides for all to authenticated
  using (is_admin()) with check (is_admin());
create policy sel_fines on fines for select to authenticated
  using (is_admin() or teaches(session_id)
         or (session_id = my_session() and roll_number = my_roll()));
create policy adm_fines on fines for all to authenticated
  using (is_admin()) with check (is_admin());

-- ── Datesheets: published readable; admin/permitted teacher author ──────────
create policy sel_datesheets on datesheets for select to authenticated
  using (published or is_admin() or teacher_can('can_manage_datesheets'));
create policy wr_datesheets on datesheets for all to authenticated
  using (is_admin() or teacher_can('can_manage_datesheets'))
  with check (is_admin() or teacher_can('can_manage_datesheets'));
create policy sel_ds_slots on datesheet_slots for select to authenticated
  using (exists (select 1 from datesheets d where d.id = datesheet_id
                 and (d.published or is_admin() or teacher_can('can_manage_datesheets'))));
create policy wr_ds_slots on datesheet_slots for all to authenticated
  using (is_admin() or teacher_can('can_manage_datesheets'))
  with check (is_admin() or teacher_can('can_manage_datesheets'));

-- ── Calendar & documents: college-wide reads; admin writes ──────────────────
create policy sel_calendar on calendar_events for select to authenticated using (true);
create policy adm_calendar on calendar_events for all to authenticated
  using (is_admin()) with check (is_admin());
create policy sel_documents on documents for select to authenticated
  using (published or is_admin());
create policy adm_documents on documents for all to authenticated
  using (is_admin()) with check (is_admin());

-- ── Exam papers: teacher uploads own; admin reviews/downloads all ────────────
create policy sel_papers on exam_paper_submissions for select to authenticated
  using (is_admin() or teacher_email = current_email());
create policy ins_papers on exam_paper_submissions for insert to authenticated
  with check (is_active_teacher() and teaches(session_id)
              and teacher_email = current_email());
create policy upd_papers on exam_paper_submissions for update to authenticated
  using (is_admin() or teacher_email = current_email())
  with check (is_admin() or teacher_email = current_email());
create policy del_papers on exam_paper_submissions for delete to authenticated
  using (is_admin() or teacher_email = current_email());

-- ── Link requests: student files own; admin/permitted teacher reviews ───────
create policy sel_link_requests on student_link_requests for select to authenticated
  using (is_admin() or teacher_can('can_approve_link_requests')
         or requested_by_email = current_email());
create policy ins_link_requests on student_link_requests for insert to authenticated
  with check (requested_by_email = current_email());
create policy upd_link_requests on student_link_requests for update to authenticated
  using (is_admin() or teacher_can('can_approve_link_requests'))
  with check (is_admin() or teacher_can('can_approve_link_requests'));
create policy del_link_requests on student_link_requests for delete to authenticated
  using (is_admin());

-- ── Notifications: audience-filtered reads; permitted senders write ─────────
create policy sel_notifications on notifications for select to authenticated
  using (
    (expires_at is null or expires_at > now())
    and (
      is_admin()
      or target_role is null or target_role = 'ALL'
      or (target_role = 'TEACHER' and is_active_teacher())
      or (target_role = 'STUDENT' and my_session() is not null
          and (target_session_id is null or target_session_id = my_session()))
      or (target_role = 'ADMIN' and is_admin())
    )
  );
create policy ins_notifications on notifications for insert to authenticated
  with check ((is_admin() or teacher_can('can_send_notifications'))
              and created_by_email = current_email());
create policy del_notifications on notifications for delete to authenticated
  using (is_admin() or created_by_email = current_email());
