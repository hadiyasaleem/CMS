const { Paragraph, TextRun, HeadingLevel, table, tableCaption } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const content = [
  new Paragraph({ text: "4.2 External APIs/SDKs", heading: HeadingLevel.HEADING_2, spacing: { after: 120 } }),
  para("Everything below is a real dependency the apps build against today, not an aspirational list — versions match 1.9's Tools and Technologies table."),
  tableCaption("Table 4-1  Details of APIs/SDKs used in the project"),
  table(
    ["API / SDK", "Purpose", "Used in"],
    [
      ["Supabase Postgrest-kt", "Typed REST client over the Postgres schema — every Repository's read/write path.", "cmscommon/data/repository/*.kt"],
      ["Supabase Auth-kt (GoTrue)", "Sign-in, session/token refresh, password reset.", "SessionManager, RoleResolver"],
      ["Supabase Storage-kt", "Upload/download for exam papers and documents.", "ExamPaperSubmissionRepositoryImpl, DocumentRepositoryImpl"],
      ["Supabase Functions-kt", "Invokes Edge Functions for service-role-only actions (create teacher account, change teacher status).", "AdminUserProvisioner"],
      ["Ktor (OkHttp engine)", "The HTTP client every Supabase-kt module runs on.", "Configured once in SupabaseModule (Hilt)"],
      ["kotlinx.serialization", "JSON (de)serialization for every DTO.", "All *Dto.kt classes"],
      ["Room + Room-ktx", "Local offline cache + DAOs for roster/timetable/attendance.", "cmscommon/data/local/*"],
      ["Hilt", "Dependency injection across all three apps and the shared module.", "*Module.kt, @HiltViewModel classes"],
      ["Jetpack Compose + Material3", "All UI in all three apps.", "Every feature/*/*.kt screen"],
    ],
    [2600, 4160, 2600],
  ),
];

module.exports = { content };
