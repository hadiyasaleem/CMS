-- Fingerprint of everything in the public schema that the migrations create: one row per object with an md5 of its
-- normalised definition (comments and whitespace ignored). Run it on a from-scratch replay AND on production, then diff
-- the two outputs -- any row that differs or exists on one side only is drift. See Documentation/local-test-tools.md.
--
--   scratch:     node supabase/tests/local/q.mjs "$(cat supabase/tests/local/drift.sql)" > scratch.txt
--   production:  paste into the Supabase SQL editor (read-only) and export the result
--
-- Not comparable on a scratch database (skipped here): grants/ACLs (the scratch shim lacks Supabase's default
-- privileges), storage buckets/policies, pg_cron jobs, triggers on auth.users, extension versions and data. Those were
-- checked by reading production directly; see "Production snapshot vs. what the migrations intend" in
-- Documentation/local-test-tools.md.
select 'fn' as k, p.proname || '(' || pg_get_function_identity_arguments(p.oid) || ')' as name,
       md5(regexp_replace(regexp_replace(pg_get_functiondef(p.oid), '--[^\n]*', '', 'g'), '\s+', ' ', 'g')) as h
  from pg_proc p where p.pronamespace = 'public'::regnamespace and p.prokind = 'f'
union all
select 'trg', c.relname || '.' || t.tgname, md5(regexp_replace(pg_get_triggerdef(t.oid), '\s+', ' ', 'g'))
  from pg_trigger t join pg_class c on c.oid = t.tgrelid
 where not t.tgisinternal and c.relnamespace = 'public'::regnamespace
union all
select 'pol', tablename || '.' || policyname,
       md5(coalesce(cmd, '') || coalesce(roles::text, '') || coalesce(regexp_replace(qual, '\s+', ' ', 'g'), '') || '|' || coalesce(regexp_replace(with_check, '\s+', ' ', 'g'), ''))
  from pg_policies where schemaname = 'public'
union all
select 'idx', tablename || '.' || indexname, md5(regexp_replace(indexdef, '\s+', ' ', 'g'))
  from pg_indexes where schemaname = 'public'
union all
-- per table: every column (type, nullability, default, identity/generated)
select 'col', c.table_name,
       md5(string_agg(c.column_name || '|' || c.data_type || '|' || c.udt_name || '|' || c.is_nullable || '|' ||
                      coalesce(regexp_replace(c.column_default, '\s+', ' ', 'g'), '') || '|' || c.is_identity || '|' || coalesce(c.is_generated, ''),
                      ',' order by c.column_name))
  from information_schema.columns c
  join information_schema.tables t on t.table_schema = c.table_schema and t.table_name = c.table_name
 where c.table_schema = 'public' and t.table_type = 'BASE TABLE'
 group by c.table_name
union all
-- per table: primary/unique/foreign/check/exclusion constraints (PostgreSQL 18 also lists NOT NULL as constraints; ignored)
select 'con', cl.relname,
       md5(string_agg(co.conname || regexp_replace(pg_get_constraintdef(co.oid), '\s+', ' ', 'g'), ',' order by co.conname))
  from pg_constraint co join pg_class cl on cl.oid = co.conrelid
 where cl.relnamespace = 'public'::regnamespace and co.contype in ('p', 'u', 'f', 'c', 'x')
 group by cl.relname
union all
select 'enum', t.typname, md5(string_agg(e.enumlabel, ',' order by e.enumsortorder))
  from pg_type t join pg_enum e on e.enumtypid = t.oid
 where t.typnamespace = 'public'::regnamespace group by t.typname
union all
select 'rls', c.relname, md5(c.relrowsecurity::text || c.relforcerowsecurity::text)
  from pg_class c where c.relnamespace = 'public'::regnamespace and c.relkind = 'r'
union all
select 'view', c.relname, md5(regexp_replace(pg_get_viewdef(c.oid), '\s+', ' ', 'g'))
  from pg_class c where c.relnamespace = 'public'::regnamespace and c.relkind in ('v', 'm')
order by 1, 2
