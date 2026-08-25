const { Paragraph, HeadingLevel, useCaseTable, tableCaption } = require("../generate_report.js");

const content = [
  new Paragraph({ text: "UC-4  View Fee Challan", heading: HeadingLevel.HEADING_2, spacing: { after: 100 } }),
  tableCaption("Table A-4  Use Case Description — View Fee Challan"),
  useCaseTable({
    id: "UC-4",
    name: "View Fee Challan",
    actors: "Student",
    description: "A student checks their session's fee structure and due date without visiting the accounts office.",
    trigger: "Student opens More → Fee Challan.",
    preconditions: "An admin has already set up a fee structure for the student's session (FR-10); if not, the screen has nothing to show.",
    postconditions: "The student has seen the current cadence, fee heads, total, due date, and payment note.",
    normalFlow: "1. Student opens Fee Challan. 2. The app reads the session's fee structure directly from Postgrest (no local cache, since this is a low-frequency read). 3. The screen renders the cadence, each fee head with its amount, the running total, due date, and payment note.",
    alternativeFlows: "2a. No fee structure has been set for this session yet: the screen shows an empty-state message instead of a blank table.",
    businessRules: "This screen is read-only and never processes a payment — a deliberate scope decision (1.4, 1.8); the payment note always directs the student to the accounts office.",
    assumptions: "The student's account is already linked to a session_students row (FR-20) — an unlinked account has no session to look up a fee structure for.",
  }),
];

module.exports = { content };
