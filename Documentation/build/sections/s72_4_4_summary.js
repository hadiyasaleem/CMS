const { Paragraph, TextRun, HeadingLevel, PageBreak } = require("../generate_report.js");

const content = [
  new Paragraph({ text: "4.4 Summary", heading: HeadingLevel.HEADING_2, spacing: { after: 120 } }),
  new Paragraph({
    children: [new TextRun({
      text: "This chapter grounded the design in what's actually implemented: the two algorithms worth walking through in pseudocode, the real external SDKs each app depends on, and the state of the Git repository itself. Chapter 5 turns to testing — what's been verified, what's only been compiled, and what's still pending an actual device run.",
    })],
    spacing: { after: 160 },
  }),
  new Paragraph({ children: [new PageBreak()] }),
];

module.exports = { content };
