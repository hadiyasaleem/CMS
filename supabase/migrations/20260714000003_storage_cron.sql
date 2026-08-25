-- ═══════════════════════════════════════════════════════════════════════════
-- Storage buckets (server-side size/MIME limits) + pg_cron housekeeping.
-- ═══════════════════════════════════════════════════════════════════════════

-- exam-papers: PDF only, ≤ 5 MB — the cap is enforced by the bucket itself.
insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values ('exam-papers', 'exam-papers', false, 5242880, array['application/pdf'])
on conflict (id) do nothing;

-- photos: student/teacher profile photos, ≤ 1 MB images.
insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values ('photos', 'photos', false, 1048576, array['image/jpeg','image/png','image/webp'])
on conflict (id) do nothing;

-- documents: prospectus / rules / notices / archives, ≤ 10 MB PDFs.
insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values ('documents', 'documents', false, 10485760, array['application/pdf','application/json'])
on conflict (id) do nothing;

-- ── Storage policies (storage.objects) ──────────────────────────────────────
-- Object key conventions:
--   exam-papers/{session_id}/{semester}/{course_code}/{file}
--   photos/students/{session_id}/{roll}.jpg · photos/teachers/{email}.jpg
--   documents/{kind}/{file} · documents/archives/{session_id}.json

-- exam-papers: teachers upload/manage for sessions they teach; admin reads all;
-- students have NO access (papers are teacher→admin material).
create policy papers_teacher_write on storage.objects for insert to authenticated
  with check (bucket_id = 'exam-papers'
              and public.is_active_teacher()
              and public.teaches(split_part(name, '/', 1)));
create policy papers_read on storage.objects for select to authenticated
  using (bucket_id = 'exam-papers'
         and (public.is_admin() or owner = auth.uid()));
create policy papers_delete on storage.objects for delete to authenticated
  using (bucket_id = 'exam-papers'
         and (public.is_admin() or owner = auth.uid()));
create policy papers_update on storage.objects for update to authenticated
  using (bucket_id = 'exam-papers' and (public.is_admin() or owner = auth.uid()));

-- photos: teacher photos are app-public; student photos limited to admin, the
-- teacher(s) of that session, and classmates in the same session. Key layout:
--   photos/teachers/{email}.jpg · photos/students/{session_id}/{roll}.jpg
create policy photos_read on storage.objects for select to authenticated using (
  bucket_id = 'photos' and (
    split_part(name, '/', 1) = 'teachers'
    or is_admin()
    or (split_part(name, '/', 1) = 'students'
        and (public.teaches(split_part(name, '/', 2))
             or split_part(name, '/', 2) = public.my_session()))
  )
);
create policy photos_admin_write on storage.objects for all to authenticated
  using (bucket_id = 'photos' and public.is_admin())
  with check (bucket_id = 'photos' and public.is_admin());

-- documents: everyone signed-in reads published material; admin manages.
-- (archives/ subfolder is service-role only — no authenticated read policy needed
--  because RLS default-denies anything not matched below.)
create policy docs_read on storage.objects for select to authenticated
  using (bucket_id = 'documents'
         and (public.is_admin() or split_part(name, '/', 1) <> 'archives'));
create policy docs_admin_write on storage.objects for all to authenticated
  using (bucket_id = 'documents' and public.is_admin())
  with check (bucket_id = 'documents' and public.is_admin());

-- ── pg_cron housekeeping ─────────────────────────────────────────────────────
-- NOTE: pg_cron activity does NOT count as project activity and will not prevent
-- the Free-tier 7-day auto-pause. The keep-alive must be an EXTERNAL API hit
-- (e.g. a GitHub Actions cron curling one REST endpoint with the anon key).
create extension if not exists pg_cron;

-- Nightly: purge expired notifications.
select cron.schedule('purge-expired-notifications', '15 2 * * *',
  $$delete from notifications where expires_at is not null and expires_at < now()$$);

-- Nightly: flag sessions past their end year as inactive (archival stays manual).
select cron.schedule('flag-ended-sessions', '30 2 * * *',
  $$update academic_sessions
      set is_active = false
    where is_active and end_year < extract(year from current_date)$$);
