const { Paragraph, HeadingLevel, frTable, tableCaption } = require("../generate_report.js");

const content = [
  new Paragraph({ text: "2.3.8 FR-8: Approve/Reject Mark Edit Request", heading: HeadingLevel.HEADING_3, spacing: { after: 100 } }),
  tableCaption("Table 2-9  Description of FR-8"),
  frTable({
    id: "FR-8",
    title: "Approve/Reject Mark Edit Request",
    requirement: "The Admin shall see every pending mark-edit request in one queue, with the current score, the requested score, and the teacher's reason, and shall be able to approve or reject each one. Approving writes the new score onto the student's record and closes the request; rejecting just closes it.",
    source: "Direct requirement — the other half of FR-7.",
    rationale: "Centralising this in one queue means an admin doesn't have to go hunting through individual classes to find what's pending.",
    businessRule: "Approval is two sequential writes (update the score, then mark the request approved), not a single atomic transaction — acceptable here because both writes are admin-gated and idempotent if one had to be retried.",
    dependencies: "FR-7.",
    priority: "Medium",
  }),
];

module.exports = { content };
