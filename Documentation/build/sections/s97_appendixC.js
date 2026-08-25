const { Paragraph, TextRun, HeadingLevel, PageBreak, ImageRun, AlignmentType, fs } = require("../generate_report.js");
const path = require("path");

const SHOT_DIR = path.join(__dirname, "..", "screenshots");

function shot(filename, caption, w = 220, h = 489) {
  const data = fs.readFileSync(path.join(SHOT_DIR, filename));
  return [
    new Paragraph({
      children: [new ImageRun({ type: "png", data, transformation: { width: w, height: h } })],
      alignment: AlignmentType.CENTER,
      spacing: { after: 40 },
    }),
    new Paragraph({
      children: [new TextRun({ text: caption, italics: true, size: 16 })],
      alignment: AlignmentType.CENTER,
      spacing: { after: 200 },
    }),
  ];
}

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const content = [
  new Paragraph({ children: [new PageBreak()] }),
  new Paragraph({ text: "Appendix C", spacing: { before: 2000, after: 0 } }),
  new Paragraph({ text: "Prototype", heading: HeadingLevel.TITLE, spacing: { after: 4000 } }),
  new Paragraph({ children: [new PageBreak()] }),

  new Paragraph({ text: "Appendix-C  Application Prototype", heading: HeadingLevel.HEADING_1, spacing: { after: 160 } }),
  para("The screenshots below were captured from all three apps' real debug builds running on a physical Android device (Samsung Galaxy A07, USB debugging), signed into real accounts against the live Supabase project — not mockups. They're grouped by app, and each one is chosen to show a feature this report discusses by name rather than a generic tour."),

  new Paragraph({ text: "Admin App", heading: HeadingLevel.HEADING_2, spacing: { before: 200, after: 120 } }),
  ...shot("admin_01.png", "Figure C-1  Admin login screen"),
  ...shot("admin_02_home.png", "Figure C-2  Admin dashboard — live enrolment/faculty/department counts"),
  ...shot("admin_05_session_detail.png", "Figure C-3  Session detail — semester promotion, enrollment, Manage/Curriculum (FR-2)"),
  ...shot("admin_06_fee_structure.png", "Figure C-4  Session fee structure editor (FR-10)"),
  ...shot("admin_09_insights.png", "Figure C-5  Admin Insights — college-wide session/at-risk/exam data (FR-22)"),
  ...shot("admin_11b_teachers_scrolled.png", "Figure C-6  Faculty records — status & permission management (FR-21)"),

  new Paragraph({ text: "Teacher App", heading: HeadingLevel.HEADING_2, spacing: { before: 200, after: 120 } }),
  ...shot("teacher_01_launch.png", "Figure C-7  Teacher home — today's load, quick actions"),
  ...shot("teacher_02_exams_hub.png", "Figure C-8  Teacher Exams hub (Marks Entry, Exam Paper, Semester Results, Datesheets)"),
  ...shot("teacher_04_marks_roster.png", "Figure C-9  Marks Entry — locked scores with edit-request pencil (FR-6)"),
  ...shot("teacher_05_request_edit_dialog.png", "Figure C-10  Request mark change dialog (FR-7)"),
  ...shot("teacher_07_attendance_roster.png", "Figure C-11  Mark Attendance — live at-risk highlighting"),
  ...shot("teacher_09_insights.png", "Figure C-12  Teacher Insights — automatically scoped to their own class only (FR-22)"),

  new Paragraph({ text: "Student App", heading: HeadingLevel.HEADING_2, spacing: { before: 200, after: 120 } }),
  ...shot("student_01_launch.png", "Figure C-13  Student login/register screen"),
  ...shot("student_02_home.png", "Figure C-14  Student home — CGPA gauge, next class, weakest-subject alert"),
  ...shot("student_03_exams_hub.png", "Figure C-15  Student Exams hub (Marks, Results, Datesheets)"),
  ...shot("student_04_marks.png", "Figure C-16  Student Marks screen"),
  ...shot("student_06_fee_challan.png", "Figure C-17  Student Fee Challan (FR-11)"),
];

module.exports = { content };
