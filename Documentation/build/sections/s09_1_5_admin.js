const { Paragraph, TextRun, HeadingLevel, bullet } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const content = [
  new Paragraph({ text: "1.5 System Components", heading: HeadingLevel.HEADING_2, spacing: { after: 120 } }),
  para("The system is organised as three client apps sharing one backend and one common library module. Each app's modules are listed below, grouped app by app."),
  new Paragraph({ text: "1.5.1 Client Admin App (cmsadmin)", heading: HeadingLevel.HEADING_3, spacing: { after: 100 } }),
  bullet("Departments — create/list departments; each owns many sessions."),
  bullet("Sessions — create/delete a session (intake) under a department; promote its current semester."),
  bullet("Curriculum — per-session, per-semester subject list."),
  bullet("Master Timetable & Session Timetable — weekly slot grid (subject/teacher/room) with double-booking prevention."),
  bullet("Session Fee Structure — per-session fee heads, cadence, due date, payment note."),
  bullet("Teachers — roster, account creation, permission flags, status lifecycle (disable/ban/reactivate)."),
  bullet("Link Requests — approve/reject a student's claim to a roll number."),
  bullet("Attendance Records — dept → session → semester report + PDF/CSV export."),
  bullet("Calendar — create/delete college-wide events (holidays, exams, deadlines) with audience targeting."),
  bullet("Datesheets — build an exam grid (slots) and publish it."),
  bullet("Documents — upload a PDF/DOCX or type body text (prospectus/rules/report/other) and publish it."),
  bullet("Mark Edit Requests — review queue to approve/reject a teacher's request to change an already-locked score."),
  bullet("Insights — college-wide session overview, at-risk students, exam-score statistics."),
  bullet("Notifications — send/manage notices to teachers and/or students."),
];

module.exports = { content };
