const { Paragraph, TextRun, HeadingLevel, table, tableCaption } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const content = [
  new Paragraph({ text: "Notices", heading: HeadingLevel.HEADING_4, spacing: { after: 100 } }),
  para("The broadcast/publication side of the system — calendar, exam datesheets, documents, exam papers, and admin notifications. Most rows here have a published/draft flag or an audience/target column, since visibility control is the whole point of this group."),
  tableCaption("Table 3-4  Data Dictionary — Notices"),
  table(
    ["Entity.Field", "Type", "Description"],
    [
      ["calendar_events.audience", "enum", "ADMIN / TEACHER / STUDENT / ALL (FR-14)."],
      ["datesheets.published", "boolean", "Non-managing roles only see published=true rows (FR-15/FR-16)."],
      ["datesheet_slots.datesheet_id", "uuid (FK)", "One datesheet has many slots — exam_date, times, room, invigilator."],
      ["documents.storage_path / body", "text / text", "Either or both may be set — an uploaded file, typed text, or both (FR-17)."],
      ["documents.kind", "enum", "PROSPECTUS / RULES / REPORT / OTHER."],
      ["documents.search", "tsvector", "Full-text search column, generated — not written directly by the app."],
      ["exam_paper_submissions.storage_path", "text", "Points into the exam-papers Storage bucket; purged along with its row when a session's semester completes, to stay within the free-tier quota."],
      ["notifications.target_role / target_dept_id / target_session_id", "enum / text / text", "All nullable — a notification can be broadcast (all null) or scoped to any combination."],
    ],
    [2900, 1600, 4460],
  ),
];

module.exports = { content };
