const { Paragraph, TextRun, HeadingLevel, ImageRun, fs, AlignmentType, PageBreak } = require("../generate_report.js");
const path = require("path");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const imgBuffer = fs.readFileSync(path.join(__dirname, "..", "seq_documents.png"));

const content = [
  new Paragraph({ text: "3.6.4 Document Publish and Download", heading: HeadingLevel.HEADING_3, spacing: { after: 100 } }),
  para("This is the only one of the four flows that touches Supabase Storage rather than just Postgrest. The upload and the metadata insert are two separate calls, not one transaction — if the metadata insert failed after a successful upload, the blob would be orphaned in Storage with no documents row pointing at it, which is a real (if minor) gap noted again in 3.7."),
  new Paragraph({
    children: [new ImageRun({ type: "png", data: imgBuffer, transformation: { width: 560, height: 308 } })],
    alignment: AlignmentType.CENTER,
    spacing: { after: 100 },
  }),
  new Paragraph({
    children: [new TextRun({ text: "Figure 3-5  Document publish/download sequence", italics: true, size: 18 })],
    alignment: AlignmentType.CENTER,
    spacing: { after: 160 },
  }),
  new Paragraph({ children: [new PageBreak()] }),
];

module.exports = { content };
