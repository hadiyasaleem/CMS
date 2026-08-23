const { Paragraph, TextRun, HeadingLevel, table, tableCaption } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const content = [
  new Paragraph({ text: "3.4.1 Data Dictionary", heading: HeadingLevel.HEADING_3, spacing: { after: 100 } }),
  new Paragraph({ text: "Identity", heading: HeadingLevel.HEADING_4, spacing: { after: 100 } }),
  para("Who a signed-in account is, and which role/roster row it resolves to. session_students carries a rich profile (30+ columns covering contact details, guardians, emergency contacts, and enrollment status) beyond what's listed here — only the fields that other tables actually reference are shown."),
  tableCaption("Table 3-1  Data Dictionary — Identity"),
  table(
    ["Entity.Field", "Type", "Description"],
    [
      ["profiles.id", "uuid (PK)", "Matches the GoTrue auth user ID — the JWT subject."],
      ["profiles.email", "text", "Normalized identity key used across the schema."],
      ["profiles.role", "enum", "ADMIN / TEACHER / STUDENT."],
      ["profiles.linked_session_id / linked_roll", "text", "Set once a student account is approved (FR-20); together they point at one session_students row."],
      ["profiles.status", "enum", "Mirrors the account's lifecycle status (see teachers.status)."],
      ["teachers.email", "text (PK)", "Normalized identity key; also the FK target from timetable_periods.teacher_email."],
      ["teachers.status", "enum", "ACTIVE / DISABLED / BANNED (FR-21)."],
      ["teachers.can_approve_link_requests / can_edit_timetable / can_send_notifications / can_manage_datesheets", "boolean", "Per-teacher permission flags, admin-set (FR-21)."],
      ["session_students.session_id + roll_number", "text (composite PK)", "Natural key — no surrogate ID."],
      ["session_students.linked_email", "text", "Set by FR-20's approval; empty string until linked."],
      ["session_students.gpa / cgpa", "numeric", "Snapshot updated by the record_semester_result RPC (FR-9), not written directly."],
      ["session_students.enrollment_status", "enum", "ACTIVE / WITHDRAWN / etc."],
    ],
    [2600, 1400, 4960],
  ),
];

module.exports = { content };
