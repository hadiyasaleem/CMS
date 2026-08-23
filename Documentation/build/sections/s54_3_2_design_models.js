const { Paragraph, TextRun, HeadingLevel } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const content = [
  new Paragraph({ text: "3.2 Design Models", heading: HeadingLevel.HEADING_2, spacing: { after: 120 } }),
  para("Three kinds of diagram cover this system, not all of the object-oriented standard set. An architectural component diagram (3.3) shows how the layers fit together. A data dictionary (3.4) stands in for a class diagram — the domain here is genuinely closer to a relational schema than an object graph, since almost every domain \"object\" is a thin Kotlin data class mapped straight onto a Postgres row, with the real behaviour living in Repository functions and RLS policies rather than in methods on the objects themselves. Sequence diagrams (3.6) cover the four flows worth walking through step by step: login, the marks lock/edit-request/approval loop, attendance marking, and document publish/download."),
  para("A state-transition diagram was left out on purpose. The one place a state machine would apply — a teacher account's ACTIVE/DISABLED/BANNED lifecycle, or a mark-edit-request's PENDING/APPROVED/REJECTED status — is a two- or three-state enum with no intermediate transitions or side-effect-heavy states worth drawing out. A one-line description in the relevant FR (2.3.21, 2.3.8) covers it better than a diagram would."),
];

module.exports = { content };
