-- Central failure logging: crashes and unexpected/critical failures from every client (mobile +
-- desktop, all three roles) land here, buffered locally in Room first and flushed during the
-- normal sync cycle (see AppLogRepositoryImpl). Expected/user-recoverable errors (validation,
-- permission, auth, not-found, network) are never uploaded — see ErrorClassifier.Severity.
--
-- Deliberately deviates from the usual table shape (buildings/rooms, etc): this table is
-- append-only and never synced back down to a client, so it carries none of the usual audit
-- machinery:
--   * no updated_at column / trg_touch_ trigger  -- rows are never updated
--   * no is_deleted soft-delete block            -- rows are never deleted by clients
-- log_id is client-generated (a UUID minted alongside the LogRecord) so a record that fails to
-- upload and is retried on the next sync never creates a duplicate row.
create table app_logs (
  log_id         text primary key,
  occurred_at    timestamptz not null,
  severity       text not null,
  kind           text,
  tag            text,
  message        text not null,
  stack_trace    text,
  account_email  text,
  app_id         text,
  app_version    text,
  platform       text,
  device_info    text,
  created_at     timestamptz not null default now(),
  created_by     text
);

create index idx_app_logs_occurred_at on app_logs (occurred_at desc);

alter table app_logs enable row level security;

-- Any authenticated user may insert log rows, but only their own (account_email must match their
-- JWT, or be null for a pre-sign-in crash) -- this is a write-only endpoint for clients, not a
-- place to write about someone else's session.
create policy ins_app_logs on app_logs for insert to authenticated
  with check (account_email is null or account_email = current_email());

-- Only admins can read logs back -- this table exists for diagnosing field failures, not for
-- clients to consume.
create policy sel_app_logs on app_logs for select to authenticated
  using (is_admin());

-- No update/delete policy at all: rows are immutable to every client once written.
