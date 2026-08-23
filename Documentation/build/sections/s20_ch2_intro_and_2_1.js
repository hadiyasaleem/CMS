const { Paragraph, TextRun, HeadingLevel, PageBreak, table, tableCaption } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const content = [
  // Chapter divider page
  new Paragraph({ children: [new PageBreak()] }),
  new Paragraph({ text: "Chapter 2", spacing: { before: 2000, after: 0 } }),
  new Paragraph({ text: "Requirements Analysis", heading: HeadingLevel.TITLE, spacing: { after: 4000 } }),
  new Paragraph({ children: [new PageBreak()] }),

  new Paragraph({ text: "2. Analysis", heading: HeadingLevel.HEADING_1, spacing: { after: 160 } }),
  para("Chapter 1 described what the system is and why it exists. This chapter pins that down into requirements a reviewer could actually check the finished apps against: who uses the system, how those requirements were gathered, and — in the sections that follow — the specific functional and non-functional requirements themselves."),

  new Paragraph({ text: "2.1 User Classes and Characteristics", heading: HeadingLevel.HEADING_2, spacing: { after: 120 } }),
  para("Three roles use the system, and a fourth distinction — permission flags on the teacher role — matters enough to call out separately."),
  tableCaption("Table 2-1  User Classes and Characteristics"),
  table(
    ["User Class", "Characteristics"],
    [
      [
        "Admin",
        "College staff who run the academic structure end to end: departments, sessions, curriculum, timetable, fees, teacher accounts, and the approval queues (student link requests, mark-edit requests). Comfortable with a college's actual bureaucracy — the roles map closely to what an accounts office or registrar already does on paper. Full read/write access, gated by nothing but their own admin flag in Postgres.",
      ],
      [
        "Teacher (base)",
        "Faculty who mark attendance, enter marks for the classes they teach, submit exam papers, and record semester results. Their write access is scoped by the college timetable itself — a teacher can only touch a session/course they're actually assigned to teach, enforced at the database level rather than trusted to the app.",
      ],
      [
        "Teacher (with permission flags)",
        "Same as above, plus one or more of: approving student link requests, editing the master timetable, sending notifications, or managing datesheets — each a boolean flag an admin sets on that teacher's account, not a separate role.",
      ],
      [
        "Student",
        "Enrolled students checking their own attendance, marks, semester results, fee challan, datesheets, and college notices. Read-only everywhere except their own link-request submission — RLS scopes every query to session_id = my_session() so a student physically cannot query another student's row even by guessing an ID.",
      ],
    ],
    [2200, 7160],
  ),
];

module.exports = { content };
