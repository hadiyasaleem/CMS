const { Paragraph, TextRun, HeadingLevel } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}
function bullet(text) {
  return new Paragraph({ children: [new TextRun({ text })], bullet: { level: 0 }, spacing: { after: 60 } });
}

const content = [
  new Paragraph({ text: "6.3 Post Deployment Testing", heading: HeadingLevel.HEADING_2, spacing: { after: 120 } }),
  para("Once the six deployment steps in 6.2 are done for a pilot department, the following smoke checks confirm the rollout actually worked before opening it up to more departments — and correspond directly to the FT/IT test cases specified in Chapter 5, run for real this time rather than only specified:"),
  bullet("The bootstrap admin can sign in and see the pilot department, session, and roster they created."),
  bullet("A teacher account tied to that session can mark attendance and enter marks for their assigned class only — and cannot see or touch a different department's data (confirms RLS scoping, not just UI hiding)."),
  bullet("A student's link request submitted against the pilot roster is visible in the admin's Link Requests queue and, once approved, that student sees their real attendance/marks/fee data."),
  bullet("Password-reset and account-verification emails actually arrive (confirms the SMTP configuration from 6.2 step 2 took effect)."),
  para("If any of these fail, the fix belongs in the corresponding chapter — an RLS gap is a Chapter 3 design bug, a missing email is a Chapter 6 configuration gap, not a new category of its own."),
];

module.exports = { content };
