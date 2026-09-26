-- Performance advisor fixes; no change to who can read or write what.
--
-- 1. multiple_permissive_policies: each admin "FOR ALL" policy also applied to SELECT, next to the table's own
--    select policy. Every one of those select policies already admits admins (is_admin() or true), so the admin
--    policy is split into INSERT / UPDATE / DELETE and SELECT is left to the single select policy.
-- 2. auth_rls_initplan: profiles policies call auth.uid() once per statement via (select auth.uid()).

do $$
declare
  p record;
begin
  for p in
    select tablename, policyname from pg_policies
    where schemaname = 'public' and cmd = 'ALL' and qual = 'is_admin()' and with_check = 'is_admin()'
      and tablename in (
        'academic_sessions', 'buildings', 'calendar_events', 'datesheet_slots', 'datesheets', 'departments',
        'fee_overrides', 'fines', 'period_sessions', 'rooms', 'semester_terms', 'session_fee_heads',
        'session_fees', 'session_students', 'session_subjects', 'student_semester_gpa', 'teachers',
        'timetable_periods')
  loop
    execute format('drop policy %I on public.%I', p.policyname, p.tablename);
    execute format('create policy %I on public.%I for insert to authenticated with check (is_admin())', p.policyname || '_ins', p.tablename);
    execute format('create policy %I on public.%I for update to authenticated using (is_admin()) with check (is_admin())', p.policyname || '_upd', p.tablename);
    execute format('create policy %I on public.%I for delete to authenticated using (is_admin())', p.policyname || '_del', p.tablename);
  end loop;
end $$;

-- profiles: one select and one update policy, each covering the owner and admins.
drop policy adm_profiles on profiles;
drop policy sel_profiles on profiles;
drop policy upd_profiles_own on profiles;
create policy sel_profiles on profiles for select to authenticated
  using (id = (select auth.uid()) or is_admin());
create policy upd_profiles on profiles for update to authenticated
  using (id = (select auth.uid()) or is_admin())
  with check (id = (select auth.uid()) or is_admin());
create policy ins_profiles_admin on profiles for insert to authenticated with check (is_admin());
create policy del_profiles_admin on profiles for delete to authenticated using (is_admin());
