const { Paragraph, HeadingLevel, frTable, tableCaption } = require("../generate_report.js");

const content = [
  new Paragraph({ text: "2.3.10 FR-10: Manage Session Fee Structure", heading: HeadingLevel.HEADING_3, spacing: { after: 100 } }),
  tableCaption("Table 2-11  Description of FR-10"),
  frTable({
    id: "FR-10",
    title: "Manage Session Fee Structure",
    requirement: "The Admin shall be able to set a session's fee cadence (semester or annual), its fee heads (label + amount, e.g. Tuition, Lab), due date, late-fine note, and payment note.",
    source: "College accounts office fee schedule.",
    rationale: "Fees were originally modelled per-department; a project-mid-course correction moved them to per-session, since two intakes of the same department can have different fee schedules (a new intake's tuition can rise year over year).",
    businessRule: "Saving fee heads deletes-then-inserts the whole set for that session, same pattern as curriculum (FR-3) — a small, infrequent write where correctness matters more than avoiding a full rewrite.",
    dependencies: "FR-2.",
    priority: "High",
  }),
];

module.exports = { content };
