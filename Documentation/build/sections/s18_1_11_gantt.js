const { Paragraph, TextRun, HeadingLevel, ImageRun, fs, AlignmentType } = require("../generate_report.js");
const path = require("path");

const imgPath = path.join(__dirname, "..", "gantt.png");
const imgBuffer = fs.readFileSync(imgPath);

const content = [
  new Paragraph({ text: "1.11 Project Planning", heading: HeadingLevel.HEADING_2, spacing: { after: 120 } }),
  new Paragraph({
    children: [new TextRun({
      text: "[NOTE — reconstructed, not the original academic plan: the timeline below is built from actual git commit history (Jan 14 – Jun 17 2026) for the original Java/Firebase build, plus session records for the Kotlin/Compose/Supabase rebuild (Jul 2026), since that later work was never committed to git as individual dated commits. Please confirm the real FYDP milestone/deadline dates and swap them in if they differ.]",
      italics: true, color: "C00000",
    })],
    spacing: { after: 160 },
  }),
  new Paragraph({
    children: [new ImageRun({ type: "png", data: imgBuffer, transformation: { width: 620, height: 260 } })],
    alignment: AlignmentType.CENTER,
    spacing: { after: 100 },
  }),
  new Paragraph({
    children: [new TextRun({ text: "Figure 1-1  Project timeline, reconstructed from commit history and session records", italics: true, size: 18 })],
    alignment: AlignmentType.CENTER,
    spacing: { after: 160 },
  }),
];

module.exports = { content };
