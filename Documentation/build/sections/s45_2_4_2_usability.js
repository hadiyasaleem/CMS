const { Paragraph, TextRun, HeadingLevel } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const content = [
  new Paragraph({ text: "2.4.2 Usability", heading: HeadingLevel.HEADING_3, spacing: { after: 100 } }),
  para("All three apps share one Compose component library, so a Ledger card, a section header, or a segmented toggle looks and behaves identically whether it's in the admin, teacher, or student app — someone who's learned one app already knows the visual language of the other two. Marks entry validates as the teacher types rather than only on submit, and every destructive action (deleting a session, banning a teacher) goes through a confirmation dialog that states what else gets affected, not just a bare \"Are you sure?\"."),
  para("One real usability bug did slip through and got caught late: a shared segmented-toggle component had an unconstrained divider that, under certain screen heights, expanded to swallow the rest of the screen below the marks-entry exam-type selector. It was found from a user screenshot, not a design review, which says something about the limits of not having done any structured usability testing yet — noted honestly in Chapter 6's Challenges section rather than left out."),
];

module.exports = { content };
