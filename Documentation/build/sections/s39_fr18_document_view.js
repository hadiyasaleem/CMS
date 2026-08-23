const { Paragraph, HeadingLevel, frTable, tableCaption } = require("../generate_report.js");

const content = [
  new Paragraph({ text: "2.3.18 FR-18: View/Download Document", heading: HeadingLevel.HEADING_3, spacing: { after: 100 } }),
  tableCaption("Table 2-19  Description of FR-18"),
  frTable({
    id: "FR-18",
    title: "View/Download Document",
    requirement: "A Teacher or Student shall be able to browse published documents, read inline body text, and download an attached file to open it locally.",
    source: "Direct requirement — the read side of FR-17.",
    rationale: "None beyond the obvious.",
    businessRule: "Downloaded files are opened through the device's own file-provider/viewer rather than an in-app viewer, since supporting every document format in-house wasn't worth building.",
    dependencies: "FR-17.",
    priority: "Low",
  }),
];

module.exports = { content };
