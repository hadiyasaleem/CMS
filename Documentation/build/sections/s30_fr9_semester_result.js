const { Paragraph, HeadingLevel, frTable, tableCaption } = require("../generate_report.js");

const content = [
  new Paragraph({ text: "2.3.9 FR-9: Record Semester Result", heading: HeadingLevel.HEADING_3, spacing: { after: 100 } }),
  tableCaption("Table 2-10  Description of FR-9"),
  frTable({
    id: "FR-9",
    title: "Record Semester Result",
    requirement: "A Teacher shall be able to record a student's GPA, CGPA, result status (promoted/repeated/probation/pending), class position, remarks, and any supply (retake) subjects for a completed semester.",
    source: "End-of-semester result compilation, currently done by hand.",
    rationale: "This is where the paper process's biggest error risk lived — a hand-compiled CGPA with no independent check. Recording it through one RPC call means the CGPA snapshot on the student's record updates atomically with the semester row.",
    businessRule: "The snapshot only updates when the semester being recorded is the highest one seen so far for that student — so if an 8th-semester student's result gets entered before catching up an earlier gap, the snapshot doesn't jump ahead incorrectly.",
    dependencies: "FR-2, FR-6 (marks feed into the GPA calculation, though the calculation itself happens outside the app).",
    priority: "High",
  }),
];

module.exports = { content };
