const { Paragraph, TextRun, HeadingLevel, table, tableCaption } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const content = [
  new Paragraph({ text: "Analytics Views", heading: HeadingLevel.HEADING_4, spacing: { after: 100 } }),
  para("None of these are tables — all five are Postgres views marked security_invoker, which is the one property that makes FR-22's tiered access work: querying a security_invoker view re-checks the caller's own RLS against the underlying tables, rather than running with whatever privileges created the view."),
  tableCaption("Table 3-5  Data Dictionary — Analytics Views"),
  table(
    ["View.Field", "Type", "Description"],
    [
      ["session_overview", "view", "session_id, dept_id, shift, current_semester, students, avg_cgpa, avg_attendance — one row per session, joined from academic_sessions/session_students/session_attendance_summary."],
      ["at_risk_students", "view", "session_id, roll_number, name, cgpa, attendance — flags cgpa < 2.00 or attendance < 75%."],
      ["exam_stats", "view", "session_id, semester, course_code, exam_type, entered, avg_score, min_score, max_score, stddev, out_of, pass_rate — aggregated straight from session_marks."],
      ["student_gpa_progression", "view", "session_id, roll_number, semester, gpa, cgpa, term_label, result_status, class_position, gpa_delta (gpa_delta computed vs. the prior semester)."],
      ["session_attendance_summary", "view", "session_id, semester, course_code, roll_number, present, absent, leave, total_marked, percentage — the aggregate every other attendance-percentage figure in the app is built from."],
    ],
    [2900, 1600, 4460],
  ),
];

module.exports = { content };
