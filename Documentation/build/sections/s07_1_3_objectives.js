const { Paragraph, TextRun, HeadingLevel, bullet } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const content = [
  new Paragraph({ text: "1.3 Objectives of the Proposed System", heading: HeadingLevel.HEADING_2, spacing: { after: 120 } }),
  para("The system is built around the following measurable objectives:"),
  bullet("Let a student generate and view their own fee challan from their phone, with zero in-person visits to the accounts office required for a routine, unmodified challan."),
  bullet("Let a teacher mark a full class's daily attendance in under one minute per class, replacing the paper register with a digital one that is immediately visible to admin and the affected students."),
  bullet("Enforce that a midterm or sessional score, once entered, cannot be silently changed — any correction must go through an explicit admin-approved request, closing the main source of unaudited mark changes."),
  bullet("Give every student visibility into their own semester GPA/CGPA progression, including supply subjects, without waiting for a printed transcript."),
  bullet("Publish exam datesheets and college notices (holidays, deadlines, events) to every affected student and teacher the moment they're published, replacing the noticeboard."),
  bullet("Scope every piece of data — attendance, marks, fees, analytics — to the correct role automatically via database-level access rules (Postgres Row-Level Security), rather than relying on the application to remember to check permissions."),
  bullet("Keep the system usable when connectivity drops, by caching the data a user reads most often (roster, timetable, attendance) locally and reconciling once the connection returns."),
];

module.exports = { content };
