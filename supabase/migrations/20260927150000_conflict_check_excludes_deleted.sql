-- fn_check_timetable_conflict never excluded soft-deleted rows, so a removed period kept blocking its
-- old room/time slot forever (the app's own "move a period" flow -- save the new row, then soft-delete
-- the old one -- relies on the vacated slot actually being free for later inserts).
create or replace function fn_check_timetable_conflict() returns trigger
language plpgsql set search_path = public as $$
begin
  if new.period_type <> 'LECTURE' then return new; end if;
  if new.teacher_email is not null and exists (
    select 1 from timetable_periods p
    where p.id <> new.id and p.period_type = 'LECTURE' and not p.is_deleted
      and p.teacher_email = new.teacher_email and p.day = new.day
      and p.start_time < new.end_time and new.start_time < p.end_time
      and coalesce(p.effective_from, '-infinity'::date) <= coalesce(new.effective_to, 'infinity'::date)
      and coalesce(new.effective_from, '-infinity'::date) <= coalesce(p.effective_to, 'infinity'::date)
  ) then
    raise exception 'Teacher % already has an overlapping lecture on % at %', new.teacher_email, new.day, new.start_time;
  end if;
  if new.room_no is not null and exists (
    select 1 from timetable_periods p
    where p.id <> new.id and p.period_type = 'LECTURE' and not p.is_deleted
      and p.room_no = new.room_no and p.day = new.day
      and p.start_time < new.end_time and new.start_time < p.end_time
      and coalesce(p.effective_from, '-infinity'::date) <= coalesce(new.effective_to, 'infinity'::date)
      and coalesce(new.effective_from, '-infinity'::date) <= coalesce(p.effective_to, 'infinity'::date)
  ) then
    raise exception 'Room % is already booked overlapping % on %', new.room_no, new.start_time, new.day;
  end if;
  return new;
end $$;
