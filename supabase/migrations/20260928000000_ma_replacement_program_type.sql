-- Add MA-Replacement (2-year) program type alongside the existing 4-year BS program.
--
-- A department may now run a BS session (semesters 1-8, end_year = start_year+4) and an
-- MA-Replacement session (semesters 5-8 only, end_year = start_year+2) for the same start_year at
-- once. session_id keeps its BS shape "{deptId}_{startYear}" and gains a "_ma" suffix for
-- MA-Replacement ("{deptId}_{startYear}_ma"), so the two rows never collide on session_id.
--
-- academic_sessions already holds live data by the time this migration was written (unlike the
-- 0926 shift-consolidation migration, which ran against an empty table). Every existing row is
-- already BS-shaped (session_id = "{deptId}_{startYear}", end_year = start_year+4, semester 1-8),
-- verified directly before writing this migration, so the new program_type column backfills to
-- 'BS' via its DEFAULT and every new/existing constraint below is satisfied without a data migration.

create type program_type as enum ('BS', 'MA_REPLACEMENT');

alter table academic_sessions add column program_type program_type not null default 'BS';
alter table academic_sessions alter column program_type drop default;  -- the app always chooses explicitly
comment on column academic_sessions.program_type is
  'BS: 4-year program, semesters 1-8. MA_REPLACEMENT: 2-year program, semesters 5-8 only.';

alter table academic_sessions drop constraint academic_sessions_dept_id_start_year_key;
alter table academic_sessions add constraint academic_sessions_dept_id_start_year_program_key
  unique (dept_id, start_year, program_type);

alter table academic_sessions drop constraint academic_sessions_session_id_format;
alter table academic_sessions add constraint academic_sessions_session_id_format
  check (
    (program_type = 'BS' and session_id = dept_id || '_' || start_year::text)
    or (program_type = 'MA_REPLACEMENT' and session_id = dept_id || '_' || start_year::text || '_ma')
  );

alter table academic_sessions drop constraint academic_sessions_current_semester_check;
alter table academic_sessions add constraint academic_sessions_current_semester_check
  check (
    (program_type = 'BS' and current_semester between 1 and 8)
    or (program_type = 'MA_REPLACEMENT' and current_semester between 5 and 8)
  );

alter table academic_sessions add constraint academic_sessions_end_year_check
  check (
    (program_type = 'BS' and end_year = start_year + 4)
    or (program_type = 'MA_REPLACEMENT' and end_year = start_year + 2)
  );
