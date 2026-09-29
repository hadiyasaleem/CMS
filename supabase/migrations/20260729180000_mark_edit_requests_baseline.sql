-- Reconstructed from live history: "mark_edit_requests_baseline" (applied 2026-07-29 18:16:54 UTC,
-- untracked locally). Recovered by diffing the live `mark_edit_requests` table against what the
-- preceding "mark_edit_requests" migration (20260721210143) created -- not applied here, the live DB
-- already has it; this file only backfills local history.
--
-- `mark_edit_requests` was created eight days after the base schema's dynamic touch-trigger loop ran
-- (see 20260714000001_schema.sql), so it never got a trg_touch_ trigger. This "baseline" migration
-- brings the table in line with every other audited table by wiring up the same updated_at trigger,
-- and adds the same idx_..._updated_at pattern the loop's sibling tables already had.
--
-- Replayability: the backfilled 20260721210000 file creates the table WITHOUT updated_at (the live table
-- already had it when this ran), so a from-scratch replay failed here with `column "updated_at" does not
-- exist`. The statements below are all no-ops on the live database (column, index and trigger exist there)
-- and make a fresh database reach the same state.
alter table mark_edit_requests add column if not exists updated_at timestamptz not null default now();

drop trigger if exists trg_touch_mark_edit_requests on mark_edit_requests;
create trigger trg_touch_mark_edit_requests before update on mark_edit_requests
  for each row execute function fn_touch_updated_at();

create index if not exists idx_mark_edit_requests_updated_at on mark_edit_requests(updated_at);
