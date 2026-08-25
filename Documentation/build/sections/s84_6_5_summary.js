const { Paragraph, TextRun, HeadingLevel, PageBreak } = require("../generate_report.js");

const content = [
  new Paragraph({ text: "6.5 Summary", heading: HeadingLevel.HEADING_2, spacing: { after: 120 } }),
  new Paragraph({
    children: [new TextRun({
      text: "Deployment here is a phased, per-department rollout rather than a cutover from an existing system, since the thing being replaced is paper rather than software. The real value of this chapter is the Challenges section — three concrete bugs that changed the design (a serialization/RLS interaction, a bulk-write assumption that broke under tightened security, and a layout bug only a real user surfaced) — recorded as they actually happened rather than smoothed into a success story. Chapter 7 closes the report: whether the objectives from Chapter 1 were actually met, and what's left to build.",
    })],
    spacing: { after: 160 },
  }),
  new Paragraph({ children: [new PageBreak()] }),
];

module.exports = { content };
