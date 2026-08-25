# CMS Supabase backend

Everything here is **ready to apply but not yet applied** — Phase 0 is blocked until the
correct Supabase account is active (a different account is currently logged in).

## Deploy (once, on the right account)

```bash
supabase login
supabase projects create cms-mbd --region <pick-nearest>   # or via dashboard/MCP
supabase link --project-ref <ref>
supabase db push                                            # applies migrations/ in order
supabase functions deploy admin-create-user promote-session \
  archive-and-delete-session set-teacher-status revoke-student-link
```

Then, in the dashboard:
1. **Auth → Providers → Email**: keep email/password on. For student self-signup either
   disable "Confirm email" or wire a free SMTP provider (Resend/Brevo) — the built-in
   SMTP is rate-limited to a few emails/hour. Disable "secure email change"
   (email is the app's identity key; changes must stay admin-managed).
2. **Create the bootstrap admin** (Auth → Add user, auto-confirm) with the email in
   `bootstrap_admin_email()` (`migrations/20260714000002_rls.sql`) — currently
   `admin@example.com`; change BOTH the SQL function and
   `cmsadmin LoginViewModel.ADMIN_BOOTSTRAP_EMAIL` if you use a different address.
   The signup trigger creates their profile as STUDENT — **upgrade it** so the admin app's
   role gate shows the admin UI (RLS already treats the bootstrap email as admin regardless):
   ```sql
   update profiles set role = 'ADMIN' where email = 'admin@example.com';
   ```
3. Copy the **Project URL + anon key** into each developer's `local.properties`:
   ```properties
   supabase.url=https://<ref>.supabase.co
   supabase.anonKey=<anon key>
   ```
   (Read by `app/build.gradle.kts` into BuildConfig. Never commit them.)

## Free-tier keep-alive

The Free tier pauses projects after ~7 days without **API** activity (pg_cron does not
count). Add a GitHub Actions cron that pings one cheap endpoint every 3 days:

```yaml
# .github/workflows/supabase-keepalive.yml
on: { schedule: [{ cron: "0 6 */3 * *" }] }
jobs:
  ping:
    runs-on: ubuntu-latest
    steps:
      - run: |
          curl -s "$SUPABASE_URL/rest/v1/departments?select=dept_id&limit=1" \
            -H "apikey: $SUPABASE_ANON_KEY" -H "Authorization: Bearer $SUPABASE_ANON_KEY"
        env:
          SUPABASE_URL: ${{ secrets.SUPABASE_URL }}
          SUPABASE_ANON_KEY: ${{ secrets.SUPABASE_ANON_KEY }}
```

Remove it if you upgrade to Pro.

## Layout

- `migrations/20260714000001_schema.sql` — enums, tables, triggers, security-invoker
  views, indexes (incl. the `timerange` type + teacher/room no-double-booking
  exclusion constraints).
- `migrations/20260714000002_rls.sql` — helper functions, `record_semester_result`
  RPC (the ONLY teacher write-path for GPA), all row-level-security policies.
  Invariant: **admin can read but never write attendance.**
- `migrations/20260714000003_storage_cron.sql` — buckets (`exam-papers` PDF ≤ 5 MB,
  `photos`, `documents`) + storage policies + pg_cron purge jobs.
- `functions/` — service-role Edge Functions; every one verifies the CALLER is an
  admin via `_shared/auth.ts` before acting.

## After deploying

Run the security & performance advisors (dashboard → Advisors, or MCP `get_advisors`)
and fix anything they flag before real use.
