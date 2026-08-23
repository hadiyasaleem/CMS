const { Paragraph, HeadingLevel, frTable, tableCaption } = require("../generate_report.js");

const content = [
  new Paragraph({ text: "2.3.21 FR-21: Manage Teacher Status & Permissions", heading: HeadingLevel.HEADING_3, spacing: { after: 100 } }),
  tableCaption("Table 2-22  Description of FR-21"),
  frTable({
    id: "FR-21",
    title: "Manage Teacher Status & Permissions",
    requirement: "The Admin shall be able to disable, ban, or reactivate a teacher's account, and toggle their permission flags (approve link requests, edit timetable, send notifications, manage datesheets).",
    source: "College HR/administrative process for faculty account lifecycle.",
    rationale: "Disabling or banning immediately revokes that teacher's ability to sign in — it bans the underlying auth account, not just a flag the app checks — so access actually stops the moment it's toggled, not the next time some check happens to run.",
    businessRule: "A disabled or banned teacher still appears in the roster (so admin can reactivate them later); only an outright delete removes the row.",
    dependencies: "None.",
    priority: "Medium",
  }),
];

module.exports = { content };
