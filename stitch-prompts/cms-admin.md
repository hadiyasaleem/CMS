# CMS Admin — Google Stitch Prompt

Here's a complete list of all screens for the **CMS Admin app** (Govt Graduate College, Mandi Bahauddin) to generate as **mobile app designs** in Google Stitch:

_Target: native mobile (phone) app — portrait viewport, bottom navigation, FABs, bottom sheets/modals, thumb-reachable actions, and data tables that scroll horizontally with a frozen first column on a narrow screen._

**Design language — "The Academic Ledger":** navy authority (`#000A1E` ink / `#002147` scholar navy), **Admin signature accent = crimson `#B22B1D`**, gold accents/dividers (`#B18000` / `#F7BD48`), off-white "paper" surfaces (`#F9F9F9`) with 1dp hairline card strokes (near-flat, minimal shadow). Type: **Newsreader** serif for display/headlines/serif titles, **Public Sans** for body/labels. Signature header motif on every screen: *Eyebrow* (tiny uppercase tracked gold/muted label) → *Serif Headline* → short 48×3dp **crimson accent rule**. Records/management screens use responsive **Data Tables** (sticky uppercase header, sortable columns, right-aligned numerics, status badges, trailing action column, horizontal scroll with a frozen identity column on phones), not card lists.

## 🔐 Auth Flow

**1. Splash Screen**
Decorative 5%-opacity navy blobs top-right/bottom-left; centered white "seal" card (rounded-square, soft shadow) with navy college crest; gold eyebrow "CENTRAL LEDGER SYSTEM"; serif college name; italic tagline flanked by gold dashes; slim crimson progress bar; footer shield + "AUTHENTICATED ACCESS ONLY".

**2. Admin Login Screen**
Split card (12dp, hairline): left navy-gradient panel (serif college name white + 48×4dp crimson rule + "Central Ledger System v1.0" micro) stacking above on mobile; right form panel — eyebrow "SECURITY PORTAL" (gold) → serif "Admin Login" → underline email field (uppercase micro-label) → underline password + eye toggle → "Forgot Password?" gold link → inline crimson error line → filled navy "LOGIN" button (uppercase) → spinner loading state → fine-print disclaimer.

## 🏠 Main App (Bottom Nav: Dashboard · Academics · People · Records · More)

**3. Dashboard / Command Center**
- App bar: avatar + serif "The Academic Ledger" + notification bell with badge + refresh.
- SectionHeader: eyebrow "INSTITUTIONAL OVERSIGHT" → serif "Welcome, {name}." → crimson rule → italic "Session {term} · {session}".
- **StatCard bento row** (horizontal-scroll, alternating white/navy cards): Students · Teachers · Departments · Pending Requests — giant serif italic number, uppercase micro-label, small crimson/gold bottom accent bar.
- Active-Term navy banner with "Manage" gold CTA.
- Quick-actions 2-col grid (icon badge + serif title): Add Student, Add Teacher, New Notification, Review Requests.
- Recent Alerts list (numbered badge + serif title + uppercase meta); empty → "All caught up."

## 📚 Academics

**4. Academics Hub**
SectionHeader "ACADEMICS" → list of navigation cards (icon badge + serif title + subtitle count + chevron): Academic Terms · Departments · Class Offerings.

**5. Academic Terms — Data Table**
Toolbar (search, filter, add). Columns: Label · Year · Type (Fall/Spring/Summer) · Active (crimson/gold status badge) · Dates · Actions (Activate / Delete). Active row carries a gold left-stripe accent. Navy FAB "+". States: skeleton rows, empty ("No terms yet — add one"), offline banner.

**6. Add / Edit Term Dialog**
White 16dp dialog, serif title; fields: label, academic year, term-type segmented selector (Fall / Spring / Summer); text Cancel + navy Create.

**7. Departments — Data Table**
Columns: Name (serif, frozen) · Code · #Subjects · #Classes · Actions. Tap row → Department Detail. Navy FAB (Add Department dialog: name, code). Delete routes through the confirm-destructive dialog.

**8. Department Detail Hub**
Header with dept name + code chips; three large navigation cards (icon + title + count + chevron): Subjects · Classes · Class Offerings (active term).

**9. Subjects — Data Table**
Columns: Course Code · Name (serif) · Semester · Credit Hrs (numeric) · Type (THEORY/LAB badge) · Actions. FAB → add dialog (code, name, semester, credit hours, THEORY/LAB toggle).

**10. Classes — Data Table**
Columns: Name (serif) · Semester (1–8) · Session (Morning/Evening badge) · Actions. FAB → add dialog. Empty state.

**11. Class Offerings — Data Table**
Header shows active term. Columns: Offering (serif) · Semester · Session · #Students (numeric) · Actions. Tap row → Offering Detail. FAB "Instantiate offering" opens a class-slot picker bottom sheet. Empty ("No offerings for {term}").

**12. Offering Detail Hub**
Header = offering name; navigation cards: Students · Teacher Assignments · Timetable · Attendance Report · Exams & Marks · Fee Structure.

**13. Teacher Assignments — Data Table**
Columns: Subject (serif) · Assigned Teacher (avatar + name, or "Unassigned" crimson tag) · Actions (Reassign / Unassign). FAB "Assign".

**14. Assign Teacher Bottom Sheet**
16dp top corners + drag handle; pick subject (chips) → pick teacher (searchable list with avatar + name + check state); navy "Assign" action.

**15. Timetable Editor**
Header + Save action; day-grouped list of period cards (day + time serif + subject·teacher; gold left-stripe if assigned, "Unassigned" tag if not). FAB "Add period" → dialog (day chips Mon–Sat, start/end time, subject picker, optional teacher). Save snackbar "Schedules regenerated."

**16. Fee Structure Editor**
SectionHeader + tabs **Semester | Annual**; editable ScoreEntryGrid-style rows (fee-head label + amount input) with running **Total** (serif); "Add head" field; navy Save button. Empty ("No structure set").

## 👥 People

**17. People Hub**
SectionHeader "PEOPLE" → navigation cards: Students · Teachers · Student Link Requests.

**18. Students — Data Table**
Toolbar: search (name/roll), filter (class/session/status), sort, Export, overflow. Columns: ☑ · Avatar+Name (frozen) · Roll · Class/Offering · Status chip · Actions (View/Edit/Delete). Row-select → contextual bar (bulk Delete / Export / Notify). Pagination or lazy-load. Navy FAB "Add". States: skeleton, empty, filtered-empty, offline.

**19. Student Detail + Enrollment History**
Header: avatar + name + roll + current-class chips; **Enrollment history** timeline (term → class → status chip: PROMOTED / REPEATED / …); edit/delete actions.

**20. Add Student Dialog**
Serif title "Add Student"; fields: roll number, full name; Cancel + navy Add. (Roll number becomes the student's identity key.)

**21. Teachers — Data Table**
Toolbar (search, filter by dept, Export). Columns: Avatar+Name (frozen) · Email · Dept · Permissions (compact "N/3" gold badge) · Status · Actions (Permissions / Delete). Navy FAB "Add teacher".

**22. Add Teacher Dialog**
Serif title; fields: full name, email, temporary password; loading state (secondary-auth account creation) → success snackbar; inline crimson error.

**23. Teacher Permissions Dialog**
"{name} · Permissions" serif title; three MD3 switch rows: Approve link requests · Edit timetable · Send notifications; Done.

**24. Student Link Requests**
List cards: "Claims roll {X}" serif + requester email + crimson note if no matching record; **Approve** (navy) / **Reject** (text) per row; per-row error line if a claim can't be resolved. Empty ("No pending requests").

## 📊 Records

**25. Records Hub**
SectionHeader "RECORDS" → navigation cards: Attendance Reports · Exams & Marks · Fees overview.

**26. Attendance Report — flagship Data Table**
Toolbar: search (roll/name), filter sheet (subject + date range), **Export CSV/PDF**, refresh. Columns: ☑ · Roll · Name (avatar+serif, frozen) · Subject · P · A · L · **%** (AttendancePercentageBadge — green ≥75, amber 60–74, crimson <60) · Actions (day-by-day). Sortable, sticky header, right-aligned numerics, row-select bulk export/notify. Skeleton / empty / filtered-empty / offline states.

**27. Exams & Marks**
In-screen tabs **Marks | Exam Papers**. *Marks:* table (Roll · Name · Subject · Midterm/Sessional score or "Not entered" muted). *Papers:* subject filter chips → submission cards (file name + open/download + delete). Empty per tab.

## ⚙️ More

**28. More Menu**
List rows (gold icon + label + chevron): Notifications · Profile · Settings (theme, about) · **Sign Out** (error-outlined) + version footer.

**29. Notifications — Data Table / List**
Authored notifications (title serif + body + target-role chip + time). Navy FAB "Send". Delete per item. Empty state.

**30. Send Notification Dialog**
Serif title; fields: title, message; target-role chips (All / Admin / Teacher / Student); optional offering; navy Send.

**31. Profile**
Centered: college wordmark, "Administrator", email; Settings entry; **Sign Out** (error-outlined).

## ⚠️ Shared Components & States

**32. Confirm-Destructive Dialog**
White 16dp dialog, serif title ("Delete {name}?"), body summarizing the **cascade** ("This permanently deletes … 42 attendance records. Cannot be undone."), error-colored Confirm + text Cancel.

**33. Global States Mockup Set**
Not a screen — design the reusable states: skeleton shimmer rows (table + card), EmptyState (64–88dp outlined icon + serif line + primary action), and a slim amber "Offline — showing cached data" banner under the app bar.

## 📋 Summary Table

| # | Screen | Nav Location |
|---|---|---|
| 1 | Splash | Auth flow |
| 2 | Admin Login | Auth flow |
| 3 | Dashboard / Command Center | Bottom nav |
| 4 | Academics Hub | Bottom nav (Academics) |
| 5 | Academic Terms | Sub-screen (Academics) |
| 6 | Add/Edit Term Dialog | Modal from Academic Terms |
| 7 | Departments | Sub-screen (Academics) |
| 8 | Department Detail Hub | Sub-screen (Academics) |
| 9 | Subjects | Sub-screen (Dept Detail) |
| 10 | Classes | Sub-screen (Dept Detail) |
| 11 | Class Offerings | Sub-screen (Academics) |
| 12 | Offering Detail Hub | Sub-screen (Offerings) |
| 13 | Teacher Assignments | Sub-screen (Offering Detail) |
| 14 | Assign Teacher Bottom Sheet | Modal from Teacher Assignments |
| 15 | Timetable Editor | Sub-screen (Offering Detail) |
| 16 | Fee Structure Editor | Sub-screen (Offering Detail) |
| 17 | People Hub | Bottom nav (People) |
| 18 | Students | Sub-screen (People) |
| 19 | Student Detail + Enrollment History | Sub-screen (Students) |
| 20 | Add Student Dialog | Modal from Students |
| 21 | Teachers | Sub-screen (People) |
| 22 | Add Teacher Dialog | Modal from Teachers |
| 23 | Teacher Permissions Dialog | Modal from Teachers |
| 24 | Student Link Requests | Sub-screen (People) |
| 25 | Records Hub | Bottom nav (Records) |
| 26 | Attendance Report | Sub-screen (Records) |
| 27 | Exams & Marks | Sub-screen (Records) |
| 28 | More Menu | Bottom nav (More) |
| 29 | Notifications | Sub-screen (More) |
| 30 | Send Notification Dialog | Modal from Notifications |
| 31 | Profile | Sub-screen (More) |
| 32 | Confirm-Destructive Dialog | Modal (global) |
| 33 | Global States Mockup Set | Shared components |

That's 33 screens/components covering the full user journey.
