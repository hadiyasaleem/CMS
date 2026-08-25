const { Paragraph, HeadingLevel, frTable, tableCaption } = require("../generate_report.js");

const content = [
  new Paragraph({ text: "2.3.3 FR-3: Manage Curriculum", heading: HeadingLevel.HEADING_3, spacing: { after: 100 } }),
  tableCaption("Table 2-4  Description of FR-3"),
  frTable({
    id: "FR-3",
    title: "Manage Curriculum",
    requirement: "The Admin shall be able to set the subject list for a given session and semester (course code, name, credit hours), and replace that list wholesale when it needs correcting.",
    source: "College administration, following the program's official scheme of studies.",
    rationale: "Curriculum is stored per-session rather than per-department because two intakes of the same program can legitimately differ — a dropped elective, a renumbered course — and the timetable/marks/attendance screens all need to know which subjects apply to which session's current semester.",
    businessRule: "Saving a semester's subject list deletes the old rows for that session+semester and inserts the new ones, rather than diffing and patching — simpler to reason about, and curriculum changes are infrequent enough that this isn't a performance concern.",
    dependencies: "FR-2 (a session must exist first).",
    priority: "High",
  }),
];

module.exports = { content };
