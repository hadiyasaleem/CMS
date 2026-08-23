const { Paragraph, HeadingLevel, frTable, tableCaption } = require("../generate_report.js");

const content = [
  new Paragraph({ text: "2.3.13 FR-13: View Fines", heading: HeadingLevel.HEADING_3, spacing: { after: 100 } }),
  tableCaption("Table 2-14  Description of FR-13"),
  frTable({
    id: "FR-13",
    title: "View Fines",
    requirement: "A Student shall be able to see a read-only list of fines issued against them, with a running total.",
    source: "Direct requirement — pairs with FR-12.",
    rationale: "A student finding out about a fine through the app instead of at graduation clearance is the whole point of digitising this.",
    businessRule: "None.",
    dependencies: "FR-12.",
    priority: "Low",
  }),
];

module.exports = { content };
