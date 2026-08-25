const { Paragraph, TextRun, HeadingLevel } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const content = [
  new Paragraph({ text: "2.2 Requirement Identifying Technique", heading: HeadingLevel.HEADING_2, spacing: { after: 120 } }),
  para("Use-case modeling was the main technique. Every one of the three apps is an interactive, session-based tool where a specific actor triggers a specific action and expects a specific result — mark this student present, publish this datesheet, approve this fee change — which is exactly the shape a use case captures well. Storyboarding wasn't a fit here: nothing in the system is graphically driven enough to need it, and the UI itself (the shared \"Modernist\" Compose component set) was designed after the use cases and requirements, not before them."),
  para("In practice, requirements came from watching how the college's existing paper process actually worked — who fills in what, who signs off on it, what happens when someone makes a mistake — and then asking what a digital equivalent of that step would need to do. The mark-lock-then-request-edit requirement is a direct example: it exists because the paper process already had an informal version of it (a teacher didn't get to unilaterally change a mark sheet once it was submitted to the office), so the requirement formalizes an existing norm rather than inventing a new one."),
];

module.exports = { content };
