# Exception & Success-Message Audit — 42 Screens

Tracks the per-screen verification sweep requested after several silent-failure bugs were found
in production (hard-coded `errorMessage = null` at a screen's call site, unlogged crashes, a raw
internal exception message shown verbatim to the user). One task per screen (shared
`*Workspace.kt` composable in `mobile-shared/src/main/java/com/mbd/cmscommon/ui/components/`,
mirrored byte-for-byte to `desktop-shared/src/main/kotlin/com/mbd/cmscommon/ui/components/`).

Each task verifies: (a) every user-triggered action's failure is caught, classified, and shown via
`CmsNotice`/`CmsErrorDialog` with a relevant message; (b) a critical/unexpected failure reaches
`CmsLog` (via `ScreenController.userMessageLogged()` or `orLogCritical`/`isSuccessLogged`); (c) a
successful action gives the user a clear, visible confirmation — not silent success.

**How to update this file:** when a task chip is started, change its Status to `In progress`; when
its commit lands, change Status to `Done` and check the box. When a new batch of task chips is
generated for the `Pending` rows below, change their Status to `Generated` and fill in the Task ID.

## Legend

- **Generated** — a task chip exists for this screen; the user can start it from the queue.
- **Pending** — no task chip exists yet (either never created, or evicted from the 20-slot queue
  before it could be started — the task-chip queue holds at most 20 pending suggestions at once).
- **Done** — the audit ran and its commit (or "nothing to fix" conclusion) landed.

## Status summary

| Status | Count |
|---|---|
| Generated | 0 |
| Pending | 0 |
| Done | 42 |
| **Total** | **42** |

## Tasks

| # | Done | Screen | Workspace file | App(s) | Status | Task ID |
|---|---|---|---|---|---|---|
| 1 | [x] | Administrator Directory | AdministratorDirectoryWorkspace.kt | admin | Done | task_9c7f8dda |
| 2 | [x] | Attendance History | AttendanceHistoryWorkspace.kt | teacher | Done | task_c73ea8c6 |
| 3 | [x] | Buildings & Rooms | BuildingsRoomsWorkspace.kt | admin | Done | task_ee2a42af |
| 4 | [x] | Calendar | CalendarWorkspace.kt | admin | Done | task_dbe98e3e |
| 5 | [x] | Datesheet | DatesheetWorkspace.kt | admin | Done | task_1cab14ff |
| 6 | [x] | Department Detail | DepartmentDetailWorkspace.kt | admin | Done | task_b8ad1436 |
| 7 | [x] | Exam Paper Review | ExamPaperReviewWorkspace.kt | teacher | Done | task_cc1ec8bc |
| 8 | [x] | Exam Paper Submission | ExamPaperSubmissionWorkspace.kt | teacher | Done | task_e102b998 |
| 9 | [x] | Exams Hub (teacher) | ExamsHubWorkspace.kt | teacher | Done | task_65f739ce |
| 10 | [x] | Insights | InsightsWorkspace.kt | admin | Done | task_2144dbbb |
| 11 | [x] | Link Request Review (admin) | LinkRequestReviewWorkspace.kt | admin | Done | task_74fec688 |
| 12 | [x] | Mark Attendance | MarkAttendanceWorkspace.kt | teacher | Done | task_3dba8a8c |
| 13 | [x] | Mark Edit Request Review | MarkEditRequestReviewWorkspace.kt | teacher | Done | task_c03e3279 |
| 14 | [x] | Marks Entry | MarksEntryWorkspace.kt | teacher | Done | task_44790e2d |
| 15 | [x] | Master Timetable | MasterTimetableWorkspace.kt | admin | Done | task_da0c0f07 |
| 16 | [x] | More Hub (admin) | MoreHubWorkspace.kt | admin | Done | task_fb87e510 |
| 17 | [x] | Notification | NotificationWorkspace.kt | admin, teacher | Done | task_7e813589 |
| 18 | [x] | People Hub | PeopleHubWorkspace.kt | admin | Done | task_4dd9e36b |
| 19 | [x] | Profile (admin/teacher) | ProfileWorkspace.kt | admin, teacher | Done | task_5a24af30 |
| 20 | [x] | Records Hub | RecordsHubWorkspace.kt | admin | Done | task_17541710 |
| 21 | [x] | Semester Curriculum | SemesterCurriculumWorkspace.kt | admin | Done | task_6273cfa3 |
| 22 | [x] | Semester Results | SemesterResultsWorkspace.kt | teacher | Done | task_e5057738 |
| 23 | [x] | Session Fee | SessionFeeWorkspace.kt | admin | Done | task_7b05b211 |
| 24 | [x] | Session Operations | SessionOperationsWorkspace.kt | admin | Done | task_7d36b730 |
| 25 | [x] | Session Timetable | SessionTimetableWorkspace.kt | admin | Done | task_8babb199 |
| 26 | [x] | Student Attendance | StudentAttendanceWorkspace.kt | student | Done | task_4ffaaac8 |
| 27 | [x] | Student Auth (login/register) | StudentAuthWorkspace.kt | student | Done | task_dd82c92a |
| 28 | [x] | Student Exams Hub | StudentExamsHubWorkspace.kt | student | Done | task_4f769c7e |
| 29 | [x] | Student Fee | StudentFeeWorkspace.kt | student | Done | task_a3b0b9e1 |
| 30 | [x] | Student Home | StudentHomeWorkspace.kt | student | Done | task_3ad841df |
| 31 | [x] | Student Link Request | StudentLinkRequestWorkspace.kt | student | Done | task_944134cd |
| 32 | [x] | Student Marks | StudentMarksWorkspace.kt | student | Done | task_a518b290 |
| 33 | [x] | Student More | StudentMoreWorkspace.kt | student | Done | task_4e15323f |
| 34 | [x] | Student Profile | StudentProfileWorkspace.kt | student | Done | task_c83b984d |
| 35 | [x] | Student Results | StudentResultsWorkspace.kt | student | Done | task_333b86a7 |
| 36 | [x] | Student Roster (session students, admin view) | StudentRosterWorkspace.kt | admin | Done | task_8d31aaf1 |
| 37 | [x] | Student Timetable | StudentTimetableWorkspace.kt | student | Done | task_d7df1fbf |
| 38 | [x] | Teacher Directory | TeacherDirectoryWorkspace.kt | admin | Done | task_06d2f861 |
| 39 | [x] | Teacher Home | TeacherHomeWorkspace.kt | teacher | Done | task_62504c5b |
| 40 | [x] | Teacher Menu | TeacherMenuWorkspace.kt | teacher | Done | task_47399623 |
| 41 | [x] | Teacher Schedule | TeacherScheduleWorkspace.kt | teacher | Done | task_bef3b2f3 |
| 42 | [x] | Teacher Student Roster | TeacherStudentRosterWorkspace.kt | teacher | Done | task_12c73fc9 |

## Notes

- Rows 10, 12, 14, 15, 19–22 are confirmed Done via their landed commits on `master` (Insights
  6507bedc, Mark Attendance d669f721, Marks Entry 15bfa394, Master Timetable 3d13ae71, Profile
  639ff698, Records Hub 57afc82b, Semester Curriculum b2e63030, Semester Results d042b75a).
- The previous batch of 20 chips (rows 10–29) was regenerated on 2026-09-07: only the 8 rows above
  had a confirmed commit, so the other 12 (11, 13, 16–18, 23–29) were reset to Pending — it wasn't
  possible to confirm whether they'd actually run — and 20 new chips were generated for rows 1–9,
  11, 13, 16–18, 23–28 to refill the queue. Row 29 (Student Fee) stayed Pending with no chip since
  the 20-slot queue was already full at that point.
- The task-chip queue holds at most 20 pending suggestions. Creating a 21st evicts the oldest
  unseen one. Rows 30–42 (and now row 29) need a fresh chip generated once room opens up in the
  queue (start or dismiss some of the 20 `Generated` rows first).
- Contributor rotation for commits produced by these tasks: **Sharfa Kiran**, **Hadiya Saleem**,
  **Laraib Kazmi** — rotate through all three (not just two), whichever wasn't the immediately
  preceding commit's author. No `Co-Authored-By` (or other AI-attribution) trailer in any commit
  message.
