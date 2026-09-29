-- Backfill of changes that exist on the live project but in no repo migration, found by diffing every public
-- function, trigger, policy, index, column, constraint, enum, view and RLS flag of a from-scratch replay against
-- production (see Documentation/local-test-tools.md, "Known drift from production").
--
-- Every statement here is a NO-OP on the live database (it already has exactly this state) and makes a fresh
-- database match it. Nothing in this file needs to be applied to the live project.

-- 1) roll_block_error -- live migration "fix_single_shift_roll_block" (applied 2026-09-28 01:57:54 UTC).
-- The roll-number block rule keeps Morning and Evening roll numbers from colliding when ONE session serves both
-- shifts. For a single-shift session there is no other shift to avoid, so the "Evening must be > cap" half of the
-- rule wrongly rejected roll numbers starting at 1 (e.g. an Evening-only program's real roster). That half is now
-- scoped to shift_mode = 'BOTH'; single-shift sessions only need the serial within 1..max_students.
create or replace function roll_block_error(p_mode shift_mode, p_max int, p_shift shift, p_roll text) returns text
language plpgsql immutable set search_path = public as $$
declare
  cap int := case when p_mode = 'BOTH' then p_max / 2 else p_max end;
  digits text := substring(p_roll from '([0-9]+)\s*$');
  serial int;
begin
  if digits is null or length(digits) > 6 then
    return format('Roll number %s must end with a serial number, e.g. IT-22-09.', p_roll);
  end if;
  serial := digits::int;
  if p_mode = 'BOTH' then
    if p_shift = 'MORNING' and (serial < 1 or serial > cap) then
      return format('Morning roll numbers use serials 1-%s; %s is outside that range.', cap, p_roll);
    end if;
    if p_shift = 'EVENING' and serial <= cap then
      return format('Evening roll numbers start after serial %s; %s is in the Morning range.', cap, p_roll);
    end if;
  else
    if serial < 1 or serial > cap then
      return format('Roll numbers for this session use serials 1-%s; %s is outside that range.', cap, p_roll);
    end if;
  end if;
  return null;
end $$;

-- 2) Marks can only be UPDATED by an admin on production.
-- 20260714000002_rls.sql lets an admin OR an active teacher of the class update session_marks (`upd_marks`), but
-- the live policy is admin-only (and not restricted to `authenticated`). That matches the app: a teacher's saved
-- score is locked and a correction goes through a mark edit request that an admin approves. Without this, a
-- database built from the repo let teachers overwrite saved marks directly.
alter policy upd_marks on session_marks to public using (is_admin()) with check (is_admin());

-- The same policy on mark_edit_requests is not limited to `authenticated` on production (is_admin() is false
-- for anonymous callers, so the effect is identical); kept identical so the two databases compare equal.
alter policy adm_mark_edit_requests on mark_edit_requests to public;

-- 3) session_attendance keeps its entity_id column and the two indexes built on it.
-- 20260905000004_drop_unused_entity_id_dup_indexes_view.sql dropped entity_id everywhere else, keeping it on
-- session_attendance as the pagination tie-breaker (order by updated_at, then entity_id). Production has both
-- indexes; the repo never created them.
create index if not exists idx_session_attendance_updated_entity on session_attendance (updated_at, entity_id);
create unique index if not exists ux_session_attendance_entity_id on session_attendance (entity_id);
