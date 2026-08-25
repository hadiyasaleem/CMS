const {
  Paragraph, TextRun, HeadingLevel, PageBreak, AlignmentType, table,
} = require("../generate_report.js");

const titlePage = [
  new Paragraph({ text: "Final Year Design Project", alignment: AlignmentType.CENTER, spacing: { before: 800, after: 400 } }),
  new Paragraph({
    children: [new TextRun({ text: "College Management System", bold: true, size: 32 })],
    alignment: AlignmentType.CENTER, spacing: { after: 200 },
  }),
  new Paragraph({
    children: [new TextRun({ text: "for Govt. Graduate College, Mandi Bahauddin", italics: true, size: 26 })],
    alignment: AlignmentType.CENTER, spacing: { after: 800 },
  }),
  new Paragraph({ children: [new TextRun({ text: "By", bold: true })], alignment: AlignmentType.CENTER, spacing: { after: 300 } }),
  new Paragraph({ children: [new TextRun({ text: "Hadia" })], alignment: AlignmentType.CENTER, spacing: { after: 60 } }),
  new Paragraph({ children: [new TextRun({ text: "085668" })], alignment: AlignmentType.CENTER, spacing: { after: 200 } }),
  new Paragraph({ children: [new TextRun({ text: "Sharfa Kiran" })], alignment: AlignmentType.CENTER, spacing: { after: 60 } }),
  new Paragraph({ children: [new TextRun({ text: "085646" })], alignment: AlignmentType.CENTER, spacing: { after: 200 } }),
  new Paragraph({ children: [new TextRun({ text: "Syeda Laraib Qamar Kazmi" })], alignment: AlignmentType.CENTER, spacing: { after: 60 } }),
  new Paragraph({ children: [new TextRun({ text: "085713" })], alignment: AlignmentType.CENTER, spacing: { after: 600 } }),
  new Paragraph({ children: [new TextRun({ text: "Under the supervision of", italics: true })], alignment: AlignmentType.CENTER, spacing: { after: 100 } }),
  new Paragraph({ children: [new TextRun({ text: "Prof. Ubaid Ullah", bold: true })], alignment: AlignmentType.CENTER, spacing: { after: 600 } }),
  new Paragraph({
    children: [new TextRun({ text: "Bachelor of Science in Information Technology (2022-2026)", bold: true, italics: true })],
    alignment: AlignmentType.CENTER, spacing: { after: 300 },
  }),
  new Paragraph({ children: [new TextRun({ text: "DEPARTMENT OF INFORMATION TECHNOLOGY", bold: true })], alignment: AlignmentType.CENTER, spacing: { after: 100 } }),
  new Paragraph({ children: [new TextRun({ text: "GOVT. GRADUATE COLLEGE, MANDI BAHAUDDIN", bold: true })], alignment: AlignmentType.CENTER }),
  new Paragraph({ children: [new PageBreak()] }),
];

const innerTitlePage = [
  new Paragraph({
    children: [new TextRun({ text: "College Management System for Govt. Graduate College, Mandi Bahauddin", bold: true })],
    alignment: AlignmentType.CENTER, spacing: { after: 400 },
  }),
  new Paragraph({ children: [new TextRun({ text: "A project presented to" })], alignment: AlignmentType.CENTER, spacing: { after: 60 } }),
  new Paragraph({ children: [new TextRun({ text: "Govt. Graduate College, Mandi Bahauddin" })], alignment: AlignmentType.CENTER, spacing: { after: 400 } }),
  new Paragraph({ children: [new TextRun({ text: "In partial fulfilment" })], alignment: AlignmentType.CENTER, spacing: { after: 60 } }),
  new Paragraph({ children: [new TextRun({ text: "of the requirement for the degree of" })], alignment: AlignmentType.CENTER, spacing: { after: 400 } }),
  new Paragraph({
    children: [new TextRun({ text: "Bachelor of Science in Information Technology (2022-2026)", bold: true, italics: true })],
    alignment: AlignmentType.CENTER, spacing: { after: 400 },
  }),
  new Paragraph({ children: [new TextRun({ text: "By" })], alignment: AlignmentType.CENTER, spacing: { after: 200 } }),
  new Paragraph({ children: [new TextRun({ text: "Hadia" })], alignment: AlignmentType.CENTER, spacing: { after: 60 } }),
  new Paragraph({ children: [new TextRun({ text: "085668" })], alignment: AlignmentType.CENTER, spacing: { after: 200 } }),
  new Paragraph({ children: [new TextRun({ text: "Sharfa Kiran" })], alignment: AlignmentType.CENTER, spacing: { after: 60 } }),
  new Paragraph({ children: [new TextRun({ text: "085646" })], alignment: AlignmentType.CENTER, spacing: { after: 200 } }),
  new Paragraph({ children: [new TextRun({ text: "Syeda Laraib Qamar Kazmi" })], alignment: AlignmentType.CENTER, spacing: { after: 60 } }),
  new Paragraph({ children: [new TextRun({ text: "085713" })], alignment: AlignmentType.CENTER, spacing: { after: 600 } }),
  new Paragraph({ children: [new TextRun({ text: "DEPARTMENT OF INFORMATION TECHNOLOGY", bold: true })], alignment: AlignmentType.CENTER, spacing: { after: 100 } }),
  new Paragraph({ children: [new TextRun({ text: "GOVT. GRADUATE COLLEGE, MANDI BAHAUDDIN", bold: true })], alignment: AlignmentType.CENTER }),
  new Paragraph({ children: [new PageBreak()] }),
];

const declarationPage = [
  new Paragraph({ text: "DECLARATION", heading: HeadingLevel.HEADING_1, alignment: AlignmentType.CENTER, spacing: { after: 400 } }),
  new Paragraph({
    children: [new TextRun({ text: "We hereby declare that this software, neither whole nor as a part, has been copied out from any source. It is further declared that we have developed this software and accompanied report entirely on the basis of our personal efforts. If any part of this project is proved to be copied out from any source or found to be reproduction of some other, we will stand by the consequences. No portion of the work presented has been submitted as part of any application for any other degree or qualification of this or any other university or institute of learning." })],
    spacing: { after: 160 },
  }),
  new Paragraph({ spacing: { before: 600, after: 200 } }),
  new Paragraph({
    tabStops: [{ type: "right", position: 8000 }],
    children: [
      new TextRun({ text: "Signature: ___________________" }),
      new TextRun({ text: "\tSignature: ___________________" }),
    ],
  }),
  new Paragraph({
    tabStops: [{ type: "right", position: 8000 }],
    spacing: { after: 400 },
    children: [
      new TextRun({ text: "Hadia [085668]" }),
      new TextRun({ text: "\tSharfa Kiran [085646]" }),
    ],
  }),
  new Paragraph({ children: [new TextRun({ text: "Signature: ___________________" })] }),
  new Paragraph({ children: [new TextRun({ text: "Syeda Laraib Qamar Kazmi [085713]" })] }),
  new Paragraph({ children: [new PageBreak()] }),
];

const certificatePage = [
  new Paragraph({ text: "CERTIFICATE OF APPROVAL", heading: HeadingLevel.HEADING_1, alignment: AlignmentType.CENTER, spacing: { after: 400 } }),
  new Paragraph({
    children: [new TextRun({ text: "It is to certify that the final year design project (FYDP) of BSIT, session (2022-2026), titled “College Management System for Govt. Graduate College, Mandi Bahauddin” was developed by Hadia (085668), Sharfa Kiran (085646), and Syeda Laraib Qamar Kazmi (085713) under the supervision of Prof. Ubaid Ullah; in my opinion, it is fully adequate, in scope and quality, for the degree of Bachelor of Science in Information Technology." })],
    spacing: { after: 160 },
  }),
  new Paragraph({ spacing: { before: 400 }, children: [new TextRun({ text: "Signature: ___________________________________" })] }),
  new Paragraph({ children: [new TextRun({ text: "FYDP Advisor: Prof. Ubaid Ullah", bold: true })], spacing: { after: 400 } }),
  new Paragraph({ children: [new TextRun({ text: "Signatures (Faculty Advisory Committee – FAC)", bold: true })], spacing: { after: 100 } }),
  table(
    ["", "FAC1", "FAC2"],
    [["Name", "Prof. Muhammad Faiyaz", "Prof. Ubaid Ullah"], ["Signature", "", ""]],
    [1600, 3880, 3880],
  ),
  new Paragraph({ spacing: { before: 400 }, children: [new TextRun({ text: "Signature: ___________________________________" })] }),
  new Paragraph({ children: [new TextRun({ text: "Head of FYDP Coordination Office: Prof. Muhammad Faiyaz", bold: true })], spacing: { after: 400 } }),
  new Paragraph({
    tabStops: [{ type: "right", position: 8000 }],
    children: [
      new TextRun({ text: "Signature: ___________________________________" }),
      new TextRun({ text: "\tDated: ______________" }),
    ],
  }),
  new Paragraph({ children: [new TextRun({ text: "Head of Department, Information Technology: Prof. Muhammad Faiyaz", bold: true })] }),
  new Paragraph({ children: [new PageBreak()] }),
];

module.exports = { content: [...titlePage, ...innerTitlePage, ...declarationPage, ...certificatePage] };
