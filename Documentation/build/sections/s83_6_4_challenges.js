const { Paragraph, TextRun, HeadingLevel } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}
function h4(text) {
  return new Paragraph({ text, heading: HeadingLevel.HEADING_4, spacing: { before: 160, after: 100 } });
}

const content = [
  new Paragraph({ text: "6.4 Challenges", heading: HeadingLevel.HEADING_2, spacing: { after: 120 } }),
  para("Three real problems surfaced during development that are worth recording honestly, since each one changed how a piece of the system is built — not hypothetical risks, things that actually broke."),

  h4("The DTO default-collision bug (hit three times before the pattern was recognised)"),
  para("kotlinx.serialization is configured with encodeDefaults=false, so any DTO field left at its Kotlin default gets dropped from the JSON payload entirely. That's normally harmless — until a bulk insert has several rows where most share a field's default value, and Postgrest fills the missing column with NULL for every row in that batch, not just the ones genuinely missing a value. It first surfaced as attendance rows silently failing to save is_late correctly, then again with exam-type fields, before the actual cause was pinned down. The fix became a standing rule rather than a one-off patch: any DTO field backing a NOT NULL column with no database default gets a sentinel default value that can never occur for real (an empty string instead of a real status name, for instance), so the field is never silently omitted."),

  h4("Tightening marks RLS broke the bulk-save path"),
  para("When the mark-lock/edit-request feature (2.3.6–2.3.8) restricted session_marks UPDATE to admin-only, the existing \"Save all scores in this class\" button started failing outright — it always bulk-upserted the whole roster, and Postgrest's upsert is an INSERT with an ON CONFLICT DO UPDATE clause, which needs UPDATE privilege the moment any row in the batch already exists. One already-locked student in a batch of thirty was enough to abort the entire save. The fix was in the ViewModel, not the database: the save action now filters out any student who already has a saved score before it ever calls the bulk-write, so the call is a pure INSERT and never touches the tightened UPDATE policy."),

  h4("A UI bug that only showed up in a user's screenshot"),
  para("A shared segmented-toggle component (used for exam-type selection, fee-cadence selection, and elsewhere) had a VerticalDivider with no height constraint. Under Material3's defaults that divider tries to fill all available vertical space, and since the surrounding layout had no bounded height either, the whole toggle expanded to swallow the rest of the screen on the Marks Entry page specifically. No design review caught it — it was found because a user sent a screenshot of what looked like a broken, mostly-blank screen. The fix (constrain the row to its content's intrinsic height) was small once found, but finding it relied on an actual user hitting it rather than any structured testing process, which is the real lesson: this project doesn't yet have a UI review step that would have caught this before it shipped."),
];

module.exports = { content };
