const { Paragraph, TextRun, HeadingLevel, ImageRun, fs, AlignmentType } = require("../generate_report.js");
const path = require("path");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const imgBuffer = fs.readFileSync(path.join(__dirname, "..", "seq_marks.png"));

const content = [
  new Paragraph({ text: "3.6.2 Marks Entry, Edit Request, and Approval", heading: HeadingLevel.HEADING_3, spacing: { after: 100 } }),
  para("This is the one flow in the whole system built entirely around a single business rule (2.3.6-2.3.8): once session_marks.score exists for a student, only an admin-gated UPDATE can change it, so a teacher's own correction has to become a request row instead of a second write to the same table. The three-part split below — first entry, later correction, admin review — mirrors the three FRs it implements one for one."),
  new Paragraph({
    children: [new ImageRun({ type: "png", data: imgBuffer, transformation: { width: 580, height: 350 } })],
    alignment: AlignmentType.CENTER,
    spacing: { after: 100 },
  }),
  new Paragraph({
    children: [new TextRun({ text: "Figure 3-3  Marks entry / edit-request / approval sequence", italics: true, size: 18 })],
    alignment: AlignmentType.CENTER,
    spacing: { after: 160 },
  }),
];

module.exports = { content };
