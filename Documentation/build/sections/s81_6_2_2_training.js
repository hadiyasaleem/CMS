const { Paragraph, TextRun, HeadingLevel, placeholder } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}
function step(n, text) {
  return new Paragraph({ children: [new TextRun({ text: `${n}. ${text}` })], spacing: { after: 80 } });
}

const content = [
  new Paragraph({ text: "6.2.2 Training", heading: HeadingLevel.HEADING_3, spacing: { after: 100 } }),
  para("Two of the system's most frequently used flows are walked through below as a short user manual. Screenshots aren't included in this draft — see the placeholder note — but the steps themselves are accurate to the current UI and can be followed on the actual app."),
  placeholder("Screenshots for each numbered step below were not captured for this draft (no device/emulator session was available while writing it). Insert a screenshot of the named screen at each step before final submission."),

  new Paragraph({ text: "Marking Attendance (Teacher)", heading: HeadingLevel.HEADING_4, spacing: { before: 160, after: 100 } }),
  step(1, "Open the Faculty Ledger app and sign in."),
  step(2, "Tap Attend in the bottom navigation."),
  step(3, "Pick the class (session + course) you're about to teach."),
  step(4, "Mark each student Present, Absent, or Leave using the P/A/L control next to their name."),
  step(5, "Tap Save. The class's attendance for today is now recorded — tap History from the same screen to review any previous day."),

  new Paragraph({ text: "Viewing Fee Challan (Student)", heading: HeadingLevel.HEADING_4, spacing: { before: 200, after: 100 } }),
  step(1, "Open the Student Ledger app and sign in."),
  step(2, "Tap More in the bottom navigation, then Fee Challan."),
  step(3, "The screen shows your session's fee cadence (semester or annual), each fee head with its amount, the total, and the due date."),
  step(4, "This screen is informational only — pay the amount shown at the college's accounts office, not through the app."),
];

module.exports = { content };
