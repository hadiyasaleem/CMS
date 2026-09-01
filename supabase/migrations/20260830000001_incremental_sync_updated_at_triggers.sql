-- Incremental clients query updated_at >= their per-table high-water mark. Every table
-- participating in that protocol needs audit columns, a soft-delete tombstone, an index, and a
-- trigger that advances updated_at on every update.
do $$
declare
  target_table text;
begin
  foreach target_table in array array[
    'profiles',
    'departments',
    'teachers',
    'academic_sessions',
    'session_subjects',
    'semester_terms',
    'session_students',
    'timetable_periods',
    'period_sessions',
    'session_attendance',
    'session_marks',
    'mark_edit_requests',
    'student_semester_gpa',
    'session_fees',
    'session_fee_heads',
    'fee_overrides',
    'fines',
    'datesheets',
    'datesheet_slots',
    'calendar_events',
    'exam_paper_submissions',
    'student_link_requests',
    'notifications'
  ] loop
    if to_regclass(format('public.%I', target_table)) is not null then
      execute format('alter table public.%I add column if not exists created_at timestamptz not null default now()', target_table);
      execute format('alter table public.%I add column if not exists created_by text', target_table);
      execute format('alter table public.%I add column if not exists updated_at timestamptz not null default now()', target_table);
      execute format('alter table public.%I add column if not exists updated_by text', target_table);
      execute format('alter table public.%I add column if not exists is_deleted boolean not null default false', target_table);
      execute format('alter table public.%I add column if not exists deleted_at timestamptz', target_table);
      execute format('alter table public.%I add column if not exists deleted_by text', target_table);

      execute format(
        'create index if not exists %I on public.%I (updated_at)',
        'idx_' || target_table || '_updated_at',
        target_table
      );
      execute format('drop trigger if exists trg_touch_%I on public.%I', target_table, target_table);
      execute format(
        'create trigger trg_touch_%I before update on public.%I for each row execute function public.fn_touch_updated_at()',
        target_table,
        target_table
      );
    end if;
  end loop;
end $$;
