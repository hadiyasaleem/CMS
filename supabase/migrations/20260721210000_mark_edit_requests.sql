-- Reconstructed from live history: "mark_edit_requests" (applied 2026-07-21 21:01:43 UTC, untracked
-- locally). Recovered via information_schema.columns/pg_constraint/pg_indexes/pg_policies against the
-- live project (ygmvyvjhdkxddkqxtrdw) -- not applied here, the live DB already has it; this file only
-- backfills local history.
--
-- A teacher-requested correction to an already-recorded mark, subject to admin review.
create type mark_edit_status as enum ('PENDING', 'APPROVED', 'REJECTED');

create table mark_edit_requests (
  id               uuid primary key default gen_random_uuid(),
  session_id       text not null references academic_sessions(session_id) on delete cascade,
  semester         int not null,
  course_code      text not null,
  exam_type        exam_type not null,
  roll_number      text not null,
  current_score    int,
  requested_score  int not null,
  reason           text,
  status           mark_edit_status not null default 'PENDING',
  requested_by     text not null,
  reviewed_by      text,
  requested_at     timestamptz not null default now(),
  reviewed_at      timestamptz
);

alter table mark_edit_requests enable row level security;

create policy sel_mark_edit_requests on mark_edit_requests for select to authenticated
  using (is_admin() or teaches(session_id));
create policy ins_mark_edit_requests on mark_edit_requests for insert to authenticated
  with check (is_active_teacher() and teaches(session_id));
create policy adm_mark_edit_requests on mark_edit_requests for update to authenticated
  using (is_admin()) with check (is_admin());

create index idx_mark_edit_requests_session on mark_edit_requests(session_id, course_code, exam_type);
create index idx_mark_edit_requests_status  on mark_edit_requests(status);
