const { Paragraph, TextRun, HeadingLevel } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const content = [
  new Paragraph({ text: "2.5.4 Communications Interfaces", heading: HeadingLevel.HEADING_3, spacing: { after: 100 } }),
  para("All network traffic is HTTPS/REST against the Supabase project, via Ktor. Account verification and password-reset flows go through GoTrue's built-in email delivery, which means the college's SMTP configuration on the Supabase dashboard is a real dependency for those two flows working at all — noted as a standing deployment item in Chapter 6."),
];

module.exports = { content };
