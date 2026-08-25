const { Paragraph, HeadingLevel, frTable, tableCaption } = require("../generate_report.js");

const content = [
  new Paragraph({ text: "2.3.4 FR-4: Manage Session Timetable", heading: HeadingLevel.HEADING_3, spacing: { after: 100 } }),
  tableCaption("Table 2-5  Description of FR-4"),
  frTable({
    id: "FR-4",
    title: "Manage Session Timetable",
    requirement: "The Admin shall be able to assign a subject, teacher, and room to a weekly time slot for a session, and the system shall refuse to save a slot that would double-book a teacher or a room at the same day and time.",
    source: "College administration and the master timetable process.",
    rationale: "A teacher physically cannot be in two rooms at once; catching that at save-time is cheaper than discovering it on the first day of class.",
    businessRule: "Double-booking is checked with a database trigger, not app-side logic, since the admin app and any future client writing to the same table would otherwise need to duplicate the check.",
    dependencies: "FR-2, FR-3 (a slot references a session's existing subjects).",
    priority: "Medium",
  }),
];

module.exports = { content };
