const { Paragraph, TextRun, HeadingLevel, ImageRun, fs, AlignmentType } = require("../generate_report.js");
const path = require("path");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const imgBuffer = fs.readFileSync(path.join(__dirname, "..", "seq_login.png"));

const content = [
  new Paragraph({ text: "3.6.1 Login Flow", heading: HeadingLevel.HEADING_3, spacing: { after: 100 } }),
  para("The non-obvious step here is RoleResolver: a JWT alone doesn't tell the app whether it's talking to an admin, teacher, or student — that still has to be looked up from the profiles/teachers rows, and the resolver checks the local Room cache before it checks the network so a previously-signed-in user doesn't stall on a slow connection just to find out their own role again."),
  new Paragraph({
    children: [new ImageRun({ type: "png", data: imgBuffer, transformation: { width: 560, height: 311 } })],
    alignment: AlignmentType.CENTER,
    spacing: { after: 100 },
  }),
  new Paragraph({
    children: [new TextRun({ text: "Figure 3-2  Login sequence", italics: true, size: 18 })],
    alignment: AlignmentType.CENTER,
    spacing: { after: 160 },
  }),
];

module.exports = { content };
