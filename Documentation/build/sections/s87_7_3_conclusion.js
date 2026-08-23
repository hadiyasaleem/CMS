const { Paragraph, TextRun, HeadingLevel } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const content = [
  new Paragraph({ text: "7.3 Conclusion", heading: HeadingLevel.HEADING_2, spacing: { after: 120 } }),
  para("Six of the seven objectives from Chapter 1 are met outright, and the seventh — staying usable offline — is met for reads but not yet for writes made while disconnected, which is a real, specific gap rather than a rounding error. What this project actually demonstrates is narrower and more interesting than \"we built three apps\": that a single set of Postgres Row-Level Security policies can serve three different roles' worth of access control without the applications themselves ever encoding who's allowed to see what. FR-22's Insights feature is the cleanest proof of that — the exact same query, run by an admin, a teacher, and a student, returns three different result sets because the database decided that, not the app."),
  para("The path here wasn't smooth in a straight line — the backend was rebuilt mid-project from Firebase to Supabase, the data model moved from offering-centric to department-centric, and three concrete bugs (documented honestly in 6.4) each forced a real design change rather than a quick patch. That the finished system still builds green across all three apps and matches the requirements in Chapter 2 is the actual claim being made here, not that the process was clean."),
];

module.exports = { content };
