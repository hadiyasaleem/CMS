-- A teacher with only can_approve_link_requests (not is_admin) could never actually approve a
-- link request: the client wrote to session_students and profiles directly, but only
-- student_link_requests has a teacher_can('can_approve_link_requests') RLS exception -- the other
-- two writes silently matched 0 rows, while the final status='APPROVED' write succeeded. The
-- request ended up stuck APPROVED with the student never actually linked.
--
-- Fixed the same way record_semester_result already handles "a permitted, non-admin caller needs
-- to touch a narrow slice of otherwise admin-only tables, atomically": a security definer RPC that
-- performs every write itself, instead of broadening session_students/profiles' general RLS (both
-- hold other sensitive columns -- gpa/cgpa, role/status/email -- unrelated to this one action).
-- Also closes an adjacent gap: no status='PENDING' guard meant a double-approve or approving an
-- already-REJECTED request would silently re-apply.
create or replace function approve_link_request(p_request_id uuid, p_reviewed_by text)
returns void
language plpgsql security definer set search_path = public as $$
declare
  v_request record;
  v_previous_email text;
begin
  if not (is_admin() or teacher_can('can_approve_link_requests')) then
    raise exception 'not allowed';
  end if;

  select * into v_request from student_link_requests
    where request_id = p_request_id and status = 'PENDING';
  if not found then
    raise exception 'This request is no longer pending.';
  end if;

  select linked_email into v_previous_email from session_students
    where session_id = v_request.session_id and roll_number = v_request.roll_number_claimed;
  if not found then
    raise exception 'No student % in session % -- add that student to the roster first.',
      v_request.roll_number_claimed, v_request.session_id;
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

grant execute on function approve_link_request(uuid, text) to authenticated;
