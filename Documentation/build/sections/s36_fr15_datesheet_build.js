const { Paragraph, HeadingLevel, frTable, tableCaption } = require("../generate_report.js");

const content = [
  new Paragraph({ text: "2.3.15 FR-15: Build & Publish Datesheet", heading: HeadingLevel.HEADING_3, spacing: { after: 100 } }),
  tableCaption("Table 2-16  Description of FR-15"),
  frTable({
    id: "FR-15",
    title: "Build & Publish Datesheet",
    requirement: "The Admin shall be able to create a datesheet (title, exam type, instructions), add exam slots to it (date, time, course, room, invigilator), and toggle it between draft and published.",
    source: "Exam scheduling process — replaces the printed datesheet pinned to the noticeboard.",
    rationale: "A draft/published split matters because a datesheet is rarely finished in one sitting — an admin needs to build it over several sessions before it's ready for students to see.",
    businessRule: "Students and non-managing teachers can only read published datesheets (see FR-16); drafts are only visible to admin and teachers with datesheet-management permission.",
    dependencies: "FR-2, FR-3.",
    priority: "Medium",
  }),
];

module.exports = { content };
