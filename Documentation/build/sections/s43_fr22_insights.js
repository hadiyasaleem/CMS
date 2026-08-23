const { Paragraph, HeadingLevel, frTable, tableCaption } = require("../generate_report.js");

const content = [
  new Paragraph({ text: "2.3.22 FR-22: View Tiered Insights", heading: HeadingLevel.HEADING_3, spacing: { after: 100 } }),
  tableCaption("Table 2-23  Description of FR-22"),
  frTable({
    id: "FR-22",
    title: "View Tiered Insights",
    requirement: "The Admin shall see college-wide session overviews, at-risk students (low attendance or CGPA), and exam-score statistics. A Teacher shall see the same three views scoped automatically to only the sessions and courses they teach.",
    source: "Direct requirement — analytics for oversight, one of the last features built.",
    rationale: "\"Tiered\" doesn't mean two separate screens with two separate query paths — it's the same three Postgres views (session_overview, at_risk_students, exam_stats) queried by both roles. Row-Level Security on the underlying tables does the scoping, so there's no role-branching logic in the app at all.",
    businessRule: "session_overview joins from a world-readable table (academic_sessions), so a teacher's query technically returns a row for every session college-wide — just with student counts and averages blank for sessions they don't teach, since RLS blocks the joined rows, not the session row itself. No student-level data ever leaks through this.",
    dependencies: "FR-5, FR-6, FR-9 (the underlying views read attendance, marks, and GPA data).",
    priority: "Low",
  }),
];

module.exports = { content };
