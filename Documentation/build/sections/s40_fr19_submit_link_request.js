const { Paragraph, HeadingLevel, frTable, tableCaption } = require("../generate_report.js");

const content = [
  new Paragraph({ text: "2.3.19 FR-19: Submit Student Link Request", heading: HeadingLevel.HEADING_3, spacing: { after: 100 } }),
  tableCaption("Table 2-20  Description of FR-19"),
  frTable({
    id: "FR-19",
    title: "Submit Student Link Request",
    requirement: "A newly-registered account with no linked student record shall be able to submit a claim — session, roll number, name, CNIC/B-Form, date of birth — asking to be linked to that roll number.",
    source: "Onboarding — a student's Supabase auth account and their session_students roster row don't exist as the same thing until this link happens.",
    rationale: "An unlinked account can't read the roster to check whether its claimed roll number is even real, so the claim itself has to be a blind submission that admin verifies server-side.",
    businessRule: "The (session, roll) pair is verified against the actual roster only at approval time (FR-20), not at submission time, since RLS prevents an unlinked account from querying the roster directly.",
    dependencies: "FR-2 (the session and roll have to already exist in the roster for the claim to succeed).",
    priority: "Medium",
  }),
];

module.exports = { content };
