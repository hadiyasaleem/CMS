const {
  Document, Packer, Paragraph, TextRun, HeadingLevel, Table, TableRow, TableCell,
  WidthType, AlignmentType, PageBreak, BorderStyle, ShadingType, VerticalAlign, fs,
  h1, h2, h3, p, pRuns, italic, bold, placeholder, bullet, chapterBreakPage,
  cell, table, frTable,
} = require("./generate_report.js");

// =========================================================================
// FRONT MATTER
// =========================================================================

const titlePage = [
  new Paragraph({ text: "Final Year Design Project", alignment: AlignmentType.CENTER, spacing: { before: 800, after: 400 } }),
  new Paragraph({
    children: [new TextRun({ text: "College Management System", bold: true, size: 32 })],
    alignment: AlignmentType.CENTER, spacing: { after: 200 },
  }),
  new Paragraph({
    children: [new TextRun({ text: "for Govt. Graduate College, Mandi Bahauddin", italics: true, size: 26 })],
    alignment: AlignmentType.CENTER, spacing: { after: 800 },
  }),
  new Paragraph({ children: [new TextRun({ text: "By", bold: true })], alignment: AlignmentType.CENTER, spacing: { after: 300 } }),
  new Paragraph({ children: [new TextRun({ text: "Hadia" })], alignment: AlignmentType.CENTER, spacing: { after: 60 } }),
  new Paragraph({ children: [new TextRun({ text: "085668" })], alignment: AlignmentType.CENTER, spacing: { after: 200 } }),
  new Paragraph({ children: [new TextRun({ text: "Sharfa Kiran" })], alignment: AlignmentType.CENTER, spacing: { after: 60 } }),
  new Paragraph({ children: [new TextRun({ text: "085646" })], alignment: AlignmentType.CENTER, spacing: { after: 200 } }),
  new Paragraph({ children: [new TextRun({ text: "Syeda Laraib Qamar Kazmi" })], alignment: AlignmentType.CENTER, spacing: { after: 60 } }),
  new Paragraph({ children: [new TextRun({ text: "085713" })], alignment: AlignmentType.CENTER, spacing: { after: 600 } }),
  new Paragraph({ children: [new TextRun({ text: "Under the supervision of", italics: true })], alignment: AlignmentType.CENTER, spacing: { after: 100 } }),
  new Paragraph({ children: [new TextRun({ text: "Prof. Ubaid Ullah", bold: true })], alignment: AlignmentType.CENTER, spacing: { after: 600 } }),
  new Paragraph({
    children: [new TextRun({ text: "Bachelor of Science in Information Technology (2022-2026)", bold: true, italics: true })],
    alignment: AlignmentType.CENTER, spacing: { after: 300 },
  }),
  new Paragraph({ children: [new TextRun({ text: "DEPARTMENT OF INFORMATION TECHNOLOGY", bold: true })], alignment: AlignmentType.CENTER, spacing: { after: 100 } }),
  new Paragraph({ children: [new TextRun({ text: "GOVT. GRADUATE COLLEGE, MANDI BAHAUDDIN", bold: true })], alignment: AlignmentType.CENTER }),
  new Paragraph({ children: [new PageBreak()] }),
];

const innerTitlePage = [
  new Paragraph({
    children: [new TextRun({ text: "College Management System for Govt. Graduate College, Mandi Bahauddin", bold: true })],
    alignment: AlignmentType.CENTER, spacing: { after: 400 },
  }),
  new Paragraph({ children: [new TextRun({ text: "A project presented to" })], alignment: AlignmentType.CENTER, spacing: { after: 60 } }),
  new Paragraph({ children: [new TextRun({ text: "Govt. Graduate College, Mandi Bahauddin" })], alignment: AlignmentType.CENTER, spacing: { after: 400 } }),
  new Paragraph({ children: [new TextRun({ text: "In partial fulfilment" })], alignment: AlignmentType.CENTER, spacing: { after: 60 } }),
  new Paragraph({ children: [new TextRun({ text: "of the requirement for the degree of" })], alignment: AlignmentType.CENTER, spacing: { after: 400 } }),
  new Paragraph({
    children: [new TextRun({ text: "Bachelor of Science in Information Technology (2022-2026)", bold: true, italics: true })],
    alignment: AlignmentType.CENTER, spacing: { after: 400 },
  }),
  new Paragraph({ children: [new TextRun({ text: "By" })], alignment: AlignmentType.CENTER, spacing: { after: 200 } }),
  new Paragraph({ children: [new TextRun({ text: "Hadia" })], alignment: AlignmentType.CENTER, spacing: { after: 60 } }),
  new Paragraph({ children: [new TextRun({ text: "085668" })], alignment: AlignmentType.CENTER, spacing: { after: 200 } }),
  new Paragraph({ children: [new TextRun({ text: "Sharfa Kiran" })], alignment: AlignmentType.CENTER, spacing: { after: 60 } }),
  new Paragraph({ children: [new TextRun({ text: "085646" })], alignment: AlignmentType.CENTER, spacing: { after: 200 } }),
  new Paragraph({ children: [new TextRun({ text: "Syeda Laraib Qamar Kazmi" })], alignment: AlignmentType.CENTER, spacing: { after: 60 } }),
  new Paragraph({ children: [new TextRun({ text: "085713" })], alignment: AlignmentType.CENTER, spacing: { after: 600 } }),
  new Paragraph({ children: [new TextRun({ text: "DEPARTMENT OF INFORMATION TECHNOLOGY", bold: true })], alignment: AlignmentType.CENTER, spacing: { after: 100 } }),
  new Paragraph({ children: [new TextRun({ text: "GOVT. GRADUATE COLLEGE, MANDI BAHAUDDIN", bold: true })], alignment: AlignmentType.CENTER }),
  new Paragraph({ children: [new PageBreak()] }),
];

const declarationPage = [
  new Paragraph({ text: "DECLARATION", heading: HeadingLevel.HEADING_1, alignment: AlignmentType.CENTER, spacing: { after: 400 } }),
  p("We hereby declare that this software, neither whole nor as a part, has been copied out from any source. It is further declared that we have developed this software and accompanied report entirely on the basis of our personal efforts. If any part of this project is proved to be copied out from any source or found to be reproduction of some other, we will stand by the consequences. No portion of the work presented has been submitted as part of any application for any other degree or qualification of this or any other university or institute of learning."),
  new Paragraph({ spacing: { before: 600, after: 200 } }),
  new Paragraph({
    tabStops: [{ type: "right", position: 8000 }],
    children: [
      new TextRun({ text: "Signature: ___________________" }),
      new TextRun({ text: "\tSignature: ___________________" }),
    ],
  }),
  new Paragraph({
    tabStops: [{ type: "right", position: 8000 }],
    spacing: { after: 400 },
    children: [
      new TextRun({ text: "Hadia [085668]" }),
      new TextRun({ text: "\tSharfa Kiran [085646]" }),
    ],
  }),
  new Paragraph({ children: [new TextRun({ text: "Signature: ___________________" })] }),
  new Paragraph({ children: [new TextRun({ text: "Syeda Laraib Qamar Kazmi [085713]" })] }),
  new Paragraph({ children: [new PageBreak()] }),
];

const certificatePage = [
  new Paragraph({ text: "CERTIFICATE OF APPROVAL", heading: HeadingLevel.HEADING_1, alignment: AlignmentType.CENTER, spacing: { after: 400 } }),
  p("It is to certify that the final year design project (FYDP) of BSIT, session (2022-2026), titled “College Management System for Govt. Graduate College, Mandi Bahauddin” was developed by Hadia (085668), Sharfa Kiran (085646), and Syeda Laraib Qamar Kazmi (085713) under the supervision of Prof. Ubaid Ullah; in my opinion, it is fully adequate, in scope and quality, for the degree of Bachelor of Science in Information Technology."),
  new Paragraph({ spacing: { before: 400 }, children: [new TextRun({ text: "Signature: ___________________________________" })] }),
  new Paragraph({ children: [new TextRun({ text: "FYDP Advisor: Prof. Ubaid Ullah", bold: true })], spacing: { after: 400 } }),
  new Paragraph({ children: [new TextRun({ text: "Signatures (Faculty Advisory Committee – FAC)", bold: true })], spacing: { after: 100 } }),
  table(
    ["", "FAC1", "FAC2"],
    [["Name", "Prof. Muhammad Faiyaz", "Prof. Ubaid Ullah"], ["Signature", "", ""]],
    [1600, 3880, 3880],
  ),
  new Paragraph({ spacing: { before: 400 }, children: [new TextRun({ text: "Signature: ___________________________________" })] }),
  new Paragraph({ children: [new TextRun({ text: "Head of FYDP Coordination Office: Prof. Muhammad Faiyaz", bold: true })], spacing: { after: 400 } }),
  new Paragraph({
    tabStops: [{ type: "right", position: 8000 }],
    children: [
      new TextRun({ text: "Signature: ___________________________________" }),
      new TextRun({ text: "\tDated: ______________" }),
    ],
  }),
  new Paragraph({ children: [new TextRun({ text: "Head of Department, Information Technology: Prof. Muhammad Faiyaz", bold: true })] }),
  new Paragraph({ children: [new PageBreak()] }),
];

const executiveSummary = [
  h1("Executive Summary"),
  p("Govt. Graduate College, Mandi Bahauddin runs its academic life on paper. Roughly 3,000 students across Intermediate and BS programmes, and about 50 faculty members, still queue up at the accounts window for a fee challan, wait for a hand-written attendance register to be tallied, and find out their semester result from a notice board rather than a phone. Every one of these steps is a place where a form goes missing, a number gets mistyped, or a student simply doesn't find out in time. That was the starting problem: a college of this size cannot keep running its records by hand without the errors and delays showing up somewhere — a wrong CGPA on a transcript, a fee challan that doesn't match what the accounts office actually has on file, an exam schedule that reaches half the class."),
  p("Our answer is a suite of three Android applications — one for admins, one for teachers, one for students — backed by a single shared database and a common design system, so the same piece of data (a student's attendance, a session's fee structure, a published datesheet) is always the same regardless of which app is reading it. The admin app is where the college's actual structure lives: departments, the sessions (intakes) within them, each session's curriculum and timetable, and its fee structure. Teachers mark attendance, enter midterm and sessional marks, submit exam papers, and record semester GPA/CGPA against that same structure. Students see their own slice of it — attendance, marks, fee challan, datesheets, calendar notices — without ever walking into an office."),
  p("Midway through the project we made a deliberate architectural change: the backend moved from Firebase (Firestore + Auth) to Supabase (Postgres, with Row-Level Security enforcing who can see and write what, GoTrue for authentication, and Storage for uploaded files like exam papers and documents), and the UI layer moved from XML Activities/Fragments to Jetpack Compose with a proper MVVM/Repository split, wired together with Hilt. The reasoning was straightforward: a relational schema with real foreign keys and row-level policies gives us guarantees a document store couldn't — a mark can't be attributed to a student who was never enrolled in that session, a fee record can't outlive the session it belongs to — and Compose let us build one shared component library (the “Modernist” red/ink design system) that all three apps use identically instead of maintaining three sets of XML layouts."),
  p("Every major workflow in this document reflects that rebuilt system as it exists in the codebase today, not the original Firebase/Java prototype: department-centric academic structure, per-session fees, a lock-then-request-approval workflow for marks (a teacher enters a score once; changing it after the fact requires the admin's sign-off), semester result recording with GPA/CGPA and supply-subject tracking, fines, a calendar, datesheets, an uploadable-documents library, and a tiered Insights dashboard built directly on Postgres views so that what an admin sees college-wide, a teacher sees scoped to their own classes, purely through the database's own access rules — no extra application logic required. All three apps build and run against the live Supabase project."),
  new Paragraph({ children: [new TextRun({ text: "Keywords: ", bold: true }), new TextRun({ text: "College Management System, Row-Level Security, Jetpack Compose, Supabase, Android" })], spacing: { after: 160 } }),
  new Paragraph({ children: [new PageBreak()] }),
];

const acknowledgement = [
  h1("Acknowledgement"),
  placeholder("Personal acknowledgement — thank your advisor Prof. Ubaid Ullah, the FYP Coordination Office, family, and anyone else who supported the project. Write this yourself; it should be in your own words."),
  new Paragraph({ spacing: { before: 600 } }),
  new Paragraph({
    tabStops: [{ type: "right", position: 8000 }],
    children: [
      new TextRun({ text: "Signature: ___________________" }),
      new TextRun({ text: "\tSignature: ___________________" }),
    ],
  }),
  new Paragraph({
    tabStops: [{ type: "right", position: 8000 }],
    spacing: { after: 400 },
    children: [
      new TextRun({ text: "Hadia [085668]" }),
      new TextRun({ text: "\tSharfa Kiran [085646]" }),
    ],
  }),
  new Paragraph({ children: [new TextRun({ text: "Signature: ___________________" })] }),
  new Paragraph({ children: [new TextRun({ text: "Syeda Laraib Qamar Kazmi [085713]" })] }),
  new Paragraph({ children: [new PageBreak()] }),
];

const abbreviations = [
  h1("Abbreviations"),
  p("Abbreviations and acronyms used throughout this report, ordered alphabetically."),
  table(
    ["Abbreviation", "Description"],
    [
      ["API", "Application Programming Interface"],
      ["BS", "Bachelor of Science"],
      ["BSIT", "Bachelor of Science in Information Technology"],
      ["CGPA", "Cumulative Grade Point Average"],
      ["CMS", "College Management System"],
      ["CRUD", "Create, Read, Update, Delete"],
      ["DI", "Dependency Injection (Hilt)"],
      ["DTO", "Data Transfer Object"],
      ["FAC", "Faculty Advisory Committee"],
      ["FCIT", "Faculty of Computing and Information Technology"],
      ["FR", "Functional Requirement"],
      ["FYDP", "Final Year Design Project"],
      ["GGC MBD", "Govt. Graduate College, Mandi Bahauddin"],
      ["GPA", "Grade Point Average"],
      ["JWT", "JSON Web Token"],
      ["MVVM", "Model-View-ViewModel"],
      ["NFR", "Non-Functional Requirement"],
      ["PU", "University of the Punjab"],
      ["RLS", "Row-Level Security (Postgres)"],
      ["RPC", "Remote Procedure Call (a Postgres function invoked from the app)"],
      ["SDK", "Software Development Kit"],
      ["SRS", "Software Requirements Specification"],
      ["UI", "User Interface"],
      ["UUID", "Universally Unique Identifier"],
    ],
    [3000, 6360],
  ),
  new Paragraph({ children: [new PageBreak()] }),
];

module.exports = {
  titlePage, innerTitlePage, declarationPage, certificatePage,
  executiveSummary, acknowledgement, abbreviations,
};
