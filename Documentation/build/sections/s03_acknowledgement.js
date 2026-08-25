const { Paragraph, TextRun, HeadingLevel, PageBreak } = require("../generate_report.js");

const content = [
  new Paragraph({ text: "Acknowledgement", heading: HeadingLevel.HEADING_1, spacing: { after: 200 } }),
  new Paragraph({
    children: [new TextRun({
      text: "[PLACEHOLDER — write this yourself, in your own words. It's the one section of this report a template can't fill in for you. Typically thanks: Prof. Ubaid Ullah (your FYDP advisor) for supervision and guidance; the FYP Coordination Office / Prof. Muhammad Faiyaz; the Department of Information Technology, Govt. Graduate College, Mandi Bahauddin; and family or anyone else who supported you through the project.]",
      italics: true, color: "C00000",
    })],
    spacing: { after: 160 },
  }),
  new Paragraph({ spacing: { before: 600 } }),
  new Paragraph({
    tabStops: [{ type: "right", position: 8000 }],
    children: [
      new TextRun({ text: "Signature: ___________________" }),
      new TextRun({ text: "\tSignature: ___________________" }),
    ],
  }),
  new Paragraph({
    tabStops: [{ type: "right", position: 8000 }],
    spacing: { after: 400 },
    children: [
      new TextRun({ text: "Hadia [085668]" }),
      new TextRun({ text: "\tSharfa Kiran [085646]" }),
    ],
  }),
  new Paragraph({ children: [new TextRun({ text: "Signature: ___________________" })] }),
  new Paragraph({ children: [new TextRun({ text: "Syeda Laraib Qamar Kazmi [085713]" })] }),
  new Paragraph({ children: [new PageBreak()] }),
];

module.exports = { content };
