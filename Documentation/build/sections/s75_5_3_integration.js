const { Paragraph, TextRun, HeadingLevel, testCaseTable, tableCaption } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const content = [
  new Paragraph({ text: "5.3 Integration Testing (IT)", heading: HeadingLevel.HEADING_2, spacing: { after: 120 } }),
  para("Integration tests check that pieces which pass unit and functional tests on their own still behave correctly once wired together — specifically here, that the RLS policies on one table interact correctly with a write that touches two tables in sequence."),
  tableCaption("Table 5-6  Testcase IT-1"),
  testCaseTable({
    id: "IT-1",
    requirementId: "FR-6, FR-7",
    title: "Bulk marks save skips already-locked students without erroring",
    description: "A class has some students with scores already saved and some without; the teacher enters new scores only for the unsaved ones and hits Save.",
    objective: "Confirm the bulk-save call only ever contains new rows, so it never triggers the admin-only UPDATE policy on session_marks for an already-existing row (which would abort the batch — see 3.7).",
    precondition: "A class of at least 3 students, 1 of whom already has a saved score.",
    steps: "1. Open Marks Entry for that class. 2. Enter scores only for the 2 unsaved students. 3. Tap Save.",
    input: "Two new scores; the third student's field is left as its existing locked value.",
    expected: "Save succeeds and only the 2 new rows are written; the already-locked student's score is untouched and the call does not error.",
  }),
  new Paragraph({ spacing: { before: 200 } }),
  tableCaption("Table 5-7  Testcase IT-2"),
  testCaseTable({
    id: "IT-2",
    requirementId: "FR-19, FR-20",
    title: "Approving a link request updates both profiles and session_students",
    description: "A student submits a link request; admin approves it.",
    objective: "Confirm approval's multi-step write (profiles.linked_session_id/linked_roll AND session_students.linked_email) lands consistently, and any previously-linked account on that roll number is revoked.",
    precondition: "A pending link request for a roll number not yet linked to any account.",
    steps: "1. Submit a link request as an unlinked student account. 2. As admin, open Link Requests and approve it. 3. Sign back in as the student.",
    input: "Session ID and roll number matching an existing roster row.",
    expected: "After approval, the student's home screen shows their real roster data (name, attendance, etc.) instead of the unlinked placeholder state.",
  }),
];

module.exports = { content };
