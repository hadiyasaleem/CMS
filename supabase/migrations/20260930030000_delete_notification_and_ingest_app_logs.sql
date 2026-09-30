-- Two server-side functions that replace client writes Row Level Security was silently refusing
-- (found by the remote-write audit, see Documentation/remote-write-audit.md).
--
-- NOT YET APPLIED to the live project: apply this BEFORE releasing app versions that call the functions, otherwise
-- notification delete and log upload fail with "function not found".
--
-- 1) Deleting a notification.
--    The apps soft-delete (`update notifications set is_deleted = true`), but `notifications` has no UPDATE policy, so the
--    statement matched 0 rows for everyone -- the app dropped the notification locally and reported "deleted" while the
--    server row stayed. A broad UPDATE policy would let an author rewrite the audience their own INSERT policy restricts,
--    so the soft-delete is exposed as a definer function instead. The soft delete (not a hard DELETE) is kept on purpose:
--    other devices learn about a deletion from the row's newer updated_at.
create or replace function delete_notification(p_id uuid) returns void
language plpgsql security definer set search_path = public as $$
begin
  update notifications set is_deleted = true
   where id = p_id and (is_admin() or created_by_email = current_email());
  if not found then
    raise exception 'You can only delete notifications you sent, and this one no longer exists.';
  end if;
end $$;
revoke all on function delete_notification(uuid) from public, anon;
grant execute on function delete_notification(uuid) to authenticated, service_role;

-- 2) Uploading buffered app logs.
--    The client re-sends a batch when the local delete after a successful upload did not commit. `app_logs` has INSERT
--    and SELECT policies only, so the same log_id arriving twice was refused (42501) for both DO UPDATE and DO NOTHING,
--    and that batch -- plus everything queued behind it -- could never flush. This function is idempotent on log_id and
--    keeps the INSERT policy's rule: a caller can only store logs for their own account (or with no account).
create or replace function ingest_app_logs(p_rows jsonb) returns void
language sql security definer set search_path = public as $$
  insert into app_logs (log_id, occurred_at, severity, kind, tag, message, stack_trace, account_email, app_id, app_version, platform, device_info)
  select r.log_id, r.occurred_at, r.severity, r.kind, r.tag, r.message, r.stack_trace, r.account_email, r.app_id, r.app_version, r.platform, r.device_info
    from jsonb_to_recordset(p_rows) as r(log_id text, occurred_at timestamptz, severity text, kind text, tag text, message text,
         stack_trace text, account_email text, app_id text, app_version text, platform text, device_info text)
   where r.account_email is null or r.account_email = current_email()
  on conflict (log_id) do nothing
$$;
revoke all on function ingest_app_logs(jsonb) from public, anon;
grant execute on function ingest_app_logs(jsonb) to authenticated, service_role;
