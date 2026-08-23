const { Paragraph, TextRun, HeadingLevel, bullet } = require("../generate_report.js");

const content = [
  new Paragraph({ text: "1.5.4 Shared Library Module (cmscommon)", heading: HeadingLevel.HEADING_3, spacing: { after: 100 } }),
  bullet("Domain models, DTOs, and Repository interfaces/implementations — the single source of truth for every table each app touches."),
  bullet("Supabase client wiring — Postgrest (database), Auth/GoTrue (sign-in), Storage (uploaded files: exam papers, documents), Edge Functions (admin-privileged actions like creating a teacher account or changing its status)."),
  bullet("Room offline cache + SyncEngine — background sync for high-read-volume data (roster, timetable, attendance); low-volume features (fees, fines, calendar, datesheets, documents, insights) read directly from Postgrest instead."),
  bullet("The “Modernist” Compose design system — shared theme, typography, and components (LedgerCard, HubNavCard, SegmentToggle, StatusBadge, etc.) used identically by all three apps."),
];

module.exports = { content };
