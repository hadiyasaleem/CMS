const { Paragraph, HeadingLevel, frTable, tableCaption } = require("../generate_report.js");

const content = [
  new Paragraph({ text: "2.3.12 FR-12: Issue Fine", heading: HeadingLevel.HEADING_3, spacing: { after: 100 } }),
  tableCaption("Table 2-13  Description of FR-12"),
  frTable({
    id: "FR-12",
    title: "Issue Fine",
    requirement: "The Admin shall be able to issue a fine against a student's record with a category (library, attendance, exam, disciplinary, other), amount, and reason.",
    source: "College disciplinary/library process.",
    rationale: "Fines needed to attach to the student's existing profile screen rather than live as a separate module, since an admin issuing one is usually already looking at that student's record for another reason.",
    businessRule: "None beyond admin-only write access.",
    dependencies: "FR-2 (a student has to exist in a session first).",
    priority: "Low",
  }),
];

module.exports = { content };
