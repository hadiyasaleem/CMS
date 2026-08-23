const { Paragraph, TextRun, HeadingLevel } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const content = [
  new Paragraph({ text: "2.5.3 Hardware Interfaces", heading: HeadingLevel.HEADING_3, spacing: { after: 100 } }),
  para("No special hardware — a standard Android phone or tablet is all any of the three apps need. Nothing uses the camera, biometric sensors, or any other device peripheral; a document upload picks an existing file from the device's storage rather than scanning one."),
];

module.exports = { content };
