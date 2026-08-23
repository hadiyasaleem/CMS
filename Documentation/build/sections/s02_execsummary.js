const { Paragraph, TextRun, HeadingLevel, PageBreak } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const content = [
  new Paragraph({ text: "Executive Summary", heading: HeadingLevel.HEADING_1, spacing: { after: 200 } }),
  para("Govt. Graduate College, Mandi Bahauddin runs its academic life on paper. Roughly 3,000 students across Intermediate and BS programmes, and about 50 faculty members, still queue up at the accounts window for a fee challan, wait for a hand-written attendance register to be tallied, and find out their semester result from a notice board rather than a phone. Every one of these steps is a place where a form goes missing, a number gets mistyped, or a student simply doesn't find out in time. That was the starting problem: a college of this size cannot keep running its records by hand without the errors and delays showing up somewhere — a wrong CGPA on a transcript, a fee challan that doesn't match what the accounts office actually has on file, an exam schedule that reaches half the class."),
  para("Our answer is a suite of three Android applications — one for admins, one for teachers, one for students — backed by a single shared database and a common design system, so the same piece of data (a student's attendance, a session's fee structure, a published datesheet) is always the same regardless of which app is reading it. The admin app is where the college's actual structure lives: departments, the sessions (intakes) within them, each session's curriculum and timetable, and its fee structure. Teachers mark attendance, enter midterm and sessional marks, submit exam papers, and record semester GPA/CGPA against that same structure. Students see their own slice of it — attendance, marks, fee challan, datesheets, calendar notices — without ever walking into an office."),
  para("Midway through the project we made a deliberate architectural change: the backend moved from Firebase (Firestore + Auth) to Supabase (Postgres, with Row-Level Security enforcing who can see and write what, GoTrue for authentication, and Storage for uploaded files like exam papers and documents), and the UI layer moved from XML Activities/Fragments to Jetpack Compose with a proper MVVM/Repository split, wired together with Hilt. The reasoning was straightforward: a relational schema with real foreign keys and row-level policies gives us guarantees a document store couldn't — a mark can't be attributed to a student who was never enrolled in that session, a fee record can't outlive the session it belongs to — and Compose let us build one shared component library (the “Modernist” red/ink design system) that all three apps use identically instead of maintaining three separate sets of XML layouts."),
  para("Every major workflow in this report reflects that rebuilt system as it exists in the codebase today, not the original Firebase/Java prototype: department-centric academic structure, per-session fees, a lock-then-request-approval workflow for marks (a teacher enters a score once; changing it after the fact requires the admin's sign-off), semester result recording with GPA/CGPA and supply-subject tracking, fines, a calendar, datesheets, an uploadable-documents library, and a tiered Insights dashboard built directly on Postgres views so that what an admin sees college-wide, a teacher sees scoped to their own classes, purely through the database's own access rules — no extra application logic required. All three apps build and run against the live Supabase project."),
  new Paragraph({
    children: [new TextRun({ text: "Keywords: ", bold: true }), new TextRun({ text: "College Management System, Row-Level Security, Jetpack Compose, Supabase, Android" })],
    spacing: { after: 160 },
  }),
  new Paragraph({ children: [new PageBreak()] }),
];

module.exports = { content };
