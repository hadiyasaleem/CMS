const { Paragraph, HeadingLevel, frTable, tableCaption } = require("../generate_report.js");

const content = [
  new Paragraph({ text: "2.3.7 FR-7: Request Mark Edit", heading: HeadingLevel.HEADING_3, spacing: { after: 100 } }),
  tableCaption("Table 2-8  Description of FR-7"),
  frTable({
    id: "FR-7",
    title: "Request Mark Edit",
    requirement: "For a student whose score is already saved (locked), a Teacher shall be able to submit a request with the proposed new score and an optional reason, instead of editing the score directly.",
    source: "Direct requirement from the college — a mark, once submitted, shouldn't be silently changeable by the same person who entered it.",
    rationale: "This is the audit trail the paper process never had: every correction after the fact is now a named, timestamped record instead of an invisible edit.",
    businessRule: "The database enforces this, not just the UI — the session_marks UPDATE policy is admin-only, so even a teacher who bypassed the app couldn't directly overwrite a saved score.",
    dependencies: "FR-6 (there has to be a saved score to request a change to).",
    priority: "Medium",
  }),
];

module.exports = { content };
