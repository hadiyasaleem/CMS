const { Paragraph, TextRun, HeadingLevel, PageBreak, useCaseTable, tableCaption } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const content = [
  new Paragraph({ children: [new PageBreak()] }),
  new Paragraph({ text: "Appendix A", spacing: { before: 2000, after: 0 } }),
  new Paragraph({ text: "Use Case Description (Fully Dressed Format)", heading: HeadingLevel.TITLE, spacing: { after: 4000 } }),
  new Paragraph({ children: [new PageBreak()] }),

  new Paragraph({ text: "Appendix-A  Use Case Description (Fully Dressed Format)", heading: HeadingLevel.HEADING_1, spacing: { after: 160 } }),
  para("Six use cases are detailed below, covering the workflows most central to this report — sign-in (shared by all three apps), the full marks lock/edit-request/approval loop, viewing a fee challan, and the two publish-workflows (datesheets, documents)."),

  new Paragraph({ text: "UC-1  Sign In", heading: HeadingLevel.HEADING_2, spacing: { after: 100 } }),
  tableCaption("Table A-1  Use Case Description — Sign In"),
  useCaseTable({
    id: "UC-1",
    name: "Sign In",
    actors: "Admin, Teacher, Student (any of the three apps)",
    description: "An account holder authenticates to reach their role-scoped home screen.",
    trigger: "User opens the app (or returns to it after a sign-out) and enters credentials.",
    preconditions: "The account already exists in GoTrue and, for a Teacher or Student, its profile row has a resolved role.",
    postconditions: "A valid session/JWT is held by the app; the user lands on their role's home screen with their own data loading.",
    normalFlow: "1. User opens the app. 2. User enters email and password. 3. App calls GoTrue sign-in. 4. On success, RoleResolver reads the account's role from profiles. 5. App navigates to the Admin/Teacher/Student home screen and begins syncing that role's reference data.",
    alternativeFlows: "3a. Invalid credentials: GoTrue returns an error; the app shows it inline and stays on the sign-in screen. 4a. Account has no resolved role yet (a student who hasn't linked to a roster row): app shows the \"not yet linked\" state and offers the link-request flow (UC not detailed separately — see FR-19).",
    businessRules: "A DISABLED or BANNED teacher account (FR-21) is blocked at the GoTrue level, not just app-side — the ban is on the underlying auth user.",
    assumptions: "The device has network connectivity for the initial sign-in call; subsequent app opens can restore a cached session without one.",
  }),
];

module.exports = { content };
