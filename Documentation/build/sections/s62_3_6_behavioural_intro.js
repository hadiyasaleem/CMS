const { Paragraph, TextRun, HeadingLevel } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const content = [
  new Paragraph({ text: "3.6 Behavioural Model", heading: HeadingLevel.HEADING_2, spacing: { after: 120 } }),
  para("Four flows are walked through as sequence diagrams below — chosen because each one crosses at least three of the four architectural layers (3.3) and each one has a non-obvious step that a class diagram alone wouldn't surface: login resolves a role before the app can show anything; the marks flow is the whole point of the mark-lock design (2.3.6-2.3.8); attendance marking is the highest-frequency write in the system; and document publish/download is the one flow that touches Storage rather than just Postgrest."),
];

module.exports = { content };
