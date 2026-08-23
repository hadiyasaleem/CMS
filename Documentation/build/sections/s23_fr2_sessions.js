const { Paragraph, HeadingLevel, frTable, tableCaption } = require("../generate_report.js");

const content = [
  new Paragraph({ text: "2.3.2 FR-2: Manage Sessions", heading: HeadingLevel.HEADING_3, spacing: { after: 100 } }),
  tableCaption("Table 2-3  Description of FR-2"),
  frTable({
    id: "FR-2",
    title: "Manage Sessions",
    requirement: "The Admin shall be able to create a session (an intake, e.g. \"2021 Morning\") under a department, delete it, and promote its current semester as the class progresses. Deleting a session shall cascade to its students, timetable, and curriculum.",
    source: "College administration — a session is a specific batch of students admitted in a given year and shift.",
    rationale: "Attendance, marks, fees, and timetable all key off a session, not just a department, because two intakes of the same program can be at different semesters simultaneously.",
    businessRule: "Session ID is built as {deptId}_{startYear}_{shift}; shift is MORNING or EVENING.",
    dependencies: "FR-1 (a session cannot exist without its department).",
    priority: "High",
  }),
];

module.exports = { content };
