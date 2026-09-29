# Documentation Rewrite Tasks — CMS FYP Report

Source document: `Documentation/CMS_FYP_Report.a4.pdf` (50 pages)
Template of record: `Documentation/Manual_Template v 6.1.pdf` (University of Gujrat, Manual Template v6.1)
Repository under documentation: `D:\CMS` (Kotlin multi-module — six apps + Supabase backend)

This file is the backlog for rewriting the report section by section. **One task per heading, down to h3 level.** Each task is self-contained and executable on its own.

---

## The job (applies to every task)

Every task carries the same three-part instruction. Do all three; they are not optional.

1. **Humanize the writing.** Make each section read like a person who built the system wrote it — plain, direct, specific. Kill template boilerplate, hedging, and the "generated" cadence (no "In today's fast-paced world", no tricolon padding, no restating the heading as the first sentence). Use the `humanize` skill before drafting anything longer than a paragraph.
2. **Update paragraphs against the current repository.** Verify every factual claim against the code as it exists now in `D:\CMS` — module names, table names, RLS rules, screens, versions, counts. If the report says something the code no longer does, fix the report to match the code. Cite the real artifact (`supabase/migrations/*.sql`, a ViewModel, `gradle/libs.versions.toml`, `CONTRIBUTORS.md`) rather than repeating the report's own prose.
3. **Don't write it as a chronological history.** Describe the system as it *is*, not the path it took to get there. No "we first used Firebase, then migrated to Supabase", no "originally in XML, later Compose". State the present design and its reasons. (The Executive Summary is the single allowed exception: it may name the architectural change once, because it already does and it is load-bearing there.)

**Definition of done for a task:** the section reads naturally, every claim in it is true of the current codebase, and it contains no change-log narration.

---

## Status summary

| Metric | Count |
|---|---|
| **Total tasks generated** | **138** |
| Executed | 138 |
| Pending | 0 |
| Author-only (excluded from execution) | 1 — Acknowledgement |

Status legend: `⬜ Pending` · `🔄 In progress` · `✅ Executed` · `⏭️ Skipped/N-A`

Breakdown by chapter:

| Group | Tasks | Executed | Pending |
|---|---|---|---|
| Front matter | 1 | 1 | 0 |
| Ch 1 — Introduction | 19 | 19 | 0 |
| Ch 2 — Requirements Analysis | 52 | 52 | 0 |
| Ch 3 — Design and Architecture | 27 | 27 | 0 |
| Ch 4 — Implementation | 17 | 17 | 0 |
| Ch 5 — Testing and Evaluation | 6 | 6 | 0 |
| Ch 6 — System Conversion | 8 | 8 | 0 |
| Ch 7 — Conclusion | 5 | 5 | 0 |
| Appendices A–C | 3 | 3 | 0 |
| **Total** | **138** | **138** | **0** |

> **Repo-drift flags** — sections most likely to be stale, because a migration or recent commit changed the feature. Prioritise verification here: **FR-17 / exam papers** (`20260910130000_exam_paper_rework.sql`, `20260905…exam_paper_review_columns`), **FR-18–19 / datesheet** (`20260908120000_datesheet_rework.sql`), **FR-21 / fee challan** (recent commit "Rework fee challan… 3-copy PDF export"), **FR-25–26 / documents** (`20260827190000_drop_documents_feature_table.sql` — ⚠️ VERIFIED via T-FM1: the Documents feature (prospectus/rules/report uploads) was **removed entirely**, not merely moved to storage-only; Room drops its cache in `CmsDatabaseMigrations.kt` MIGRATION_30_31. FR-25/FR-26 and §3.6.5 should likely be dropped or rewritten as removed), **FR-31 / insights** (`20260905000005_drop_unused_insights_views.sql`), **3.3.x / rooms** (`20260905120000_buildings_and_rooms.sql` — rooms/buildings are picker lookup tables; timetable room stays free text. NOTE per T-2.3.8: no-double-booking is a BEFORE trigger, **not** a GiST/timerange exclusion constraint — those were dropped; do not describe it as timerange anywhere in Ch 3 data design).

---

## Content-type key (last column of each table)

- **Prose** — narrative section; rewrite in full.
- **Table** — the section is built around a table; verify the table's data against the repo, then humanize the framing prose.
- **Diagram** — the section anchors a figure; confirm the figure still matches the code, fix the caption and surrounding text.
- **List** — bulleted/numbered content; verify items, tighten wording.

---

## Front matter

| ID | Heading | Lvl | Verify against | Type | Status |
|---|---|---|---|---|---|
| T-FM1 | Executive Summary | h1 | `supabase/README.md`, `design.md`, `settings.gradle.kts` (six modules), overall system | Prose | ✅ Executed → `rewrite/T-FM1.md` |
| — | Acknowledgement | h1 | *Author-only placeholder — authors write this themselves; not an executable rewrite.* | Prose | ⏭️ N/A |

---

## Chapter 1 — Introduction

| ID | Heading | Lvl | Verify against | Type | Status |
|---|---|---|---|---|---|
| T-1.0 | 1. Introduction (chapter intro) | h1 | Whole system scope; six modules in `settings.gradle.kts` | Prose | ✅ Executed → `rewrite/T-1.0.md` |
| T-1.1 | 1.1 Problem Statement | h2 | Manual/paper baseline described; no repo claim to check, keep factual | Prose | ✅ Executed → `rewrite/T-1.1.md` |
| T-1.2 | 1.2 Problem Solution | h2 | `design.md`, three roles × two platforms, shared DB | Prose | ✅ Executed → `rewrite/T-1.2.md` |
| T-1.3 | 1.3 Objectives of the Proposed System | h2 | Objectives must be measurable and match delivered features | List/Prose | ✅ Executed → `rewrite/T-1.3.md` |
| T-1.4 | 1.4 Scope | h2 | Module set; what is in vs out of scope today | Prose | ✅ Executed → `rewrite/T-1.4.md` |
| T-1.5 | 1.5 System Components | h2 | `settings.gradle.kts` — six apps + `core`/`mobile-shared`/`desktop-shared` | Prose | ✅ Executed → `rewrite/T-1.5.md` |
| T-1.5.1 | 1.5.1 Admin Mobile Application | h3 | `mobile-admin/` | List | ✅ Executed → `rewrite/T-1.5.1.md` |
| T-1.5.2 | 1.5.2 Admin Desktop Application | h3 | `desktop-admin/` | List | ✅ Executed → `rewrite/T-1.5.2.md` |
| T-1.5.3 | 1.5.3 Teacher Mobile Application | h3 | `mobile-teacher/` | List | ✅ Executed → `rewrite/T-1.5.3.md` |
| T-1.5.4 | 1.5.4 Teacher Desktop Application | h3 | `desktop-teacher/` | List | ✅ Executed → `rewrite/T-1.5.4.md` |
| T-1.5.5 | 1.5.5 Student Mobile Application | h3 | `mobile-student/` | List | ✅ Executed → `rewrite/T-1.5.5.md` |
| T-1.5.6 | 1.5.6 Student Desktop Application | h3 | `desktop-student/` | List | ✅ Executed → `rewrite/T-1.5.6.md` |
| T-1.6 | 1.6 Related System Analysis / Literature Review | h2 | External systems (Table 1-1); no repo claim, keep ≤4 sentences each | Table | ✅ Executed → `rewrite/T-1.6.md` |
| T-1.7 | 1.7 Vision Statement | h2 | Keyword-template vision; align with objectives | Prose | ✅ Executed → `rewrite/T-1.7.md` |
| T-1.8 | 1.8 System Limitations and Constraints | h2 | `design.md`, platform/tooling constraints (Android min SDK, desktop JVM) | List | ✅ Executed → `rewrite/T-1.8.md` |
| T-1.9 | 1.9 Tools and Technologies | h2 | `gradle/libs.versions.toml`, `build.gradle.kts`, Supabase — real versions (Table 1-2) | Table | ✅ Executed → `rewrite/T-1.9.md` |
| T-1.10 | 1.10 Project Deliverables | h2 | `Documentation/`, appendices present | List | ✅ Executed → `rewrite/T-1.10.md` |
| T-1.11 | 1.11 Project Planning | h2 | Milestones (Figure 1-1); keep as plan, not a diary of what happened | Diagram | ✅ Executed → `rewrite/T-1.11.md` |
| T-1.12 | 1.12 Summary | h2 | Recap of chapter; no new claims | Prose | ✅ Executed → `rewrite/T-1.12.md` |

---

## Chapter 2 — Requirements Analysis

### 2.0–2.2 Framing and technique

| ID | Heading | Lvl | Verify against | Type | Status |
|---|---|---|---|---|---|
| T-2.0 | 2. Analysis (chapter intro) | h1 | Chapter scope | Prose | ✅ Executed → `rewrite/T-2.0.md` |
| T-2.1 | 2.1 User Classes and Characteristics | h2 | Roles: admin / teacher / student (Table 2-1); RLS role model in `20260714000002_rls.sql` | Table | ✅ Executed → `rewrite/T-2.1.md` |
| T-2.2 | 2.2 Requirement Identifying Technique | h2 | Use-case + prototype approach | Prose | ✅ Executed → `rewrite/T-2.2.md` |
| T-2.2.1 | 2.2.1 Process and Stakeholder Review | h3 | College workflow basis | Prose | ✅ Executed → `rewrite/T-2.2.1.md` |
| T-2.2.2 | 2.2.2 Prototype and Cross-Platform Review | h3 | Six-app prototype coverage (Appendix C) | Prose | ✅ Executed → `rewrite/T-2.2.2.md` |
| T-2.2.3 | 2.2.3 Use Case Descriptions | h3 | UC-01..UC-20 fully dressed; must match Appendix A | Table | ✅ Executed → `rewrite/T-2.2.3.md` (⚠️ UC-18 renamed, UC-07 FR-25 dropped — Appendix A must mirror) |
| T-2.2.4 | 2.2.4 Use Case Diagrams | h3 | Figures 2-1..2-6 (per-app use case diagrams) | Diagram | ✅ Executed → `rewrite/T-2.2.4.md` (⚠️ 6 figure images need documents node removed) |

### 2.3 Functional Requirements (verify each FR against its implementing module + schema)

| ID | Heading | Lvl | Verify against | Type | Status |
|---|---|---|---|---|---|
| T-2.3 | 2.3 Functional Requirements (intro) | h2 | FR organisation | Prose | ✅ Executed → `rewrite/T-2.3.md` |
| T-2.3.1 | 2.3.1 FR-1: Sign In and Role Routing | h3 | `core` auth; GoTrue; RLS role routing | Table | ✅ Executed → `rewrite/T-2.3.1.md` |
| T-2.3.2 | 2.3.2 FR-2: Manage Administrator Accounts | h3 | admin apps; `profiles`/admin table | Table | ✅ Executed → `rewrite/T-2.3.2.md` |
| T-2.3.3 | 2.3.3 FR-3: Manage Departments | h3 | admin apps; `departments` (schema) | Table | ✅ Executed → `rewrite/T-2.3.3.md` |
| T-2.3.4 | 2.3.4 FR-4: Manage Academic Sessions | h3 | admin apps; `sessions`; `20260718000001_semester_terms.sql` | Table | ✅ Executed → `rewrite/T-2.3.4.md` |
| T-2.3.5 | 2.3.5 FR-5: Manage Curriculum | h3 | admin apps; curriculum/subjects tables (`session_subjects`) | Table | ✅ Executed → `rewrite/T-2.3.5.md` |
| T-2.3.6 | 2.3.6 FR-6: Manage Students and Session Rosters | h3 | admin apps; students, roster; `20260910120000_available_roll_numbers_rpc.sql` | Table | ✅ Executed → `rewrite/T-2.3.6.md` |
| T-2.3.7 | 2.3.7 FR-7: Manage Teachers and Permissions | h3 | admin apps; teachers, permissions (4 flags incl. `can_manage_datesheets`) | Table | ✅ Executed → `rewrite/T-2.3.7.md` |
| T-2.3.8 | 2.3.8 FR-8: Manage Timetables | h3 | admin apps; timetable; `20260905120000_buildings_and_rooms.sql` ⚠️ | Table | ✅ Executed → `rewrite/T-2.3.8.md` (clash = BEFORE trigger, not timerange) |
| T-2.3.9 | 2.3.9 FR-9: View Personal Schedule | h3 | teacher/student apps | Table | ✅ Executed → `rewrite/T-2.3.9.md` |
| T-2.3.10 | 2.3.10 FR-10: Mark Attendance | h3 | teacher apps; attendance table (P/A/L + is_late flag; admin read-only) | Table | ✅ Executed → `rewrite/T-2.3.10.md` |
| T-2.3.11 | 2.3.11 FR-11: View Attendance History | h3 | teacher/student apps | Table | ✅ Executed → `rewrite/T-2.3.11.md` |
| T-2.3.12 | 2.3.12 FR-12: Enter Assessment Marks | h3 | teacher apps; marks table (verified midterm 25 / sessional 15 in `AcademicEnums.kt`) | Table | ✅ Executed → `rewrite/T-2.3.12.md` |
| T-2.3.13 | 2.3.13 FR-13: Request Mark Correction | h3 | teacher apps; `20260721210000_mark_edit_requests.sql` | Table | ✅ Executed → `rewrite/T-2.3.13.md` |
| T-2.3.14 | 2.3.14 FR-14: Review Mark Correction | h3 | admin apps; `mark_edit_requests` (lock-then-approve) | Table | ✅ Executed → `rewrite/T-2.3.14.md` |
| T-2.3.15 | 2.3.15 FR-15: Record Semester Results | h3 | `20260719000001_semester_result_supply.sql`, semester_terms | Table | ✅ Executed → `rewrite/T-2.3.15.md` |
| T-2.3.16 | 2.3.16 FR-16: View Marks and Semester Results | h3 | student apps; GPA/CGPA, supply subjects | Table | ✅ Executed → `rewrite/T-2.3.16.md` |
| T-2.3.17 | 2.3.17 FR-17: Submit Examination Paper | h3 | teacher apps; `20260910130000_exam_paper_rework.sql`, Storage ⚠️ | Table | ✅ Executed → `rewrite/T-2.3.17.md` (datesheet-slot bound, mid-term only, admin review REMOVED) |
| T-2.3.18 | 2.3.18 FR-18: Build and Publish Datesheet | h3 | admin apps; `20260908120000_datesheet_rework.sql` ⚠️ | Table | ✅ Executed → `rewrite/T-2.3.18.md` (session+sem scoped, real curriculum/rooms, cross-datesheet conflict) |
| T-2.3.19 | 2.3.19 FR-19: View Datesheet | h3 | student/teacher apps; datesheet rework ⚠️ | Table | ✅ Executed → `rewrite/T-2.3.19.md` |
| T-2.3.20 | 2.3.20 FR-20: Manage Session Fee Structure | h3 | admin apps; per-session fee tables | Table | ✅ Executed → `rewrite/T-2.3.20.md` |
| T-2.3.21 | 2.3.21 FR-21: View Fee Challan | h3 | student apps; recent "Rework fee challan… 3-copy PDF" commit ⚠️ | Table | ✅ Executed → `rewrite/T-2.3.21.md` (3 copies: Student/College/Clerk) |
| T-2.3.22 | 2.3.22 FR-22: Issue Student Fine | h3 | admin apps; fines table | Table | ✅ Executed → `rewrite/T-2.3.22.md` (admin-only per RLS; teacher "or" was wrong) |
| T-2.3.23 | 2.3.23 FR-23: View Student Fines | h3 | student apps | Table | ✅ Executed → `rewrite/T-2.3.23.md` |
| T-2.3.24 | 2.3.24 FR-24: Manage Calendar Events | h3 | admin apps; calendar table | Table | ✅ Executed → `rewrite/T-2.3.24.md` (no publish state; audience-scoped) |
| T-2.3.25 | 2.3.25 FR-25: Publish College Documents | h3 | admin apps; **`20260827190000_drop_documents_feature_table.sql`** — feature REMOVED ⚠️ | Table | ✅ Executed → `rewrite/T-2.3.25.md` (WITHDRAWN — ID retained, no renumber) |
| T-2.3.26 | 2.3.26 FR-26: View and Download Documents | h3 | student/teacher apps; feature REMOVED ⚠️ | Table | ✅ Executed → `rewrite/T-2.3.26.md` (WITHDRAWN — ID retained, no renumber) |
| T-2.3.27 | 2.3.27 FR-27: Submit Student Link Request | h3 | student apps; `20260717000001_link_request_identity_fields.sql` | Table | ✅ Executed → `rewrite/T-2.3.27.md` |
| T-2.3.28 | 2.3.28 FR-28: Review Student Link Request | h3 | admin apps; link request review + relink/delink | Table | ✅ Executed → `rewrite/T-2.3.28.md` |
| T-2.3.29 | 2.3.29 FR-29: Publish Notifications | h3 | admin apps; notifications table (in-app, no push) | Table | ✅ Executed → `rewrite/T-2.3.29.md` |
| T-2.3.30 | 2.3.30 FR-30: View Notifications | h3 | all apps | Table | ✅ Executed → `rewrite/T-2.3.30.md` |
| T-2.3.31 | 2.3.31 FR-31: View Insights and Reports | h3 | admin/teacher; `20260905000005_drop_unused_insights_views.sql` ⚠️ | Table | ✅ Executed → `rewrite/T-2.3.31.md` (views dropped; computed client-side from base tables, RLS-tiered. ⚠️ Exec Summary "built on Postgres views" now imprecise — consider T-FM1 tweak) |
| T-2.3.32 | 2.3.32 FR-32: View and Maintain Personal Profile | h3 | all apps; `profiles` | Table | ✅ Executed → `rewrite/T-2.3.32.md` |
| T-2.3.33 | 2.3.33 FR-33: Use Desktop File and Print Services | h3 | desktop apps; export/print (3-copy challan PDF) | Table | ✅ Executed → `rewrite/T-2.3.33.md` |

### 2.4–2.6 Non-functional, interfaces, summary

| ID | Heading | Lvl | Verify against | Type | Status |
|---|---|---|---|---|---|
| T-2.4 | 2.4 Non-Functional Requirements (intro) | h2 | NFR framing | Prose | ✅ Executed → `rewrite/T-2.4.md` |
| T-2.4.1 | 2.4.1 Reliability | h3 | Sync triggers `20260830000001_incremental_sync_updated_at_triggers.sql`, offline behaviour | Table | ✅ Executed → `rewrite/T-2.4.1.md` |
| T-2.4.2 | 2.4.2 Usability | h3 | "Modernist" design system in shared UI kits | Table | ✅ Executed → `rewrite/T-2.4.2.md` |
| T-2.4.3 | 2.4.3 Performance | h3 | Perf targets; align with Ch 5 PT results (Figure 5-2) | Table | ✅ Executed → `rewrite/T-2.4.3.md` (5s target aligns w/ Ch5) |
| T-2.4.4 | 2.4.4 Security | h3 | `20260714000002_rls.sql`, `20260714000004_harden_functions.sql`, guard migrations | Table | ✅ Executed → `rewrite/T-2.4.4.md` (+NFR-S7 write-path invariants) |
| T-2.5 | 2.5 External Interface Requirements (intro) | h2 | Interface framing | Prose | ✅ Executed → `rewrite/T-2.5.md` |
| T-2.5.1 | 2.5.1 User Interface Requirements | h3 | Shared Compose UI kit, Material 3 | Table | ✅ Executed → `rewrite/T-2.5.1.md` (Student Desktop "document" → notice) |
| T-2.5.2 | 2.5.2 Software Interfaces | h3 | Supabase SDK, Postgrest, GoTrue, Storage; `libs.versions.toml` | Table | ✅ Executed → `rewrite/T-2.5.2.md` (verified versions; documents→files) |
| T-2.5.3 | 2.5.3 Hardware Interfaces | h3 | Android device / desktop JVM targets | Table | ✅ Executed → `rewrite/T-2.5.3.md` |
| T-2.5.4 | 2.5.4 Communications Interfaces | h3 | HTTPS to Supabase; network layer | Table | ✅ Executed → `rewrite/T-2.5.4.md` (in-app notifs, no FCM; keep-alive cron) |
| T-2.6 | 2.6 Summary | h2 | Chapter recap (fix stray "notification list." artifact from PDF) | Prose | ✅ Executed → `rewrite/T-2.6.md` |

---

## Chapter 3 — Design and Architecture

| ID | Heading | Lvl | Verify against | Type | Status |
|---|---|---|---|---|---|
| T-3.0 | 3. System Design (chapter intro) | h1 | Chapter scope | Prose | ✅ Executed → `rewrite/T-3.0.md` |
| T-3.1 | 3.1 Design Considerations | h2 | Assumptions/limitations/risks (Table 3-1); `design.md` | Table | ✅ Executed → `rewrite/T-3.1.md` (+cascade/confirm, keep-alive risks; documents→files) |
| T-3.2 | 3.2 Design Models (intro) | h2 | Model overview | Prose | ✅ Executed → `rewrite/T-3.2.md` |
| T-3.2.1 | 3.2.1 Domain and Class Relationship Model | h3 | `core/src/main/kotlin/com/mbd/cmscommon/domain` (Figure 3-1) | Diagram | ✅ Executed → `rewrite/T-3.2.1.md` (⚠️ Fig 3-1 image: remove Documents class) |
| T-3.2.2 | 3.2.2 Interaction Models | h3 | Sequence figures 3-12..3-16 | Diagram | ✅ Executed → `rewrite/T-3.2.2.md` (⚠️ drop Figure 3-16 document publication) |
| T-3.2.3 | 3.2.3 State-Transition Models | h3 | Figures 3-17..3-19 (link, mark-correction, publication states) | Diagram | ✅ Executed → `rewrite/T-3.2.3.md` (⚠️ Fig 3-19: drop documents path) |
| T-3.3 | 3.3 Architectural Design (intro) | h2 | Component overview (Figure 3-3) | Prose | ✅ Executed → `rewrite/T-3.3.md` (documents→files) |
| T-3.3.1 | 3.3.1 System Context | h3 | Six apps + Supabase (Figure 3-2) | Diagram | ✅ Executed → `rewrite/T-3.3.1.md` (⚠️ Fig 3-2 check Firebase→Supabase) |
| T-3.3.2 | 3.3.2 Component Structure | h3 | `core`/`mobile-shared`/`desktop-shared` split | Diagram | ✅ Executed → `rewrite/T-3.3.2.md` |
| T-3.3.3 | 3.3.3 Architecture Style and Module Mapping | h3 | MVVM/Repository + Hilt; layered map (Table 3-3, Figure 3-4) | Table | ✅ Executed → `rewrite/T-3.3.3.md` |
| T-3.4 | 3.4 Data Design (intro) | h2 | `supabase/migrations/20260714000001_schema.sql` (Figure 3-5) | Prose | ✅ Executed → `rewrite/T-3.4.md` (documents dropped) |
| T-3.4.1 | 3.4.1 Data Dictionary | h3 | Every entity vs current schema — drop tables/columns removed by later migrations (Table 3-4) ⚠️ | Table | ✅ Executed → `rewrite/T-3.4.1.md` (removed Document; +Building/Room; exam-paper no review; notif no priority; clash=trigger) |
| T-3.5 | 3.5 User Interface Design (intro) | h2 | Shared UI kit | Prose | ✅ Executed → `rewrite/T-3.5.md` |
| T-3.5.1 | 3.5.1 Screen Images | h3 | Figures 3-6..3-11 (per-app screens) match current UI ⚠️ | Diagram | ✅ Executed → `rewrite/T-3.5.1.md` (⚠️ verify screenshots: no documents/old-UI/Firebase) |
| T-3.5.2 | 3.5.2 Screen Objects and Actions | h3 | Attendance + mark-correction screens (Tables 3-5, 3-6) | Table | ✅ Executed → `rewrite/T-3.5.2.md` |
| T-3.5.3 | 3.5.3 Common Feedback and Interaction Rules | h3 | Shared components; feedback conventions (Table 3-7) | Table | ✅ Executed → `rewrite/T-3.5.3.md` |
| T-3.6 | 3.6 Behavioural Model (intro) | h2 | Behavioural overview | Prose | ✅ Executed → `rewrite/T-3.6.md` (§3.6.5 noted withdrawn) |
| T-3.6.1 | 3.6.1 Sign-In and Role Routing | h3 | Auth flow | Diagram | ✅ Executed → `rewrite/T-3.6.1.md` (⚠️ Fig 3-12 Firebase→GoTrue) |
| T-3.6.2 | 3.6.2 Publish College Information | h3 | Admin publication flow | Diagram | ✅ Executed → `rewrite/T-3.6.2.md` (no documents path) |
| T-3.6.3 | 3.6.3 Record Attendance and Assessment | h3 | Teacher flow | Diagram | ✅ Executed → `rewrite/T-3.6.3.md` |
| T-3.6.4 | 3.6.4 Register and Link a Student Account | h3 | Link flow; identity fields migration | Diagram | ✅ Executed → `rewrite/T-3.6.4.md` |
| T-3.6.5 | 3.6.5 Publish and Open a Document | h3 | Storage flow (documents table dropped) ⚠️ | Diagram | ✅ Executed → `rewrite/T-3.6.5.md` (WITHDRAWN — drop §3.6.5 + Fig 3-16) |
| T-3.6.6 | 3.6.6 Student Link Request States | h3 | Link request state machine (relink/delink downgrade) | Diagram | ✅ Executed → `rewrite/T-3.6.6.md` |
| T-3.6.7 | 3.6.7 Mark Correction Request States | h3 | `mark_edit_requests` states | Diagram | ✅ Executed → `rewrite/T-3.6.7.md` |
| T-3.6.8 | 3.6.8 Publication States | h3 | Publishable-info states | Diagram | ✅ Executed → `rewrite/T-3.6.8.md` (documents path dropped) |
| T-3.7 | 3.7 Design Decisions | h2 | Major decisions (Table 3-8) — reasons, stated as present rationale not history | Table | ✅ Executed → `rewrite/T-3.7.md` (+GPA write-path, in-app notifs; documents→files; no migration) |
| T-3.8 | 3.8 Summary | h2 | Chapter recap | Prose | ✅ Executed → `rewrite/T-3.8.md` |

---

## Chapter 4 — Implementation

| ID | Heading | Lvl | Verify against | Type | Status |
|---|---|---|---|---|---|
| T-4.0 | 4. Implementation (chapter intro) | h1 | Chapter scope | Prose | ✅ Executed → `rewrite/T-4.0.md` |
| T-4.1 | 4.1 Algorithm (intro) | h2 | Algorithm coverage (Table 4-1) | Prose | ✅ Executed → `rewrite/T-4.1.md` (algo 9 documents dropped) |
| T-4.1.1 | 4.1.1 Start and Route a Signed-In User | h3 | Auth/session restore code | Prose | ✅ Executed → `rewrite/T-4.1.1.md` |
| T-4.1.2 | 4.1.2 Prepare Administrative Data | h3 | Admin reference-data load | Prose | ✅ Executed → `rewrite/T-4.1.2.md` |
| T-4.1.3 | 4.1.3 Validate a Timetable Period | h3 | Clash-check logic; rooms migration | Prose | ✅ Executed → `rewrite/T-4.1.3.md` (BEFORE trigger, not timerange) |
| T-4.1.4 | 4.1.4 Record Class Attendance | h3 | Attendance save logic | Prose | ✅ Executed → `rewrite/T-4.1.4.md` |
| T-4.1.5 | 4.1.5 Record Assessment Marks | h3 | Marks entry + lock logic | Prose | ✅ Executed → `rewrite/T-4.1.5.md` (25/15) |
| T-4.1.6 | 4.1.6 Review a Mark Correction | h3 | `mark_edit_requests` approval | Prose | ✅ Executed → `rewrite/T-4.1.6.md` |
| T-4.1.7 | 4.1.7 Link a Student Account | h3 | Link-matching logic | Prose | ✅ Executed → `rewrite/T-4.1.7.md` |
| T-4.1.8 | 4.1.8 Build a Student Academic Overview | h3 | Aggregation of attendance/marks/fees | Prose | ✅ Executed → `rewrite/T-4.1.8.md` |
| T-4.1.9 | 4.1.9 Publish Information for an Audience | h3 | Role-scoped publish | Prose | ✅ Executed → `rewrite/T-4.1.9.md` |
| T-4.1.10 | 4.1.10 Complete a Desktop File Action | h3 | Desktop open/save/export/print/share | Prose | ✅ Executed → `rewrite/T-4.1.10.md` |
| T-4.2 | 4.2 External APIs/SDKs | h2 | `gradle/libs.versions.toml` — real names/versions (Table 4-2) | Table | ✅ Executed → `rewrite/T-4.2.md` |
| T-4.3 | 4.3 Code Repository (intro) | h2 | Repo link; Git usage | Prose | ✅ Executed → `rewrite/T-4.3.md` |
| T-4.3.1 | 4.3.1 Metrics of the Git Repository | h3 | `git` log/branch/PR counts — regenerate live (Table 4-3) | Table | ✅ Executed → `rewrite/T-4.3.1.md` |
| T-4.3.2 | 4.3.2 Contributor Statistics | h3 | `CONTRIBUTORS.md` + `git shortlog -sne` (Table 4-4) | Table | ✅ Executed → `rewrite/T-4.3.2.md` |
| T-4.4 | 4.4 Summary | h2 | Chapter recap | Prose | ✅ Executed → `rewrite/T-4.4.md` |

---

## Chapter 5 — Testing and Evaluation

| ID | Heading | Lvl | Verify against | Type | Status |
|---|---|---|---|---|---|
| T-5.0 | 5. Introduction (chapter intro) | h1 | Testing approach; retitle from template's stray "Introduction" | Prose | ✅ Executed → `rewrite/T-5.0.md` |
| T-5.1 | 5.1 Unit Testing (UT) | h2 | `*/src/test/*`; UT-1..UT-3 (Tables 5-3..5-5) | Table | ✅ Executed → `rewrite/T-5.1.md` |
| T-5.2 | 5.2 Functional Testing (FT) | h2 | FT-1..FT-4 (Tables 5-6..5-9) | Table | ✅ Executed → `rewrite/T-5.2.md` |
| T-5.3 | 5.3 Integration Testing (IT) | h2 | IT-1..IT-3 (Tables 5-10..5-12) | Table | ✅ Executed → `rewrite/T-5.3.md` |
| T-5.4 | 5.4 Performance Testing (PT) | h2 | PT config + PT-1..PT-2 (Figures 5-1/5-2, Tables 5-13..5-15) | Table | ✅ Executed → `rewrite/T-5.4.md` |
| T-5.5 | 5.5 Summary | h2 | Chapter recap | Prose | ✅ Executed → `rewrite/T-5.5.md` |

---

## Chapter 6 — System Conversion

| ID | Heading | Lvl | Verify against | Type | Status |
|---|---|---|---|---|---|
| T-6.0 | 6. Introduction (chapter intro) | h1 | Conversion framing | Prose | ✅ Executed → `rewrite/T-6.0.md` |
| T-6.1 | 6.1 Conversion Method | h2 | Chosen method + rationale (Table 6-1, Figure 6-1) | Table | ✅ Executed → `rewrite/T-6.1.md` |
| T-6.2 | 6.2 Deployment (intro) | h2 | Build/package the six apps; Supabase project (Tables 6-2, 6-3) | Table | ✅ Executed → `rewrite/T-6.2.md` |
| T-6.2.1 | 6.2.1 Data Conversion | h3 | Migrations + backup/restore; controls (Table 6-4, Figure 6-2) | Table | ✅ Executed → `rewrite/T-6.2.1.md` |
| T-6.2.2 | 6.2.2 Training | h3 | Two use-case walkthroughs (attendance, fee challan) (Table 6-5, Figures 6-3..6-6) | Table | ✅ Executed → `rewrite/T-6.2.2.md` |
| T-6.3 | 6.3 Post Deployment Testing | h2 | Acceptance checks (Table 6-6) | Table | ✅ Executed → `rewrite/T-6.3.md` |
| T-6.4 | 6.4 Challenges | h2 | Conversion challenges + responses (Table 6-7) | Table | ✅ Executed → `rewrite/T-6.4.md` |
| T-6.5 | 6.5 Summary | h2 | Chapter recap | Prose | ✅ Executed → `rewrite/T-6.5.md` |

---

## Chapter 7 — Conclusion

| ID | Heading | Lvl | Verify against | Type | Status |
|---|---|---|---|---|---|
| T-7.0 | 7. Introduction (chapter intro) | h1 | Chapter scope | Prose | ✅ Executed → `rewrite/T-7.0.md` |
| T-7.1 | 7.1 Evaluation | h2 | Objectives vs delivered features (Table 7-1) | Table | ✅ Executed → `rewrite/T-7.1.md` |
| T-7.2 | 7.2 Traceability Matrix | h2 | FR → design → code file → test ID; must be complete (Table 7-2) | Table | ✅ Executed → `rewrite/T-7.2.md` |
| T-7.3 | 7.3 Conclusion | h2 | Objective↔functionality correlation (Table 7-3) | Table | ✅ Executed → `rewrite/T-7.3.md` |
| T-7.4 | 7.4 Future Work | h2 | Excluded scope; prioritised list (Table 7-4) | Table | ✅ Executed → `rewrite/T-7.4.md` |

---

## Appendices

| ID | Heading | Lvl | Verify against | Type | Status |
|---|---|---|---|---|---|
| T-A | Appendix A — Fully Dressed Use Case Descriptions | h1 | UC-01..UC-20 must match Section 2.2.3 (Tables A-1..A-22) | Table | ✅ Executed → `rewrite/T-A.md` |
| T-B | Appendix B — General Coding Standards and Guidelines | h1 | Standards actually followed in Kotlin source (Table B-1) | List | ✅ Executed → `rewrite/T-B.md` |
| T-C | Appendix C — Application Prototype | h1 | Figures C-1..C-6 match current six-app UI ⚠️ | Diagram | ✅ Executed → `rewrite/T-C.md` |

---

## How to execute a task

Pick a row, then run it as its own unit of work:

1. Read the current section text from `Documentation/CMS_FYP_Report.a4.pdf` (extract with `pdftotext -layout`).
2. Open the **Verify against** artifacts and confirm/correct every factual claim.
3. Apply **the job** (humanize · update to repo · no chronology).
4. Save the rewritten section to the working draft, mark the row `✅ Executed`, and bump the status-summary counts (Executed +1, Pending −1).

Suggested order: front matter → **repo-drift flags first** (exam papers, datesheet, fee challan, documents, insights, rooms) → then chapter by chapter.
