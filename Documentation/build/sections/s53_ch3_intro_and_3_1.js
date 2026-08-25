const { Paragraph, TextRun, HeadingLevel, PageBreak } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const content = [
  new Paragraph({ children: [new PageBreak()] }),
  new Paragraph({ text: "Chapter 3", spacing: { before: 2000, after: 0 } }),
  new Paragraph({ text: "Design and Architecture", heading: HeadingLevel.TITLE, spacing: { after: 4000 } }),
  new Paragraph({ children: [new PageBreak()] }),

  new Paragraph({ text: "3. System Design", heading: HeadingLevel.HEADING_1, spacing: { after: 160 } }),
  para("Chapter 2 fixed what the system has to do. This chapter is about how it actually does it: the structural decisions, the data model, and the trade-offs that came with each one — including the ones that didn't work on the first attempt."),

  new Paragraph({ text: "3.1 Design Considerations", heading: HeadingLevel.HEADING_2, spacing: { after: 120 } }),
  new Paragraph({ children: [new TextRun({ text: "Assumptions and Dependencies", bold: true })], spacing: { after: 80 } }),
  para("The whole design assumes Supabase's security_invoker views behave correctly — a view marked security_invoker re-runs the underlying tables' RLS policies for whoever queries it, rather than running with the view owner's privileges. Tiered access (FR-22) depends entirely on that one Postgres feature working as documented; if it didn't, the Insights feature would need per-role query branching instead of one shared view."),
  new Paragraph({ children: [new TextRun({ text: "Limitations", bold: true })], spacing: { after: 80 } }),
  para("Room's schema is versioned but not migrated — every version bump so far has used fallbackToDestructiveMigration, which wipes the local cache rather than writing a real migration path. That's a deliberate pre-launch shortcut: with no users yet, there's nothing to lose by wiping the cache, and it's saved real time across nine schema bumps. It stops being acceptable the moment the app is actually deployed with live user data on a device, since a schema change would then delete a real user's offline cache."),
  new Paragraph({ children: [new TextRun({ text: "Risks", bold: true })], spacing: { after: 80 } }),
  para("The biggest risk found during the build wasn't a design flaw so much as an interaction between two correct-looking pieces: kotlinx.serialization drops any DTO field that equals its Kotlin default (encodeDefaults=false), and Postgrest's bulk upsert fills in NULL for any column missing from a row in a multi-row insert. Combine the two and a bulk write where most rows share a field's default value can silently NULL that column out for every row — not just the ones where the value was genuinely absent. It surfaced three separate times before the pattern was recognised (see 3.7), and the fix is now a standing rule: any DTO field whose column is NOT NULL with no database default gets a sentinel default that can never occur for real (empty string, not the semantically-meaningful default)."),
];

module.exports = { content };
