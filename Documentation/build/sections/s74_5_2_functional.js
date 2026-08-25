const { Paragraph, TextRun, HeadingLevel, testCaseTable, tableCaption } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const content = [
  new Paragraph({ text: "5.2 Functional Testing (FT)", heading: HeadingLevel.HEADING_2, spacing: { after: 120 } }),
  para("Functional tests exercise a whole feature end to end through the actual UI, checking the system meets the requirement it was built for rather than just that one function returns the right value."),
  tableCaption("Table 5-3  Testcase FT-1"),
  testCaseTable({
    id: "FT-1",
    requirementId: "FR-6",
    title: "Teacher enters a new midterm score",
    description: "A teacher opens Marks Entry, picks a class, and enters a score for a student who has none yet.",
    objective: "Confirm a valid new entry saves and immediately locks (becomes read-only with an edit-request pencil).",
    precondition: "Signed in as a teacher assigned to at least one session/course; that student has no midterm score yet.",
    steps: "1. Open Marks Entry. 2. Select a class and MIDTERM. 3. Type 20 into a student's score field. 4. Tap Save.",
    input: "Score = 20 for a student with no prior midterm score.",
    expected: "Save succeeds; the field becomes a read-only \"20\" with an edit-pencil icon next to it.",
  }),
  new Paragraph({ spacing: { before: 200 } }),
  tableCaption("Table 5-4  Testcase FT-2"),
  testCaseTable({
    id: "FT-2",
    requirementId: "FR-7, FR-8",
    title: "Teacher requests a mark change and admin approves it",
    description: "End-to-end run of the lock → request → approve workflow across the teacher and admin apps.",
    objective: "Confirm the requested score only lands on the student's record after admin approval, not before.",
    precondition: "A student already has a locked midterm score of 20.",
    steps: "1. In the teacher app, tap the edit-pencil next to the locked score. 2. Enter 22 as the new score with a reason. 3. Submit. 4. In the admin app, open Mark Edit Requests. 5. Approve the request.",
    input: "Requested score = 22, reason = \"transcription error\".",
    expected: "After step 3, the teacher app shows \"Pending review\" for that student and the score is still 20. After step 5, the student's score is 22 and the request disappears from the admin queue.",
  }),
  new Paragraph({ spacing: { before: 200 } }),
  tableCaption("Table 5-5  Testcase FT-3"),
  testCaseTable({
    id: "FT-3",
    requirementId: "FR-15, FR-16",
    title: "Admin publishes a datesheet and a student sees it",
    description: "Admin builds a datesheet with one exam slot and publishes it; a student in that session opens Datesheets.",
    objective: "Confirm a draft datesheet is invisible to students, and becomes visible only once published.",
    precondition: "A datesheet exists in draft state with at least one slot.",
    steps: "1. As the student, open Datesheets — confirm the draft is not listed. 2. As admin, toggle the datesheet to Published. 3. As the student, refresh Datesheets.",
    input: "One datesheet, one slot (course, date, time, room).",
    expected: "Step 1 shows no datesheet. Step 3 shows the datesheet with its slot visible after expanding it.",
  }),
];

module.exports = { content };
