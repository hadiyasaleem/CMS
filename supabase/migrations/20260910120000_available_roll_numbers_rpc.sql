-- ── Unlinked roster lookup for the account-linking roll-number picker ──────
-- sel_students (20260714000002_rls.sql) is scoped to admin/teacher/the caller's own linked
-- row (session_id = my_session() and roll_number = my_roll()), so a signed-up-but-not-yet-
-- linked account has no linked_session_id/linked_roll and can't read any session_students
-- row directly. This SECURITY DEFINER RPC intentionally bypasses that, but returns ONLY
-- roll numbers -- never name, CNIC, or any other column -- for unclaimed, active students in
-- one session, so the account-linking form can offer a roll-number dropdown instead of free
-- text without exposing classmates' personal data to an unverified caller.
create or replace function available_roll_numbers(p_session text) returns table(roll_number text)
language sql stable security definer set search_path = public as $$
  select roll_number from session_students
  where session_id = p_session and linked_email = '' and enrollment_status = 'ACTIVE'
  order by roll_number
$$;

grant execute on function available_roll_numbers(text) to authenticated;
