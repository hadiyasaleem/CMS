-- Supply (retake) subjects a student carries for a semester + extend the RPC to record them.
alter table student_semester_gpa add column if not exists supply_courses text[] not null default '{}';

drop function if exists record_semester_result(text,text,int,numeric,numeric,text,semester_result,int,text);

create or replace function record_semester_result(
  p_session text, p_roll text, p_semester int, p_gpa numeric, p_cgpa numeric,
  p_term_label text default null, p_result semester_result default 'PENDING',
  p_class_position int default null, p_remarks text default null,
  p_supply text[] default null
) returns void language plpgsql security definer set search_path to 'public' as $$
begin
  if not (is_admin() or teaches(p_session)) then raise exception 'not allowed'; end if;
  insert into student_semester_gpa
    (session_id, roll_number, semester, gpa, cgpa, term_label, result_status,
     class_position, remarks, supply_courses, created_by, updated_by)
  values
    (p_session, p_roll, p_semester, p_gpa, p_cgpa, p_term_label, p_result,
     p_class_position, p_remarks, coalesce(p_supply, '{}'), current_email(), current_email())
  on conflict (session_id, roll_number, semester) do update set
    gpa = excluded.gpa, cgpa = excluded.cgpa, term_label = excluded.term_label,
    result_status = excluded.result_status, class_position = excluded.class_position,
    remarks = excluded.remarks, supply_courses = excluded.supply_courses,
    updated_by = current_email(), updated_at = now();
  update session_students st set gpa = p_gpa, cgpa = p_cgpa, updated_at = now()
  where st.session_id = p_session and st.roll_number = p_roll
    and not exists (select 1 from student_semester_gpa g
                    where g.session_id = p_session and g.roll_number = p_roll
                      and g.semester > p_semester);
end $$;

revoke all on function record_semester_result(text,text,int,numeric,numeric,text,semester_result,int,text,text[]) from public, anon;
grant execute on function record_semester_result(text,text,int,numeric,numeric,text,semester_result,int,text,text[]) to authenticated;
