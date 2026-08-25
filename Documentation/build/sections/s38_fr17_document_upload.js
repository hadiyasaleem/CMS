const { Paragraph, HeadingLevel, frTable, tableCaption } = require("../generate_report.js");

const content = [
  new Paragraph({ text: "2.3.17 FR-17: Upload & Publish Document", heading: HeadingLevel.HEADING_3, spacing: { after: 100 } }),
  tableCaption("Table 2-18  Description of FR-17"),
  frTable({
    id: "FR-17",
    title: "Upload & Publish Document",
    requirement: "The Admin shall be able to either upload a PDF/DOCX file or type body text directly, tag it with a kind (prospectus, rules, report, other) and an audience, and publish it.",
    source: "College documents that currently only exist as physical printouts (prospectus, rulebook).",
    rationale: "Giving the admin a choice between uploading a file and just typing text covers both cases — a rulebook that's genuinely a formatted PDF, and a one-off short notice that doesn't deserve a whole file.",
    businessRule: "Uploaded files live in a private Supabase Storage bucket; a document under the archives/ path is intentionally excluded from the public-read policy, reserved for internal-only material.",
    dependencies: "None.",
    priority: "Low",
  }),
];

module.exports = { content };
