# Specific error messages — manual QA checklist

Goal: whenever something fails, the user is told **what** failed and **why** — never a bare
"Something went wrong". This checklist is what automated tests cannot cover: seeing the real message on a
real screen, on all three roles, mobile and desktop.

Automated coverage (run `./gradlew :core:test`):

| Test | What it protects |
|---|---|
| `ErrorMessageMatrixTest` | One row per failure kind (typed, network, database, edge function, sign-in, unknown): kind, wording, no internals, Ref code only for unknowns |
| `ErrorMessageSourceGuardTest` | Source scan: no stray "Something went wrong", no vague "Some … could not be loaded", every controller `launch` labelled, no raw `exception.message` shown, no untyped `IllegalArgument`/`IllegalState` throws |
| `ErrorMessageUiGuardTest` | Source scan of ViewModels, desktop screens and controllers: no reliance on the default generic fallback, no `catch` that swallows a failure, no fixed sentence set inside a `catch`, every dropped `runCatching` carries a `// Best-effort: <why>` comment |
| `EdgeFunctionGuardTest` | Source scan of `supabase/functions/**/*.ts`: no generic `httpError` message, a specific code on every 5xx, no raw `error.message` echoed, no hand-built responses, `dbError`/`authError` fallbacks that say what to do next |
| `ErrorMessageMatrixExtrasTest` | Transient/platform database codes, edge-function HTTP statuses (401/403/404/422/429/500/502/503), file-picker and export failures |
| `DatabaseScenarioMessagesTest` | (needs the local tools, otherwise skipped) real errors from a scratch Postgres, and constraint names that still exist |
| `ErrorClassifierTest`, `EdgeFunctionErrorsTest`, `AuthAndFileErrorsTest` | Detailed wording of the classifier, edge function errors, sign-in and file errors |
| `FailureSummaryTest`, `HubFailureMessageTest` | "Couldn't load fees and marks (no connection)" style aggregation |
| `AdminControllerPreChecksTest`, `TeacherControllerChecksTest`, `TimetableConflictMessageTest` | Controller pre-check wording, timetable conflict detail |

## Before you start

- **Two things are not live until you do them:** the database migration `20260930000000` (plain-words database
  errors) must be applied, and the six edge functions redeployed (real edge-function messages). Until then
  sections marked **[needs migration]** / **[needs deploy]** will still show the older wording.
- Use a test project or a copy of the data. Several checks create and delete records.
- To simulate "offline": turn on airplane mode (mobile) or disable the network adapter (desktop).
- Message wording below is the expected text; small differences in names/numbers are fine, but the message must
  name the same cause.
- Anything that shows only "Something went wrong. Please try again." with no action and no reason is a **bug** —
  note the screen and file it.

How to read an "unexpected" message: `Couldn't save the teacher. (Ref 1A2B)`. The action is specific and the Ref
code lets a developer find the logged cause. It should be rare; if a check below shows a Ref, it is worth a bug
report unless the check says otherwise.

## 1. Sign-in and account (all three apps, mobile + desktop)

| # | Do this | Expect |
|---|---|---|
| 1.1 | Sign in with a wrong password | "The email or password is incorrect." |
| 1.2 | Sign in with an unconfirmed email | "Confirm your email address before signing in…" (mentions spam folder) |
| 1.3 | Sign up with an email that already exists | "An account with this email already exists. Try signing in, or use Forgot password." |
| 1.4 | Sign up with a very short password | Says the password is too weak / how long it must be |
| 1.5 | Sign in offline | "Unable to connect. Check your internet connection and try again." |
| 1.6 | Tap Forgot password with an empty/invalid email | Asks for a valid email first (no request sent) |
| 1.7 | Send a password reset repeatedly | "Too many attempts. Please wait a moment and try again." |
| 1.8 | Student desktop profile → Reset password while offline | Error shown on the profile screen (previously silent) |
| 1.9 | Sign in as an account with no role assigned | "Your account has no role assigned yet. Contact an administrator." |
| 1.10 | Sign in with an email that has no CMS profile | "No CMS profile exists for this account. Contact an administrator to set it up." |

## 2. Refresh and dashboard loading (all roles)

| # | Do this | Expect |
|---|---|---|
| 2.1 | Press the top-bar refresh with the network off | Progress dialog, then **"Refresh incomplete"** listing what could not refresh, e.g. "Couldn't refresh administrators, departments, … (no connection)." — never just closing silently |
| 2.2 | Refresh with the network on | No dialog |
| 2.3 | Admin **More / People / Records / Exams** hubs with the network off | Each names the failed summaries, e.g. "Couldn't load teachers and student counts (no connection)." |
| 2.4 | Student **Exams** and **More** hubs offline | Names marks / results / datesheets (or calendar / fee details / profile) |
| 2.5 | Student **Datesheets** and **Marks** refresh offline | "Couldn't load datesheets (no connection)." / "Couldn't refresh marks and subjects (no connection)." |
| 2.6 | Teacher **Schedule** refresh offline | "Couldn't refresh departments, sessions and timetables (no connection)." |
| 2.7 | Admin **Master Timetable** refresh offline | "Couldn't refresh … (no connection)." naming the parts |
| 2.8 | Student **Profile** refresh offline | Names student details / fines / your profile |
| 2.9 | Teacher **My Students** → pick a class offline with an empty cache | A notice above the list: "Couldn't refresh the student list and attendance summary (no connection)." — so an empty list is not read as an empty class |
| 2.10 | Admin **Semester Results** → Refresh offline | Saved results still shown, with "Showing saved results. Couldn't refresh … (no connection)." |
| 2.11 | Admin **Student Record** with one area unreadable | Record opens; message names the missing areas |
| 2.12 | **Mark Attendance** for a past date offline | "Couldn't load the saved register for <date> (no connection). Check your connection before marking this register." (the register must not silently look unmarked) |
| 2.13 | Student **Link request** → pick a session offline | "Couldn't load the available roll numbers." (mobile and desktop) |
| 2.14 | Teacher **Marks entry** → pick a class while the local cache is unreadable / offline first-run | "Couldn't load the marks screen" dialog naming why pending edit requests could not load |
| 2.15 | Admin **Submitted papers** page opened offline | "Couldn't refresh submitted papers" dialog with the reason (list still shows saved papers) |
| 2.16 | **Semester results** (admin and teacher) → pick a class offline with nothing cached | "Couldn't load semester results" dialog with the reason |
| 2.17 | **Mark attendance** hits any unexpected failure loading or submitting | "Couldn't complete the attendance action" dialog, or the inline outcome notice — never nothing |

## 3. Admin — create / edit / delete

| # | Do this | Expect |
|---|---|---|
| 3.1 | Add a department with a code/name that exists | Names the duplicate (code or name) |
| 3.2 | Delete a department that still has sessions | Says it still has sessions and to remove them first |
| 3.3 | Add a session for a year that already exists | Says that session already exists |
| 3.4 | Add a student with a roll number already in the session | "Roll number IT-22-01 is already in this session." |
| 3.5 | Add a student to a full session | "This session is full (N students maximum). Raise the session capacity before adding more students." |
| 3.6 | Set session capacity to 0 or above the maximum | "Student capacity must be between 1 and …" |
| 3.7 | Add a teacher with a duplicate email | Names the duplicate email |
| 3.8 | Add a room/building with a duplicate name | Names the duplicate |
| 3.9 | Save a timetable period that clashes (room) | Names the room, day, time **and** the class / subject / semester it clashes with |
| 3.10 | Save a timetable period that clashes (teacher) | Names the teacher and the class / subject / semester it clashes with |
| 3.11 | Save fee structure with a blank head label or zero amount | "Every fee head needs a label." / "Every fee amount must be greater than zero." |
| 3.12 | Save term dates with end before start | "End date cannot be before start date." (and the server's reason if the save itself fails) |
| 3.13 | Approve a link request whose roster record is gone | "The selected academic session is no longer available." or the roll number problem — never the request id |
| 3.14 | Approve a link request with no roll number | "This link request has no roll number, so it cannot be approved. Ask the student to submit it again." |
| 3.15 | Approve a mark edit request whose marks row was removed | "The marks record this request refers to could not be found. It may have been removed." |
| 3.16 | Create/delete anything as a user without permission | "You do not have permission to perform this action." |
| 3.17 | **[needs migration]** Trigger a database constraint (e.g. duplicate via a second device) | Plain-words constraint message — no `violates … constraint` text |

## 4. Teacher

| # | Do this | Expect |
|---|---|---|
| 4.1 | Submit attendance with no students on the register | "This class has no students on its register, so there is nothing to submit." |
| 4.2 | Submit attendance for a day another device already marked | "Attendance for <subject> on <date> was already marked for N of M students. It has been reloaded; nothing was overwritten." |
| 4.3 | Enter marks above the maximum | Names the roll number and the allowed range |
| 4.4 | Request a mark edit for a student with no saved score | "<roll> has no saved <exam> score yet, so there is nothing to change. Enter the score directly." |
| 4.5 | Upload an exam paper that is too large / wrong type | Says the size limit / accepted type |
| 4.6 | Pick a photo/PDF that cannot be read | "Couldn't read the selected file." with the reason |
| 4.7 | Datesheet slot that clashes with another exam | Names the room/teacher/date it clashes on |

## 5. Student

| # | Do this | Expect |
|---|---|---|
| 5.1 | Submit a link request with an invalid date of birth / roll / CNIC | Field-specific message ("Choose a valid date of birth.", …) |
| 5.2 | Submit a link request for a session that was removed | "The selected academic session is no longer available." |
| 5.3 | Open Fee challan with no fee structure | Says the fee structure has not been set for the shift |
| 5.4 | Sign out while offline | Signs out locally without an error |

## 6. Exports and files (admin and teacher, mobile + desktop)

| # | Do this | Expect |
|---|---|---|
| 6.1 | Export a report to a read-only / full location | "Couldn't export the report" dialog with the reason (permission / disk full / cancelled) |
| 6.2 | Import a student list that is not a valid Excel/CSV file | Says the file could not be read and what format is expected |
| 6.3 | Import a list with bad rows | Per-row messages: "Row 7: Roll number … is already in this session." |
| 6.4 | Open a submitted paper that was deleted | Names the file and says it is no longer available |

## 7. Edge functions **[needs deploy]**

| # | Do this | Expect |
|---|---|---|
| 7.1 | Archive-and-delete an **active** session | "This session is still active. Deactivate it before archiving and deleting it." |
| 7.2 | Call an admin-only function as a teacher | "You don't have permission to do this." |
| 7.3 | Use a function while signed out / token expired | "Your session has expired. Sign in again." |
| 7.4 | Trigger a server-side failure | Sentence plus a Ref code, never a raw HTTP status or stack text |

## 8. Cross-cutting

- [ ] Launching offline does **not** pop an error dialog: the startup data load (`refreshAll` in the mobile AppRootViewModels and desktop `Main.kt`) is deliberately silent — the app is offline-first, and the hubs/screens then name what could not load. Only the manual refresh button reports a "Refresh incomplete" dialog.
- [ ] No dialog anywhere is titled just "Something went wrong" (titles say what you were doing, e.g. "Couldn't update buildings and rooms").
- [ ] Error dialogs on desktop and mobile scroll when the message is long and never fill the full window height.
- [ ] No message contains a URL, SQL, JSON, a stack trace, an exception class name, a UUID or someone else's email.
- [ ] Dismissing an error and repeating the action shows the same message (not a stale one, not none).
- [ ] The Ref code in an unexpected message also appears in the app's critical log (Settings → logs / `app_logs` table).

## Known wording divergences (UI text vs controller text)

Some pre-validation lives in the shared composables as well as in the controller, so the same mistake can read two ways
depending on which check fires first. Both are specific and correct; they just differ. Unify them if you touch either side.

| Where | Composable says | Controller says |
|---|---|---|
| Term dates dialog (`SemesterCurriculumWorkspace`, mobile + desktop) | "Use a valid YYYY-MM-DD start date." / "Use a valid YYYY-MM-DD end date." / "End date cannot be before start date." | `SemesterSubjectsController`: "Enter the start date as YYYY-MM-DD (for example 2026-09-01)." / "The term can't end (…) before it starts (…)." |
| Fine amount (`StudentProfileWorkspace`) | "Enter an amount greater than zero." | `StudentProfileEditController.issueFine`: "Fine amount must be greater than zero." |
| Session capacity (`SessionOperationsWorkspace`) | shared `capacityError` | shared `capacityError` (identical — no divergence) |

## When you find a generic message

1. Note the screen and the steps.
2. Find the controller/ViewModel that shows it. Controller failures should go through
   `launch("<what the user was doing>")`; several-things-at-once loads should use `FailureSummary`.
3. Add a row to `ErrorMessageMatrixTest` if it is a new kind of failure, then fix the wording at its source.
4. `ErrorMessageSourceGuardTest` will fail if the same pattern is reintroduced.
