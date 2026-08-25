const { Paragraph, HeadingLevel, frTable, tableCaption } = require("../generate_report.js");

const content = [
  new Paragraph({ text: "2.3.14 FR-14: Manage Calendar Events", heading: HeadingLevel.HEADING_3, spacing: { after: 100 } }),
  tableCaption("Table 2-15  Description of FR-14"),
  frTable({
    id: "FR-14",
    title: "Manage Calendar Events",
    requirement: "The Admin (or a permitted Teacher) shall be able to create and delete college-wide calendar events — holidays, exam dates, deadlines — and target them to students, teachers, or everyone.",
    source: "Replaces the physical noticeboard for date-based announcements.",
    rationale: "Audience targeting exists because a deadline for teachers (grade submission) and a deadline for students (fee due date) shouldn't both show up cluttering everyone's calendar.",
    businessRule: "Write access requires either is_admin() or a teacher with can_manage_datesheets-equivalent permission; read access is open to all authenticated roles.",
    dependencies: "None.",
    priority: "Medium",
  }),
];

module.exports = { content };
