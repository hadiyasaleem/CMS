# CMS Teacher — Google Stitch Prompt

Here's a complete list of all screens for the **CMS Teacher app** (Govt Graduate College, Mandi Bahauddin) to generate as **mobile app designs** in Google Stitch:

_Target: native mobile (phone) app — portrait viewport, bottom navigation, FABs, bottom sheets/modals, thumb-reachable actions, and grids that scroll horizontally with a frozen first column on a narrow screen._

**Design language — "The Academic Ledger":** navy authority (`#000A1E` ink / `#002147` scholar navy), **Teacher signature accent = gold `#B18000` / bright gold `#F7BD48`**, crimson `#B22B1D` reserved for alerts/absent, off-white "paper" surfaces (`#F9F9F9`) with 1dp hairline card strokes (near-flat). Type: **Newsreader** serif for display/headlines, **Public Sans** for body/labels. Signature header on every screen: *Eyebrow* (tiny uppercase tracked gold label) → *Serif Headline* → 48×3dp **gold accent rule**. A teacher's "classes" are (session × subject) pairs like "IT-301 · BS IT · 2021–25 (M)" — always shown as selectable pill chips (navy fill when selected). Timetables use a **grid UI identical to the admin app**: frozen first column, time-range column headers that scroll horizontally, rich cells (course code + subject + meta).

## 🔐 Auth Flow

**1. Splash Screen**
Decorative 5%-opacity navy blobs; centered white seal card with college crest; gold eyebrow "FACULTY LEDGER SYSTEM"; serif college name; italic tagline flanked by gold dashes; slim gold progress bar; footer "AUTHENTICATED ACCESS ONLY".

**2. Teacher Login Screen**
Navy-gradient brand panel (serif college name + gold rule + "Faculty workspace for attendance, marks & schedule" + "Faculty Ledger System v1.0" micro) stacked above the form; gold eyebrow "FACULTY PORTAL" → serif "Teacher Login" → email + password fields (uppercase micro-labels, eye toggle) → inline crimson error → filled navy "LOGIN" (uppercase) with spinner state → "Forgot Password?" gold link with a "Reset link sent ✓" confirmation state → footnote "Teacher accounts are created by an Admin — self-registration is not available."

## 🏠 Main App (Bottom Nav: Home · Attendance · Exams · Schedule · Menu)

**3. Home / Faculty Dashboard**
- App bar: avatar + serif italic "The Academic Ledger" + notification bell
- SectionHeader: eyebrow "FACULTY PORTAL" (gold) → serif "Good day, Professor." → gold rule
- 2-col quick-action grid (white paper cards, outline icon top-left + uppercase label): Mark Attendance · Marks Entry · Exam Paper · My Students · My Schedule · Notifications
- Empty/new-teacher state: "No classes assigned yet — the admin assigns you via session timetables."

## ✅ Attendance

**4. Mark Attendance Screen**
- SectionHeader: eyebrow "ATTENDANCE REGISTRY" → serif "Mark Attendance" → italic today's date ("Friday · 10 Jul 2026")
- Horizontal-scroll row of **class chips** ("IT-301 · BS IT · 2021–25 (M)") — navy fill when selected
- Live tally strip: three status badges — Present (green) · Absent (crimson) · Leave (gold) — updating as statuses change
- Roster: white cards with rounded-square initials avatar + name (serif) + roll number, trailing **P / A / L pill chips** per student; **everyone defaults to Present** — teacher only taps exceptions
- Bottom: full-width navy "SUBMIT ATTENDANCE" with "Submitting…" state, inline "Attendance submitted ✓" (navy) or crimson error line
- States: no-classes empty state; "pick a class above" prompt; empty roster ("No students enrolled in this session yet")

## 📝 Exams

**5. Exams Hub**
SectionHeader "Examinations"; two navigation cards (navy-tint icon badge + serif title + subtitle + chevron): **Marks Entry** ("Midterm /25 · Sessional /15") and **Exam Paper Submission** ("Upload question papers").

**6. Marks Entry Screen**
- SectionHeader: eyebrow "ASSESSMENT" → serif "Marks Entry" → italic "Midterm · max 25 marks"
- Class chips row (same as Attendance) + exam-type chips: "MIDTERM /25" · "SESSIONAL /15"
- Score grid: rows of student name (serif) + roll with a compact numeric input per row (placeholder "0–25"); **prefilled with already-saved scores**; invalid/out-of-range entries ignored on save
- Full-width navy "SAVE MIDTERM MARKS" with saving state, "Marks saved ✓" confirmation, crimson error line
- States: no classes / pick-a-class / empty roster

**7. Exam Paper Submission Screen**
- SectionHeader "Exam Papers"; class chips row
- Upload card: outlined "Choose PDF/DOCX" button with upload progress state
- Prior submissions list: file-name cards (gold left stripe) with open + delete actions
- Empty state per class ("No papers submitted for this subject yet")

## 📅 Schedule

**8. My Schedule — Weekly Grid** ⭐
The admin-style **timetable grid**, personal edition:
- SectionHeader: eyebrow "FACULTY" → serif "My Schedule" → italic "9 period(s) this week" + refresh action
- **Grid**: frozen first column = days (Mon–Sat); column headers = time ranges ("08:00–09:30") scrolling horizontally under a navy-tint sticky header row; cells = navy-tint rounded chips with **course code (bold) + subject name + session label** ("2021–25 (M)"); hairline row dividers
- Read-only; empty state "No periods assigned to you yet — the admin adds you to session timetables."

## 📂 Menu

**9. Menu Hub**
SectionHeader "Menu"; navigation cards: My Students · Link Requests (only if permitted — gold "PERMITTED" badge) · Notifications · Profile; bottom crimson-outlined "SIGN OUT" + version footer.

**10. My Students Screen**
- SectionHeader: eyebrow "FACULTY" → serif "My Students" → italic "IT-301 — Operating Systems · 42 students"
- Class chips row
- Read-only roster: avatar + name (serif) + roll cards with a trailing **attendance % badge** (green ≥75 / amber 60–74 / crimson <60, "—" when no data yet)

**11. Link Requests Screen (permission-gated)**
List cards: "Claims roll number: IT-21-09" (serif) + requester email; **Approve** (navy text button) / **Reject** per row; per-row crimson error line ("No student record found — open that session's roster once, then retry"). No-permission state: centered empty state "Your Admin hasn't granted you permission to approve student link requests."

**12. Notifications Screen**
Feed of notice cards (crimson left stripe): serif title + body + uppercase meta (target · time). Empty state. FAB "Send" only if the teacher has the send-notifications permission → Send Notice dialog (title, message, target chips).

**13. Teacher Profile Screen**
Centered seal card: circular initials avatar (72dp) → gold eyebrow "FACULTY" → serif teacher name → email + designation muted; crimson-outlined full-width "SIGN OUT".

## 🔔 Notification

**14. Admin Notice Notification (System UI)**
Not a screen, but design it as a notification mockup: app icon, title "New notice from Admin", body preview ("Mid-term exams begin 20 July…"), timestamp, tap opens Notifications screen.

## 📋 Summary Table

| # | Screen | Nav Location |
|---|---|---|
| 1 | Splash | Auth flow |
| 2 | Teacher Login | Auth flow |
| 3 | Home / Faculty Dashboard | Bottom nav |
| 4 | Mark Attendance | Bottom nav (Attendance) |
| 5 | Exams Hub | Bottom nav (Exams) |
| 6 | Marks Entry | Sub-screen (Exams) |
| 7 | Exam Paper Submission | Sub-screen (Exams) |
| 8 | My Schedule — Weekly Grid | Bottom nav (Schedule) |
| 9 | Menu Hub | Bottom nav (Menu) |
| 10 | My Students | Sub-screen (Menu) |
| 11 | Link Requests | Sub-screen (Menu, permission-gated) |
| 12 | Notifications | Sub-screen (Menu) |
| 13 | Teacher Profile | Sub-screen (Menu) |
| 14 | Admin Notice Notification Mockup | System UI |

That's 14 screens/components covering the full user journey.
