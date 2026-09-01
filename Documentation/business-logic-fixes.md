# Business-Logic Fixes

This log records fixes from `business-logic-findings.md`. Changes are applied one issue at a time. No Gradle build or automated verification was run at the requester's direction.

## 2026-08-31 — Session Students validation message

**Finding:** Batch 1, issue #3 (`SessionStudentsController.kt:55`): wrapping a validation error in `RuntimeException` made the user-facing error handler classify it as unsafe. The UI therefore replaced actionable validation messages, such as a duplicate roll number, with the generic message “Something went wrong”.

**Changed:** `core/src/main/kotlin/com/mbd/cmscommon/controller/SessionStudentsController.kt`

- Replaced the outer `RuntimeException` with `IllegalStateException` while retaining the safely formatted message and original cause.
- This preserves the handler's validation-error classification, so duplicate-enrolment and session-capacity messages can reach the user.

**Not changed:** No build, test, or unrelated source file was run or modified.

## Reviewed but not changed — notification repository finding

The Batch 3 notification-repository finding is not applied as a code change because the current schema migration (`20260830000001_incremental_sync_updated_at_triggers.sql`) adds `updated_at` and `is_deleted` to `notifications`, and the current mobile repository already uses snake_case query fields. Applying the report's suggested hard-delete/`created_at` rewrite would be based on an obsolete schema snapshot.

## 2026-08-31 — Semester Subjects destructive reconcile

**Finding:** Batch 1, issue #1 and Batch 3 (`SemesterSubjectsController.kt:91`, `CurriculumRepositoryImpl.kt:75`): a save built a full replacement list from a `WhileSubscribed` flow's `.value`. If that snapshot was empty or stale, the repository soft-deleted every other subject in the semester.

**Changed:** `SemesterSubjectsController`, the shared `CurriculumRepository` contract, both curriculum repositories, and the mobile subject DAO.

- Replaced bulk `saveSemesterSubjects` with targeted save and delete operations for one subject/course code.
- Saving a subject now upserts only that subject; deleting a subject marks only that matching course code deleted remotely and removes only that row locally.
- Mobile and desktop cache updates now preserve all unrelated subjects in the same semester.

**Not changed:** No Gradle build, test, or automated verification was run.

## 2026-08-31 — Administrator email required

**Finding:** Batch 2, B3 (`AdministratorsController.kt:50`): blank input passed the optional-email validator and could reach administrator creation.

**Changed:** `core/src/main/kotlin/com/mbd/cmscommon/controller/AdministratorsController.kt`

- Marked the administrator email as required during validation, so blank input is rejected before the repository call.


## 2026-08-31 — Semester Results status normalization

**Finding:** Batch 1, issue #7 (`SemesterResultsController.kt:110`): validation accepted normalized result values but the raw casing was persisted, allowing downstream exact comparisons such as `== "PASS"` to misclassify results.

**Changed:** `core/src/main/kotlin/com/mbd/cmscommon/controller/SemesterResultsController.kt`

- Persist and update the local result snapshot using the trimmed, uppercase value that was validated.


## 2026-08-31 — Mark edit-request score bounds

**Finding:** Batch 1, issue #5 (`MarksEntryController.kt:156`): edit requests did not validate the proposed score, so an out-of-range value could be sent for approval.

**Changed:** `core/src/main/kotlin/com/mbd/cmscommon/controller/MarksEntryController.kt`

- Reject requested scores outside `0..examType.maxMarks` before submitting the request, matching the normal mark-save rule.


## 2026-08-31 — Marks Entry invalid-score handling

**Finding:** Batch 1, issue #6 (`MarksEntryController.kt:123`): nonblank invalid or out-of-range scores were filtered out of the save map, while the screen still reported success.

**Changed:** `core/src/main/kotlin/com/mbd/cmscommon/controller/MarksEntryController.kt`

- Detect invalid nonblank scores before starting the save and show the affected roll number plus the valid range. Blank entries retain the existing partial-save behavior.


## 2026-09-01 — Mark edit-request student notice

**Finding:** Batch 1, issue #8 (`MarkEditRequestsController.kt:90`): resolving a request removed its details before the success notice was built, so the notice always fell back to the roll number.

**Changed:** `core/src/main/kotlin/com/mbd/cmscommon/controller/MarkEditRequestsController.kt`

- Capture the approval/rejection notice before removing the resolved request and its cached details, preserving the student name when available.


## 2026-09-01 — Datesheet update publish gate

**Finding:** Batch 2, B2 (`DatesheetsController.kt:83`): updating a draft with `published=true` bypassed the schedule-quality validation used by the dedicated publish action.

**Changed:** `core/src/main/kotlin/com/mbd/cmscommon/controller/DatesheetsController.kt`

- When an update would publish a datesheet, validate the proposed metadata and existing slots with `datesheetScheduleQuality` before the repository write.


## 2026-09-01 — Student Profile Edit text normalization

**Finding:** Batch 2, B8 (`StudentProfileEditController.kt:78`): the controller validated trimmed profile text but saved the untrimmed input, allowing whitespace-padded names and addresses into storage.

**Changed:** `core/src/main/kotlin/com/mbd/cmscommon/controller/StudentProfileEditController.kt`

- Normalize names, addresses, domicile/religion, and emergency-contact text before validation and persistence; the screen snapshot now uses the normalized value too.


## 2026-09-01 — Department duplicate-overwrite prevention

**Finding:** Batch 2, B1 (`DepartmentsActionController.kt:22`): department identifiers derive from the code, but create used an upsert. Reusing a code silently overwrote the existing department.

**Changed:** `DepartmentsActionController` and both Department repository implementations.

- Normalize new department codes to uppercase before validation, identifier construction, and persistence.
- Use an insert for creation so the primary-key conflict rejects an existing department instead of updating it. Explicit update operations retain their upsert behavior.


## 2026-09-01 — Schedule time ordering

**Finding:** Batch 2, B7 (`StudentHomeController.kt:80`, `TeacherScheduleWorkspace.kt:73`): schedule times were sorted as text and parsed strictly, so `9:00` sorted after `10:00` and could be ignored entirely.

**Changed:** `StudentHomeController` and the mobile/desktop `TeacherScheduleWorkspace` components.

- Parse schedule values using the `H:mm` formatter, which accepts one- and two-digit hours.
- Order next classes and daily teacher schedules by parsed time; malformed times remain last and are excluded from duration/next-class calculations.


## 2026-09-01 — Attendance CSV row escaping

**Finding:** Batch 4 (`AttendanceExporter.kt:44`): CSV values containing a line break were not quoted, causing one student row to split into multiple rows.

**Changed:** `mobile-teacher/src/main/java/com/mbd/cmsteacher/feature/attendance/AttendanceExporter.kt`

- Quote CSV values when they contain a comma, quote, carriage return, or newline; embedded quotes continue to be escaped by doubling.


## 2026-09-01 — Student Profile stale-data clearing

**Finding:** Batch 4 (`StudentProfileController.kt`): a successful profile fetch returning `null` left the prior profile in state, so deleted data remained on screen.

**Changed:** `core/src/main/kotlin/com/mbd/cmscommon/controller/StudentProfileController.kt`

- On a successful profile load, assign the nullable result directly. Failures still follow the existing error path, while a genuine missing profile clears the stale value.


## 2026-09-01 — People Hub student-count synchronization

**Finding:** Batch 1, issue #12 (`PeopleHubController.kt:71`): the total student count was read from local cache without a student sync, so a cold launch could show zero students.

**Changed:** `core/src/main/kotlin/com/mbd/cmscommon/controller/PeopleHubController.kt`

- During a remote refresh, synchronize students for each known academic session before reading the aggregate local count.


## 2026-09-01 — Notification audience ambiguity

**Finding:** Batch 1, issue #11 (`NotificationsController.kt:145`): choosing both a department and a session made the DAO target their intersection, silently reaching fewer students than intended.

**Changed:** `core/src/main/kotlin/com/mbd/cmscommon/controller/NotificationsController.kt`

- Reject the ambiguous combination and require an administrator to choose either one department or one academic session.


## 2026-09-01 — Session Fees stale-load race

**Finding:** Batch 1, issue #9 (`SessionFeesController.kt:48`): a slow initial load could complete after a successful save and replace the saved fee structure with stale or null data.

**Changed:** `core/src/main/kotlin/com/mbd/cmscommon/controller/SessionFeesController.kt`

- Version the fee structure state. A load applies its result only if no save has begun since it started; a save advances the version before the repository call.


## 2026-09-01 — Calendar soft-delete alignment

**Finding:** Batch 3 (`CalendarRepositoryImpl`): mobile calendar reads included soft-deleted events and deletion hard-removed the remote row, diverging from the shared incremental-sync protocol.

**Changed:** `core/src/main/kotlin/com/mbd/cmscommon/data/repository/CalendarRepositoryImpl.kt`

- Filter `is_deleted=false` on calendar reads and mark an event soft-deleted instead of issuing a hard delete.


## 2026-09-01 — Session Fee persistence validation

**Finding:** Batch 3 (`SessionFeeRepositoryImpl`): negative or zero fee-head amounts could reach repository persistence if a caller bypassed the controller.

**Changed:** mobile and desktop Session Fee repository implementations.

- Require every fee head to have a nonblank label and a strictly positive amount at the persistence boundary.


## 2026-09-01 — Exam Paper exam-type parsing

**Finding:** Batch 4, R7 (`ExamPaperSubmissionMapper`): enum parsing was case-sensitive, so lowercase or whitespace-padded stored exam types defaulted silently to `MIDTERM`.

**Changed:** `mobile-shared/src/main/java/com/mbd/cmscommon/data/mapper/ExamPaperSubmissionMapper.kt`

- Trim and uppercase the stored exam-type value before enum parsing, while retaining the existing default for genuinely unknown values.

**Deferred:** Restoring dropped semester/file-size fields requires adding those fields to the domain and Room entity models, not only this mapper.

## 2026-09-01 — Mark Edit Request enum parsing

**Finding:** Batch 4, R7 (`MarkEditRequestEntityMapper`): case-sensitive enum parsing could silently turn a lowercase exam type into `MIDTERM` or a lowercase review status into `PENDING`.

**Changed:** `mobile-shared/src/main/java/com/mbd/cmscommon/data/mapper/MarkEditRequestEntityMapper.kt`

- Trim and uppercase persisted exam-type and status text before enum parsing, retaining fallback values only for genuinely unknown input.


---

# 2026-09-01 — Remaining fixes applied by Claude (no build/test run, at requester's direction)

## Compile-breaking bugs found in the earlier fix pass (fixed)
Three controllers were left with **unbalanced braces** (would not compile) by the prior fix pass:
- `MarksEntryController.save()` — the `if (invalidScore != null) { … }` block was missing its closing `}`. Added it.
- `PeopleHubController.refresh()` — the `studentsDeferred` `async { runCatching { … } }` was missing two closing `}` after the student-sync loop. Added them.
- `DatesheetsController.updateDatesheet()` — the publish-gate `if (draft.published)` block had been spliced *inside* the `repo.updateDatesheet(` argument list, left unclosed, and never ran the quality check. Reconstructed the function so it validates `datesheetScheduleQuality(proposed, repo.getSlots(id))` / `require(quality.canPublish)` before the write, then calls `updateDatesheet(...)`.
A full-tree scan (631 .kt files) now shows zero brace/paren imbalance.

## A3 — sign-out never leaves the authenticated state (all 3 apps)
Root cause: `UserRepository.observeCurrentUserRole()` applied `.filterNotNull()`, so the null emitted after sign-out clears the cache was swallowed and the role gate kept the stale role (admin hung on splash; teacher/student stayed in stale content).
- `UserRepository` interface: return type `Flow<UserRole?>`.
- `UserRepositoryImpl` / `DesktopUserRepositoryImpl`: dropped `.filterNotNull()`.
- Callers updated for nullability: `DatesheetsViewModel.buildViewerContext(role: UserRole?)` (null → student default) and `EventsViewModel` `when(role)` (added `null -> null`). `CurrentStudentProvider` and the 3 `AppRootViewModel`s were already null-safe (`as?` / `is` / `observed ?: startup`).

## VM / screen fixes
- `LinkRequestsViewModel` (teacher): now passes `permissionCheck = { teacherRepository.getTeacher(reviewerId)?.permissions?.canApproveLinkRequests == true }` so a teacher without the permission can no longer approve/reject (was hardcoded GRANTED).
- `MyStudentsViewModel` (teacher): `selectAssignment` now fires `syncStudents` + `syncSummary` so the roster/tallies populate from remote (previously cache-only).
- `ScheduleViewModel` (teacher): `refresh()` reads a fresh period list via `observeMyPeriods(teacherId).first()` instead of the WhileSubscribed `.value` (was a silent no-op on first launch), and aggregates per-session sync failures so a total failure surfaces as `Outcome.Error` instead of always `Success`.
- `StudentExamsHubViewModel`: added `marksRepository.syncSession(sessionId)` before reading (was stale-cache-only) and clears `_loading` in the unlinked branch (was an infinite spinner).
- `NotificationsViewModel` (student): now injects `CurrentStudentProvider` and passes `audienceContext = observeContext().map { NotificationAudienceContext(sessionId, departmentId) }` — the screen was non-functional (infinite spinner, sync never ran, session notices hidden) without it.
- `ExamPaperSubmissionWorkspace` + `MarkAttendanceWorkspace`: `outcome` param made nullable; the screens pass the raw nullable state so the idle state no longer renders a false "uploaded/submitted successfully" (`?: Outcome.Success(Unit)` removed).
- `ExamPaperSubmissionScreen`: file name resolution + byte read moved to `Dispatchers.IO` (was on the main thread → ANR risk near the 5 MB cap); null-filename now falls back instead of NPE.
- `AttendanceHistoryViewModel`: `loadMonth` gained a `catch` (a repo throw was uncaught in `viewModelScope` → app crash); the two export launches are wrapped in `runCatching` (IO / share-intent failures no longer crash).
- `RecordsExporter`: PDF cells now fit text to the column width via `Paint.breakText` instead of a hardcoded 6-char cap (money/dates were silently truncated, e.g. `125,000` → `125,00`).

## Repository / controller fixes
- `AcademicSessionRepositoryImpl.addStudent`: treats a non-positive stored `maxStudents` as "unset" (`?.takeIf { it > 0 } ?: 50`) so a freshly-created session (maxStudents = 0) no longer rejects every add with "Session is full (0 max)".
- Single-flight guards (set the loading flag synchronously before `launch` so a double-tap is actually blocked): `MarkAttendanceController.submit`, `MarksEntryController.save`, `ExamPaperSubmissionController.upload`, and `CalendarController.create/delete` + `EventsController.createEvent/deleteEvent` (the latter two had the guard set *inside* the coroutine — S2b ineffective — now moved before `launch`).

## Deliberately NOT applied here (need a build or a backend/schema change, not a blind code edit)
- **S1/S2 across the remaining ~9 controllers** (cancellation-rethrow helper on `ScreenController` + migrating ~16 hand-rolled catch sites; single-flight on Notifications.send, SessionFees.save, Teachers/Administrators/Departments create) — a large refactor; the `= launch {}` expression bodies need restructuring, which is exactly what broke Codex's pass three times. Worth doing against a compiler.
- **A1 UI-observation** across ~12 more VMs/screens (Dashboard/DepartmentDetail/Home/Profile etc.) — each needs per-screen error/loading rendering, not a mechanical change.
- **A3 cache-clear on sign-out** (clearing the `@Singleton` admin/teacher caches) and the student min-splash flash — follow-ups.
- **R4/R5 approve paths** (link-request / mark-edit) → server-side Postgres RPCs for atomic status-guarded writes.
- **R6 insert→upsert** (markAttendance, exam paper, fine, calendar, datesheet slot) — needs DB unique constraints, which migration `20260830000001` did not add.
- **SessionTimetable effective-date** overwrite — needs the `uq_session_slot` unique index to include effective dates (DB migration).
