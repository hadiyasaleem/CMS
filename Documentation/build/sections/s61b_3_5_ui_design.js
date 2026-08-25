const { Paragraph, TextRun, HeadingLevel, placeholder } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const content = [
  new Paragraph({ text: "3.5 User Interface Design", heading: HeadingLevel.HEADING_2, spacing: { after: 120 } }),
  para("From the user's side, every screen in all three apps follows the same shape: a header (eyebrow label, title, a short accent rule), then either a form, a list of LedgerCards, or a HubNavCard grid, and — where relevant — a floating action button for the one \"create new\" action that screen supports. A destructive action (deleting a session, banning a teacher, rejecting a request) always routes through a confirmation dialog that names what else is affected, never a bare \"Are you sure?\"."),
  new Paragraph({ text: "3.5.1 Screen Images", heading: HeadingLevel.HEADING_3, spacing: { after: 100 } }),
  placeholder("Screenshots skipped for this draft — no Android emulator/device was available in the environment this report was generated in. Before submission, capture and insert screenshots of: Admin (Departments, Session Detail, Mark Edit Requests queue), Teacher (Marks Entry showing a locked score + edit-pencil, Semester Results), and Student (Home, Results, Fee Challan) — roughly 6-8 screens covering one from each major module."),
  new Paragraph({ text: "3.5.2 Screen Objects and Actions", heading: HeadingLevel.HEADING_3, spacing: { after: 100 } }),
  para("Marks Entry (teacher): each roster row shows ROLL · STUDENT NAME · SCORE. For a not-yet-saved student, SCORE is an editable numeric field with live validation (a red caption appears the instant the value is negative or exceeds the exam's maximum). Once saved, SCORE becomes a static label plus an edit-pencil icon button; tapping it opens the Request Mark Edit dialog (current score, a new-score field with the same live validation, an optional reason, Send Request). A \"Pending review\" caption replaces the pencil while a request is outstanding, and the pencil is disabled so a second request can't be filed on top of the first."),
  para("Mark Edit Requests (admin): each queue card shows the course/exam/roll, current score → requested score, the teacher's reason, and two actions — Approve and Reject — with no intermediate confirmation step, since the action itself is already the confirmation (the request disappears from the queue once acted on)."),
];

module.exports = { content };
