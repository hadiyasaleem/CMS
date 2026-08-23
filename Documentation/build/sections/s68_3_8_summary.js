const { Paragraph, TextRun, HeadingLevel, PageBreak } = require("../generate_report.js");

const content = [
  new Paragraph({ text: "3.8 Summary", heading: HeadingLevel.HEADING_2, spacing: { after: 120 } }),
  new Paragraph({
    children: [new TextRun({
      text: "This chapter covered how the requirements from Chapter 2 became an actual system: a four-layer architecture with Postgres RLS doing the access-control work rather than the app, a data model documented in full in the data dictionary, four sequence diagrams walking through the flows with the most moving parts, and a candid account of the design decisions that changed mid-project — the fee-structure rework and the DTO-default bug chief among them. Chapter 4 moves from design to the actual implementation: the algorithms, the third-party SDKs, and the state of the code repository itself.",
    })],
    spacing: { after: 160 },
  }),
  new Paragraph({ children: [new PageBreak()] }),
];

module.exports = { content };
