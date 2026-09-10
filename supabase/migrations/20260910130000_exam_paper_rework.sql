-- Exam paper submission rework: bind each submission to a published datesheet slot instead of a
-- free (session, course, exam_type) choice, and drop the admin "review" flow -- admin now just
-- downloads submitted papers to print, doesn't grade them. See product decision: exam papers are
-- Mid-Term-only going forward (datesheets never cover Sessional), so exam_type is redundant.

alter table exam_paper_submissions
  add column datesheet_slot_id uuid references datesheet_slots(id) on delete cascade;

-- At most one ACTIVE submission per slot -- a soft-deleted row doesn't block a fresh resubmission.
create unique index uq_exam_paper_active_slot on exam_paper_submissions(datesheet_slot_id)
  where not is_deleted;

alter table exam_paper_submissions
  drop column exam_type,
  drop column review_status,
  drop column reviewed_by,
  drop column reviewed_at,
  drop column teacher_notes,
  drop column key_storage_path,
  add column description text;

drop trigger if exists trg_guard_exam_paper_review on exam_paper_submissions;
drop function if exists fn_guard_exam_paper_review();
