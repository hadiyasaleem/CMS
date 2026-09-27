-- Students read only their own shift's published datesheet (FR-21b); the student apps already show only that
-- one, and now the database enforces it. Admins see every datesheet; active teachers see published ones.

drop policy sel_datesheets on datesheets;
create policy sel_datesheets on datesheets for select to authenticated
  using (is_admin() or (published and (is_active_teacher()
         or (session_id = my_session() and shift = my_shift()))));

drop policy sel_ds_slots on datesheet_slots;
create policy sel_ds_slots on datesheet_slots for select to authenticated
  using (exists (select 1 from datesheets d where d.id = datesheet_slots.datesheet_id
         and (is_admin() or (d.published and (is_active_teacher()
              or (d.session_id = my_session() and d.shift = my_shift()))))));
