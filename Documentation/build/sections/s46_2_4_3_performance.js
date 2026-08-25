const { Paragraph, TextRun, HeadingLevel } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const content = [
  new Paragraph({ text: "2.4.3 Performance", heading: HeadingLevel.HEADING_3, spacing: { after: 100 } }),
  para("Screens backed by the Room cache (roster, timetable, attendance) render from local data immediately and sync in the background, so there's no network round-trip on the critical path for the highest-frequency actions. Screens that read directly from Postgrest (fees, insights, documents) accept a network round-trip per load since they're checked far less often — the tradeoff is simplicity over shaving a few hundred milliseconds off an infrequent screen."),
  para("No load testing has been run against the Supabase project at anything beyond the current single-college scale; the free-tier connection and storage limits are the practical ceiling right now, and that's an open item rather than a validated number."),
];

module.exports = { content };
