# Business-Logic Verification Checklist

One row per business-logic file across the 3 mobile apps + shared `core`/`mobile-shared` (110 files). Pure data classes (domain models, DTOs, repository interfaces) and DI wiring are excluded. Per-file focus = what to verify; **bold** = computation/logic-heavy (highest bug risk). 🔵 = a live task chip already exists (the chip queue caps at 20 pending, so chips can't cover all 110 — this list is the source of truth).

_For each file verify: state transitions, loading/error/empty handling around suspend calls, coroutine scope/cancellation, race conditions, and null/empty edge cases — plus the per-file focus. Report findings as file:line. Not compiler-verified (Gradle loopback blocked in-env)._

> **Batch 1 complete:** the 20 🔵 controllers were verified — synthesis in [business-logic-findings.md](business-logic-findings.md). 4 systemic patterns + 12 ranked bugs found.

## core — controllers (39)

- [x] `AdministratorsController` — admin CRUD, state, permission gating — reviewed (batch 2)
- [x] `CalendarController` — create/delete/refresh events, audience filtering — reviewed (batch 2)
- [x] `DashboardController` — aggregated metrics/counts, empty state — reviewed (batch 2)
- [x] `DatesheetsController` — datesheet scheduling/validation — reviewed (batch 2)
- [x] `DepartmentDetailController` — dept semesters/sessions/fees aggregation — reviewed (batch 2)
- [x] `DepartmentsActionController` — dept create/update/delete, validation — reviewed (batch 2)
- [x] `EventsController` — event CRUD + role/audience filtering — reviewed (batch 2)
- [x] `ExamPaperSubmissionController` — submission/upload flow, validation 🔵 — reviewed, see findings doc
- [x] `ExamsHubController` — hub aggregation of exam sub-features 🔵 — reviewed, see findings doc
- [x] `InsightsController` — **analytics math** (avg/%/agg, div-by-zero) 🔵 — reviewed, see findings doc
- [x] `LinkRequestsController` — approve/reject flow, permission gating 🔵 — reviewed, see findings doc
- [x] `MarkAttendanceController` — **status/late/remark, already-marked, submit** 🔵 — reviewed, see findings doc
- [x] `MarkEditRequestsController` — request + review/approve flow 🔵 — reviewed, see findings doc
- [x] `MarksEntryController` — **score validation/locking/absent, save** 🔵 — reviewed, see findings doc
- [x] `MasterTimetableController` — period assembly, overlap/conflict 🔵 — reviewed, see findings doc
- [x] `MoreHubController` — hub counts/badges 🔵 — reviewed, see findings doc
- [x] `MyStudentsController` — roster assembly, attendance aggregation 🔵 — reviewed, see findings doc
- [x] `NotificationsController` — unread count, mark-read, targeting 🔵 — reviewed, see findings doc
- [x] `PeopleHubController` — people directories aggregation/counts 🔵 — reviewed, see findings doc
- [x] `RecordsHubController` — records categories/counts, export mode 🔵 — reviewed, see findings doc
- [x] `ScreenController` — **base class**: scope/lifecycle/cleanup (affects all) 🔵 — reviewed, see findings doc
- [x] `SemesterResultsController` — **GPA/CGPA/pass-fail computation** 🔵 — reviewed, see findings doc
- [x] `SemesterSubjectsController` — curriculum CRUD, credit/code validation 🔵 — reviewed, see findings doc
- [x] `SessionDetailController` — session detail aggregation, edits 🔵 — reviewed, see findings doc
- [x] `SessionFeesController` — **fee/challan money math, due/paid, fines** 🔵 — reviewed, see findings doc
- [x] `SessionStudentsController` — roster add/remove/import, roll uniqueness 🔵 — reviewed, see findings doc
- [x] `SessionTimetableController` — period CRUD, slot overlap/conflict 🔵 — reviewed, see findings doc
- [x] `StudentAttendanceController` — student attendance % computation — reviewed (batch 2)
- [x] `StudentExamsHubController` — student exams hub aggregation — reviewed (batch 2)
- [x] `StudentFeeChallanController` — challan display/status, money math — reviewed (batch 2)
- [x] `StudentHomeController` — home summary aggregation — reviewed (batch 2)
- [x] `StudentMarksController` — student marks display/derivation — reviewed (batch 2)
- [x] `StudentMoreController` — student more-hub state — reviewed (batch 4)
- [x] `StudentProfileController` — profile load/derivation — reviewed (batch 4)
- [x] `StudentProfileEditController` — profile edit validation/save — reviewed (batch 2)
- [x] `StudentResultsController` — student results/GPA display — reviewed (batch 2)
- [x] `StudentTimetableController` — student timetable assembly — reviewed (batch 2)
- [x] `TeacherScheduleController` — teacher weekly schedule assembly — reviewed (batch 2)
- [x] `TeachersController` — teacher CRUD, permission flags — reviewed (batch 2)

## core — repositories (6)

- [x] `AdministratorRepositoryImpl` — query/map/error, cache sync — reviewed (batch 3)
- [x] `CalendarRepositoryImpl` — query/map/error, cache sync — reviewed (batch 3)
- [x] `DatesheetRepositoryImpl` — query/map/error, cache sync — reviewed (batch 3)
- [x] `FineRepositoryImpl` — fine query/map, money fields — reviewed (batch 3)
- [x] `InsightsRepositoryImpl` — aggregation query correctness — reviewed (batch 3)
- [x] `MarkEditRequestRepositoryImpl` — query/map/error, status transitions — reviewed (batch 3)

## core — providers & util (2)

- [x] `TeacherAssignmentsProvider` — **assignment/identity resolution, flow combine** — reviewed (batch 3)
- [x] `StudentIdCodec` — **encode/decode round-trip, malformed input** — reviewed (batch 3)

## mobile-shared — mappers (10)

- [x] `AcademicStructureMapper` — dept/semester/session mapping fidelity — reviewed (batch 4)
- [x] `DatesheetMapper` — snake_case↔camel, null defaults — reviewed (batch 4)
- [x] `DepartmentMapper` — field fidelity, enum parse — reviewed (batch 4)
- [x] `ExamPaperSubmissionMapper` — field fidelity, null defaults — reviewed (batch 4)
- [x] `MarkEditRequestEntityMapper` — status enum, round-trip — reviewed (batch 4)
- [x] `NotificationMapper` — audience/role fields, timestamps — reviewed (batch 4)
- [x] `RecordsMapper` — record field fidelity — reviewed (batch 4)
- [x] `SessionFeeMapper` — **money fields, null/default** — reviewed (batch 4)
- [x] `StudentLinkRequestMapper` — status enum, round-trip — reviewed (batch 4)
- [x] `TeacherMapper` — **permission flags mapping** — reviewed (batch 4)

## mobile-shared — repositories (12)

- [x] `AcademicSessionRepositoryImpl` — query/map/error, observe flows — reviewed (batch 3)
- [x] `CurriculumRepositoryImpl` — query/map, subject lists — reviewed (batch 3)
- [x] `DepartmentRepositoryImpl` — active filter, query/map — reviewed (batch 3)
- [x] `ExamPaperSubmissionRepositoryImpl` — upload/query/map, error — reviewed (batch 3)
- [x] `NotificationRepositoryImpl` — unread count query, sync, targeting — reviewed (batch 3)
- [x] `SessionAttendanceRepositoryImpl` — **marksBetween/date-range, upsert** — reviewed (batch 3)
- [x] `SessionFeeRepositoryImpl` — **fee query/map, money** — reviewed (batch 3)
- [x] `SessionMarksRepositoryImpl` — **marks query/lock/save** — reviewed (batch 3)
- [x] `SessionTimetableRepositoryImpl` — period query/map, ordering — reviewed (batch 3)
- [x] `StudentLinkRequestRepositoryImpl` — pending query, approve write — reviewed (batch 3)
- [x] `TeacherRepositoryImpl` — teacher/observe, permission read — reviewed (batch 3)
- [x] `UserRepositoryImpl` — **role resolve/cache, auth identity** — reviewed (batch 3)

## mobile-admin — ViewModels & logic (12)

- [x] `AdministratorsViewModel` — delegates controller, state exposure — reviewed (batch 5)
- [x] `LoginViewModel` — auth flow, role gate, error — reviewed (batch 5)
- [x] `DashboardViewModel` — controller wiring, refresh — reviewed (batch 5)
- [x] `DepartmentDetailViewModel` — nav args, controller wiring — reviewed (batch 5)
- [x] `DepartmentsViewModel` — list state, actions — reviewed (batch 5)
- [x] `LinkRequestsViewModel` — controller wiring, approve — reviewed (batch 5)
- [x] `NotificationsBadgeViewModel` — unread flow scoping — reviewed (batch 5)
- [x] `NotificationsViewModel` — list/publish wiring — reviewed (batch 5)
- [x] `ProfileViewModel` — profile load, sign-out — reviewed (batch 5)
- [x] `RecordsExporter` — **export data completeness/format** — reviewed (batch 4)
- [x] `AppRootViewModel` — auth-check, role gate, splash timing — reviewed (batch 6)
- [x] `TeachersViewModel` — list state, CRUD wiring — reviewed (batch 5)

## mobile-student — ViewModels & logic (13)

- [x] `AttendanceSummaryViewModel` — **attendance % derivation** — reviewed (batch 4)
- [x] `AuthViewModel` — login/link flow, role gate — reviewed (batch 5)
- [x] `CurrentStudentProvider` — **current-student identity resolution** — reviewed (batch 4)
- [x] `StudentExamsHubViewModel` — hub wiring — reviewed (batch 5)
- [x] `FeeChallanViewModel` — **challan/money display** — reviewed (batch 4)
- [x] `HomeViewModel` — home summary wiring — reviewed (batch 5)
- [x] `LinkRequestViewModel` — link request submit/state — reviewed (batch 5)
- [x] `MyMarksViewModel` — marks display derivation — reviewed (batch 4)
- [x] `NotificationsBadgeViewModel` — unread flow scoping — reviewed (batch 5)
- [x] `NotificationsViewModel` — list wiring — reviewed (batch 5)
- [x] `ProfileViewModel` — profile load, sign-out — reviewed (batch 5)
- [x] `AppRootViewModel` — auth-check, role gate, splash timing — reviewed (batch 6)
- [x] `MyTimetableViewModel` — timetable assembly wiring — reviewed (batch 4)

## mobile-teacher — ViewModels & logic (16)

- [x] `AttendanceExporter` — **CSV/PDF export completeness/format** — reviewed (batch 4)
- [x] `AttendanceHistoryViewModel` — **month range, SavedStateHandle args, export** — reviewed (batch 4)
- [x] `MarkAttendanceViewModel` — controller wiring, assignments — reviewed (batch 5)
- [x] `LoginViewModel` — login flow, role gate — reviewed (batch 5)
- [x] `TeacherAssignmentsProvider` — **assignment resolution (app copy)** — reviewed (batch 3)
- [x] `ExamPaperSubmissionViewModel` — controller wiring, file pick — reviewed (batch 6)
- [x] `HomeViewModel` — home snapshot wiring — reviewed (batch 5)
- [x] `MenuViewModel` — menu snapshot, sign-out — reviewed (batch 6)
- [x] `LinkRequestsViewModel` — controller wiring, approve — reviewed (batch 5)
- [x] `MarksEntryViewModel` — controller wiring, exam type — reviewed (batch 6)
- [x] `NotificationsBadgeViewModel` — unread flow scoping — reviewed (batch 5)
- [x] `NotificationsViewModel` — list/publish wiring — reviewed (batch 5)
- [x] `ProfileViewModel` — profile load, sign-out — reviewed (batch 5)
- [x] `AppRootViewModel` — auth-check, role gate, splash timing — reviewed (batch 6)
- [x] `ScheduleViewModel` — schedule controller wiring — reviewed (batch 6)
- [x] `MyStudentsViewModel` — roster controller wiring — reviewed (batch 6)

---
**110 files total; 20 with live task chips.**
