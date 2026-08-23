const { Paragraph, TextRun, HeadingLevel, bullet } = require("../generate_report.js");

const content = [
  new Paragraph({ text: "1.10 Project Deliverables", heading: HeadingLevel.HEADING_2, spacing: { after: 120 } }),
  bullet("This FYDP report, covering requirements, design, implementation, testing, and deployment."),
  bullet("Three installable Android applications: cmsadmin, cmsteacher, cmsstudent (debug/release APKs)."),
  bullet("The Supabase backend: schema migrations, RLS policies, Edge Functions, and storage bucket configuration (in supabase/)."),
  bullet("Source code for all three apps and the shared cmscommon module, under version control."),
  bullet("This report's data dictionary and functional-requirements tables, which double as the closest thing to a standalone SRS for this project."),
];

module.exports = { content };
