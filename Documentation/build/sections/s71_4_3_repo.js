const { Paragraph, TextRun, HeadingLevel, table, tableCaption } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const content = [
  new Paragraph({ text: "4.3 Code Repository", heading: HeadingLevel.HEADING_2, spacing: { after: 120 } }),
  para("Git is the version-control tool for the whole project — all three apps, the shared cmscommon module, and the Supabase migrations/Edge Functions live in one repository."),
  new Paragraph({ children: [new TextRun({ text: "Git Repository Link: ", bold: true }), new TextRun({ text: "https://github.com/hadiyasaleem/CMS" })], spacing: { after: 160 } }),
  new Paragraph({ text: "4.3.1 Metrics of the Git Repository", heading: HeadingLevel.HEADING_3, spacing: { after: 100 } }),
  para("These reflect the state of the repository at the time of writing (git log/git shortlog on the default branch), and will keep changing as work continues."),
  tableCaption("Table 4-2  Metrics of the Git Repository"),
  table(
    ["Metric", "Value"],
    [
      ["Commits", "107"],
      ["Branches (local + remote)", "9"],
      ["Contributors", "Hadia, Sharfa Kiran, Syeda Laraib Qamar Kazmi (some appear under more than one git identity/machine)"],
      ["Latest merged PR", "#20 — Attendance fragment and activity"],
    ],
    [4000, 5360],
  ),
  para("Note on the contributor list: several team members show up under two Git identities (e.g. \"sharfakiran\" and \"Sharfa Kiran\", \"hadiyasaleem\" and \"Hadiya Saleem\") because Git config wasn't consistent across every machine used during development — the commit counts above are real, but attributing them cleanly per person would need a .mailmap rather than a straight git shortlog."),
];

module.exports = { content };
