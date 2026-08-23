const { Paragraph, TextRun, HeadingLevel, bullet } = require("../generate_report.js");

const content = [
  new Paragraph({ text: "1.5.3 Client Student App (cmsstudent)", heading: HeadingLevel.HEADING_3, spacing: { after: 100 } }),
  bullet("Home — attendance/CGPA snapshot and quick links."),
  bullet("Attendance — the student's own attendance history."),
  bullet("Exams hub — Marks (subject scores), Results (GPA/CGPA progression with supply), Datesheets (published only)."),
  bullet("Timetable — the student's own weekly schedule."),
  bullet("More hub — Calendar, Documents, Fee Challan, Notifications, Profile."),
  bullet("Link Request — a not-yet-linked account claims its roll number, name, CNIC/B-Form, and DOB for admin review."),
];

module.exports = { content };
