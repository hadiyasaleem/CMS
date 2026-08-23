const { Paragraph, HeadingLevel, frTable, tableCaption } = require("../generate_report.js");

const content = [
  new Paragraph({ text: "2.3.5 FR-5: Mark Attendance", heading: HeadingLevel.HEADING_3, spacing: { after: 100 } }),
  tableCaption("Table 2-6  Description of FR-5"),
  frTable({
    id: "FR-5",
    title: "Mark Attendance",
    requirement: "A Teacher shall be able to mark a class's attendance for the current day in a single screen, and view the attendance history for a session/course they teach.",
    source: "Daily classroom practice — replaces the paper register.",
    rationale: "This is the single most frequent write in the whole system (once per class, per day, per teacher), so it needs to be fast — the whole class in one screen, not one row at a time.",
    businessRule: "A teacher can only mark attendance for a session/course they're actually assigned to on the timetable; enforced by RLS (teaches(session_id)), not just hidden in the UI.",
    dependencies: "FR-4 (a teacher's assignment comes from the timetable).",
    priority: "High",
  }),
];

module.exports = { content };
