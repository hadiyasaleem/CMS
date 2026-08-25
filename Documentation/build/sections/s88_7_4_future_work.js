const { Paragraph, TextRun, HeadingLevel, PageBreak, bullet } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const content = [
  new Paragraph({ text: "7.4 Future Work", heading: HeadingLevel.HEADING_2, spacing: { after: 120 } }),
  para("Listed roughly in the order they'd matter most to an actual deployment, not alphabetically:"),
  bullet("Offline write queue — right now a write attempted with no connection just fails with an error; queuing it for retry once connectivity returns would close the one real gap in 7.1's offline objective."),
  bullet("Bulk roster import — the admin currently keys in every student one at a time (6.2.1); a CSV/Excel import would remove the single biggest data-entry cost of onboarding a new department."),
  bullet("Push notifications — calendar events, datesheet publishing, and fee-due reminders currently rely on the user opening the app; a push channel would make \"published\" actually mean \"notified.\""),
  bullet("Executed device testing — Chapter 5's nine test cases need to actually run on a device with real Actual Result data, and the set itself should grow well past nine once that process exists."),
  bullet("A structured UI/usability review — the segmented-toggle bug in 6.4 was found by luck, not process; even a lightweight design-review pass before each feature ships would likely have caught it earlier."),
  bullet("Teacher lifecycle audit log — disabling/banning a teacher (FR-21) changes their access immediately but doesn't currently record a history of who changed what and when."),
  bullet("Load testing against the free-tier Supabase plan's real limits, to know in advance rather than find out live where the connection/storage ceiling actually is at multi-department scale."),
  new Paragraph({ children: [new PageBreak()] }),
];

module.exports = { content };
