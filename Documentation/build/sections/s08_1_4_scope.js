const { Paragraph, TextRun, HeadingLevel, bullet } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const content = [
  new Paragraph({ text: "1.4 Scope", heading: HeadingLevel.HEADING_2, spacing: { after: 120 } }),
  para("In scope: three native Android applications (admin, teacher, student) covering the full academic-and-administrative cycle for one college — departments, sessions, curriculum, timetable, attendance, marks, semester results, per-session fees, fines, calendar, datesheets, an uploadable-documents library, student account linking, teacher lifecycle management, and role-scoped analytics. The initial deployment target is Govt. Graduate College, Mandi Bahauddin; the department-centric data model (a department owns many sessions/intakes, each session owns its own curriculum, timetable, and fee structure) is generic enough to extend to other PU-affiliated colleges without a schema change, though that rollout is not part of this project."),
  para("Out of scope: web or iOS clients (Android only), online/electronic fee payment (the system generates and displays a challan; payment still happens at the accounts office), automated exam-paper grading, and a public-facing website. Stakeholders are the college administration (department heads, the accounts office, the FYP-equivalent college management), teaching faculty, and enrolled students — all three are directly represented by one of the three apps."),
  para("Priorities, in the order features were actually built: (1) identity and the department-centric academic structure — without departments/sessions/curriculum nothing else has anywhere to attach; (2) the day-to-day teacher workflows — attendance and marks, since these are the highest-frequency actions in the system; (3) the financial and notices layer — fees, fines, calendar, datesheets, documents; (4) governance and oversight — link-request approval, teacher lifecycle, and tiered analytics, since these depend on the first three layers already existing."),
];

module.exports = { content };
