-- Reconstructed from live history: "guard_exam_paper_review_columns" (applied 2026-09-02 19:05:08 UTC,
-- untracked locally -- not in the original reconciliation list but discovered untracked during that
-- pass; included here for completeness). Recovered via pg_get_functiondef()/information_schema.triggers
-- against the live project (ygmvyvjhdkxddkqxtrdw) -- not applied here, the live DB already has it; this
-- file only backfills local history.
--
-- A teacher may still touch/soft-delete their own exam_paper_submissions row, but only an admin's
-- review action may change the review outcome columns (review_status, reviewed_by, reviewed_at,
-- teacher_notes, key_storage_path) -- and none of them may be pre-set on insert.
create or replace function fn_guard_exam_paper_review()
returns trigger
language plpgsql
security definer
set search_path to 'public'
as $$
begin
  if coalesce(auth.jwt() ->> 'role', '') = 'service_role' then return new; end if;
  if is_admin() then return new; end if;

  if tg_op = 'INSERT' then
    if new.review_status is distinct from 'SUBMITTED'
       or new.reviewed_by is not null
       or new.reviewed_at is not null
       or new.teacher_notes is not null
       or new.key_storage_path is not null then
      raise exception 'not allowed to set exam paper review columns on insert';
    end if;
    return new;
  end if;

  if new.review_status is distinct from old.review_status
     or new.reviewed_by is distinct from old.reviewed_by
     or new.reviewed_at is distinct from old.reviewed_at
     or new.teacher_notes is distinct from old.teacher_notes
     or new.key_storage_path is distinct from old.key_storage_path then
    raise exception 'not allowed to modify exam paper review columns';
  end if;
  return new;
end
$$;

create trigger trg_guard_exam_paper_review before insert or update on exam_paper_submissions
  for each row execute function fn_guard_exam_paper_review();
