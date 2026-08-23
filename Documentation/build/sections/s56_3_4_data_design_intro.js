const { Paragraph, TextRun, HeadingLevel } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const content = [
  new Paragraph({ text: "3.4 Data Design", heading: HeadingLevel.HEADING_2, spacing: { after: 120 } }),
  para("The domain here maps almost one-to-one onto Postgres tables — a department, a session, a student, a mark, a fee head are each one row somewhere, and the interesting structure is in the foreign keys and RLS policies between them, not in an object hierarchy. The data dictionary below is grouped by the same five domains used everywhere else in this report (identity, academic structure, financial, notices, analytics), pulled directly from the live schema rather than reconstructed from memory, so it reflects exactly what's deployed."),
  para("A few conventions run through the whole schema. Every foreign-key-style reference uses the natural key, not a surrogate integer — a session's ID is literally {deptId}_{startYear}_{shift} (e.g. it_2021_MORNING), and a student's ID within a session is {sessionId}_{rollNumber} — because those keys are genuinely stable and human-readable, and it means an admin looking directly at the database can identify a row without a join. Timestamps are timestamptz throughout, defaulting to now() at the database level rather than being set from the client, so a clock skew on someone's phone can't misdate a record."),
];

module.exports = { content };
