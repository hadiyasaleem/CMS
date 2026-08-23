const { Paragraph, TextRun, HeadingLevel } = require("../generate_report.js");

function line(label, text) {
  return new Paragraph({
    children: [new TextRun({ text: label + " ", bold: true, italics: true }), new TextRun({ text })],
    spacing: { after: 100 },
  });
}

const content = [
  new Paragraph({ text: "1.7 Vision Statement", heading: HeadingLevel.HEADING_2, spacing: { after: 120 } }),
  line("For", "the administration, faculty, and students of Govt. Graduate College, Mandi Bahauddin,"),
  line("Who", "need accurate, timely, and remotely-reachable academic and administrative records instead of paper-based ones,"),
  line("The", "College Management System (CMS)"),
  line("Is", "a suite of three role-based Android applications backed by a single shared database,"),
  line("That", "lets a student check attendance, marks, results, and fees from their phone, a teacher mark attendance and enter results digitally with a built-in correction workflow, and an admin manage the college's entire academic structure and oversee it through a role-scoped analytics dashboard,"),
  line("Unlike", "the current paper-based process, which requires an in-person visit for almost every interaction and has no audit trail for a changed mark,"),
  line("Our product", "makes every one of those interactions instant, auditable, and reachable from outside the campus, while still working offline for the data a user reads most."),
];

module.exports = { content };
