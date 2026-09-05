-- Reconstructed from live history: "guard_profile_exempt_service_role" (applied 2026-07-15 06:51:39 UTC,
-- untracked locally). Recovered via pg_get_functiondef()/information_schema.triggers against the live
-- project (ygmvyvjhdkxddkqxtrdw) as part of a migration-folder reconciliation pass -- not applied here,
-- the live DB already has it; this file only backfills local history.
--
-- Guards privileged columns on `profiles` (role, email, status, teacher_email, linked_session_id,
-- linked_roll) so a non-admin can only touch their own last_login_at/notification_prefs -- except
-- service_role (used by trusted server-side flows, e.g. edge functions) and admins, who are exempt.
create or replace function fn_guard_profile_update()
returns trigger
language plpgsql
security definer
set search_path to 'public'
as $$
begin
  if coalesce(auth.jwt() ->> 'role', '') = 'service_role' then return new; end if;
  if is_admin() then return new; end if;
  if new.role is distinct from old.role
     or new.email is distinct from old.email
     or new.status is distinct from old.status
     or new.teacher_email is distinct from old.teacher_email
     or new.linked_session_id is distinct from old.linked_session_id
     or new.linked_roll is distinct from old.linked_roll then
    raise exception 'not allowed to modify privileged profile columns';
  end if;
  return new;
end
$$;

create trigger trg_guard_profile before update on profiles
  for each row execute function fn_guard_profile_update();
