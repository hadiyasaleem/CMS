const { Paragraph, TextRun, HeadingLevel, table, tableCaption } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const content = [
  new Paragraph({ text: "1.9 Tools and Technologies", heading: HeadingLevel.HEADING_2, spacing: { after: 120 } }),
  para("Versions below are pinned in the project's Gradle version catalog (gradle/libs.versions.toml), so they reflect what the codebase actually builds against, not an aspirational list."),
  tableCaption("Table 1-2  Tools and Technologies for Proposed Project"),
  table(
    ["Tool / Technology", "Version", "Rationale"],
    [
      ["Kotlin", "2.1.0", "Primary language for all three apps and the shared module."],
      ["Jetpack Compose (BOM)", "2024.12.01", "Declarative UI toolkit; lets all three apps share one component library instead of duplicating XML layouts."],
      ["Hilt", "2.52", "Dependency injection across ViewModels/Repositories."],
      ["Room", "2.6.1", "Local offline cache for high-read-volume data (roster, timetable, attendance) with a background SyncEngine."],
      ["Supabase Kotlin SDK (BOM)", "3.1.4", "Postgrest (database), Auth/GoTrue, Storage, Functions, Realtime — the entire backend client."],
      ["Ktor", "3.1.1", "HTTP engine the Supabase SDK runs on."],
      ["kotlinx.serialization", "1.8.0", "JSON (de)serialization for every DTO exchanged with Postgrest."],
      ["Backend: Postgres + Row-Level Security", "Supabase-managed", "Enforces per-role read/write access at the database layer instead of in application code."],
      ["Android Studio", "current stable", "IDE for all development."],
      ["Git", "—", "Version control across the monorepo (3 apps + shared module + Supabase migrations)."],
    ],
    [3000, 1800, 4560],
  ),
];

module.exports = { content };
