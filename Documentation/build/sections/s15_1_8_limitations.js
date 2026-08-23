const { Paragraph, TextRun, HeadingLevel, bullet } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const content = [
  new Paragraph({ text: "1.8 System Limitations and Constraints", heading: HeadingLevel.HEADING_2, spacing: { after: 120 } }),
  new Paragraph({ children: [new TextRun({ text: "Limitations (external, beyond our control):", bold: true })], spacing: { after: 80 } }),
  bullet("Requires an internet connection for any write and for data not already cached locally; the offline cache only covers high-read-volume data (roster, timetable, attendance)."),
  bullet("Android only — no iOS or web client exists."),
  bullet("Depends on Supabase's free-tier limits (storage quota, function invocations) at the current scale of deployment."),
  new Paragraph({ children: [new TextRun({ text: "Constraints (self-imposed, within our control):", bold: true })], spacing: { after: 80 } }),
  bullet("Fee collection stays a two-step process by design — the app generates and displays the challan, but payment is still made at the college accounts office, not through an in-app payment gateway."),
  bullet("A score is locked after its first entry; any change afterward must go through the mark-edit-request workflow rather than a direct edit, even for the teacher who entered it."),
  bullet("The initial deployment targets a single college (GGC MBD); multi-college support is architecturally possible (departments are already scoped) but not exercised or tested."),
];

module.exports = { content };
