# CMS Student — Google Stitch Prompt

Here's a complete list of all screens for the **CMS Student app** (Govt Graduate College, Mandi Bahauddin) to generate as **mobile app designs** in Google Stitch:

_Target: native mobile (phone) app — portrait viewport, bottom navigation, FABs, bottom sheets/modals, thumb-reachable actions, and grids/tables that scroll horizontally with a frozen first column on a narrow screen._

**Design language — "The Academic Ledger":** navy authority (`#000A1E` ink / `#002147` scholar navy), **crimson `#B22B1D`** for the structural accent rule + alerts/absent, **gold `#B18000` / `#F7BD48`** for eyebrows + the "STUDENT LEDGER" wordmark, **Student signature = emerald `#2E7D32`** used only for positive/growth semantics (good attendance, present, the overall %). Off-white "paper" surfaces (`#F9F9F9`), 1dp hairline card strokes (near-flat). Type: **Newsreader** serif for display/headlines, **Public Sans** for body/labels/numbers. Every screen: navy crest badge + **gold uppercase "STUDENT LEDGER" wordmark** top bar; then *gold Eyebrow → serif Headline → 48×3dp crimson accent rule → optional italic subtitle*. Timetable uses the **same grid UI as the admin/teacher apps** (frozen first column, time-range column headers scrolling horizontally, rich cells). Attendance % badges are color-coded: emerald ≥75, amber 60–74, crimson <60. Everything is **read-only** — the student never edits records.

## 🔐 Auth Flow

**1. Splash Screen**
Decorative 5%-opacity navy blobs; centered white seal card with college crest; gold eyebrow "STUDENT LEDGER SYSTEM"; serif college name; italic tagline flanked by gold dashes; slim crimson progress bar; footer "AUTHENTICATED ACCESS ONLY".

**2. Register / Login Screen (toggle)**
Navy-gradient brand panel (crest + serif "STUDENT LEDGER" white + gold rule + "Your attendance, marks, timetable & fee challan" + "Student Ledger System v1.0") stacked above a white form panel. A segmented **Register ⇄ Login** toggle at top. Fields: email + password (uppercase micro-labels, eye toggle); **Register** adds a confirm-password field. Gold eyebrow "STUDENT PORTAL" → serif "Create Account" / "Student Login" → crimson rule → filled navy CTA ("REGISTER" / "LOGIN") with spinner state → "Forgot Password?" gold link. Helper footnote: "After registering you'll link your account to your college roll number — an admin/teacher approves it before your data appears." States: field validation, crimson error line, loading.

**3. Link-Request Gate Screen**
Shown after register/login until the account is linked to a student record — gates the whole app.
- Centered emerald outline "account linking" illustration/icon; gold eyebrow "ACCOUNT LINKING"; serif "Connect your record"; body copy
- **Roll number field** (e.g. "IT-21-09") + filled navy "SUBMIT REQUEST"
- Below: a **status card** reflecting the request — *None* (form only) / *Pending review* (amber left-stripe card with a pulsing dot, "An admin or teacher will approve shortly") / *Rejected* (crimson left-stripe, "Resubmit with the correct roll number")
- Pull-to-refresh re-checks; on approval, auto-enters the app

## 🏠 Main App (Bottom Nav: Home · Attendance · Marks · Timetable · More)

**4. Home / Student Dashboard**
- Top bar: crest + gold "STUDENT LEDGER" wordmark + notification bell (unread red dot)
- SectionHeader: gold eyebrow "MY ACADEMICS" → serif "Assalam-o-Alaikum, {name}." → crimson rule → italic "BS IT · Semester 5 · 2021–25 (Morning)"
- **Overall Attendance RingGauge card**: large circular gauge with big serif % in the center (arc color emerald/amber/crimson by threshold) + "OVERALL ATTENDANCE" caption
- **Next Class card** (navy left-stripe): gold "NEXT CLASS" eyebrow + subject serif + time + room
- **Quick links grid** (2-col paper cards, centered outline icon + uppercase label): Attendance · Marks · Timetable · Fee Challan
- **Notices preview**: latest 2 notice rows (title serif + time)
- States: skeleton on load; "No class scheduled today" for the next-class card

## ✅ Attendance

**5. Attendance Summary Screen**
- SectionHeader: gold eyebrow "MY ATTENDANCE" → serif "Attendance" → crimson rule; refresh action
- **Overall RingGauge card** at top (total % across subjects, emerald/amber/crimson)
- **Per-subject Data Table**: columns Subject (frozen) · P · A · L · **%** (color-coded badge); numeric columns right-aligned; horizontal scroll on phone with Subject frozen
- Tapping a subject row opens the Subject Attendance Detail sheet
- States: skeleton rows, empty ("No attendance recorded yet"), offline banner

**6. Subject Attendance Detail Bottom Sheet**
Triggered by tapping a subject row on Attendance Summary. 16dp top corners + drag handle; subject name serif + its P/A/L/% summary; a compact list of recorded class-days with a P/A/L status chip each (read-only).

## 📊 Marks

**7. Marks Screen**
- SectionHeader: gold eyebrow "MY MARKS" → serif "Marks" → crimson rule; refresh
- **Data Table**: columns Subject (frozen) · Midterm /25 · Sessional /15 · **Total /40** (right-aligned; "—" muted where not entered); subtle emphasis on Total
- Optional summary chip row: overall average
- States: empty ("No marks entered yet"), skeleton

## 📅 Timetable

**8. Timetable Screen — Weekly Grid** ⭐
The admin/teacher-style **grid**, read-only:
- SectionHeader: gold eyebrow "MY TIMETABLE" → serif "Timetable" → crimson rule; refresh
- **Grid**: frozen first column = days (Mon–Sat); column headers = time ranges ("08:00–09:30") scrolling horizontally under a navy-tint sticky header; cells = navy-tint rounded chips with **course code (bold) + subject + teacher/room**; current day/period subtly highlighted (gold)
- Empty state ("No timetable published for your session yet")

## 📂 More

**9. More Hub**
SectionHeader "More"; navigation cards (icon badge + serif title + subtitle + chevron): Fee Challan · Notifications · Profile; bottom crimson-outlined "SIGN OUT" + version footer.

**10. Fee Challan Screen**
- SectionHeader: gold eyebrow "FINANCIALS" → serif "Fee Challan" → crimson rule; amber notice banner ("Informational only — pay at the college bank counter")
- **Semester | Annual** segmented toggle
- Read-only fee-head list (label + amount rows) inside a bordered container + bold **Total** row (navy stat card, "Rs 25,000")
- Filled navy **"DOWNLOAD / PRINT CHALLAN"** button → generating progress state → share/open
- States: generating spinner, empty ("No fee structure set for your department")

**11. Notifications Screen**
Feed of notice cards (crimson left stripe): serif title + body + uppercase meta (from · time); unread rows carry a small emerald dot; mark-viewed on open. Unread count badge drives the bottom-nav/bell dot. Empty state.

**12. Profile + Enrollment Screen**
Centered seal card: circular initials avatar (72dp) → gold eyebrow "STUDENT" → serif student name → roll number + email muted → dept/session chips ("BS IT · 2021–25 · Morning"). Below: **Current Semester** callout ("Semester 5 of 8"). Settings row (theme). Crimson-outlined full-width "SIGN OUT".

## 🔔 Notification

**13. Admin/Teacher Notice Notification (System UI)**
Not a screen, but design it as a notification mockup: app icon, title "New notice", body preview ("Mid-term date sheet published…"), timestamp; tapping opens the Notifications screen.

## 📋 Summary Table

| # | Screen | Nav Location |
|---|---|---|
| 1 | Splash | Auth flow |
| 2 | Register / Login (toggle) | Auth flow |
| 3 | Link-Request Gate | Auth flow (gate) |
| 4 | Home / Student Dashboard | Bottom nav |
| 5 | Attendance Summary | Bottom nav (Attendance) |
| 6 | Subject Attendance Detail Sheet | Modal from Attendance |
| 7 | Marks | Bottom nav (Marks) |
| 8 | Timetable — Weekly Grid | Bottom nav (Timetable) |
| 9 | More Hub | Bottom nav (More) |
| 10 | Fee Challan | Sub-screen (More) |
| 11 | Notifications | Sub-screen (More) |
| 12 | Profile + Enrollment | Sub-screen (More) |
| 13 | Notice Notification Mockup | System UI |

That's 13 screens/components covering the full user journey.
