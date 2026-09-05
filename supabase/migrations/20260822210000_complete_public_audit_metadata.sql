-- Reconstructed from live history: "complete_public_audit_metadata" (applied 2026-08-22 21:05:17 UTC,
-- untracked locally). Recovered via pg_get_functiondef()/information_schema.triggers/pg_constraint
-- against the live project (ygmvyvjhdkxddkqxtrdw) as part of a migration-folder reconciliation pass --
-- not applied here, the live DB already has it; this file only backfills local history.
--
-- NOTE: the `entity_id` column and its indexes/duplicate-index cleanup added here were later dropped
-- by 20260905000004_drop_unused_entity_id_dup_indexes_view.sql (kept only on session_attendance,
-- where it's a genuine pagination tie-breaker) -- reconstructed here anyway for historical accuracy,
-- so a fresh `supabase db reset` replays the same path production actually took.
--
-- Adds a uniform soft-delete + full audit trail (created/updated/deleted at+by) across every
-- user-facing table, backed by one trigger function instead of ad-hoc per-table logic.
create or replace function audit_actor()
returns text
language sql
stable
set search_path to 'public'
as $$
  select nullif(lower(coalesce(auth.jwt() ->> 'email', current_setting('app.cms_actor', true), '')), '')
$$;

create or replace function fn_cms_audit_row()
returns trigger
language plpgsql
set search_path to 'public'
as $$
declare actor text := public.audit_actor();
begin
  if tg_op = 'INSERT' then
    new.created_at := now(); new.created_by := actor;
    new.updated_at := new.created_at; new.updated_by := actor;
    if new.is_deleted then
      new.deleted_at := new.updated_at; new.deleted_by := actor;
    else
      new.deleted_at := null; new.deleted_by := null;
    end if;
    return new;
  end if;
  new.created_at := old.created_at; new.created_by := old.created_by;
  new.updated_at := now(); new.updated_by := coalesce(actor, old.updated_by);
  if new.is_deleted and not old.is_deleted then
    new.deleted_at := new.updated_at; new.deleted_by := coalesce(actor, old.deleted_by);
  elsif not new.is_deleted then
    new.deleted_at := null; new.deleted_by := null;
  else
    new.deleted_at := old.deleted_at; new.deleted_by := old.deleted_by;
  end if;
  return new;
end
$$;

do $$
declare t text;
begin
  foreach t in array array[
    'academic_sessions','calendar_events','datesheet_slots','datesheets','departments',
    'exam_paper_submissions','fee_overrides','fines','mark_edit_requests','notifications',
    'period_sessions','profiles','semester_terms','session_attendance','session_fee_heads',
    'session_fees','session_marks','session_students','session_subjects','student_link_requests',
    'student_semester_gpa','teachers','timetable_periods'
  ] loop
    execute format('alter table %I add column if not exists entity_id bigint generated always as identity', t);
    execute format('alter table %I add column if not exists is_deleted boolean not null default false', t);
    execute format('alter table %I add column if not exists deleted_at timestamptz', t);
    execute format('alter table %I add column if not exists deleted_by text', t);
    execute format(
      'create trigger trg_audit_%I before insert or update on %I for each row execute function fn_cms_audit_row()',
      t, t);
  end loop;
end $$;
