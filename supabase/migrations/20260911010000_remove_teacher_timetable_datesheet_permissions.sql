-- canEditTimetable and canManageDatesheets are removed as delegatable teacher permissions --
-- timetable editing and datesheet management become admin-app-only. canEditTimetable had no
-- consumer in the teacher-facing UI at all (the teacher Schedule screen is view-only);
-- canManageDatesheets did gate real edit access on the shared Datesheets screen.
--
-- Policies referencing teacher_can('can_edit_timetable'/'can_manage_datesheets') must be rewritten
-- BEFORE the columns are dropped -- teacher_can(flag) builds its query dynamically against
-- teachers.<flag>, so a stale reference would throw "column does not exist" on every subsequent
-- read/write against these tables. This rewrites every policy that mentions either flag, including
-- the read-side sel_datesheets/sel_ds_slots (not just the write-side ones), then drops the columns.

-- ── Timetable: admin-only writes now ─────────────────────────────────────────
drop policy if exists wr_periods on timetable_periods;
create policy wr_periods on timetable_periods for all to authenticated
  using (is_admin()) with check (is_admin());

drop policy if exists wr_period_sessions on period_sessions;
create policy wr_period_sessions on period_sessions for all to authenticated
  using (is_admin()) with check (is_admin());

-- ── Datesheets: published readable; admin-only authoring now ────────────────
drop policy if exists sel_datesheets on datesheets;
create policy sel_datesheets on datesheets for select to authenticated
  using (published or is_admin());

drop policy if exists wr_datesheets on datesheets;
create policy wr_datesheets on datesheets for all to authenticated
  using (is_admin()) with check (is_admin());

drop policy if exists sel_ds_slots on datesheet_slots;
create policy sel_ds_slots on datesheet_slots for select to authenticated
  using (exists (select 1 from datesheets d where d.id = datesheet_id
                 and (d.published or is_admin())));

drop policy if exists wr_ds_slots on datesheet_slots;
create policy wr_ds_slots on datesheet_slots for all to authenticated
  using (is_admin()) with check (is_admin());

-- ── Drop the columns now that nothing references them ───────────────────────
alter table teachers
  drop column can_edit_timetable,
  drop column can_manage_datesheets;
