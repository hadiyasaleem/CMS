-- ═══════════════════════════════════════════════════════════════════════════
-- CMS v2 schema — department-centric academic model on Supabase Postgres.
-- Apply with `supabase db push` / MCP apply_migration. Ordered: extensions →
-- enums → tables (FK dependency order) → triggers → views → indexes.
-- RLS lives in 20260714000002_rls.sql; Storage buckets in ..000003_storage.sql.
-- ═══════════════════════════════════════════════════════════════════════════

-- NOTE (as-deployed): no-double-booking is enforced by a BEFORE trigger, not a GiST
-- exclusion constraint — Postgres rejects the time/date range constructors as non-immutable
-- inside an index expression (even wrapped in IMMUTABLE SQL fns), so btree_gist/timerange were dropped.

-- ── Enums ───────────────────────────────────────────────────────────────────
create type shift             as enum ('MORNING','EVENING');
create type fee_cadence       as enum ('ANNUAL','SEMESTER');      -- morning=ANNUAL, evening=SEMESTER
create type exam_type         as enum ('MIDTERM','SESSIONAL');    -- college scope only (25 / 15)
create type attendance_status as enum ('PRESENT','ABSENT','LEAVE');
create type link_status       as enum ('PENDING','APPROVED','REJECTED');
create type user_role         as enum ('ADMIN','TEACHER','STUDENT');
create type account_status    as enum ('ACTIVE','DISABLED','BANNED');
create type notif_target      as enum ('ADMIN','TEACHER','STUDENT','ALL');
create type weekday           as enum ('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY');
create type period_type       as enum ('LECTURE','ZERO','BREAK');
create type gender            as enum ('MALE','FEMALE','OTHER');
create type event_type        as enum ('HOLIDAY','EVENT','EXAM','DEADLINE');
create type doc_kind          as enum ('PROSPECTUS','RULES','REPORT','OTHER');
create type subject_type      as enum ('THEORY','LAB');
create type enrollment_status as enum ('ACTIVE','PROMOTED','REPEATED','WITHDRAWN','GRADUATED');
create type semester_result   as enum ('PROMOTED','REPEATED','PROBATION','PENDING');
create type fine_category     as enum ('LIBRARY','ATTENDANCE','EXAM','DISCIPLINARY','OTHER');
create type review_status     as enum ('SUBMITTED','REVIEWED');
create type notif_priority    as enum ('NORMAL','IMPORTANT','URGENT');

-- ── Core academic structure ─────────────────────────────────────────────────
create table departments (
  dept_id     text primary key,
  name        text not null,
  code        text not null,
  hod_email   text,                      -- FK to teachers(email) added below (circular)
  description text,
  is_active   boolean not null default true,
  archived_at timestamptz,
  created_at  timestamptz not null default now(), created_by text,
  updated_at  timestamptz not null default now(), updated_by text
);

-- People are keyed by normalized email; auth_uid links the GoTrue account once provisioned.
create table teachers (
  email       text primary key,
  auth_uid    uuid unique references auth.users(id) on delete set null,
  name        text not null,
  phone       text,
  dept_id     text references departments(dept_id) on delete set null,
  designation text, qualification text, specialization text, office_room text,
  photo_path  text,
  gender      gender,
  is_hod      boolean not null default false,
  is_admin    boolean not null default false,   -- teacher account that also has admin rights
  can_approve_link_requests boolean not null default false,
  can_edit_timetable        boolean not null default false,
  can_send_notifications    boolean not null default false,
  can_manage_datesheets     boolean not null default false,
  status      account_status not null default 'ACTIVE',
  is_active   boolean not null default true,
  archived_at timestamptz,
  created_at  timestamptz not null default now(), created_by text,
  updated_at  timestamptz not null default now(), updated_by text
);

alter table departments
  add constraint fk_departments_hod foreign key (hod_email)
  references teachers(email) on delete set null;

-- RBAC mirror of GoTrue accounts (email = app identity key).
create table profiles (
  id                uuid primary key references auth.users(id) on delete cascade,
  email             text unique not null,
  role              user_role not null default 'STUDENT',
  teacher_email     text references teachers(email) on delete set null,
  linked_session_id text,                -- composite FK added after session_students
  linked_roll       text,
  status            account_status not null default 'ACTIVE',
  last_login_at     timestamptz,
  notification_prefs jsonb not null default '{}',
  created_at        timestamptz not null default now()
);

-- An intake/batch: "IT 2021-2025 Morning". current_semester is the promotion pointer.
create table academic_sessions (
  session_id       text primary key,     -- "{deptId}_{startYear}_{SHIFT}"
  dept_id          text not null references departments(dept_id) on delete cascade,
  start_year       int not null,
  end_year         int not null,
  shift            shift not null,
  program_name     text,
  incharge_email   text references teachers(email) on delete set null,
  max_students     int not null default 50,
  current_semester int not null default 1 check (current_semester between 1 and 8),
  is_active        boolean not null default true,
  archived_at      timestamptz,
  updated_at       timestamptz not null default now(),
  unique (dept_id, start_year, shift)
);

-- PER-SESSION curriculum (subjects vary by intake).
create table session_subjects (
  session_id   text not null references academic_sessions(session_id) on delete cascade,
  semester     int not null check (semester between 1 and 8),
  course_code  text not null,
  name         text not null,
  credit_hours int not null default 3,
  subject_type subject_type not null default 'THEORY',
  is_elective  boolean not null default false,
  outline      text,
  updated_at   timestamptz not null default now(),
  primary key (session_id, semester, course_code)
);

-- Student roster + rich profile. Cap enforced by trg_roster_cap below.
create table session_students (
  session_id  text not null references academic_sessions(session_id) on delete cascade,
  roll_number text not null,             -- college roll e.g. IT-21-09
  name        text not null,
  university_roll_no text,               -- "2021-065192", issued ~end of sem 1
  registration_no    text,               -- "2021-mcm-86"
  father_name text, guardian_name text, cnic_bform text, dob date,
  gender gender,
  phone text, guardian_phone text, personal_email text,
  current_address text, permanent_address text,
  photo_path text,                        -- Storage key (no blobs in DB)
  blood_group text, domicile text, religion text,
  linked_email text not null default '',  -- linked Student account, '' until approved
  gpa  numeric(3,2), cgpa numeric(3,2),   -- CURRENT snapshot (history in student_semester_gpa)
  admission_date date,
  enrollment_status enrollment_status not null default 'ACTIVE',
  emergency_contact_name text, emergency_contact_relation text, emergency_contact_phone text,
  special_needs text,
  is_cr boolean not null default false,   -- class representative
  is_gr boolean not null default false,   -- girls' representative
  is_active boolean not null default true,
  archived_at timestamptz,
  created_at timestamptz not null default now(), created_by text,
  updated_at timestamptz not null default now(), updated_by text,
  primary key (session_id, roll_number)
);
create unique index one_cr_per_session on session_students(session_id) where is_cr;
create unique index one_gr_per_session on session_students(session_id) where is_gr;
create unique index uq_university_roll on session_students(university_roll_no) where university_roll_no is not null;
create unique index uq_registration_no on session_students(registration_no)    where registration_no is not null;

alter table profiles
  add constraint fk_profiles_student foreign key (linked_session_id, linked_roll)
  references session_students(session_id, roll_number) on delete set null;

-- ── Timetable (rooms, zero-period, break, merged lectures, conflict-free) ───
create table timetable_periods (
  id uuid primary key default gen_random_uuid(),
  primary_session_id text not null references academic_sessions(session_id) on delete cascade,
  day        weekday not null,
  start_time time not null,
  end_time   time not null,
  period_type period_type not null default 'LECTURE',
  course_code text, subject_name text, credit_hours int,
  teacher_email text references teachers(email) on delete set null,
  teacher_name  text,
  room_no text, building text, notes text,
  effective_from date, effective_to date,  -- timetable may change mid-semester
  updated_at timestamptz not null default now(),
  check (start_time < end_time)
);
-- One cell per slot per session grid (BREAK/ZERO rows included).
create unique index uq_session_slot on timetable_periods(primary_session_id, day, start_time);

-- No teacher / room double-booking — enforced by a BEFORE trigger (a GiST exclusion constraint
-- can't be used: the range constructors aren't inlinable-immutable in an index expression).
-- Merged lectures are a single row so they never self-conflict. [a,b) time overlap; NULL
-- effectivity = unbounded.
create or replace function fn_check_timetable_conflict() returns trigger
language plpgsql set search_path = public as $$
begin
  if new.period_type <> 'LECTURE' then return new; end if;
  if new.teacher_email is not null and exists (
    select 1 from timetable_periods p
    where p.id <> new.id and p.period_type = 'LECTURE'
      and p.teacher_email = new.teacher_email and p.day = new.day
      and p.start_time < new.end_time and new.start_time < p.end_time
      and coalesce(p.effective_from, '-infinity'::date) <= coalesce(new.effective_to, 'infinity'::date)
      and coalesce(new.effective_from, '-infinity'::date) <= coalesce(p.effective_to, 'infinity'::date)
  ) then
    raise exception 'Teacher % already has an overlapping lecture on % at %', new.teacher_email, new.day, new.start_time;
  end if;
  if new.room_no is not null and exists (
    select 1 from timetable_periods p
    where p.id <> new.id and p.period_type = 'LECTURE'
      and p.room_no = new.room_no and p.day = new.day
      and p.start_time < new.end_time and new.start_time < p.end_time
      and coalesce(p.effective_from, '-infinity'::date) <= coalesce(new.effective_to, 'infinity'::date)
      and coalesce(new.effective_from, '-infinity'::date) <= coalesce(p.effective_to, 'infinity'::date)
  ) then
    raise exception 'Room % is already booked overlapping % on %', new.room_no, new.start_time, new.day;
  end if;
  return new;
end $$;
create trigger trg_timetable_conflict before insert or update on timetable_periods
  for each row execute function fn_check_timetable_conflict();

-- Merged lecture = one period serving N sessions (includes its primary).
create table period_sessions (
  period_id  uuid references timetable_periods(id) on delete cascade,
  session_id text references academic_sessions(session_id) on delete cascade,
  primary key (period_id, session_id)
);

-- ── Attendance & marks — semester-scoped history ────────────────────────────
create table session_attendance (
  session_id  text not null,
  semester    int not null check (semester between 1 and 8),
  course_code text not null,
  date        date not null,
  roll_number text not null,
  status      attendance_status not null,
  teacher_email text not null default '',  -- deliberate snapshot: survives teacher deletion
  is_late     boolean not null default false,
  lecture_topic text, remark text,
  recorded_at timestamptz not null default now(),
  updated_at  timestamptz not null default now(),
  primary key (session_id, course_code, date, roll_number),
  foreign key (session_id, roll_number)
    references session_students(session_id, roll_number) on delete cascade
);

create table session_marks (
  session_id  text not null,
  semester    int not null check (semester between 1 and 8),
  course_code text not null,
  exam_type   exam_type not null,
  roll_number text not null,
  score       int,                         -- null = not entered
  max_marks   int not null,
  was_absent  boolean not null default false,
  exam_date   date,
  remarks     text,
  teacher_email text not null default '',
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  check (score is null or score between 0 and max_marks),
  check ((exam_type = 'MIDTERM' and max_marks = 25) or (exam_type = 'SESSIONAL' and max_marks = 15)),
  primary key (session_id, semester, course_code, exam_type, roll_number),
  foreign key (session_id, roll_number)
    references session_students(session_id, roll_number) on delete cascade
);

create table student_semester_gpa (
  session_id  text not null,
  roll_number text not null,
  semester    int not null check (semester between 1 and 8),
  gpa  numeric(3,2) not null check (gpa between 0 and 4),
  cgpa numeric(3,2) not null check (cgpa between 0 and 4),
  term_label text,
  result_status semester_result not null default 'PENDING',
  class_position int,
  remarks text,
  created_at timestamptz not null default now(), created_by text,
  updated_at timestamptz not null default now(), updated_by text,
  primary key (session_id, roll_number, semester),
  foreign key (session_id, roll_number)
    references session_students(session_id, roll_number) on delete cascade
);

-- ── Fees (per session, cadence from shift) + fines ──────────────────────────
create table session_fees (
  session_id text primary key references academic_sessions(session_id) on delete cascade,
  cadence    fee_cadence not null,
  academic_year text,
  due_date date,
  late_fine_note text,
  payment_note text default 'Payable at the college accounts office (accountant), not a bank',
  updated_at timestamptz not null default now(), updated_by text
);

create table session_fee_heads (
  session_id text not null references session_fees(session_id) on delete cascade,
  label      text not null,
  amount     numeric(10,2) not null,
  position   int not null default 0,
  primary key (session_id, label)
);

create table fee_overrides (
  session_id  text not null,
  roll_number text not null,
  label       text not null,
  amount      numeric(10,2) not null,
  reason      text not null default '',
  created_at timestamptz not null default now(), created_by text,
  updated_at timestamptz not null default now(), updated_by text,
  primary key (session_id, roll_number, label),
  foreign key (session_id, roll_number)
    references session_students(session_id, roll_number) on delete cascade
);

create table fines (                       -- INFORMATIONAL only (no paid/unpaid tracking)
  id uuid primary key default gen_random_uuid(),
  session_id  text not null,
  roll_number text not null,
  category    fine_category not null default 'OTHER',
  amount      numeric(10,2) not null,
  reason      text not null,
  issued_by   text,
  issued_at   timestamptz not null default now(),
  foreign key (session_id, roll_number)
    references session_students(session_id, roll_number) on delete cascade
);

-- ── Datesheets, calendar, documents ─────────────────────────────────────────
create table datesheets (
  id uuid primary key default gen_random_uuid(),
  title text not null,
  exam_type exam_type,
  session_id text references academic_sessions(session_id) on delete cascade,
  published boolean not null default false,
  instructions text,
  created_at timestamptz not null default now(), created_by text,
  updated_at timestamptz not null default now(), updated_by text
);

create table datesheet_slots (
  id uuid primary key default gen_random_uuid(),
  datesheet_id uuid not null references datesheets(id) on delete cascade,
  exam_date date not null,
  start_time time, end_time time, duration_minutes int,
  course_code text, subject_name text, room_no text, building text,
  invigilator_email text references teachers(email) on delete set null
);

create table calendar_events (
  id uuid primary key default gen_random_uuid(),
  title text not null,
  event_type event_type not null,
  start_date date not null,
  end_date date,
  start_time time, end_time time,
  description text, venue text,
  audience notif_target not null default 'ALL',
  dept_id    text references departments(dept_id) on delete cascade,      -- null = college-wide
  session_id text references academic_sessions(session_id) on delete cascade,
  created_at timestamptz not null default now(), created_by text,
  updated_at timestamptz not null default now(), updated_by text
);

-- Prospectus/rules = uploaded docs (storage_path) or rich text (body); reports from views.
create table documents (
  id uuid primary key default gen_random_uuid(),
  kind doc_kind not null,
  title text not null,
  storage_path text,
  body text,
  dept_id text references departments(dept_id) on delete cascade,
  audience notif_target not null default 'ALL',
  tags text[],
  published boolean not null default true,
  published_by text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  search tsvector generated always as
    (to_tsvector('simple', coalesce(title,'') || ' ' || coalesce(body,''))) stored
);
create index idx_documents_search on documents using gin(search);

-- ── Exam-paper files (metadata only; blobs live in Storage) ─────────────────
create table exam_paper_submissions (
  id uuid primary key default gen_random_uuid(),
  session_id  text not null references academic_sessions(session_id) on delete cascade,
  semester    int not null check (semester between 1 and 8),
  course_code text not null,
  exam_type   exam_type not null,
  teacher_email text not null,
  storage_path text not null,
  file_name    text not null,
  file_size_bytes bigint check (file_size_bytes is null or file_size_bytes <= 5242880),
  mime_type    text default 'application/pdf',
  key_storage_path text,                 -- optional answer-key file
  teacher_notes text,
  review_status review_status not null default 'SUBMITTED',
  reviewed_by  text,
  reviewed_at  timestamptz,
  uploaded_at  timestamptz not null default now(),
  created_by   text
);

-- ── Link requests & notifications ───────────────────────────────────────────
create table student_link_requests (
  request_id uuid primary key default gen_random_uuid(),
  requested_by_email  text not null,
  roll_number_claimed text not null,
  cnic_bform_claimed text, university_roll_claimed text, registration_no_claimed text,
  message text,
  session_id text references academic_sessions(session_id) on delete set null, -- matched, null = none
  status link_status not null default 'PENDING',
  reviewed_by text, reviewed_at timestamptz, rejection_reason text,
  attempt_count int not null default 1,
  created_at timestamptz not null default now()
);

create table notifications (               -- in-app only (no FCM)
  id uuid primary key default gen_random_uuid(),
  title text not null,
  body  text not null,
  priority notif_priority not null default 'NORMAL',
  target_role notif_target,               -- null = all roles
  target_dept_id    text references departments(dept_id) on delete cascade,
  target_session_id text references academic_sessions(session_id) on delete cascade,
  attachment_path text,
  expires_at timestamptz,
  created_by_email text not null,
  created_at timestamptz not null default now()
);

-- ── Triggers ────────────────────────────────────────────────────────────────
-- updated_at bump on every mutable table (drives the app's incremental sync).
create or replace function fn_touch_updated_at() returns trigger
language plpgsql as $$
begin
  new.updated_at := now();
  return new;
end $$;

do $$
declare t text;
begin
  foreach t in array array[
    'departments','teachers','academic_sessions','session_subjects','session_students',
    'timetable_periods','session_attendance','session_marks','student_semester_gpa',
    'session_fees','fee_overrides','datesheets','calendar_events','documents'
  ] loop
    execute format(
      'create trigger trg_touch_%I before update on %I for each row execute function fn_touch_updated_at()',
      t, t);
  end loop;
end $$;

-- Roster cap: reads the session's own max_students (default 50), not a hard-coded limit.
create or replace function fn_enforce_roster_cap() returns trigger
language plpgsql as $$
declare cap int; n int;
begin
  select max_students into cap from academic_sessions where session_id = new.session_id;
  select count(*) into n from session_students where session_id = new.session_id;
  if n >= coalesce(cap, 50) then
    raise exception 'Session % is full (% students max)', new.session_id, coalesce(cap, 50);
  end if;
  return new;
end $$;
create trigger trg_roster_cap before insert on session_students
  for each row execute function fn_enforce_roster_cap();

-- Auto-create a STUDENT profile for every new GoTrue signup (teachers/admins are
-- provisioned by the admin-create-user Edge Function, which upgrades the row).
create or replace function fn_handle_new_user() returns trigger
language plpgsql security definer set search_path = public as $$
begin
  insert into profiles (id, email)
  values (new.id, lower(new.email))
  on conflict (id) do nothing;
  return new;
end $$;
create trigger trg_on_auth_user_created after insert on auth.users
  for each row execute function fn_handle_new_user();

-- ── Views (ALL security_invoker so table RLS applies to the querying user) ──
create view session_attendance_summary with (security_invoker = true) as
select session_id, semester, course_code, roll_number,
  count(*) filter (where status = 'PRESENT') as present,
  count(*) filter (where status = 'ABSENT')  as absent,
  count(*) filter (where status = 'LEAVE')   as leave,
  count(*)                                   as total_marked,
  round(100.0 * count(*) filter (where status = 'PRESENT') / nullif(count(*), 0), 1) as percentage
from session_attendance
group by session_id, semester, course_code, roll_number;

create view exam_stats with (security_invoker = true) as
select session_id, semester, course_code, exam_type,
  count(score) filter (where not was_absent)                as entered,
  round(avg(score) filter (where not was_absent), 1)        as avg_score,
  min(score) as min_score, max(score) as max_score,
  round(stddev_pop(score) filter (where not was_absent), 2) as stddev,
  max(max_marks) as out_of,
  round(100.0 * count(*) filter (where not was_absent and score >= max_marks * 0.4)
        / nullif(count(score) filter (where not was_absent), 0), 1) as pass_rate
from session_marks
group by session_id, semester, course_code, exam_type;

create view student_gpa_progression with (security_invoker = true) as
select session_id, roll_number, semester, gpa, cgpa, term_label, result_status, class_position,
  gpa - lag(gpa) over (partition by session_id, roll_number order by semester) as gpa_delta
from student_semester_gpa;

create view session_overview with (security_invoker = true) as
select s.session_id, s.dept_id, s.shift, s.current_semester,
  count(distinct st.roll_number)  as students,
  round(avg(st.cgpa), 2)          as avg_cgpa,
  round(avg(sm.percentage), 1)    as avg_attendance
from academic_sessions s
left join session_students st using (session_id)
left join session_attendance_summary sm using (session_id, roll_number)
group by s.session_id, s.dept_id, s.shift, s.current_semester;

create view at_risk_students with (security_invoker = true) as
select st.session_id, st.roll_number, st.name, st.cgpa,
  round(avg(sm.percentage), 1) as attendance
from session_students st
left join session_attendance_summary sm using (session_id, roll_number)
group by st.session_id, st.roll_number, st.name, st.cgpa
having round(avg(sm.percentage), 1) < 75 or st.cgpa < 2.00;

-- ── Indexes (sync + lookups) ────────────────────────────────────────────────
create index idx_teachers_updated       on teachers(updated_at);
create index idx_teachers_dept          on teachers(dept_id);
create index idx_sessions_dept          on academic_sessions(dept_id);
create index idx_sessions_updated       on academic_sessions(updated_at);
create index idx_subjects_updated       on session_subjects(updated_at);
create index idx_students_updated       on session_students(updated_at);
create index idx_students_linked_email  on session_students(linked_email);
create index idx_periods_session_day    on timetable_periods(primary_session_id, day);
create index idx_periods_teacher        on timetable_periods(teacher_email);
create index idx_periods_updated        on timetable_periods(updated_at);
create index idx_period_sessions_sess   on period_sessions(session_id);
create index idx_attendance_course      on session_attendance(session_id, course_code);
create index idx_attendance_updated     on session_attendance(updated_at);
create index idx_marks_lookup           on session_marks(session_id, semester, course_code, exam_type);
create index idx_marks_updated          on session_marks(updated_at);
create index idx_gpa_updated            on student_semester_gpa(updated_at);
create index idx_fines_student          on fines(session_id, roll_number);
create index idx_papers_session_sem     on exam_paper_submissions(session_id, semester);
create index idx_link_requests_status   on student_link_requests(status);
create index idx_link_requests_by       on student_link_requests(requested_by_email);
create index idx_notifications_created  on notifications(created_at desc);
create index idx_calendar_start         on calendar_events(start_date);
