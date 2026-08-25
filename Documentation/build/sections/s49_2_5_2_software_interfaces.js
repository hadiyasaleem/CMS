const { Paragraph, TextRun, HeadingLevel } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const content = [
  new Paragraph({ text: "2.5.2 Software Interfaces", heading: HeadingLevel.HEADING_3, spacing: { after: 100 } }),
  para("Supabase (self-hosted-compatible, currently cloud-hosted) provides four services the app talks to directly: Postgrest for the database, GoTrue for authentication, Storage for uploaded files, and Edge Functions for the handful of actions that need service-role privileges (creating a teacher account, changing a teacher's status) rather than what the signed-in user's own RLS permissions allow. Locally, Room provides the offline cache described in 2.4.1. All three apps and the shared cmscommon module talk to Supabase through the official supabase-kt SDK over Ktor."),
];

module.exports = { content };
