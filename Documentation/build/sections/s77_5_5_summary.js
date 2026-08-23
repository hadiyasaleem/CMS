const { Paragraph, TextRun, HeadingLevel, PageBreak } = require("../generate_report.js");

const content = [
  new Paragraph({ text: "5.5 Summary", heading: HeadingLevel.HEADING_2, spacing: { after: 120 } }),
  new Paragraph({
    children: [new TextRun({
      text: "Nine test cases are specified across unit, functional, integration, and performance levels, covering the workflows this report leans on most heavily — the marks lock/edit-request/approval loop, link-request approval, and datesheet publishing. None of them have been executed on a real device yet, which is stated plainly rather than papered over with invented results. Chapter 6 covers what deploying this system to GGC MBD actually looks like, including the real challenges already hit during development.",
    })],
    spacing: { after: 160 },
  }),
  new Paragraph({ children: [new PageBreak()] }),
];

module.exports = { content };
