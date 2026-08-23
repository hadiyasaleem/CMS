const { Paragraph, HeadingLevel, frTable, tableCaption } = require("../generate_report.js");

const content = [
  new Paragraph({ text: "2.3.6 FR-6: Enter Marks (Locks on First Entry)", heading: HeadingLevel.HEADING_3, spacing: { after: 100 } }),
  tableCaption("Table 2-7  Description of FR-6"),
  frTable({
    id: "FR-6",
    title: "Enter Marks",
    requirement: "A Teacher shall be able to enter midterm (out of 25) or sessional (out of 15) scores for every student in a class they teach. The field shall reject a negative value or a value above the exam's maximum as the teacher types, before they even try to save. Once a score is saved for a student, it becomes read-only in this screen — a further change has to go through FR-7.",
    source: "Direct requirement — see 1.3 objectives, and the current problem of unaudited mark changes.",
    rationale: "Catching an out-of-range score live (rather than after Save, or worse, not at all) prevents a mistyped '250' from ever reaching the database in the first place.",
    businessRule: "The Save action only writes rows for students who don't already have a saved score — it never re-submits an existing row, since the database itself would reject that update (see FR-7's business rule).",
    dependencies: "FR-4 (a teacher's class assignment), FR-3 (which exam types/max-marks apply).",
    priority: "High",
  }),
];

module.exports = { content };
