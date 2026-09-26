-- Fix fn_guard_session_shift_change: appending the bare literal 'a fee structure' to a text[] was parsed as an
-- array literal ("malformed array literal"), so a blocked BOTH -> single switch raised that instead of the
-- intended message. Build the list with array_append throughout.

create or replace function fn_guard_session_shift_change() returns trigger
language plpgsql set search_path = public as $$
declare
  v_students int; v_fees int; v_periods int; v_datesheets int;
  v_parts text[] := '{}';
  v_bad record;
  v_error text;
begin
  if new.shift_mode is distinct from old.shift_mode and new.shift_mode <> 'BOTH' then
    select count(*) into v_students from session_students
      where session_id = new.session_id and not is_deleted and shift::text <> new.shift_mode::text;
    select count(*) into v_fees from session_fees
      where session_id = new.session_id and not is_deleted and shift::text <> new.shift_mode::text;
    select count(*) into v_periods from timetable_periods
      where primary_session_id = new.session_id and not is_deleted and shift::text <> new.shift_mode::text;
    select count(*) into v_datesheets from datesheets
      where session_id = new.session_id and not is_deleted and shift::text <> new.shift_mode::text;
    if v_students > 0 then v_parts := array_append(v_parts, format('%s student(s)', v_students)); end if;
    if v_fees > 0 then v_parts := array_append(v_parts, 'a fee structure'::text); end if;
    if v_periods > 0 then v_parts := array_append(v_parts, format('%s timetable period(s)', v_periods)); end if;
    if v_datesheets > 0 then v_parts := array_append(v_parts, format('%s datesheet(s)', v_datesheets)); end if;
    if cardinality(v_parts) > 0 then
      raise exception 'Cannot switch to % only: the other shift still has %. Move or remove them first.',
        initcap(new.shift_mode::text), array_to_string(v_parts, ', ');
    end if;
  end if;

  if new.shift_mode is distinct from old.shift_mode or new.max_students is distinct from old.max_students then
    for v_bad in
      select roll_number, shift from session_students where session_id = new.session_id and not is_deleted
    loop
      v_error := roll_block_error(new.shift_mode, new.max_students, v_bad.shift, v_bad.roll_number);
      if v_error is not null then
        raise exception '% Adjust the capacity or renumber that student first.', v_error;
      end if;
    end loop;
  end if;
  return new;
end $$;

revoke all on function fn_guard_session_shift_change() from public, anon, authenticated;
