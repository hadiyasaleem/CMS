const { Paragraph, TextRun, HeadingLevel } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const content = [
  new Paragraph({ text: "6.2.1 Data Conversion", heading: HeadingLevel.HEADING_3, spacing: { after: 100 } }),
  para("Minimal, because there's no source database to extract from — the college's existing records are paper registers and hand-written rosters, not a system with an export format. The one real data-entry task is the admin manually keying in each session's roster (roll number, name, and the optional richer profile fields) through the Session Students screen the first time a department comes onto the system; there's no bulk-import tool for this yet, which is listed as a gap in Future Work (7.4) rather than something already solved."),
  para("Backup, in the absence of anything to convert, means the normal Supabase database backup/restore that comes with the hosted project — nothing project-specific was built on top of it."),
];

module.exports = { content };
