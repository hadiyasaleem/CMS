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
| Generated | 20 |
| Pending | 22 |
| Done | 0 |
| **Total** | **42** |

## Tasks

| # | Done | Screen | Workspace file | App(s) | Status | Task ID |
|---|---|---|---|---|---|---|
| 1 | [ ] | Administrator Directory | AdministratorDirectoryWorkspace.kt | admin | Pending | — (evicted) |
| 2 | [ ] | Attendance History | AttendanceHistoryWorkspace.kt | teacher | Pending | — (evicted) |
| 3 | [ ] | Buildings & Rooms | BuildingsRoomsWorkspace.kt | admin | Pending | — (evicted) |
| 4 | [ ] | Calendar | CalendarWorkspace.kt | admin | Pending | — (evicted) |
| 5 | [ ] | Datesheet | DatesheetWorkspace.kt | admin | Pending | — (evicted) |
| 6 | [ ] | Department Detail | DepartmentDetailWorkspace.kt | admin | Pending | — (evicted) |
| 7 | [ ] | Exam Paper Review | ExamPaperReviewWorkspace.kt | teacher | Pending | — (evicted) |
| 8 | [ ] | Exam Paper Submission | ExamPaperSubmissionWorkspace.kt | teacher | Pending | — (evicted) |
| 9 | [ ] | Exams Hub (teacher) | ExamsHubWorkspace.kt | teacher | Pending | — (evicted) |
| 10 | [ ] | Insights | InsightsWorkspace.kt | admin | Generated | task_2144dbbb |
| 11 | [ ] | Link Request Review (admin) | LinkRequestReviewWorkspace.kt | admin | Generated | task_eb7f1cc2 |
| 12 | [ ] | Mark Attendance | MarkAttendanceWorkspace.kt | teacher | Generated | task_3dba8a8c |
| 13 | [ ] | Mark Edit Request Review | MarkEditRequestReviewWorkspace.kt | teacher | Generated | task_2fb928ce |
| 14 | [ ] | Marks Entry | MarksEntryWorkspace.kt | teacher | Generated | task_44790e2d |
| 15 | [ ] | Master Timetable | MasterTimetableWorkspace.kt | admin | Generated | task_da0c0f07 |
| 16 | [ ] | More Hub (admin) | MoreHubWorkspace.kt | admin | Generated | task_94cb1a0b |
| 17 | [ ] | Notification | NotificationWorkspace.kt | admin, teacher | Generated | task_95851d0b |
| 18 | [ ] | People Hub | PeopleHubWorkspace.kt | admin | Generated | task_55cad2be |
| 19 | [ ] | Profile (admin/teacher) | ProfileWorkspace.kt | admin, teacher | Generated | task_5a24af30 |
| 20 | [ ] | Records Hub | RecordsHubWorkspace.kt | admin | Generated | task_17541710 |
| 21 | [ ] | Semester Curriculum | SemesterCurriculumWorkspace.kt | admin | Generated | task_6273cfa3 |
| 22 | [ ] | Semester Results | SemesterResultsWorkspace.kt | teacher | Generated | task_e5057738 |
| 23 | [ ] | Session Fee | SessionFeeWorkspace.kt | admin | Generated | task_78506663 |
| 24 | [ ] | Session Operations | SessionOperationsWorkspace.kt | admin | Generated | task_57c64f83 |
| 25 | [ ] | Session Timetable | SessionTimetableWorkspace.kt | admin | Generated | task_7bcb7767 |
| 26 | [ ] | Student Attendance | StudentAttendanceWorkspace.kt | student | Generated | task_0927bc28 |
| 27 | [ ] | Student Auth (login/register) | StudentAuthWorkspace.kt | student | Generated | task_3455543f |
| 28 | [ ] | Student Exams Hub | StudentExamsHubWorkspace.kt | student | Generated | task_a18fc1e4 |
| 29 | [ ] | Student Fee | StudentFeeWorkspace.kt | student | Generated | task_752491a0 |
| 30 | [ ] | Student Home | StudentHomeWorkspace.kt | student | Pending | — |
| 31 | [ ] | Student Link Request | StudentLinkRequestWorkspace.kt | student | Pending | — |
| 32 | [ ] | Student Marks | StudentMarksWorkspace.kt | student | Pending | — |
| 33 | [ ] | Student More | StudentMoreWorkspace.kt | student | Pending | — |
| 34 | [ ] | Student Profile | StudentProfileWorkspace.kt | student | Pending | — |
| 35 | [ ] | Student Results | StudentResultsWorkspace.kt | student | Pending | — |
| 36 | [ ] | Student Roster (session students, admin view) | StudentRosterWorkspace.kt | admin | Pending | — |
| 37 | [ ] | Student Timetable | StudentTimetableWorkspace.kt | student | Pending | — |
| 38 | [ ] | Teacher Directory | TeacherDirectoryWorkspace.kt | admin | Pending | — |
| 39 | [ ] | Teacher Home | TeacherHomeWorkspace.kt | teacher | Pending | — |
| 40 | [ ] | Teacher Menu | TeacherMenuWorkspace.kt | teacher | Pending | — |
| 41 | [ ] | Teacher Schedule | TeacherScheduleWorkspace.kt | teacher | Pending | — |
| 42 | [ ] | Teacher Student Roster | TeacherStudentRosterWorkspace.kt | teacher | Pending | — |

## Notes

- The task-chip queue holds at most 20 pending suggestions. Creating a 21st evicts the oldest
  unseen one. Rows 1–9 above were evicted this way before they could be started, and rows 30–42
  were never created for the same reason — both groups need a fresh task chip generated once room
  opens up in the queue (start or dismiss some of the 20 `Generated` rows first).
- Contributor rotation for commits produced by these tasks: **Sharfa Kiran**, **Hadiya Saleem**,
  **Laraib Kazmi** — rotate through all three (not just two), whichever wasn't the immediately
  preceding commit's author. No `Co-Authored-By` (or other AI-attribution) trailer in any commit
  message.
