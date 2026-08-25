const { Paragraph, TextRun, HeadingLevel, table, tableCaption } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const content = [
  new Paragraph({ text: "Academic Structure", heading: HeadingLevel.HEADING_4, spacing: { after: 100 } }),
  para("The core of the schema — departments own sessions, sessions own everything else (subjects, timetable, attendance, marks, results)."),
  tableCaption("Table 3-2  Data Dictionary — Academic Structure"),
  table(
    ["Entity.Field", "Type", "Description"],
    [
      ["departments.dept_id", "text (PK)", "No underscores allowed — session IDs split on '_' to recover this."],
      ["academic_sessions.session_id", "text (PK)", "{deptId}_{startYear}_{shift}, e.g. it_2021_MORNING."],
      ["academic_sessions.current_semester", "int", "The one field FR-2's \"promote\" action changes; drives which curriculum/timetable rows apply."],
      ["session_subjects (session_id, semester, course_code)", "composite PK", "Curriculum — replaced wholesale on save (FR-3), never diffed."],
      ["timetable_periods.id", "uuid (PK)", "day + start_time + end_time + primary_session_id are checked by a trigger to prevent double-booking a teacher/room (FR-4)."],
      ["session_attendance (session_id, semester, course_code, date, roll_number)", "composite PK", "One row per student per class per day; is_late defaults false but is explicitly always-encoded (see 3.7) since it hit the DTO-default bug."],
      ["session_marks (session_id, semester, course_code, exam_type, roll_number)", "composite PK", "score is nullable until entered; UPDATE is admin-only (FR-6/FR-7)."],
      ["mark_edit_requests.id", "uuid (PK)", "status: PENDING / APPROVED / REJECTED (FR-7/FR-8)."],
      ["student_semester_gpa (session_id, roll_number, semester)", "composite PK", "supply_courses is a text[]; written only via the record_semester_result RPC, never a direct insert (FR-9)."],
    ],
    [2900, 1600, 4460],
  ),
];

module.exports = { content };
