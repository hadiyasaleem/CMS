const { Paragraph, TextRun, HeadingLevel, table, tableCaption } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const content = [
  new Paragraph({ text: "6.2 Deployment", heading: HeadingLevel.HEADING_2, spacing: { after: 120 } }),
  para("There's no production server to provision — Supabase's hosted platform is the backend, and the three Android apps are distributed as APKs rather than through the Play Store for this initial rollout. Deployment is really two checklists: one on the Supabase dashboard, one for getting the apps onto devices."),
  tableCaption("Table 6-1  Deployment Steps"),
  table(
    ["Step", "Action"],
    [
      ["1", "On the Supabase dashboard, create the bootstrap admin account and set its role to ADMIN in the profiles table — every other admin action depends on at least one such account existing."],
      ["2", "Configure Auth → SMTP so account-verification and password-reset emails actually send; without this, FR-19 (link request) and password reset silently fail to deliver mail."],
      ["3", "Add cms://login-callback to Auth → Redirect URLs, required for the deep-link back into the app after email verification."],
      ["4", "Build a signed release APK for each of the three apps (:app:cmsadmin:assembleRelease, etc.)."],
      ["5", "Install each APK on the intended device population — clean install (uninstall any prior debug build first) to avoid a stale Room schema colliding with a fresh one."],
      ["6", "Have the bootstrap admin create the first department, session, and a small pilot roster, then confirm a teacher and student account can each sign in and see their own scoped data."],
    ],
    [1200, 8160],
  ),
];

module.exports = { content };
