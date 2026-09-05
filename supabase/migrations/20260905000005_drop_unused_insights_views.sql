-- session_overview, at_risk_students, and exam_stats were believed used by InsightsRepositoryImpl,
-- but that class turns out to be dead code — never bound via DI on mobile or desktop (both bind
-- InsightsRepositoryLocalImpl, which recomputes the same aggregates from locally-synced raw tables
-- instead). Confirmed via grep: InsightsRepositoryImpl is never constructed anywhere. Its desktop
-- counterpart (DesktopInsightsMapper) was equally dead. Dropping all 4 remaining views — the 3
-- above plus session_attendance_summary, whose only dependents were session_overview and
-- at_risk_students (verified via pg_depend before dropping).
drop view if exists at_risk_students;
drop view if exists session_overview;
drop view if exists session_attendance_summary;
drop view if exists exam_stats;
