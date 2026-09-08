-- Datesheet rework: scope each datesheet to one (session, semester) instead of a free-floating
-- title, drop the sessional exam type (sessional is continuous assessment, not scheduled), link
-- papers to the session's real curriculum and to real buildings/rooms instead of free text, and
-- enforce room/invigilator conflicts across ALL datesheets in the database (client-side checks can
-- only ever see one datesheet's papers at a time).
--
-- Existing datesheets are test data with no papers -- wiped per product decision rather than
-- migrated, since there is no sensible (session, semester) to backfill onto free-text rows.

delete from datesheet_slots;
delete from datesheets;

alter table datesheets
  drop column title,
  drop column exam_type,
  alter column session_id set not null,
  add column semester int not null check (semester between 1 and 8),
  add column default_start_time time,
  add column default_end_time time,
  add column default_building_id text references buildings(building_id) on delete set null;

create unique index uq_datesheet_session_semester on datesheets(session_id, semester);

alter table datesheet_slots
  alter column exam_date drop not null,
  alter column course_code set not null,
  alter column subject_name set not null,
  drop column duration_minutes,
  add column building_id text references buildings(building_id) on delete set null,
  add column room_id text references rooms(room_id) on delete set null;

-- One row per curriculum subject (a paper can't be scheduled twice), and at most one paper per
-- date within a datesheet (a student can't sit two exams the same day). Postgres treats every NULL
-- as distinct from every other NULL, so prefilled-but-unscheduled papers (exam_date is null) never
-- collide with each other.
create unique index uq_datesheet_slot_course on datesheet_slots(datesheet_id, course_code);
create unique index uq_datesheet_slot_date on datesheet_slots(datesheet_id, exam_date);

-- Cross-datesheet room/invigilator conflict guard. Mirrors fn_check_timetable_conflict()'s shape
-- (schema.sql) but a slot's effective time can come from its own datesheet's default, so each
-- candidate row's time is resolved via its own parent datesheet, not just NEW's.
create or replace function fn_check_datesheet_conflict() returns trigger
language plpgsql set search_path = public as $$
declare
  eff_start time;
  eff_end time;
begin
  if new.exam_date is null then
    return new;
  end if;

  select coalesce(new.start_time, d.default_start_time), coalesce(new.end_time, d.default_end_time)
    into eff_start, eff_end
    from datesheets d
    where d.id = new.datesheet_id;

  if eff_start is null or eff_end is null then
    return new;
  end if;

  if new.room_id is not null and exists (
    select 1 from datesheet_slots s
    join datesheets d2 on d2.id = s.datesheet_id
    where s.id <> new.id
      and s.room_id = new.room_id
      and s.exam_date = new.exam_date
      and coalesce(s.start_time, d2.default_start_time) < eff_end
      and eff_start < coalesce(s.end_time, d2.default_end_time)
  ) then
    raise exception 'Room is already booked for an overlapping exam on %', new.exam_date;
  end if;

  if new.invigilator_email is not null and exists (
    select 1 from datesheet_slots s
    join datesheets d2 on d2.id = s.datesheet_id
    where s.id <> new.id
      and s.invigilator_email = new.invigilator_email
      and s.exam_date = new.exam_date
      and coalesce(s.start_time, d2.default_start_time) < eff_end
      and eff_start < coalesce(s.end_time, d2.default_end_time)
  ) then
    raise exception 'Invigilator % already has an overlapping exam duty on %', new.invigilator_email, new.exam_date;
  end if;

  return new;
end $$;

revoke all on function fn_check_datesheet_conflict() from public, anon, authenticated;

drop trigger if exists trg_check_datesheet_conflict on datesheet_slots;
create trigger trg_check_datesheet_conflict
  before insert or update on datesheet_slots
  for each row execute function fn_check_datesheet_conflict();
