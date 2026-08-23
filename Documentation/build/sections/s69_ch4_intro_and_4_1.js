const { Paragraph, TextRun, HeadingLevel, PageBreak, Table, TableRow, TableCell, WidthType, ShadingType } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}
function mono(text, bold) {
  return new Paragraph({ children: [new TextRun({ text, font: "Consolas", size: 18, bold: !!bold })], spacing: { after: 40 } });
}
function algoBox(titleText, lines) {
  return new Table({
    width: { size: 9360, type: WidthType.DXA },
    columnWidths: [9360],
    rows: [
      new TableRow({ children: [new TableCell({
        shading: { type: ShadingType.CLEAR, fill: "D9D9D9" },
        margins: { top: 60, bottom: 60, left: 100, right: 100 },
        children: [new Paragraph({ children: [new TextRun({ text: titleText, bold: true })] })],
      })] }),
      new TableRow({ children: [new TableCell({
        margins: { top: 100, bottom: 100, left: 100, right: 100 },
        children: lines.map(l => mono(l)),
      })] }),
    ],
  });
}

const content = [
  new Paragraph({ children: [new PageBreak()] }),
  new Paragraph({ text: "Chapter 4", spacing: { before: 2000, after: 0 } }),
  new Paragraph({ text: "Implementation", heading: HeadingLevel.TITLE, spacing: { after: 4000 } }),
  new Paragraph({ children: [new PageBreak()] }),

  new Paragraph({ text: "4. Implementation", heading: HeadingLevel.HEADING_1, spacing: { after: 160 } }),
  para("This chapter is about what's actually running, not the code itself — pseudocode for the pieces worth walking through, the third-party SDKs the apps depend on, and the state of the Git repository the three apps and the shared module live in."),

  new Paragraph({ text: "4.1 Algorithm", heading: HeadingLevel.HEADING_2, spacing: { after: 120 } }),
  para("Two pieces of logic are worth showing as pseudocode rather than just describing in prose: recording a semester result (the one place GPA/CGPA actually gets written), and the live score-validation check that runs on every keystroke in Marks Entry (2.3.6)."),
  new Paragraph({ children: [new TextRun({ text: "Algorithm 1  record_semester_result (Postgres RPC, SECURITY DEFINER)", italics: true })], spacing: { after: 80 } }),
  algoBox("record_semester_result(session, roll, semester, gpa, cgpa, term_label, result, class_position, remarks, supply[])", [
    "Input: session id, roll number, semester number, gpa, cgpa, optional term label/result/",
    "       position/remarks, optional supply-subject list",
    "Output: none (writes two tables)",
    "",
    "1:  if NOT (caller is_admin() OR caller teaches(session)) then",
    "2:      raise exception 'not allowed'",
    "3:  upsert student_semester_gpa row keyed on (session, roll, semester)",
    "4:      on conflict: overwrite gpa/cgpa/term_label/result/position/remarks/supply",
    "5:  update session_students.gpa, .cgpa for (session, roll)",
    "6:      WHERE no row exists in student_semester_gpa for this student with a HIGHER",
    "7:            semester number than the one just recorded",
    "8:  -- guarantees the snapshot always reflects the LATEST completed semester,",
    "9:  --   even if an earlier semester is (re-)recorded out of order",
  ]),
  new Paragraph({ children: [new TextRun({ text: "Algorithm 2  scoreError(raw, maxMarks) — live validation in Marks Entry", italics: true })], spacing: { before: 200, after: 80 } }),
  algoBox("scoreError(raw: String, maxMarks: Int): String?", [
    "Input: raw text currently in the score field, the exam type's max marks",
    "Output: an error message to show, or null if the field is valid so far",
    "",
    "1:  if raw is blank then return null        -- empty is not yet an error",
    "2:  n <- raw.trim().toIntOrNull()",
    "3:  if n is null then return \"Numbers only\"",
    "4:  if n < 0 then return \"Can't be negative\"",
    "5:  if n > maxMarks then return \"Max is {maxMarks}\"",
    "6:  return null",
  ]),
];

module.exports = { content };
