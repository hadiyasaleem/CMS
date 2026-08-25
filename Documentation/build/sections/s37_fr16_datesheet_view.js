const { Paragraph, HeadingLevel, frTable, tableCaption } = require("../generate_report.js");

const content = [
  new Paragraph({ text: "2.3.16 FR-16: View Datesheet", heading: HeadingLevel.HEADING_3, spacing: { after: 100 } }),
  tableCaption("Table 2-17  Description of FR-16"),
  frTable({
    id: "FR-16",
    title: "View Datesheet",
    requirement: "A Teacher or Student shall be able to browse published datesheets and expand one to see its exam slots.",
    source: "Direct requirement — the read side of FR-15.",
    rationale: "None beyond the obvious — a student needs to know when their own exams are.",
    businessRule: "Enforced by RLS: only rows with published = true are visible to a non-managing role.",
    dependencies: "FR-15.",
    priority: "Medium",
  }),
];

module.exports = { content };
