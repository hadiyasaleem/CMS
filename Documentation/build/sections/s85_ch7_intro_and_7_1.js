const { Paragraph, TextRun, HeadingLevel, PageBreak, table, tableCaption } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const content = [
  new Paragraph({ children: [new PageBreak()] }),
  new Paragraph({ text: "Chapter 7", spacing: { before: 2000, after: 0 } }),
  new Paragraph({ text: "Conclusion", heading: HeadingLevel.TITLE, spacing: { after: 4000 } }),
  new Paragraph({ children: [new PageBreak()] }),

  new Paragraph({ text: "7. Introduction", heading: HeadingLevel.HEADING_1, spacing: { after: 160 } }),
  para("This chapter checks the objectives from Chapter 1 against what actually got built, traces requirements through to code and tests, and closes with what's genuinely left to do — not a polished wrap-up, but an honest accounting."),

  new Paragraph({ text: "7.1 Evaluation", heading: HeadingLevel.HEADING_2, spacing: { after: 120 } }),
  tableCaption("Table 7-1  Evaluation — Objectives vs Status"),
  table(
    ["Objective (from 1.3)", "Status"],
    [
      ["Student generates/views their own fee challan with zero in-person visits for a routine challan", "Met — FR-11, informational view only, no payment processing (deliberately out of scope, see 1.4/1.8)"],
      ["Teacher marks a full class's attendance in under a minute, visible to admin/students immediately", "Met — FR-5; timing itself untested on-device (5.4/PT-1 pending)"],
      ["A mark, once entered, can't be silently changed — correction requires admin approval", "Met — FR-6/FR-7/FR-8, enforced at the RLS layer, not just the UI"],
      ["Student sees semester GPA/CGPA progression, including supply subjects, without a printed transcript", "Met — FR-9, ResultsScreen"],
      ["Datesheets and notices reach affected students/teachers the moment they're published", "Met — FR-14/FR-15/FR-16; \"the moment\" is on next screen open, not a push notification (see 7.4)"],
      ["Data is scoped to the correct role via database-level rules, not application-remembered checks", "Met — every table/view is RLS-protected; FR-22's tiered Insights is the clearest demonstration of this"],
      ["System stays usable when connectivity drops (cached roster/timetable/attendance)", "Partially met — Room caches the high-frequency reads; there is no queued-retry for writes made while offline (2.4.1)"],
    ],
    [5500, 3860],
  ),
];

module.exports = { content };
