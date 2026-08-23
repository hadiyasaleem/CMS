const { Paragraph, TextRun, HeadingLevel, PageBreak } = require("../generate_report.js");

function item(n, text) {
  return new Paragraph({ children: [new TextRun({ text: `${n}. ${text}` })], spacing: { after: 100 } });
}

const content = [
  new Paragraph({ children: [new PageBreak()] }),
  new Paragraph({ text: "Appendix B", spacing: { before: 2000, after: 0 } }),
  new Paragraph({ text: "Coding Standards", heading: HeadingLevel.TITLE, spacing: { after: 4000 } }),
  new Paragraph({ children: [new PageBreak()] }),

  new Paragraph({ text: "Appendix-B  General Coding Standards & Guidelines", heading: HeadingLevel.HEADING_1, spacing: { after: 160 } }),
  new Paragraph({
    children: [new TextRun({ text: "These are the conventions actually followed across cmsadmin, cmsteacher, cmsstudent, and the shared cmscommon module — not a generic checklist, but what the codebase does in practice." })],
    spacing: { after: 160 },
  }),

  item(1, "Naming: camelCase for variables, properties, and function names (rollNumber, syncSession()); PascalCase for classes, interfaces, and Composable functions (SessionMarksRepository, MarksEntryScreen); SCREAMING_SNAKE_CASE only for true compile-time constants (CMS_DATABASE_VERSION)."),
  item(2, "Suffix conventions carry meaning and are used consistently: *Repository (interface) / *RepositoryImpl (implementation), *Dto (a Postgrest row shape), *Entity (a Room table row), *ViewModel, *Screen. A reader can tell a type's role from its name alone without opening the file."),
  item(3, "One class/interface per file, filename matching the type name (SessionFeeRepository in SessionFeeRepository.kt) — the one common exception is small, tightly-related DTOs or sealed-class variants grouped in one *Dtos.kt or *Enums.kt file when splitting them out would add navigation overhead for no real benefit."),
  item(4, "Dependency injection is Hilt throughout: @HiltViewModel + @Inject constructor for ViewModels, @Binds in a *Module.kt for interface-to-implementation wiring, @Provides for anything needing manual construction (Room database instances, Supabase client). No manual service-locator pattern anywhere in the codebase."),
  item(5, "Repositories are the only layer allowed to know about DTOs, table names, or Postgrest/Room APIs — a ViewModel calls a Repository method and gets back a domain model, never a raw row shape. This is enforced by convention/code review, not a compiler check."),
  item(6, "Comments explain the non-obvious, not the obvious: a comment exists to record a hidden constraint, a subtle invariant, or the reason behind a workaround (see the DTO-default-collision note in AttendanceRowDto, 3.7) — not to restate what a well-named function already says. No boilerplate KDoc blocks on self-explanatory getters."),
  item(7, "Kotlin's own formatting conventions (4-space indent, trailing commas in multi-line argument lists, expression-body functions where they fit on one line) are followed via the default Android Studio/Kotlin formatter — no custom style config beyond that."),
  item(8, "Package structure mirrors architecture, not feature-of-the-week: domain/model, domain/repository, data/remote/dto, data/repository, data/local (entity/dao) sit in cmscommon; each app's own feature/<name>/ folder holds that feature's Screen + ViewModel together."),
  item(9, "Errors surface to the user rather than being silently swallowed — Repository calls that can fail are wrapped in runCatching at the ViewModel boundary and turned into an Outcome.Error the UI actually displays, not a logged-and-ignored exception."),
  item(10, "Follow DRY where it's genuinely the same logic twice, not where it merely looks similar: shared UI (LedgerCard, HubNavCard, SegmentToggle) and shared data-access patterns (delete-then-insert for wholesale replacement, described in 3.7) are factored into cmscommon once; three near-identical but independently-evolving screens (like each app's own Datesheets screen) are left separate rather than forced into one over-parameterized component."),
  item(11, "Row-Level Security is the actual access-control boundary (2.4.4) — application code is written as if a malicious or buggy client could bypass every UI check, because RLS, not the app, is what actually enforces who can read or write a row."),
];

module.exports = { content };
