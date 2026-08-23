const { Paragraph, HeadingLevel, frTable, tableCaption } = require("../generate_report.js");

const content = [
  new Paragraph({ text: "2.3.20 FR-20: Approve/Reject Link Request", heading: HeadingLevel.HEADING_3, spacing: { after: 100 } }),
  tableCaption("Table 2-21  Description of FR-20"),
  frTable({
    id: "FR-20",
    title: "Approve/Reject Link Request",
    requirement: "The Admin (or a permitted Teacher) shall see every pending link request, verify the claimed roll number actually exists in that session's roster, and approve or reject it. Approving revokes any account previously linked to that same roll number.",
    source: "Direct requirement — the other half of FR-19.",
    rationale: "Revoking a prior link on approval closes an obvious abuse case: without it, a roll number could end up claimed by two different accounts simultaneously.",
    businessRule: "The match check (does this session+roll exist?) happens server-side at approval time, using the reviewer's own admin/teacher privileges — the requester's account never gets roster read-access itself.",
    dependencies: "FR-19.",
    priority: "Medium",
  }),
];

module.exports = { content };
