# Govt Graduate College, Mandi Bahauddin — CMS Design System & Screen Specification

> Audience: Stitch AI (high-fidelity mockup generation) and, subsequently, Jetpack Compose implementation. Every screen and component is described visually and functionally. Design language: **"The Academic Ledger"** — an institutional, editorial, trustworthy aesthetic built on Material Design 3.
>
> Scope: three apps — **Admin**, **Teacher**, **Student** — sharing one design system with a subtle per-app signature accent. Covers 100% of current app screens; screens with no polished predecessor are designed fresh in the same language.

---

## 1. Design Philosophy

- **Editorial & institutional.** Large serif headlines (Newsreader) paired with a crisp sans (Public Sans) evoke a printed academic ledger / broadsheet. Generous whitespace, clear hierarchy, restrained ornament.
- **Navy authority, gold prestige, crimson urgency.** Deep navy is the backbone; gold marks scholarly/active accents and dividers; crimson signals alerts, absence, and required actions.
- **Paper, not glass.** Off-white "paper" surfaces, near-flat cards separated by hairline outlines rather than heavy shadows. Elevation is used sparingly and purposefully.
- **Signature motifs (used consistently across all three apps):** the *eyebrow* (tiny uppercase tracked label), the *serif hero headline*, the short *accent rule* under headlines, *left-stripe cards*, and *pill/segmented controls*.
- **One family, three voices.** Identical system; each app has a signature accent (Admin crimson, Teacher gold, Student emerald) and an app wordmark, so they feel related but distinct.
- **Right container for the data — never card-everything.** Match presentation to content: **Cards** for summaries/dashboards/stats/quick actions; **Lists** for simple navigation/feeds; **Data Tables (grids)** for structured records, schedules, attendance, marks, reports, and admin management. The Admin app in particular is a professional ERP-style console that leans heavily on data grids; Teacher/Student use tables wherever data is naturally tabular (attendance history, timetable, marks, leave history) rather than forcing cards. See §5.5 and the presentation matrix.

---

## 2. Color System

### 2.1 Core tokens (light)
| Token | Hex | Role |
|---|---|---|
| primary (ink navy) | `#000A1E` | Brand base: hero text, primary buttons, dark panels |
| primaryContainer (scholar navy) | `#002147` | Navy panels/banners, gradients, secondary navy |
| onPrimary | `#FFFFFF` | Text/icons on navy |
| onPrimaryContainer | `#708AB5` | Muted text on navy panels |
| primaryFixed | `#D6E3FF` | Light navy tint (icon badges, table header) |
| secondary (crimson) | `#B22B1D` | Alerts, absent, active nav indicator, required actions |
| secondaryContainer | `#FE624E` | Progress fills, bright accents |
| onSecondary | `#FFFFFF` | Text on crimson |
| tertiary/accent (gold) | `#B18000` | Scholarly accents, active states, links |
| goldBright | `#F7BD48` | Dividers, underlines, borders, active-tile stroke |
| goldFill (amber) | `#FFDEA6` | Active-tile / banner fill |
| eyebrowGold | `#5D4200` | Eyebrow label text, left-border accents |
| surface / background | `#F9F9F9` | App background ("paper") |
| surfaceContainerLowest | `#FFFFFF` | Cards |
| surfaceContainerLow | `#F3F3F3` | Subtle raised fills, inactive tiles, zebra |
| surfaceContainer | `#EEEEEE` | Inputs, mid fills |
| surfaceContainerHigh | `#E8E8E8` | Chips, segmented track, table header |
| surfaceContainerHighest | `#E2E2E2` | Progress track, dividers |
| onSurface | `#1A1C1C` | Primary text |
| onSurfaceVariant | `#44474E` | Secondary text |
| outline | `#74777F` | Borders, tertiary text |
| outlineVariant | `#C4C6CF` | Hairline dividers, card strokes |
| success | `#2E7D32` | Present status, confirmed |
| liveDot | `#10B981` | Pulsing "live/online" indicator |
| error | `#BA1A1A` | Errors, destructive, sign-out |
| errorContainer | `#FFDAD6` | Error field backgrounds |

### 2.2 Per-app signature accent
Shared palette everywhere; each app selects a **signature accent** used for its active nav indicator, hero rule, key CTAs' emphasis, and app badge:
- **Admin → crimson `#B22B1D`** (oversight/authority).
- **Teacher → gold `#B18000` / `#F7BD48`** (scholarly).
- **Student → emerald `#2E7D32`** (growth; harmonizes with success). Student's crimson/gold remain available for status semantics.

### 2.3 Dark theme (derive, don't invent)
Provide a dark variant: background `#0E1116`, surfaces `#141922`→`#1B212C`→`#222A36`, onSurface `#E6E8EC`, onSurfaceVariant `#AEB4BD`, primary becomes light navy `#AEC7F6` on dark, gold `#F7BD48` and crimson `#FF8A7A` brighten for contrast. Navy hero panels stay navy. Maintain ≥4.5:1 contrast for body text. (Light is the primary/default theme; dark mirrors it.)

### 2.4 Usage rules
- Navy = structure & primary actions. Gold = active/selected + hairline dividers under headings. Crimson = alerts/absent/urgent only (never decorative). Green = present/success only.
- Cards are `surfaceContainerLowest` (#FFF) on `surface` (#F9F9F9) paper, separated by a 1dp `outlineVariant` stroke; shadows minimal.

---

## 3. Typography

**Typefaces:** *Newsreader* (serif) for display/headlines/serif card titles; *Public Sans* (sans) for everything else.

| Style | Font | Size / Line | Tracking | Case | Usage |
|---|---|---|---|---|---|
| Display / Hero | Newsreader Bold | 36–40sp / 1.1 | −0.02em | — | Dashboard greetings, splash college name |
| Headline Large | Newsreader Bold | 28–30sp / 1.15 | — | — | Screen titles ("Attendance Registry") |
| Headline Medium | Newsreader Bold | 20–22sp | — | — | Section headers, card titles |
| Title Large | Newsreader Bold | 17sp | — | — | List/card item titles (serif) |
| Title Medium | Public Sans SemiBold | 15sp | — | — | Dense titles, dialog titles |
| Body Large | Public Sans Regular | 14sp / 1.5 | — | — | Primary body |
| Body Medium | Public Sans Regular | 12sp / 1.5 | — | — | Secondary body, captions, table cells |
| Label Large | Public Sans SemiBold | 13sp | 0.12em | UPPER (buttons) | Buttons, tabs |
| Eyebrow / Label Small | Public Sans SemiBold | 10–11sp | 0.15–0.22em | UPPER | Eyebrows above headlines, chips, table headers |
| Footer / Micro | Public Sans SemiBold | 9sp | 0.12–0.18em | UPPER | Footnotes, version, meta |

**Signature rule:** a screen header = *Eyebrow* (uppercase gold/muted) → *Serif Headline* → short **accent rule** (48×3dp, app signature color) → optional italic subtitle (Newsreader Italic).

---

## 4. Spacing, Shape, Elevation, Icons, Motion

- **Spacing scale (dp):** 2, 4, 6, 8, 12, 16, 20, 24, 28, 32, 40, 48, 64. Screen horizontal padding **20dp** (24dp on hero headers). Section vertical rhythm **16–24dp**. Card inner padding **16–20dp**. List row gap **8–12dp**.
- **Corner radius:** inputs/buttons/small **8dp**; cards/containers/tables **12dp**; large panels/sheets **16–20dp**; chips/segmented/badges **full pill**. Icon badges (rounded-square) **8dp**.
- **Elevation:** content cards **0dp** (hairline stroke instead); interactive/raised **1–2dp**; app bars **0–2dp**; bottom nav & sticky action bars **8dp**; dialogs **6dp**; FAB **6dp**. Navy "seal"/hero cards may use a soft 8dp shadow tinted `#1A000A1E`.
- **Icons:** Material Symbols (outlined/rounded), 1.5–2dp stroke feel. Sizes: inline 16dp, standard UI 22–24dp, nav 24dp, hero/empty-state 48–88dp. Tint = onSurfaceVariant default, navy/gold/crimson for emphasis.
- **Motion (MD3 easing, 150–350ms):** screen transitions = shared-axis/fade-through; list items = subtle stagger fade+rise (200ms); FAB & dialogs = scale/fade; selection (tabs, semester tiles, P/A/L) = 150ms color+indicator slide; refresh = pull-to-refresh spinner; live dot = 700ms opacity pulse loop; compliance/attendance rings = sweep-in animation on first show (600ms). Buttons: state-layer ripple, no heavy elevation bounce.

---

## 5. Component Library

### 5.1 Standard MD3 components (styled)
- **Buttons:** *Filled* = navy bg, white uppercase Label Large, 8dp radius, 52–56dp tall, subtle ripple. *Tonal* = surfaceContainerHigh bg, navy text. *Outlined* = 1dp outlineVariant, navy text, transparent. *Text* = navy label. *Destructive* = error-outlined (sign-out) or error-filled (confirm delete). Primary CTAs often full-width with a trailing icon.
- **Cards:** #FFF, 12dp radius, 1dp outlineVariant stroke, 0–1dp elevation, 16–20dp padding. **Left-stripe variant:** 4dp colored left edge (navy=neutral/inactive, gold=active, crimson=urgent, green=present).
- **Text fields:** two styles — (a) **Outlined box** (12dp radius, 1dp outlineVariant, floating/label-above, leading icon) for search & data entry; (b) **Underline** (transparent, 1dp bottom stroke, uppercase micro-label above) for login/recovery for the editorial feel. Password fields have an eye toggle. Error = crimson helper text + red stroke.
- **Top app bar:** paper bg, 0–2dp elevation. Left = app wordmark or back arrow; center/left title (serif for dashboards, uppercase Public Sans for utility screens); right = actions (notification bell w/ badge, refresh, avatar). Height 56dp + status inset.
- **Bottom navigation:** 5 items, paper bg, 8dp elevation, labeled (uppercase 10sp), icons 24dp. Active = signature accent icon+label + a pill active-indicator (48×4dp, signature-tinted); inactive = onSurfaceVariant.
- **FAB:** navy, 16dp radius, primary "add/create" on list/table screens (Admin add-term/department/etc., "Send notification"). Extended FAB for primary create actions with label.
- **Dialogs:** #FFF, 16dp radius, serif Title Medium, body, text buttons (Cancel / confirm). **Confirm-destructive dialog** shows the cascade summary ("This permanently deletes … 42 attendance records. Cannot be undone.") with an error-colored confirm button.
- **Bottom sheets:** for pickers/filters (subject picker, term switch, override editor, table filter) — 16dp top corners, drag handle, list of options with check states.
- **Snackbars:** bottom, dark navy surface, white text, optional gold action label; used for refresh success/failure ("You're offline — showing cached data"), save confirmations.
- **Chips:** pill, surfaceContainerHigh bg; filter/selection chips get navy fill when active. Status chips: Submitted=green tint, Pending=amber tint, Absent=crimson tint.
- **Tabs (in-screen):** for Exams (Marks/Papers), Fees (Semester/Annual) — underlined MD3 tabs, navy indicator.

### 5.2 Signature composites (define once, reuse everywhere)
1. **SectionHeader** — Eyebrow + Serif Headline + accent rule (+ optional italic subtitle). Every screen top.
2. **StatCard (bento)** — 200×150dp, horizontal-scroll row; alternating **white** and **navy** cards; giant serif italic number, uppercase micro label, tiny trend, 40×3dp bottom accent bar (crimson on white cards, gold on navy). Used in Admin/Teacher/Student dashboards.
3. **LeftStripeListCard** — card with 4dp colored left edge; used for departments, offerings, alerts, deadlines, active-session, exam status.
4. **EyebrowValueRow / DataChipGrid** — 2-col grid of "LABEL → value" chips (e.g., Last Sync, Status).
5. **SegmentedPAL** — full-pill container (surfaceContainerHigh), three segments Present/Absent/Leave; selected segment = navy pill + white text; used in Mark Attendance.
6. **RingGauge** — circular progress (track = surfaceContainerHighest, arc = gold [compliance] or green/gold/crimson by threshold [attendance %]); center = big serif % + uppercase caption. Used in Teacher compliance panel & Student attendance summary.
7. **ScoreEntryGrid** — an editable **DataTable** specialization: sticky column header row (primaryFixed bg, uppercase labels), rows with avatar+name+roll, numeric input cells (surfaceContainer, 8dp), running total cell (serif). Reused for Midterm, Sessional, and Admin fee-heads editor.
8. **AvatarInitials** — rounded-square (8dp), primaryFixed bg with cycling tints, serif initials. **AvatarStack** — overlapping circles + "+N".
9. **StatusBadge** — pill: Submitted (green), Pending (amber), Action Required (crimson), Verified (gold), Active/Inactive.
10. **LiveDot** — 8dp emerald dot with pulse; paired with "CURRENT SESSION"/"LIVE" eyebrow.
11. **EmptyState** — centered 64–88dp outlined icon + serif line + muted body + optional primary action ("Add your first department"). One per list/table.
12. **LoadingState** — top linear indeterminate on refresh; centered circular for first load; **skeletons** (shimmer placeholder cards/rows) preferred for lists and tables.
13. **ErrorState / OfflineBanner** — inline card with icon + message + Retry; a slim amber "Offline — showing cached data" banner under the app bar when a sync fails (offline-first: cached content still shows).
14. **DecorativeBlob** — 5%-opacity navy/crimson rounded corner shape top-right & bottom-left of splash/auth/dashboard heroes (very subtle).
15. **NavyBanner / AmberBanner** — full-width call-to-action or notice panel: navy banner (white text, gold or crimson CTA) for audits/announcements; amber banner (left crimson border) for integrity/warning notes.

### 5.3 Global states (apply to every screen)
- **Empty:** use EmptyState composite with context copy + primary create action where the user can add.
- **Loading:** skeletons for lists/cards/tables; circular for detail screens; top linear bar during pull-to-refresh (never blocks cached content).
- **Error/Offline:** OfflineBanner + keep showing Room-cached data; destructive/network actions show snackbar on failure, never crash.
- **Success:** snackbar or inline green StatusBadge (e.g., "Attendance submitted", "Marks saved").

### 5.4 Accessibility
- Contrast ≥4.5:1 body / ≥3:1 large text (navy-on-paper, white-on-navy verified). Touch targets ≥48dp. Every icon-only button has a content description. Respect system font scale (layouts reflow, no clipping). Focus order logical; state not conveyed by color alone (P/A/L also show label; status badges have text). Dark theme supported.

---

## 5.5 Data Tables (Data Grids) — first-class component

Structured records use a shared, responsive **DataTable** rather than card lists. It is the backbone of the Admin console and is reused (in lighter form) in Teacher/Student for tabular data.

### 5.5.1 Anatomy
- **Table toolbar (above grid):** title/eyebrow on the left; on the right a cluster of — **Search** field (outlined, leading magnifier, debounced), **Filter** chips/button (opens a filter bottom sheet: by term/department/session/status/date range as relevant), **Sort** menu (or sortable headers), **Export** action (CSV/PDF where appropriate — Admin reports), and overflow. When rows are selected, the toolbar swaps to a **contextual selection bar** (count + bulk actions: Delete, Export, Notify).
- **Header row:** sticky (stays pinned while the body scrolls vertically), background `surfaceContainerHigh`/`primaryFixed` tint, **uppercase Label Small** column titles (`onSurfaceVariant`), sortable columns show an up/down caret that toggles asc/desc (active sort column tinted navy). Optional leading **select-all checkbox**.
- **Body rows:** 48–56dp height, hairline `outlineVariant` bottom divider (flat, no zebra by default; optional very-subtle zebra `surfaceContainerLow` for very dense financial/marks tables). Row hover/press = state layer; **selected row** = 8% navy tint + leading checkbox checked. First column often bold/serif (name/identity) and may be **frozen** on horizontal scroll.
- **Cells:** left-align text, **right-align numbers** (marks, amounts, counts, %), center-align short status. Rich cells allowed: AvatarInitials + name, **StatusBadge** (Present/Absent/Submitted/Pending/Active/%), AttendancePercentageBadge, mini progress bar.
- **Action column (trailing, pinned right):** compact icon buttons (View, Edit, Delete) or a row overflow (⋮) menu; destructive actions route through the confirm-destructive dialog with cascade summary.
- **Footer / pagination:** either **pagination controls** (rows-per-page selector + page nav + "1–25 of 320") for large admin datasets, or **lazy load / infinite scroll** (load-more spinner row) — pick per screen; both keep the header sticky.

### 5.5.2 Responsive behavior
- **Wide (tablet/landscape):** full multi-column grid, all columns visible, comfortable density.
- **Phone (default):** the grid becomes a **horizontally scrollable data grid** — first identity column **frozen**, remaining columns scroll under it; a subtle right-edge fade hints at more columns. Column widths respect min-widths; never crush text. Density can tighten (44dp rows).
- **Alternative compact mode (optional, for simple 2–3 field tables):** collapse into two-line rows (primary + secondary metadata) — but prefer the scrollable grid for true tables so headers/columns stay meaningful.
- Row height, font, and horizontal scroll all respect system font scaling.

### 5.5.3 States
- **Loading (first load):** **skeleton rows** (shimmer bars matching column layout, ~8 rows) under a real header.
- **Loading more:** spinner row at the bottom (lazy) or disabled pager (pagination).
- **Empty:** EmptyState inside the table body (icon + "No records yet" + primary create action where applicable).
- **Error/Offline:** OfflineBanner above the grid; Room-cached rows still render; failed refresh shows a snackbar (never blanks the table).
- **Filtered-empty:** "No results for '{query}' / filters" with a Clear-filters action.

### 5.5.4 Styling tokens
Header bg `primaryFixed`(#D6E3FF)/`surfaceContainerHigh`; header text uppercase 10sp `onSurfaceVariant`; body text Body Medium `onSurface`; dividers `outlineVariant`; selected-row tint navy@8%; numeric cells tabular/right-aligned; action icons `onSurfaceVariant` (Delete = `error`); corner radius 12dp on the table container with a 1dp `outlineVariant` stroke; sticky header carries a 1dp bottom divider + faint shadow when scrolled.

### 5.5.5 Presentation matrix (which container each screen uses)
| Screen | Primary container |
|---|---|
| Admin Dashboard | Cards (stats/bento) + short alert **list** |
| Academics / hubs (Dept detail, Offering detail) | **List** of navigation cards |
| Academic Terms | **DataTable** (Label, Year, Type, Active, Dates, Actions) |
| Departments | **DataTable** (Name, Code, #Subjects, #Classes, Actions) |
| Subjects | **DataTable** (Code, Name, Semester, Credits, Type, Actions) |
| Classes | **DataTable** (Name, Semester, Session, Actions) |
| Class Offerings | **DataTable** (Offering, Semester, Session, #Students, Actions) |
| Students (roster / all) | **DataTable** (Avatar+Name, Roll, Class, Status, Actions) + search/sort/filter/pagination + row-select bulk actions |
| Teachers | **DataTable** (Avatar+Name, Email, Dept, Permissions summary, Status, Actions) |
| Teacher Assignments | **DataTable** (Subject, Assigned Teacher, Actions) |
| Student Link Requests | **DataTable** (Claimed Roll, Requester, Matched?, Requested, Actions: Approve/Reject) |
| Timetable (view + editor) | **DataTable / weekly grid** (Day × Period columns, or Day/Time/Subject/Teacher rows) |
| Attendance Report | **DataTable** (flagship: Roll, Name, Subject, P, A, L, %, Actions) + subject/date filters + export |
| Exams & Marks | **DataTable** per exam type (Roll, Name, Score /Max, Status) + Papers sub-tab as a table (Subject, File, Submitted, Actions) |
| Fee Structure editor | **DataTable-style editable grid** (Head, Amount, remove) + Total row |
| Notifications (Admin manage) | **DataTable** (Title, Target, Sent, Actions) — compose via dialog |
| Teacher Home | Cards + deadline **list** |
| Mark Attendance | **DataTable/grid** (Avatar+Name, Roll, P/A/L segmented cell) + sticky summary + submit |
| Marks Entry (Midterm/Sessional) | **Editable DataTable** (identity + numeric input columns + Total) |
| My Students (Teacher) | **DataTable** (Avatar+Name, Roll, Attendance %, Actions:View) |
| Teacher Schedule | **DataTable / weekly grid** |
| Student Attendance | Summary **RingGauge cards** on top + per-subject **DataTable** (Subject, P, A, L, %) |
| Student Marks | **DataTable** (Subject, Midterm /25, Sessional /15, Total) |
| Student Timetable | **DataTable / weekly grid** (Day × periods) |
| Student Fee Challan | **DataTable** (Fee Head, Amount) + Total + Download |
| Profiles / Auth / Splash / Link-gate | Cards / forms (not tables) |

---

## 6. Navigation Architecture

### 6.1 Admin — Bottom Nav (5 tabs) + hub sub-screens
- **Dashboard** — command center (stats, active term, quick actions, alerts).
- **Academics** — hub → Academic Terms · Departments (→ Subjects, Classes) · Class Offerings (→ Offering detail: Timetable, Teacher Assignments, Fee Structure).
- **People** — hub → Students (→ detail + enrollment history) · Teachers (→ create account, permissions) · Student Link Requests.
- **Records** — hub → Attendance Reports · Exams & Marks · Fees overview.
- **More** — Notifications (view/send) · Profile · Settings · Sign out.

Hubs use a card list; detail screens push with back arrow. FAB on leaf list/table screens for create. Login/Splash sit outside the nav.

### 6.2 Teacher — Bottom Nav (5 tabs)
- **Home** (dashboard) · **Attendance** (select → Mark Attendance) · **Exams** (select params → Marks Entry; + Exam Paper Submission) · **Schedule** (my timetable, conditional edit) · **Menu** (My Students, Link Requests*, Notifications*, Profile, Sign out). *=permission-gated. Login/Forgot-password outside nav.

### 6.3 Student — Bottom Nav (5 tabs)
- **Home** (dashboard) · **Attendance** (summary) · **Marks** · **Timetable** · **More** (Fee Challan, Notifications, Profile+enrollment history, Sign out). Auth/Register + Link-Request-gate sit outside nav (app is gated until link approved).

---

## 7. Screen Specifications

> Each screen: **Purpose · Flow · Layout (top→bottom) · Components · States**. Shared patterns (SectionHeader, top app bar, list rows, FAB, dialogs, empty/loading/error) are per §5 unless noted.
>
> **Container rule:** where §5.5.5's presentation matrix marks a screen as a **DataTable**, its records are rendered with the §5.5 DataTable component (columns per the matrix; search/sort/filter/pagination/row-select/action-column/export as applicable; responsive horizontal-scroll on phones with a frozen identity column). The per-screen text below focuses on that screen's specific content, columns, actions, and states — read "list/cards" there as the matrix dictates.

### 7A. SHARED / AUTH SCREENS

**A1 · Splash (all apps)**
- *Purpose:* branded launch + auth check. *Flow:* shows 1–2s, routes to Login or Home.
- *Layout:* decorative navy blobs (top-right/bottom-left); centered **seal card** (170dp rounded-square, white, 8dp shadow, 88dp college/app icon navy); Eyebrow (app name, gold); serif **college name** 40sp navy centered; italic tagline flanked by 28×2dp gold dashes; slim progress bar (160×2dp, crimson fill on gray track); uppercase status caption ("INITIALIZING SECURE PORTAL…"); footer shield + "AUTHENTICATED ACCESS ONLY" + © line.
- *Components:* seal card, DecorativeBlob, LiveDot (top bar), progress. *States:* animated progress; on error → route to Login.

**A2 · Login (Admin/Teacher)**
- *Purpose:* email/password sign-in (no self-register). *Flow:* enter → validate → Firebase → Home / inline error.
- *Layout:* top app bar (icon + "THE ACADEMIC LEDGER" uppercase). Big **split card** (12dp, hairline): **left navy-gradient panel** (college name serif white, 48×4dp crimson rule, description, "CENTRAL/FACULTY LEDGER SYSTEM v…" micro) — on mobile it stacks above the form; **right form panel:** Eyebrow "SECURITY/FACULTY PORTAL" (gold) → serif "Admin/Teacher Login" 34sp → **underline email field** (uppercase micro-label) → **underline password** + eye toggle → "Forgot Password?" gold-underline link → inline error text (crimson) → **filled navy Login button** (uppercase) → loading spinner → hairline + fine-print disclaimer. Footer: portal name + © + Security/Terms/Support links.
- *States:* field validation, crimson error line, button→spinner loading, disabled while submitting.

**A3 · Forgot Password (Teacher; Admin optional)**
- *Layout:* app bar w/ back. Centered card: navy circle badge (72dp) + reset icon → serif "Credential Recovery" → body → underline email → error/success (green badge "Reset link sent") → filled "SEND RESET LINK" (trailing arrow) → "RETURN TO LOGIN" text link. Footer security line.

**A4 · Student Register / Login (toggle)**
- *Purpose:* students self-register then log in. *Layout:* same split-card pattern, emerald signature accent; a segmented toggle or link switches **Register ⇄ Login**; register adds confirm-password; helper copy explains next step (link request). *States:* validation, loading, error.

**A5 · Student Link-Request Gate**
- *Purpose:* after register/login, gate all features until admin approves the record link. *Layout:* centered illustration/icon; Eyebrow "ACCOUNT LINKING"; serif "Connect your record"; body; **roll-number field**; filled "SUBMIT REQUEST"; below, a **status card** (amber "Pending review" with pulse, or crimson "Rejected — resubmit"); pull-to-refresh to re-check. *States:* none/submitting/pending/approved(→enter app)/rejected.

---

### 7B. ADMIN APP

**B1 · Dashboard (Home tab)**
- *Purpose:* institutional overview + quick actions. *Layout:* app bar (avatar + "The Academic Ledger" serif + notification bell w/ badge + refresh). SectionHeader: Eyebrow "INSTITUTIONAL OVERSIGHT" → serif "Welcome, {name}." 36sp → crimson rule → italic "Session {term} · {session}". **StatCard bento row** (Students / Teachers / Departments / Pending Requests — alternating white/navy). **Active Term banner** (navy, term name + "Manage" gold CTA). **Quick Actions grid** (2-col cards: Add Student, Add Teacher, New Notification, Review Requests — icon badge + serif title). **Recent Alerts** list (numbered badge + serif title + body + uppercase meta). *States:* skeleton cards on load; empty alerts → "All caught up."

**B2 · Academics hub (tab)**
- *Layout:* SectionHeader "ACADEMICS". Card list linking to: Academic Terms, Departments, Class Offerings (each row = icon badge + title + subtitle count + chevron). *Empty:* n/a (static hub).

**B3 · Academic Terms — DataTable**
- *Layout:* SectionHeader "Academic Terms" + toolbar (search, Export). **DataTable** columns: Label (frozen) · Year · Type (Fall/Spring/Summer) · **Active** (StatusBadge) · Start–End dates · Actions (Activate / Delete). **FAB "+"** → Add-Term dialog (label, year, Fall/Spring/Summer selector). *States:* empty ("No terms yet — add one"), delete→confirm-destructive dialog (cascade warning: deletes all offerings for the term), skeleton rows.

**B4 · Departments — DataTable**
- *Layout:* SectionHeader "Departments" + toolbar. **DataTable** columns: Name (frozen) · Code · #Subjects · #Classes · Actions (View→detail / Delete). Tap row → Department Detail. FAB add (name, code dialog). Delete→confirm w/ cascade summary. Empty/skeleton.

**B5 · Department Detail (hub)**
- *Layout:* header w/ dept name; three big cards: **Subjects**, **Classes**, **Class Offerings (active term)** — each icon + title + count + chevron.

**B6 · Subjects — DataTable** — SectionHeader + toolbar (search, filter by semester/type). Columns: Code (frozen) · Name · Semester · Credits · Type (THEORY/LAB) · Actions (Edit/Delete). FAB add (code, name, semester, credit hours, THEORY/LAB toggle); delete→confirm (destroys attendance/marks/etc.). Empty state.

**B7 · Classes — DataTable** — columns: Name (frozen) · Semester · Session (Morning/Evening) · Actions. FAB add (semester 1–8, Morning/Evening); delete→confirm. Empty.

**B8 · Class Offerings — DataTable** — header shows active term; columns: Offering (frozen) · Semester · Session · #Students · Actions (Open→Offering Detail / Delete). FAB "Instantiate offering" → picker bottom sheet (choose class slot); delete→confirm. Empty ("No offerings for {term}").

**B9 · Offering Detail (hub)** — header offering name; cards: **Students**, **Teacher Assignments**, **Timetable**, **Attendance Report**, **Exams & Marks**, **Fee Structure**.

**B10 · Students (roster / all) — DataTable** — SectionHeader "Students"; table toolbar (search name/roll, filter by class/session/status, sort, Export, overflow). **DataTable** columns: ☑ · Avatar+Name (frozen) · Roll · Class/Offering · Status chip · Actions (View/Edit/Delete). Row-select → bulk Delete/Export/Notify contextual bar. Tap row → Student Detail (B11). **FAB add** (roll, name). Pagination/lazy load. Delete→confirm-destructive. Empty/skeleton/filtered-empty/offline.

**B11 · Student Detail + Enrollment History** — header avatar + name + roll + current class chips; **Enrollment history** as a table/timeline (Term · Class · Status chip PROMOTED/REPEATED/…); edit/delete actions.

**B12 · Teachers — DataTable** — toolbar (search, filter by dept, Export). **DataTable** columns: Avatar+Name (frozen) · Email · Department · Permissions (compact chips: Approve/Timetable/Notify) · Status · Actions (Edit/Delete). Row expand or a Permissions action opens the three MD3 switches (Approve link requests / Edit timetable / Send notifications). **FAB "Add teacher"** → dialog (name, email, temp password) with loading (secondary-auth create), inline error, success snackbar. Delete→confirm (revokes access). Empty/skeleton.

**B13 · Teacher Assignments (within offering) — DataTable** — SectionHeader; columns: Subject (frozen) · Assigned Teacher · Actions (Unassign). FAB assign → bottom sheet (pick subject, pick teacher). Empty ("No subjects assigned").

**B14 · Student Link Requests — DataTable** — SectionHeader "Link Requests"; columns: Claimed Roll (frozen) · Requester (email) · Matched? (green check / crimson "no match") · Requested (time) · Actions (**Approve** navy / **Reject** text). Approve triggers atomic link + snackbar. Empty ("No pending requests").

**B15 · Timetable Editor (within offering) — weekly grid** — header + **Save** action. **DataTable / weekly grid** (Day × time-slot cells, or rows of Day · Time · Subject · Teacher with "Unassigned" tag + gold stripe when assigned). FAB add period → dialog (day chips Mon–Sat, start/end time, subject picker, optional teacher). Remove per row/cell. Regenerates teacher schedules on save (snackbar). Empty.

**B16 · Attendance Report (within offering) — flagship DataTable** — SectionHeader; **table toolbar** (search by roll/name, filter sheet by subject + date range, **Export CSV/PDF**, refresh). **DataTable** columns: ☑ · Roll · Name (avatar+serif, frozen on scroll) · Subject · **P** · **A** · **L** · **%** (as AttendancePercentageBadge: green ≥75, amber 60–74, crimson <60) · Actions (View day-by-day). Sortable by roll/name/%; sticky header; numeric columns right-aligned; row-select → bulk export/notify. Pagination or lazy load for large rosters. *States:* skeleton rows, empty ("No attendance recorded"), filtered-empty, offline banner (cached rows persist).

**B17 · Exams & Marks (within offering)** — SectionHeader + **in-screen tabs: Marks | Exam Papers**. *Marks tab:* **DataTable** per exam type (Roll (frozen) · Name · Score /Max · Status: entered/"Not entered"). *Papers tab:* **DataTable** (Subject · File name · Submitted (time) · Actions: Open/Download/Delete) with subject filter. Empty per tab.

**B18 · Fee Structure (within offering)** — SectionHeader + tabs **Semester | Annual**; **editable DataTable (ScoreEntryGrid)** of fee-heads (Head · Amount · remove) with a running **Total** row; "Add head" field; **Save** button; per-student override entry point. Empty ("No structure set").

**B19 · Notifications (More) — DataTable** — SectionHeader "Notifications"; **DataTable** columns: Title (frozen) · Target (All/Admin/Teacher/Student/offering) · Sent (time) · Actions (Delete). FAB "Send" → dialog (title, message, target-role chips, optional offering). Empty.

**B20 · Profile (More)** — centered: college wordmark, "Administrator", email; Settings entry (theme, about); **Sign Out** (error-outlined).

---

### 7C. TEACHER APP

**C1 · Home (dashboard)** — app bar (teacher avatar + "Faculty Portal" serif + bell). SectionHeader: Eyebrow "SENIOR FACULTY" gold → serif "Good Morning,\n{name}." 40sp → navy 96×4dp rule. **Active/Current Session card** (LeftStripe navy): LiveDot + "CURRENT SESSION", subject serif, location·time row, filled "MARK ATTENDANCE" (trailing icon). **Faculty Dashboard** action grid (2-col cards → Mark Attendance, Marks Entry, Submit Paper, My Students). **Academic Deadlines** list (colored left-border rows: title serif + subtitle + due label crimson). *States:* empty session → "No live class right now"; skeletons.

**C2 · Attendance (tab)** — SectionHeader centered "Attendance Registry" serif + gold rule + "SESSION {term}" eyebrow. **Assigned Departments/Offerings** list (LeftStripeListCard, gold stripe = selected, navy = unselected; dept serif + course + icon). **Academic Period** semester selector (2 rows of 4 pill/outlined tiles; selected = amber fill + gold stroke). **Subject** picker (from teacher's own assignments only). Filled full-width **"MARK DAILY ATTENDANCE"** (elevated, trailing icon). **Faculty Integrity panel:** navy-header card + **RingGauge** (gold compliance %) + audit summary + DataChipGrid (Last Sync / Status). *States:* empty ("No assigned classes this term").

**C3 · Mark Attendance — data grid** — toolbar (back + "MARK ATTENDANCE" uppercase + overflow). **Session header card** (module eyebrow gold, subject serif, date·session row, capacity + "Mark all present" tonal). **Search field** (outlined, leading icon). **DataTable/grid**: rows = AvatarInitials + name + roll, trailing **SegmentedPAL** cell (Present/Absent/Leave; navy pill selected). **Status summary strip** (3 cards: Present green-stripe / Absent crimson-stripe / Leave gold-stripe, big serif counts). **Sticky Submit bar** ("SUBMIT ATTENDANCE REGISTRY", navy, trailing icon) floating above nav. *States:* search empty result; submit loading→success snackbar + admin notification; offline banner.

**C4 · Exams (tab)** — SectionHeader "Exam Management" + gold rule + "MARK ENTRY PARAMETERS". Selectors: **Academic Year** & **Semester** (dropdown cards), **Department/Offering** (selected card + change hint), **Assessment Type** = two **radio-as-card** options (Midterm / Sessional; selected = 2dp navy border + navy icon badge, unselected = tonal). Filled "PROCEED TO SUBJECT LIST" (trailing arrow) → subject picker → Marks Entry. Also entry to **Submit Exam Paper**. Disclaimer micro-copy.

**C5 · Midterm Mark Entry — editable DataTable** — page header (eyebrow + serif "Midterm Mark Entry" + body). Subject selector + search. **ScoreEntryGrid** sticky header (STUDENT IDENTITY · ROLL NUMBER · SCORE (MAX 25)); rows = avatar + name serif + meta, roll, numeric score input (serif, "/25", right-aligned). **Sticky bottom action bar:** Save Draft (outlined) + Submit Marks (navy, verified icon). *States:* empty roster; validation (0–25); save/submit loading + snackbar.

**C6 · Sessional Mark Entry — editable DataTable** — like C5 but multi-column: header STUDENT · ATTEND (5) · ASSIGN (5) · QUIZ (5) · TOTAL (15); each row three numeric inputs + auto **Total** serif cell (right-aligned). (Note: corrected to sum to **15**, not the old 50.) Sticky Save/Submit bar.

**C7 · Submit Exam Paper** — SectionHeader; subject chips (own assignments); **FileUploadPicker** button ("Choose PDF/DOCX") with upload progress; **DataTable** of prior submissions (File name · Submitted · Actions). *States:* uploading progress, success badge, error snackbar, empty.

**C8 · Schedule (tab) — weekly grid** — SectionHeader "My Schedule" + refresh. **DataTable / weekly grid** (Day × time, or Day · Time · Subject · Offering rows). If `canEditTimetable`: chips to pick an assigned offering → **edit mode** (add/remove periods, Save). *States:* empty ("No periods scheduled").

**C9 · Menu (tab)** — app bar (rotated navy badge + "Faculty Portal" + bell + avatar). **Administrative Actions:** e.g. "Approve Student Requests" card (crimson "ACTION REQUIRED" badge, AvatarStack of pending, navy "Review" button) — only if permitted. **Institutional Resources:** rows (gold icon + label + chevron) → My Students, etc. **Support & Account:** grouped card (Help, Account Settings). **Notifications** entry (view; "Send" only if permitted). **Sign Out** (error-outlined) + version footer.

**C10 · My Students (read-only) — DataTable** — SectionHeader; offering chips (own); table toolbar (search). **DataTable** columns: Avatar+Name (frozen) · Roll · Attendance % (badge) · Actions (View → read-only student info sheet). Sortable; responsive horizontal scroll. Empty/skeleton.

**C11 · Link Requests (Teacher, if permitted)** — same DataTable as Admin B14, gated; if not permitted → EmptyState ("Your admin hasn't granted this permission").

**C12 · Notifications (Teacher)** — list (targeted to teacher); if `canSendNotifications`, FAB "Send to students". Empty.

**C13 · Profile (Teacher)** — avatar, name serif, email, designation; Sign Out.

---

### 7D. STUDENT APP (designed fresh, emerald signature)

**D1 · Home (dashboard)** — app bar (avatar + "Student Portal" serif + bell). SectionHeader: Eyebrow "MY ACADEMICS" emerald → serif "Assalam-o-Alaikum,\n{name}." → emerald rule. **Overall Attendance RingGauge** card (big % + threshold color). **Next Class card** (LeftStripe navy: subject serif + time + room). **Quick links grid** (Attendance, Marks, Timetable, Fee Challan — icon cards). **Notifications preview** (latest 2). *States:* if unlinked → shouldn't reach here (gated at A5); skeletons; empty next-class.

**D2 · Attendance Summary (tab)** — SectionHeader "My Attendance" + refresh. **Top:** overall **RingGauge** card (total %). **Below: DataTable** of subjects — columns Subject (frozen) · P · A · L · **%** (badge). Sortable; numeric right-aligned; responsive horizontal scroll on phone. *States:* empty ("No attendance recorded"), offline banner, skeleton rows.

**D3 · Marks (tab) — DataTable** — SectionHeader "My Marks" + refresh. **DataTable** columns: Subject (frozen) · Midterm /25 · Sessional /15 · **Total** (right-aligned; "—" muted where not entered). Sortable by subject. *States:* empty, skeleton.

**D4 · Timetable (tab) — weekly grid** — SectionHeader "My Timetable" + refresh. **DataTable / weekly grid:** rows = time slots, columns = Mon–Sat (or a Day/Time/Subject/Room table); current day/period highlighted (gold). Horizontal scroll on phone with the time column frozen; optional day tab strip fallback. *States:* empty ("No timetable published").

**D5 · Fee Challan (More)** — SectionHeader "Fee Challan" + notice ("Informational only — pay at college"). Toggle **Semester | Annual**; **DataTable** preview of fee heads (Fee Head · Amount) + **Total** row; **"Download / Print Challan"** filled button → generates PDF (progress) → opens/share. *States:* generating spinner, error snackbar, empty (no structure).

**D6 · Notifications (More)** — list of role/class-targeted NotificationListItems; unread badge count on nav; mark-viewed on open. Empty.

**D7 · Profile + Enrollment History (More)** — centered avatar + name serif + roll; **Enrollment history** as a table/timeline (Term · Class · Status chip). Settings (theme), **Sign Out**.

**D8 · More (tab hub)** — card list → Fee Challan, Notifications, Profile.

---

## 8. Consistency Checklist (for Stitch prompts & implementation)
- Every screen opens with the **Eyebrow → Serif Headline → accent rule** header.
- Cards = white, 12dp, hairline stroke, near-flat; left-stripe for status.
- Navy = primary/structure; gold = active/dividers; crimson = alert/absent; green = present/success. App signature accent drives the active nav indicator + hero rule.
- Buttons uppercase Label Large; primary CTAs full-width w/ trailing icon; destructive = error styling + confirm dialog with cascade summary.
- **Choose the container per the §5.5.5 matrix:** Cards for summaries/stats/quick-actions, Lists for navigation/feeds, **DataTables for structured records/reports/schedules/attendance/marks/management**. Don't default to cards.
- Data tables use §5.5: sticky uppercase header, sortable columns, search/filter toolbar, right-aligned numerics, status badges, trailing action column, row-select bulk bar, pagination or lazy load, Export where useful, responsive horizontal-scroll with a frozen identity column on phones.
- Lists & tables: skeleton loading, EmptyState with create action, filtered-empty state, OfflineBanner on failed sync (cached data stays).
- Bottom nav on all three apps (Admin included), 5 tabs, pill active indicator.
- Serif for display/titles, Public Sans for body/labels; uppercase tracked micro-labels.
- Dark theme parity; ≥48dp targets; content descriptions; system font scaling.

---

## 9. Implementation Mapping (for later Compose work — not for Stitch)
- **Theme tokens** → `:app:common/ui/theme` (Color.kt, Type.kt, Shape.kt, Theme.kt). Add the full token set in §2; wire Newsreader + Public Sans font families (already bundled in the old modules).
- **Signature composites & DataTable** → `:app:common/ui/components` (SectionHeader, StatCard, LeftStripeCard, SegmentedPAL, RingGauge, ScoreEntryGrid, DataTable, AvatarInitials, StatusBadge, EmptyState/Loading/OfflineBanner, etc.), reused by all three app modules.
- **Per-app signature accent** injected via the shared theme (Admin crimson, Teacher gold, Student emerald).
- Screens map 1:1 to existing `feature/<name>` packages in `cmsadmin`/`cmsteacher`/`cmsstudent`; redesign replaces the current basic composables while keeping ViewModels/repositories unchanged.
