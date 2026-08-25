const { Paragraph, TextRun, HeadingLevel } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const content = [
  new Paragraph({ text: "2.5 External Interface Requirements", heading: HeadingLevel.HEADING_2, spacing: { after: 120 } }),
  new Paragraph({ text: "2.5.1 User Interfaces Requirements", heading: HeadingLevel.HEADING_3, spacing: { after: 100 } }),
  para("All three apps follow one shared design system internally called \"Modernist\" — a flat red/ink Archivo-based look with square (non-rounded) cards, a consistent eyebrow/title/rule header pattern, and one shared set of components (LedgerCard, HubNavCard, SegmentToggle, StatusBadge, and similar). Bottom navigation follows Android's standard 5-tab pattern; destructive actions always show a confirmation dialog naming what else is affected, not a bare confirm/cancel. There's no separate accessibility pass yet — standard Material3 contrast and touch-target defaults apply, nothing beyond that has been verified."),
];

module.exports = { content };
