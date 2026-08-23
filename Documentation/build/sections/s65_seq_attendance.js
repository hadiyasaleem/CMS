const { Paragraph, TextRun, HeadingLevel, ImageRun, fs, AlignmentType } = require("../generate_report.js");
const path = require("path");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const imgBuffer = fs.readFileSync(path.join(__dirname, "..", "seq_attendance.png"));

const content = [
  new Paragraph({ text: "3.6.3 Attendance Marking", heading: HeadingLevel.HEADING_3, spacing: { after: 100 } }),
  para("The roster load reads from Room, not the network — this is the highest-frequency screen in the system (2.4.1), so it has to open instantly even on a weak connection. The save is the opposite: it goes straight to Postgrest as one bulk insert, and only updates the local cache afterward, so the source of truth for a write is always the server, never the offline copy."),
  new Paragraph({
    children: [new ImageRun({ type: "png", data: imgBuffer, transformation: { width: 580, height: 280 } })],
    alignment: AlignmentType.CENTER,
    spacing: { after: 100 },
  }),
  new Paragraph({
    children: [new TextRun({ text: "Figure 3-4  Attendance marking sequence", italics: true, size: 18 })],
    alignment: AlignmentType.CENTER,
    spacing: { after: 160 },
  }),
];

module.exports = { content };
