const { Paragraph, TextRun, HeadingLevel, table, AlignmentType } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const content = [
  new Paragraph({ text: "1.6 Related System Analysis / Literature Review", heading: HeadingLevel.HEADING_2, spacing: { after: 120 } }),
  para("A handful of existing systems cover pieces of the same ground. None of them fit a Pakistani public-sector college's actual structure (departments → sessions/intakes → semesters, per-session fees, an accounts-office-based fee-collection process) out of the box."),
  new Paragraph({ children: [new TextRun({ text: "Table 1-1  Related System Analysis with proposed project solution", bold: true, italics: true })], alignment: AlignmentType.CENTER, spacing: { after: 80 } }),
  table(
    ["Application Name", "Weakness", "Proposed Project Solution"],
    [
      [
        "Google Classroom",
        "Built around individual courses, not a college's administrative structure — no fee/challan concept, no institution-wide attendance percentage tracking, no per-session curriculum/timetable model.",
        "Models the college itself (departments, sessions, curriculum, fees) as first-class data, not just course content.",
      ],
      [
        "Generic ERP-style college portals (common in Pakistani colleges)",
        "Usually web-only, built for desktop use at an admin's desk; require a developer to touch the database directly for anything not in the original scope; marks corrections have no audit trail.",
        "Native mobile apps for all three roles; every mark change after first entry goes through an explicit, logged approval request instead of a silent database edit.",
      ],
      [
        "Paper-based systems (the status quo at GGC MBD)",
        "No real-time visibility for students/teachers, no cross-checking of manually compiled GPA/CGPA, notices depend on physically visiting a noticeboard.",
        "Every role sees the same live data instantly; RLS enforces who can see/write what without a physical visit.",
      ],
    ],
    [2400, 3600, 3360],
  ),
];

module.exports = { content };
