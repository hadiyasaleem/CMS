const { Paragraph, TextRun, HeadingLevel, PageBreak } = require("../generate_report.js");

const content = [
  new Paragraph({ text: "1.12 Summary", heading: HeadingLevel.HEADING_2, spacing: { after: 120 } }),
  new Paragraph({
    children: [new TextRun({
      text: "This chapter set out the actual problem — a college running entirely on paper records — and the three-app, Supabase-backed solution built to address it, along with the concrete objectives, scope, module breakdown, and technology choices behind that solution. The remaining chapters go into that solution in depth: Chapter 2 specifies its requirements formally, Chapter 3 covers its design and data model, Chapter 4 its implementation, Chapter 5 its testing, Chapter 6 its deployment, and Chapter 7 closes by checking the finished system against the objectives set out here.",
    })],
    spacing: { after: 160 },
  }),
  new Paragraph({ children: [new PageBreak()] }),
];

module.exports = { content };
