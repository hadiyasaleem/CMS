const { Paragraph, HeadingLevel, useCaseTable, tableCaption } = require("../generate_report.js");

const content = [
  new Paragraph({ text: "UC-5  Build & Publish Datesheet", heading: HeadingLevel.HEADING_2, spacing: { after: 100 } }),
  tableCaption("Table A-5  Use Case Description — Build & Publish Datesheet"),
  useCaseTable({
    id: "UC-5",
    name: "Build & Publish Datesheet",
    actors: "Admin (or a Teacher with datesheet-management permission)",
    description: "Admin assembles an exam datesheet (title, slots) and publishes it so students/teachers can see it.",
    trigger: "Admin opens Records → Datesheets and creates a new datesheet.",
    preconditions: "The session(s) the datesheet covers already exist.",
    postconditions: "The datesheet exists with its slots; once published, it's visible to students and non-managing teachers.",
    normalFlow: "1. Admin creates a datesheet (title, exam type, instructions) — starts as draft. 2. Admin adds one or more slots (exam date, time, course, room, invigilator). 3. Admin reviews the assembled datesheet. 4. Admin toggles it to Published.",
    alternativeFlows: "4a. Admin leaves it as draft to keep building later: it stays invisible to students/non-managing teachers until explicitly published. 2a. Admin deletes a slot entered by mistake before publishing.",
    businessRules: "Only published=true datesheets are visible to students and teachers without datesheet-management permission — enforced by RLS on the datesheets/datesheet_slots tables, not by hiding drafts in the UI alone.",
    assumptions: "One admin builds a given datesheet without simultaneous editing by a second admin.",
  }),
];

module.exports = { content };
