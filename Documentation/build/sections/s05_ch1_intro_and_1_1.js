const { Paragraph, TextRun, HeadingLevel, PageBreak, AlignmentType } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const content = [
  // Chapter divider page
  new Paragraph({ children: [new PageBreak()] }),
  new Paragraph({ text: "Chapter 1", spacing: { before: 2000, after: 0 } }),
  new Paragraph({ text: "Introduction", heading: HeadingLevel.TITLE, spacing: { after: 4000 } }),
  new Paragraph({ children: [new PageBreak()] }),

  new Paragraph({ text: "1. Introduction", heading: HeadingLevel.HEADING_1, spacing: { after: 160 } }),
  para("This chapter lays out why the College Management System (CMS) exists, what it is meant to achieve, and how it is built. It covers the problem the college was actually living with, the three-app solution we built for it, the concrete objectives that solution targets, its scope, the modules that make it up, how it compares to systems like it, and the tools that went into building it."),

  new Paragraph({ text: "1.1 Problem Statement", heading: HeadingLevel.HEADING_2, spacing: { after: 120 } }),
  para("Govt. Graduate College, Mandi Bahauddin has roughly 3,000 students enrolled across its Intermediate and Bachelor's programmes, taught by about 50 faculty members, and every one of its academic and administrative processes is still run on paper. Fee challans are written out by hand at the accounts office, which means a student cannot get one without physically visiting during office hours and waiting in a queue. Attendance is marked in a physical register per class, then manually tallied at the end of the term to decide who meets the minimum-attendance requirement for exams — a process that is slow and open to arithmetic error. Midterm and sessional marks are recorded on paper sheets before being compiled into a semester result and GPA/CGPA by hand, with no independent check on that compilation beyond the teacher's and admin's own diligence. Exam datesheets and college notices are posted on physical noticeboards, so a student who does not visit campus on the right day may simply miss them."),
  para("None of this is a single catastrophic failure — it is the ordinary way many colleges of this size and resourcing operate. But it accumulates: a misread number on a marks sheet becomes a wrong CGPA on a transcript; a fee challan written from memory can disagree with what the accounts office has on record; an attendance shortfall discovered only at the end of the semester leaves no time to correct it. And every one of these processes requires the student or teacher to be physically present at the right office at the right time, which is the part that does not scale as enrolment grows. The problem this project addresses is exactly that: how does a college of this size keep its records accurate, timely, and reachable from outside the campus, without expanding its administrative staff to match?"),
];

module.exports = { content };
