const { Paragraph, TextRun, HeadingLevel, table, tableCaption } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const content = [
  new Paragraph({ text: "Financial", heading: HeadingLevel.HEADING_4, spacing: { after: 100 } }),
  para("Fees moved from per-department to per-session partway through the project (3.7 explains why); fines were always per-student."),
  tableCaption("Table 3-3  Data Dictionary — Financial"),
  table(
    ["Entity.Field", "Type", "Description"],
    [
      ["session_fees.session_id", "text (PK)", "One row per session — cadence is ANNUAL or SEMESTER, not per-fee-head."],
      ["session_fees.due_date / late_fine_note / payment_note", "date / text / text", "payment_note defaults to a fixed reminder that fees are payable at the accounts office, not through the app."],
      ["session_fee_heads (session_id, label)", "composite PK", "The actual line items (Tuition, Lab, Library, …); replaced wholesale on save, same delete-then-insert pattern as curriculum."],
      ["fines.id", "uuid (PK)", "category is an enum (LIBRARY / ATTENDANCE / EXAM / DISCIPLINARY / OTHER); amount and reason are required."],
    ],
    [2900, 1600, 4460],
  ),
];

module.exports = { content };
