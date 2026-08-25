const { Paragraph, TextRun, HeadingLevel, PageBreak, table } = require("../generate_report.js");

const content = [
  new Paragraph({ text: "Abbreviations", heading: HeadingLevel.HEADING_1, spacing: { after: 200 } }),
  new Paragraph({
    children: [new TextRun({ text: "Abbreviations and acronyms used throughout this report, ordered alphabetically." })],
    spacing: { after: 160 },
  }),
  table(
    ["Abbreviation", "Description"],
    [
      ["API", "Application Programming Interface"],
      ["BS", "Bachelor of Science"],
      ["BSIT", "Bachelor of Science in Information Technology"],
      ["CGPA", "Cumulative Grade Point Average"],
      ["CMS", "College Management System"],
      ["CRUD", "Create, Read, Update, Delete"],
      ["DI", "Dependency Injection (Hilt)"],
      ["DTO", "Data Transfer Object"],
      ["FAC", "Faculty Advisory Committee"],
      ["FR", "Functional Requirement"],
      ["FYDP", "Final Year Design Project"],
      ["GGC MBD", "Govt. Graduate College, Mandi Bahauddin"],
      ["GPA", "Grade Point Average"],
      ["JWT", "JSON Web Token"],
      ["MVVM", "Model-View-ViewModel"],
      ["NFR", "Non-Functional Requirement"],
      ["RLS", "Row-Level Security (Postgres)"],
      ["RPC", "Remote Procedure Call (a Postgres function invoked from the app)"],
      ["SDK", "Software Development Kit"],
      ["SRS", "Software Requirements Specification"],
      ["UI", "User Interface"],
      ["UUID", "Universally Unique Identifier"],
    ],
    [3000, 6360],
  ),
  new Paragraph({ children: [new PageBreak()] }),
];

module.exports = { content };
