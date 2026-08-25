const { Paragraph, HeadingLevel, useCaseTable, tableCaption } = require("../generate_report.js");

const content = [
  new Paragraph({ text: "UC-3  Approve/Reject Mark Edit Request", heading: HeadingLevel.HEADING_2, spacing: { after: 100 } }),
  tableCaption("Table A-3  Use Case Description — Approve/Reject Mark Edit Request"),
  useCaseTable({
    id: "UC-3",
    name: "Approve/Reject Mark Edit Request",
    actors: "Admin",
    description: "Admin reviews a teacher's pending request to change an already-locked score and approves or rejects it.",
    trigger: "Admin opens the Mark Edit Requests queue (People hub).",
    preconditions: "At least one PENDING request exists (created via UC-2's alternative flow).",
    postconditions: "Approve: the requested score is written onto session_marks and the request is marked APPROVED. Reject: the request is marked REJECTED and the score is unchanged.",
    normalFlow: "1. Admin opens Mark Edit Requests. 2. For a listed request, admin reviews the course, roll number, current score, requested score, and reason. 3. Admin taps Approve. 4. The system writes the new score, then marks the request APPROVED, then removes it from the pending queue.",
    alternativeFlows: "3a. Admin taps Reject instead: the request is marked REJECTED and step 4's score write never happens.",
    businessRules: "Approval is two sequential writes (score update, then request status update), not one atomic transaction — both are admin-gated and idempotent if a retry were needed, which is an accepted trade-off for simplicity over strict atomicity (3.7).",
    assumptions: "Only one admin is reviewing the queue at a time; concurrent approval of the same request by two admins is not specifically guarded against.",
  }),
];

module.exports = { content };
