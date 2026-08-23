-- Per-session, per-semester class start/end dates (admin-editable). Drives the attendance-records
-- report (which months belong to a semester) and general term boundaries.
create table semester_terms (
  session_id text not null references academic_sessions(session_id) on delete cascade,
  semester   int not null check (semester between 1 and 8),
  start_date date,
  end_date   date,
  updated_at timestamptz not null default now(),
  primary key (session_id, semester)
);

alter table semester_terms enable row level security;
create policy sel_semester_terms on semester_terms for select to authenticated using (true);
create policy adm_semester_terms on semester_terms for all to authenticated
  using (is_admin()) with check (is_admin());

create trigger trg_touch_semester_terms before update on semester_terms
  for each row execute function fn_touch_updated_at();
