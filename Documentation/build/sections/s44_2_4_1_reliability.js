const { Paragraph, TextRun, HeadingLevel } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const content = [
  new Paragraph({ text: "2.4 Non-Functional Requirements", heading: HeadingLevel.HEADING_2, spacing: { after: 120 } }),
  new Paragraph({ text: "2.4.1 Reliability", heading: HeadingLevel.HEADING_3, spacing: { after: 100 } }),
  para("High-read-volume data — roster, timetable, attendance — is cached locally in Room and kept current by a background SyncEngine, so a teacher marking attendance mid-class doesn't lose the roster if the connection drops for a few seconds. Lower-volume features (fees, fines, calendar, datesheets, documents, insights) read directly from Postgrest instead of maintaining a local copy, since they're checked far less often and a brief loading spinner on a weak connection is an acceptable cost for not maintaining a second cache layer."),
  para("Failure mode when a write genuinely can't reach the server: the screen shows the error instead of silently discarding the action — there's no queued-retry mechanism for writes yet, which is a real gap the Future Work section (7.4) calls out rather than papers over."),
];

module.exports = { content };
