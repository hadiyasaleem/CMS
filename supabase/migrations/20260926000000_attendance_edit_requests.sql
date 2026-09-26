-- A teacher-requested correction to one attendance cell (student x course x date), subject to admin
-- review. Mirrors mark_edit_requests, but approval goes through a security definer RPC because admins
-- have no RLS write access to session_attendance (upd_attendance is teacher-only).
create table attendance_edit_requests (
  id                 uuid primary key default gen_random_uuid(),
  session_id         text not null references academic_sessions(session_id) on delete cascade,
  semester           int not null check (semester between 1 and 8),
  course_code        text not null,
  date               date not null,
  roll_number        text not null,
  current_status     attendance_status,
  current_is_late    boolean,
  requested_status   attendance_status not null,
  requested_is_late  boolean not null default false,
  reason             text check (reason is null or char_length(reason) <= 500),
  status             mark_edit_status not null default 'PENDING',
  requested_by       text not null default current_email(),
  reviewed_by        text,
  requested_at       timestamptz not null default now(),
  reviewed_at        timestamptz,
  created_at         timestamptz not null default now(),
  created_by         text,
  updated_at         timestamptz not null default now(),
  updated_by         text,
  is_deleted         boolean not null default false,
  deleted_at         timestamptz,
  deleted_by         text,
  foreign key (session_id, roll_number) references session_students(session_id, roll_number) on delete cascade
);

alter table attendance_edit_requests enable row level security;

create policy sel_attendance_edit_requests on attendance_edit_requests for select to authenticated
  using (is_admin() or teaches(session_id));
create policy ins_attendance_edit_requests on attendance_edit_requests for insert to authenticated
  with check (is_active_teacher() and teaches(session_id) and requested_by = current_email());
create policy adm_attendance_edit_requests on attendance_edit_requests for update to authenticated
  using (is_admin()) with check (is_admin());

create trigger trg_audit_attendance_edit_requests before insert or update on attendance_edit_requests
  for each row execute function fn_cms_audit_row();
create trigger trg_touch_attendance_edit_requests before update on attendance_edit_requests
  for each row execute function fn_touch_updated_at();

create index idx_attendance_edit_requests_cell on attendance_edit_requests(session_id, course_code, date);
create index idx_attendance_edit_requests_status on attendance_edit_requests(status);
create index idx_attendance_edit_requests_updated_at on attendance_edit_requests(updated_at);
create unique index uq_attendance_edit_requests_pending_cell
  on attendance_edit_requests(session_id, course_code, date, roll_number) where status = 'PENDING';

create or replace function approve_attendance_edit_request(p_request_id uuid, p_reviewed_by text)
returns void
language plpgsql security definer set search_path = public as $$
declare
  v_request attendance_edit_requests%rowtype;
begin
  if not is_admin() then
    raise exception 'not allowed';
  end if;

  select * into v_request from attendance_edit_requests
    where id = p_request_id and status = 'PENDING'
    for update;
  if not found then
    raise exception 'This request is no longer pending.';
  end if;

  insert into session_attendance (session_id, semester, course_code, date, roll_number, status, is_late, teacher_email)
  values (v_request.session_id, v_request.semester, v_request.course_code, v_request.date,
          v_request.roll_number, v_request.requested_status, v_request.requested_is_late, v_request.requested_by)
  on conflict (session_id, course_code, date, roll_number) do update
    set status = excluded.status, is_late = excluded.is_late, is_deleted = false;

  update attendance_edit_requests set status = 'APPROVED', reviewed_by = p_reviewed_by, reviewed_at = now()
    where id = p_request_id;
end $$;

revoke all on function approve_attendance_edit_request(uuid, text) from public;
revoke all on function approve_attendance_edit_request(uuid, text) from anon;
grant execute on function approve_attendance_edit_request(uuid, text) to authenticated;
