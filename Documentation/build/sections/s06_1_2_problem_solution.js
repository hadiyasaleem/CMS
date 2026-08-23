const { Paragraph, TextRun, HeadingLevel } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const content = [
  new Paragraph({ text: "1.2 Problem Solution", heading: HeadingLevel.HEADING_2, spacing: { after: 120 } }),
  para("The solution is a suite of three role-based Android applications — cmsadmin, cmsteacher, and cmsstudent — that all read and write the same Supabase (Postgres) database, so a fact recorded once (a student's attendance for the day, a session's fee structure, a published exam datesheet) is visible identically to everyone who is allowed to see it, and to no one who isn't. Which app you sign into decides what you can do, not which data exists."),
  para("The admin app owns the college's structure: departments, the sessions (intakes) inside each department, each session's curriculum and weekly timetable, its fee structure, and its roster of students. From there, admin also runs the approval queues (student account linking, teacher-submitted mark-edit requests), the teacher lifecycle (disable/ban/reactivate, permission flags), and the notices side of the system — calendar events, datesheets, and an uploadable documents library — plus a tiered Insights dashboard for exam performance and at-risk students."),
  para("The teacher app is where the day-to-day academic work happens: marking attendance, entering midterm/sessional marks (locked after first entry, with a request-based path to correct a mistake), submitting exam papers, and recording semester GPA/CGPA including supply subjects. The student app is a read-mostly window onto all of it — attendance history, marks, semester results, fee challan, datesheets, calendar, and documents — plus the one write path a student has: submitting a link request to claim their own roll number."),
  para("Underneath all three, a shared library module (cmscommon) owns the domain models, the Supabase-backed repositories, an offline Room cache with a background SyncEngine for data that's read often (rosters, timetables, attendance), and the “Modernist” Compose design system so the three apps look and behave as one product rather than three separately-built ones. Postgres Row-Level Security — not application code — is what actually enforces who can read or write which row, which is what lets the same Insights queries return a college-wide view for admin and an automatically-scoped view for a teacher without any role-branching in the app."),
];

module.exports = { content };
