-- Datesheets gain a default room (parallels default_building_id): the room a department's own
-- papers fall back to when a paper doesn't set its own room override. The cross-datesheet
-- conflict trigger must resolve a slot's EFFECTIVE room the same way it already resolves time
-- (coalesce the slot's own value with its own datesheet's default) -- otherwise two papers that
-- both rely on a sheet's default room would never be flagged as a clash.

alter table datesheets
  add column default_room_id text references rooms(room_id) on delete set null;

create or replace function fn_check_datesheet_conflict() returns trigger
language plpgsql set search_path = public as $$
declare
  eff_start time;
  eff_end time;
  eff_room text;
  v_other record;
  v_room text;
begin
  if new.exam_date is null then
    return new;
  end if;

  select coalesce(new.start_time, d.default_start_time), coalesce(new.end_time, d.default_end_time),
         coalesce(new.room_id, d.default_room_id)
    into eff_start, eff_end, eff_room
    from datesheets d
    where d.id = new.datesheet_id;

  if eff_start is null or eff_end is null then
    return new;
  end if;

  if eff_room is not null then
    select s.course_code, s.subject_name, d2.session_id, d2.semester, d2.shift,
           coalesce(s.start_time, d2.default_start_time) as other_start,
           coalesce(s.end_time, d2.default_end_time) as other_end
      into v_other
      from datesheet_slots s
      join datesheets d2 on d2.id = s.datesheet_id
      where s.id <> new.id
        and coalesce(s.room_id, d2.default_room_id) = eff_room
        and s.exam_date = new.exam_date
        and coalesce(s.start_time, d2.default_start_time) < eff_end
        and eff_start < coalesce(s.end_time, d2.default_end_time)
      limit 1;
    if found then
      select r.room_no || coalesce(' (' || b.name || ')', '') into v_room
        from rooms r left join buildings b on b.building_id = r.building_id
        where r.room_id = eff_room;
      raise exception '%', format(
        '%s is already booked for an overlapping exam: %s (%s) for %s on %s, %s–%s.',
        coalesce('Room ' || v_room, 'This room'),
        v_other.subject_name, v_other.course_code,
        coalesce(msg_class_label(v_other.session_id, v_other.shift, v_other.semester), 'another class'),
        to_char(new.exam_date, 'FMDD Mon YYYY'),
        to_char(v_other.other_start, 'HH24:MI'), to_char(v_other.other_end, 'HH24:MI'));
    end if;
  end if;

  if new.invigilator_email is not null then
    select s.course_code, s.subject_name, d2.session_id, d2.semester, d2.shift,
           coalesce(s.start_time, d2.default_start_time) as other_start,
           coalesce(s.end_time, d2.default_end_time) as other_end
      into v_other
      from datesheet_slots s
      join datesheets d2 on d2.id = s.datesheet_id
      where s.id <> new.id
        and s.invigilator_email = new.invigilator_email
        and s.exam_date = new.exam_date
        and coalesce(s.start_time, d2.default_start_time) < eff_end
        and eff_start < coalesce(s.end_time, d2.default_end_time)
      limit 1;
    if found then
      raise exception '%', format(
        'Invigilator %s already has an overlapping exam duty: %s (%s) for %s on %s, %s–%s.',
        msg_teacher_label(new.invigilator_email),
        v_other.subject_name, v_other.course_code,
        coalesce(msg_class_label(v_other.session_id, v_other.shift, v_other.semester), 'another class'),
        to_char(new.exam_date, 'FMDD Mon YYYY'),
        to_char(v_other.other_start, 'HH24:MI'), to_char(v_other.other_end, 'HH24:MI'));
    end if;
  end if;

  return new;
end $$;
