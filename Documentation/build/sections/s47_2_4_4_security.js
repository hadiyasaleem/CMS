const { Paragraph, TextRun, HeadingLevel } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const content = [
  new Paragraph({ text: "2.4.4 Security", heading: HeadingLevel.HEADING_3, spacing: { after: 100 } }),
  para("Every table is protected by Postgres Row-Level Security, so access control lives in the database, not in application code that a bug could accidentally skip. A student's queries are scoped to session_id = my_session() and roll_number = my_roll(); a teacher's are scoped through a teaches(session_id) function keyed off the timetable; admin bypasses both via an is_admin() check. Authentication itself is Supabase's GoTrue (JWT-based), so the app never handles or stores a raw password."),
  para("The clearest proof this actually holds is the Insights feature (FR-22): the exact same SQL query, run by an admin, a teacher, or a student, returns a different result set for each — not because the app checked their role and branched, but because RLS decided what rows existed for that JWT before the app ever saw them."),
];

module.exports = { content };
