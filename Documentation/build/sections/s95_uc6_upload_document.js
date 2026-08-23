const { Paragraph, HeadingLevel, PageBreak, useCaseTable, tableCaption } = require("../generate_report.js");

const content = [
  new Paragraph({ text: "UC-6  Upload & Publish Document", heading: HeadingLevel.HEADING_2, spacing: { after: 100 } }),
  tableCaption("Table A-6  Use Case Description — Upload & Publish Document"),
  useCaseTable({
    id: "UC-6",
    name: "Upload & Publish Document",
    actors: "Admin",
    description: "Admin makes a college document (prospectus, rules, report) available to students/teachers, either as an uploaded file or typed text.",
    trigger: "Admin opens Records → Documents and adds a new document.",
    preconditions: "None beyond being signed in as admin.",
    postconditions: "The document exists, tagged with a kind and audience; once published, the intended audience can view/download it.",
    normalFlow: "1. Admin chooses to either upload a PDF/DOCX file or type body text directly. 2. Admin sets the document's kind (prospectus/rules/report/other) and audience. 3. Admin publishes it. 4. An uploaded file is stored in the private documents Storage bucket; typed text is stored inline.",
    alternativeFlows: "1a. Admin provides both a file and typed text (e.g. a summary plus the full attached document). 3a. Admin leaves it unpublished as a draft to finish later.",
    businessRules: "A document filed under the archives/ storage path is excluded from the general public-read policy, reserved for internal-only material — everything else published is readable by its target audience.",
    assumptions: "The uploaded file is a reasonable size for the free-tier Storage quota; no client-side file-size limit is currently enforced beyond what Storage itself rejects.",
  }),
  new Paragraph({ children: [new PageBreak()] }),
];

module.exports = { content };
