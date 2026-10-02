-- Lets an admin track triage state for an app_logs row (New -> In progress -> Fixed, reopenable)
-- from the admin app's "App Logs" screen. app_logs has no UPDATE policy at all -- it was designed
-- append-only, immutable once written (see its original migration) -- so this is a definer function
-- rather than a broad UPDATE policy, the same way delete_notification/ingest_app_logs already work
-- around that for their own writes.
alter table app_logs add column status text not null default 'NEW';
alter table app_logs add constraint app_logs_status_check check (status in ('NEW', 'IN_PROGRESS', 'FIXED'));

create or replace function update_app_log_status(p_log_id text, p_status text) returns void
language plpgsql security definer set search_path = public as $$
begin
  if not is_admin() then
    raise exception 'Only an admin can change a log''s status.';
  end if;
  if p_status not in ('NEW', 'IN_PROGRESS', 'FIXED') then
    raise exception 'Invalid status.';
  end if;
  update app_logs set status = p_status where log_id = p_log_id;
  if not found then
    raise exception 'This log entry no longer exists.';
  end if;
end $$;
revoke all on function update_app_log_status(text, text) from public, anon;
grant execute on function update_app_log_status(text, text) to authenticated, service_role;
