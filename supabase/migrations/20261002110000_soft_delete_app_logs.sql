-- Lets an admin clear the "App Logs" viewer in bulk. `app_logs` was deliberately designed with no
-- is_deleted column ("rows are never deleted by clients" -- see its original migration), but the
-- triage workflow added since (NEW -> IN_PROGRESS -> FIXED) means the list now needs a way to
-- clear out everything that's been dealt with, without losing the rows for audit/history. A soft
-- delete keeps that: the row stays, it just stops showing up.
--
-- `app_logs` has no UPDATE policy at all (by design, same reason as update_app_log_status), so this
-- goes through a definer function rather than a broad UPDATE policy.
alter table app_logs add column is_deleted boolean not null default false;
alter table app_logs add column deleted_at timestamptz;
alter table app_logs add column deleted_by text;

-- Deleted rows stop showing up for admins too -- that's the point of clearing the viewer.
drop policy sel_app_logs on app_logs;
create policy sel_app_logs on app_logs for select to authenticated
  using (is_admin() and not is_deleted);

create or replace function delete_all_app_logs() returns void
language plpgsql security definer set search_path = public as $$
begin
  if not is_admin() then
    raise exception 'Only an admin can clear the app logs.';
  end if;
  update app_logs set is_deleted = true, deleted_at = now(), deleted_by = current_email()
   where not is_deleted;
end $$;
revoke all on function delete_all_app_logs() from public, anon;
grant execute on function delete_all_app_logs() to authenticated, service_role;
