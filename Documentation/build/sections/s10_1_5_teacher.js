const { Paragraph, TextRun, HeadingLevel, bullet } = require("../generate_report.js");

const content = [
  new Paragraph({ text: "1.5.2 Client Teacher App (cmsteacher)", heading: HeadingLevel.HEADING_3, spacing: { after: 100 } }),
  bullet("Home — at-a-glance dashboard for the signed-in teacher."),
  bullet("Mark Attendance + History — daily attendance entry per class, with a history view."),
  bullet("Exams hub — Marks Entry (midterm/sessional, live negative/overflow validation, locks after first save), Submit Exam Paper, Semester Results (GPA/CGPA/supply), Datesheets (view published)."),
  bullet("Schedule — the teacher's own weekly timetable."),
  bullet("Menu hub — My Students, Calendar, Documents, Insights (scoped to the teacher's own sessions/courses via RLS), Link Requests (for permitted teachers), Notifications, Profile."),
];

module.exports = { content };
