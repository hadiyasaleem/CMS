const {
  Document, Packer, Paragraph, TextRun, HeadingLevel, Table, TableRow, TableCell,
  WidthType, AlignmentType, PageBreak, BorderStyle, ShadingType, LevelFormat,
  VerticalAlign, TabStopType, TabStopPosition, ImageRun,
} = require("docx");
const fs = require("fs");

// ---------- shared helpers ----------
const FULL_W = 9360; // usable width in DXA at 1" margins on A4-ish page (approx)

function h1(text) {
  return new Paragraph({ text, heading: HeadingLevel.HEADING_1, spacing: { before: 240, after: 120 } });
}
function h2(text) {
  return new Paragraph({ text, heading: HeadingLevel.HEADING_2, spacing: { before: 200, after: 100 } });
}
function h3(text) {
  return new Paragraph({ text, heading: HeadingLevel.HEADING_3, spacing: { before: 160, after: 80 } });
}
function p(text, opts = {}) {
  return new Paragraph({ children: [new TextRun({ text, ...opts })], spacing: { after: 160 } });
}
function pRuns(runs, opts = {}) {
  return new Paragraph({ children: runs, spacing: { after: 160 }, ...opts });
}
function italic(text) { return new TextRun({ text, italics: true }); }
function bold(text) { return new TextRun({ text, bold: true }); }
function placeholder(text) {
  return new Paragraph({
    children: [new TextRun({ text: `[${text}]`, italics: true, color: "C00000" })],
    spacing: { after: 160 },
  });
}
function bullet(text) {
  return new Paragraph({ text, bullet: { level: 0 }, spacing: { after: 60 } });
}
function chapterBreakPage(chapterNo, chapterTitle) {
  return [
    new Paragraph({ children: [new PageBreak()] }),
    new Paragraph({ text: `Chapter ${chapterNo}`, spacing: { before: 2000, after: 0 } }),
    new Paragraph({ text: chapterTitle, heading: HeadingLevel.TITLE, spacing: { after: 4000 } }),
    new Paragraph({ children: [new PageBreak()] }),
  ];
}
function cell(text, opts = {}) {
  return new TableCell({
    width: opts.width ? { size: opts.width, type: WidthType.DXA } : undefined,
    shading: opts.header ? { type: ShadingType.CLEAR, fill: "D9D9D9" } : undefined,
    verticalAlign: VerticalAlign.CENTER,
    margins: { top: 80, bottom: 80, left: 100, right: 100 },
    children: [new Paragraph({
      children: [new TextRun({ text, bold: !!opts.header })],
    })],
  });
}
function table(headerCells, rows, colWidths) {
  const total = colWidths.reduce((a, b) => a + b, 0);
  return new Table({
    width: { size: total, type: WidthType.DXA },
    columnWidths: colWidths,
    rows: [
      new TableRow({ children: headerCells.map((t, i) => cell(t, { header: true, width: colWidths[i] })) }),
      ...rows.map(r => new TableRow({ children: r.map((t, i) => cell(t, { width: colWidths[i] })) })),
    ],
  });
}
function frTable(fr) {
  // fr = {id, title, requirement, source, rationale, businessRule, dependencies, priority}
  const rowsData = [
    ["Identifier", fr.id],
    ["Title", fr.title],
    ["Requirement", fr.requirement],
    ["Source", fr.source],
    ["Rationale", fr.rationale],
    ["Business Rule", fr.businessRule || "N/A"],
    ["Dependencies", fr.dependencies || "None"],
    ["Priority", fr.priority],
  ];
  return new Table({
    width: { size: 9360, type: WidthType.DXA },
    columnWidths: [2200, 7160],
    rows: rowsData.map(([k, v]) => new TableRow({
      children: [
        cell(k, { header: true, width: 2200 }),
        cell(v, { width: 7160 }),
      ],
    })),
  });
}

function testCaseTable(tc) {
  // tc = {id, requirementId, title, description, objective, precondition, steps, input, expected}
  const rowsData = [
    ["Testcase ID", tc.id],
    ["Requirement ID", tc.requirementId],
    ["Title", tc.title],
    ["Description", tc.description],
    ["Objective", tc.objective],
    ["Driver/precondition", tc.precondition],
    ["Test steps", tc.steps],
    ["Input", tc.input],
    ["Expected Results", tc.expected],
    ["Actual Result", "Pending execution — not yet run on a device/emulator"],
    ["Remarks", "Pending"],
  ];
  return new Table({
    width: { size: 9360, type: WidthType.DXA },
    columnWidths: [2200, 7160],
    rows: rowsData.map(([k, v]) => new TableRow({
      children: [
        cell(k, { header: true, width: 2200 }),
        cell(v, { width: 7160 }),
      ],
    })),
  });
}

function useCaseTable(uc) {
  // uc = {id, name, actors, description, trigger, preconditions, postconditions,
  //       normalFlow, alternativeFlows, businessRules, assumptions}
  const rowsData = [
    ["Use Case ID", uc.id],
    ["Use Case Name", uc.name],
    ["Actors", uc.actors],
    ["Description", uc.description],
    ["Trigger", uc.trigger],
    ["Preconditions", uc.preconditions],
    ["Postconditions", uc.postconditions],
    ["Normal Flow", uc.normalFlow],
    ["Alternative Flows", uc.alternativeFlows],
    ["Business Rules", uc.businessRules],
    ["Assumptions", uc.assumptions],
  ];
  return new Table({
    width: { size: 9360, type: WidthType.DXA },
    columnWidths: [2200, 7160],
    rows: rowsData.map(([k, v]) => new TableRow({
      children: [
        cell(k, { header: true, width: 2200 }),
        cell(v, { width: 7160 }),
      ],
    })),
  });
}

function tableCaption(text) {
  return new Paragraph({
    children: [new TextRun({ text, bold: true, italics: true })],
    alignment: AlignmentType.CENTER,
    spacing: { after: 80 },
  });
}

module.exports = {
  Document, Packer, Paragraph, TextRun, HeadingLevel, Table, TableRow, TableCell,
  WidthType, AlignmentType, PageBreak, BorderStyle, ShadingType, LevelFormat,
  VerticalAlign, ImageRun, fs,
  h1, h2, h3, p, pRuns, italic, bold, placeholder, bullet, chapterBreakPage,
  cell, table, frTable, testCaseTable, useCaseTable, tableCaption, FULL_W,
};
