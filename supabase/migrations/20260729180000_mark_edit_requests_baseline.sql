-- Reconstructed from live history: "mark_edit_requests_baseline" (applied 2026-07-29 18:16:54 UTC,
-- untracked locally). Recovered by diffing the live `mark_edit_requests` table against what the
-- preceding "mark_edit_requests" migration (20260721210143) created -- not applied here, the live DB
-- already has it; this file only backfills local history.
--
-- `mark_edit_requests` was created eight days after the base schema's dynamic touch-trigger loop ran
-- (see 20260714000001_schema.sql), so it never got a trg_touch_ trigger. This "baseline" migration
-- brings the table in line with every other audited table by wiring up the same updated_at trigger,
-- and adds the same idx_..._updated_at pattern the loop's sibling tables already had.
create trigger trg_touch_mark_edit_requests before update on mark_edit_requests
  for each row execute function fn_touch_updated_at();

create index idx_mark_edit_requests_updated_at on mark_edit_requests(updated_at);
