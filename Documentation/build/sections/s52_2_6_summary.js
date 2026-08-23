const { Paragraph, TextRun, HeadingLevel, PageBreak } = require("../generate_report.js");

const content = [
  new Paragraph({ text: "2.6 Summary", heading: HeadingLevel.HEADING_2, spacing: { after: 120 } }),
  new Paragraph({
    children: [new TextRun({
      text: "This chapter turned Chapter 1's goals into something checkable: three user classes, use-case-driven requirement gathering rooted in the college's actual paper process, twenty-two functional requirements spanning academic structure through governance and analytics, and the non-functional and interface requirements that constrain how all of it has to behave. Chapter 3 now covers how these requirements were turned into an actual system design — the data model, the architecture, and the decisions made along the way.",
    })],
    spacing: { after: 160 },
  }),
  new Paragraph({ children: [new PageBreak()] }),
];

module.exports = { content };
