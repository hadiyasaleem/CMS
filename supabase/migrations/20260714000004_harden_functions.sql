-- Advisor hardening (applied after the initial deploy):
--  • pin search_path on the remaining functions that lacked it
--  • strip the default PUBLIC/anon EXECUTE grant off SECURITY DEFINER helpers so they
--    aren't callable via /rest/v1/rpc by unauthenticated users. The RLS helpers stay
--    executable by `authenticated` (policies evaluate them as the querying role); the pure
--    trigger functions are revoked from everyone.

alter function fn_touch_updated_at() set search_path = public;
alter function fn_enforce_roster_cap() set search_path = public;
alter function fn_check_timetable_conflict() set search_path = public;
alter function bootstrap_admin_email() set search_path = public;

-- Trigger functions: never an API endpoint.
revoke all on function fn_touch_updated_at() from public, anon, authenticated;
revoke all on function fn_enforce_roster_cap() from public, anon, authenticated;
revoke all on function fn_check_timetable_conflict() from public, anon, authenticated;
revoke all on function fn_guard_profile_update() from public, anon, authenticated;
revoke all on function fn_handle_new_user() from public, anon, authenticated;

-- RLS helpers + the marks RPC: authenticated only (drop public + anon).
do $$
declare fn text;
begin
  foreach fn in array array[
    'current_email()', 'is_admin()', 'is_active_teacher()', 'teacher_can(text)',
    'teaches(text)', 'my_session()', 'my_roll()', 'bootstrap_admin_email()',
    'record_semester_result(text, text, int, numeric, numeric, text, semester_result, int, text)'
  ] loop
    execute format('revoke all on function %s from public, anon', fn);
  end loop;
end $$;

grant execute on function current_email() to authenticated;
grant execute on function is_admin() to authenticated;
grant execute on function is_active_teacher() to authenticated;
grant execute on function teacher_can(text) to authenticated;
grant execute on function teaches(text) to authenticated;
grant execute on function my_session() to authenticated;
grant execute on function my_roll() to authenticated;
grant execute on function record_semester_result(text, text, int, numeric, numeric, text, semester_result, int, text) to authenticated;
