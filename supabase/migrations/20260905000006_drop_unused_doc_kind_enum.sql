-- The `documents` table (drop_documents_feature_table, reconstructed as
-- 20260827190000_drop_documents_feature_table.sql) was dropped, but the `doc_kind` enum it used
-- (PROSPECTUS, RULES, REPORT, OTHER) was left behind. Verified via information_schema.columns that
-- no column anywhere references it before dropping.
drop type if exists doc_kind;
