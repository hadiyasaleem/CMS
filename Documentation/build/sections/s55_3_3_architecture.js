const { Paragraph, TextRun, HeadingLevel, ImageRun, fs, AlignmentType } = require("../generate_report.js");
const path = require("path");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const imgBuffer = fs.readFileSync(path.join(__dirname, "..", "architecture.png"));

const content = [
  new Paragraph({ text: "3.3 Architectural Design", heading: HeadingLevel.HEADING_2, spacing: { after: 120 } }),
  para("The system is four layers, and every one of the three apps goes through all four the same way — there's no shortcut where cmsadmin talks to Postgres directly while cmsteacher goes through a different path. Compose screens hold a ViewModel each; the ViewModel calls into cmscommon's Repository layer, which is the only code that knows about DTOs, Supabase table names, or SQL-shaped queries. Below that, a Repository either reads/writes Room (for the handful of high-frequency, cached features) or talks straight to the Supabase Kotlin SDK, which is what actually reaches Postgrest, GoTrue, Storage, or an Edge Function over the network."),
  new Paragraph({
    children: [new ImageRun({ type: "png", data: imgBuffer, transformation: { width: 580, height: 386 } })],
    alignment: AlignmentType.CENTER,
    spacing: { after: 100 },
  }),
  new Paragraph({
    children: [new TextRun({ text: "Figure 3-1  CMS layered architecture", italics: true, size: 18 })],
    alignment: AlignmentType.CENTER,
    spacing: { after: 160 },
  }),
  para("This is closest to a layered/client-server hybrid rather than a textbook MVC or MVVM-only pattern — MVVM describes the top two layers (Compose View + ViewModel), but the Repository/Room/Supabase split underneath is really about where data lives and how fresh it needs to be, which MVVM alone doesn't prescribe. The actual enforcement of who can do what isn't in this diagram at all: it lives in Postgres RLS policies at the very bottom, which is why the same architecture serves three different roles without three different code paths."),
];

module.exports = { content };
