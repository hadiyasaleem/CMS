const { Paragraph, TextRun, HeadingLevel, bullet } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const content = [
  new Paragraph({ text: "3.7 Design Decisions", heading: HeadingLevel.HEADING_2, spacing: { after: 120 } }),
  bullet("RLS over app-side checks. Every access rule (a teacher only sees their own sessions, a student only their own row) is a Postgres policy, not an if-statement in a Repository. The Insights feature is the proof this pays off: the same query serves three roles with zero role-branching in Kotlin. The cost is that RLS bugs are invisible in the Kotlin code — you have to go look at the policy — which is a real debugging cost the team accepted."),
  bullet("Fees moved from per-department to per-session mid-project. The original design put a fee structure on each department, shared by every session under it. That broke the first time two intakes of the same department needed different tuition — a genuine requirements miss, not a technical one — so the schema, repository, and both the admin editor and student challan screen were rebuilt around session_fees/session_fee_heads instead."),
  bullet("Delete-then-insert for small, infrequent collections. Curriculum, session fee heads, and timetable-adjacent lists are all replaced wholesale on save rather than diffed and patched. For a handful of rows edited a few times a semester, the simplicity of \"delete the old set, insert the new one\" beat writing and maintaining a diffing algorithm nobody would notice the absence of."),
  bullet("Direct Postgrest over Room caching, for low-traffic features. Fees, fines, calendar, datesheets, documents, and insights all skip the offline cache entirely and read straight from the database. Only roster, timetable, and attendance — the screens opened dozens of times a day — got the Room + SyncEngine treatment. Caching everything would have meant maintaining nine more Room tables for data that's read a handful of times a week."),
  bullet("The DTO-default-collision rule. kotlinx.serialization's encodeDefaults=false drops any field equal to its Kotlin default; combined with Postgrest's bulk-upsert behaviour (missing columns get filled with NULL, not the database's own default), this silently NULLed out a NOT NULL column three separate times before the pattern was understood. The fix that stuck: any DTO field backing a NOT NULL column with no database-level default gets a sentinel Kotlin default that can never be a real value (empty string, not the semantically normal default) — audited into every new DTO written after the bug was found, not just the three that broke."),
  bullet("Marks lock after first entry, no exceptions for the entering teacher. This was a deliberate, not incidental, restriction — the alternative (let a teacher edit freely, log the change) was considered and rejected because a log nobody has to look at doesn't function as an audit trail. Requiring admin sign-off, even for the teacher's own typo, was the actual point."),
];

module.exports = { content };
