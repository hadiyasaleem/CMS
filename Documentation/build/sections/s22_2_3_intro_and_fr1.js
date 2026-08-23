const { Paragraph, TextRun, HeadingLevel, frTable, tableCaption } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const content = [
  new Paragraph({ text: "2.3 Functional Requirements", heading: HeadingLevel.HEADING_2, spacing: { after: 120 } }),
  para("The requirements below are grouped by the module that owns them — academic structure, attendance/marks, financial, notices, and governance/analytics — which follows how the admin app itself is organized rather than an arbitrary alphabetical list. Each one uses the same eight-attribute template so a reviewer can cross-reference it against the traceability matrix in Chapter 7 without guessing at what \"source\" or \"business rule\" means from row to row."),

  new Paragraph({ text: "2.3.1 FR-1: Manage Departments", heading: HeadingLevel.HEADING_3, spacing: { after: 100 } }),
  tableCaption("Table 2-2  Description of FR-1"),
  frTable({
    id: "FR-1",
    title: "Manage Departments",
    requirement: "The Admin shall be able to create and list departments. Each department is a top-level container that owns its own sessions (intakes); nothing else in the system can exist without a department to attach to.",
    source: "College administration — departments are the college's real organisational unit (e.g. Computer Science, Information Technology).",
    rationale: "Every other piece of academic data (sessions, curriculum, fees, students) is scoped under a department, so this is the first thing that has to exist.",
    businessRule: "A department's ID cannot contain underscores, since session IDs are built as {deptId}_{startYear}_{shift} and are parsed back apart by splitting on underscore.",
    dependencies: "None — this is the root of the data model.",
    priority: "High",
  }),
];

module.exports = { content };
