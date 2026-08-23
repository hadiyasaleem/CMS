const { Paragraph, TextRun, HeadingLevel, PageBreak } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const content = [
  new Paragraph({ children: [new PageBreak()] }),
  new Paragraph({ text: "Chapter 6", spacing: { before: 2000, after: 0 } }),
  new Paragraph({ text: "System Conversion", heading: HeadingLevel.TITLE, spacing: { after: 4000 } }),
  new Paragraph({ children: [new PageBreak()] }),

  new Paragraph({ text: "6. Introduction", heading: HeadingLevel.HEADING_1, spacing: { after: 160 } }),
  para("There's no legacy digital system at GGC MBD to migrate away from — the \"old system\" this project replaces is paper: registers, hand-filled fee challans, printed datesheets. That changes what \"conversion\" means here: there's no data export from a previous system, no cutover window where two databases need to agree, just a rollout of something genuinely new."),

  new Paragraph({ text: "6.1 Conversion Method", heading: HeadingLevel.HEADING_2, spacing: { after: 120 } }),
  para("Phased conversion is the right fit, not direct or parallel. The four modules that matter most for day-one usefulness — attendance, marks entry, fee challan viewing, and datesheets — can go live as soon as the admin has entered a department's sessions and rosters; deeper features (Insights, teacher lifecycle management, fines) can follow once the college's admin staff are comfortable with the basics. Direct conversion doesn't apply since there's no old system to abruptly switch off, and parallel conversion doesn't make sense either — there's nothing to run in parallel with."),
  para("Rollout is naturally per-department too, not all-at-once college-wide: onboarding one department's admin, teachers, and students first surfaces onboarding friction (unfamiliar roll-number formats, missing CNIC data, teachers unsure which session they're assigned to) on a small population before every department hits the same issues simultaneously."),
];

module.exports = { content };
