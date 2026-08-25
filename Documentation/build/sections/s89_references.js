const { Paragraph, TextRun, HeadingLevel, PageBreak } = require("../generate_report.js");

function ref(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 120 } });
}

const content = [
  new Paragraph({ children: [new PageBreak()] }),
  new Paragraph({ text: "References", heading: HeadingLevel.HEADING_1, spacing: { after: 200 } }),

  ref("Supabase. \"Row Level Security.\" Internet: https://supabase.com/docs/guides/database/postgres/row-level-security, [accessed 2026]."),
  ref("Supabase. \"Postgrest, Auth, Storage, and Edge Functions.\" Internet: https://supabase.com/docs, [accessed 2026]."),
  ref("Supabase. \"supabase-kt — Kotlin client for Supabase.\" Internet: https://github.com/supabase-community/supabase-kt, [accessed 2026]."),
  ref("Android Developers. \"Jetpack Compose.\" Internet: https://developer.android.com/develop/ui/compose, [accessed 2026]."),
  ref("Android Developers. \"Room persistence library.\" Internet: https://developer.android.com/training/data-storage/room, [accessed 2026]."),
  ref("Android Developers. \"Guide to app architecture (MVVM, ViewModel, Repository).\" Internet: https://developer.android.com/topic/architecture, [accessed 2026]."),
  ref("Google. \"Material Design 3.\" Internet: https://m3.material.io, [accessed 2026]."),
  ref("Google. \"Hilt dependency injection.\" Internet: https://developer.android.com/training/dependency-injection/hilt-android, [accessed 2026]."),
  ref("Kotlin. \"kotlinx.serialization.\" Internet: https://github.com/Kotlin/kotlinx.serialization, [accessed 2026]."),
  ref("PostgreSQL Global Development Group. \"Row Security Policies\" and \"CREATE VIEW (security_invoker).\" Internet: https://www.postgresql.org/docs/current/ddl-rowsecurity.html, [accessed 2026]."),
];

module.exports = { content };
