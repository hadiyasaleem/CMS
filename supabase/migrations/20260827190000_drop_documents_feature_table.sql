-- Reconstructed from live history: "drop_documents_feature_table" (applied 2026-08-27 19:00:00 UTC,
-- exact original time 18:58:29, untracked locally). Recovered by confirming `public.documents` no
-- longer exists live while its `doc_kind` enum type still does (orphaned) -- not applied here, the
-- live DB already has it; this file only backfills local history.
--
-- The Documents feature (prospectus/rules/report uploads) was removed. This mirrors the local Room
-- side, which drops its own `documents` cache table in MIGRATION_30_31
-- (core/src/main/kotlin/com/mbd/cmscommon/data/local/CmsDatabaseMigrations.kt).
drop table if exists documents;
