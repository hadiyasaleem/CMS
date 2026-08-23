const { Paragraph, HeadingLevel, frTable, tableCaption } = require("../generate_report.js");

const content = [
  new Paragraph({ text: "2.3.11 FR-11: View Fee Challan", heading: HeadingLevel.HEADING_3, spacing: { after: 100 } }),
  tableCaption("Table 2-12  Description of FR-11"),
  frTable({
    id: "FR-11",
    title: "View Fee Challan",
    requirement: "A Student shall be able to view their own session's fee structure — cadence, fee heads, total, due date, and payment note — without visiting the accounts office.",
    source: "Direct requirement — this is the objective from Chapter 1 about generating a fee challan without an office visit.",
    rationale: "This is informational only; it doesn't process a payment. That was a deliberate scope call (see 1.4/1.8) — the accounts office still collects the money, this screen just removes the reason to visit in person beforehand.",
    businessRule: "None beyond RLS scoping the query to the student's own session.",
    dependencies: "FR-10.",
    priority: "High",
  }),
];

module.exports = { content };
