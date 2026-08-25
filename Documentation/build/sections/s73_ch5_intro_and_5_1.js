const { Paragraph, TextRun, HeadingLevel, PageBreak, testCaseTable, tableCaption } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const content = [
  new Paragraph({ children: [new PageBreak()] }),
  new Paragraph({ text: "Chapter 5", spacing: { before: 2000, after: 0 } }),
  new Paragraph({ text: "Testing and Evaluation", heading: HeadingLevel.TITLE, spacing: { after: 4000 } }),
  new Paragraph({ children: [new PageBreak()] }),

  new Paragraph({ text: "5. Introduction", heading: HeadingLevel.HEADING_1, spacing: { after: 160 } }),
  para("The honest state of testing on this project: every feature described in this report compiles and builds green across all three apps, and has been manually exercised during development. What hasn't happened yet is a structured device/emulator test pass recording actual pass/fail results — the test cases below are fully specified (steps, inputs, expected results) but their Actual Result and Remarks columns say \"Pending execution\" rather than a real outcome, because that's what's true right now. Running these on a real device and filling in real results is the one thing standing between this chapter and a finished testing record."),
  para("Test cases are grouped the same way most of this report is: by the layer of the system they exercise (unit, functional, integration, performance), following the template below for each one."),

  new Paragraph({ text: "5.1 Unit Testing (UT)", heading: HeadingLevel.HEADING_2, spacing: { after: 120 } }),
  para("Unit tests target pure logic with no database or UI involved — the best candidate in this codebase is the live score-validation function (scoreError, 4.1 Algorithm 2), since it's a pure function with clean input/output boundaries."),
  tableCaption("Table 5-1  Testcase UT-1"),
  testCaseTable({
    id: "UT-1",
    requirementId: "FR-6",
    title: "scoreError rejects a negative score",
    description: "Unit test of the live-validation function used in Marks Entry.",
    objective: "Confirm a negative input produces the \"Can't be negative\" message and no other case does.",
    precondition: "None — scoreError is a pure Kotlin function.",
    steps: "1. Call scoreError(\"-5\", 25). 2. Call scoreError(\"5\", 25) as a control case.",
    input: "raw = \"-5\", maxMarks = 25 (and raw = \"5\" for the control case).",
    expected: "First call returns \"Can't be negative\"; control case returns null.",
  }),
  new Paragraph({ spacing: { before: 200 } }),
  tableCaption("Table 5-2  Testcase UT-2"),
  testCaseTable({
    id: "UT-2",
    requirementId: "FR-6",
    title: "scoreError rejects a score above the exam maximum",
    description: "Unit test of the live-validation function used in Marks Entry.",
    objective: "Confirm a value above maxMarks produces \"Max is {maxMarks}\", and a value exactly at the maximum is accepted.",
    precondition: "None.",
    steps: "1. Call scoreError(\"30\", 25). 2. Call scoreError(\"25\", 25) as a boundary case.",
    input: "raw = \"30\", maxMarks = 25 (and raw = \"25\" for the boundary case).",
    expected: "First call returns \"Max is 25\"; boundary case (exactly at max) returns null.",
  }),
];

module.exports = { content };
