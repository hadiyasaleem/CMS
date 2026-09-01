# Business-Logic Verification — Findings (Batch 1: 20 core controllers)

Read-only verification of the 20 core `*Controller` classes reviewed 2026-08-29. Not compiler-verified (Gradle loopback blocked in-env). Findings are reports, not fixes.

## The big picture: 4 systemic patterns account for most findings

These repeat across nearly every controller, so fixing them **centrally** (mostly on `ScreenController`) closes many bugs at once — higher leverage than per-file patches.

### S1 — `catch(Throwable)` / `runCatching` swallows `CancellationException` (systemic, ~10 controllers)
`ScreenController.launch` correctly rethrows `CancellationException`, but subclass inner `try/catch (t: Throwable)` and `runCatching { … }` blocks catch it *first* and convert it into an `Outcome.Error`/failure. On scope teardown (screen closed mid-op) this breaks cooperative cancellation and writes a bogus error/`_loading`-stuck / corrupt snapshot to a dying state holder.
Seen in: MarksEntry, SemesterResults, MarkAttendance, ExamPaperSubmission, MoreHub, RecordsHub, PeopleHub, SessionDetail, SessionStudents(reverse case), MarkEditRequests.
**Fix leverage:** add a result-carrying helper on `ScreenController` (e.g. `launchResult(state){…}`) that owns the "rethrow `CancellationException` first" discipline, and migrate the ~16 hand-rolled catch sites onto it.

### S2 — No single-flight guard on save/submit/send → double-submit → duplicate writes (systemic, ~11 controllers)
Mutating actions `launch` without checking an in-flight flag, and the "already done" flag is set only *after* success. A double-tap fires two concurrent writes.
Seen in: MarksEntry(save), SessionFees(save), MarkAttendance(submit — **duplicate attendance**), ExamPaperSubmission(**duplicate paper**), Notifications(send — **duplicate notice to whole audience**), MarkEditRequests, ExamsHub, MoreHub, Insights, MasterTimetable, LinkRequests(minor).
**Fix leverage:** a single-flight/`_busy`-gated launch helper on `ScreenController`, set synchronously before `launch`.

### S3 — Reading `.value` of `WhileSubscribed(5000)` flows for critical decisions → stale/empty snapshot when no collector (HIGH — causes data loss)
When the UI's collector is inactive (>5s detached, or action fired before first emission), `.value` is empty/stale, and code that treats it as authoritative does damage:
- **SemesterSubjects #1 — mass soft-delete:** `saveSubject` passes `subjects.value` (possibly `emptyList()`) to a full-list reconcile that **soft-deletes every course code not in the list** → silently wipes a semester's subjects.
- **SessionStudents #2 — silent overwrite:** uniqueness check off stale `students.value` passes, then upsert overwrites an existing student's row.
- Also weakens validation/saves in MarksEntry(#6), SessionTimetable(#5), LinkRequests(#2), MarkAttendance.

### S4 — Overlapping refresh/reload race; `_loading` reset not version-guarded (systemic)
`refresh()`/`reload()` `launch` with no prior-job cancel; the older response can land after the newer (stale UI), and the first coroutine's `finally { _loading=false }` clears the spinner while a newer load is still running.
Seen in: SemesterResults, Insights, MasterTimetable, ExamsHub, Notifications, MoreHub, RecordsHub(writes guarded, `_loading` not).

Related systemic minor: **background-sync errors swallowed by `runCatching`** so the `error` channel is never populated → silent failures / blank screens with no message (MyStudents #1, SessionDetail #1, MasterTimetable #1, MarkAttendance #4); and **loading-vs-empty indistinguishable** (no loading flag) in MyStudents, SessionStudents, SessionDetail, SessionTimetable.

## Top individual bugs (ranked, beyond the systemic ones)

| # | Severity | File:line | Bug |
|---|---|---|---|
| 1 | **HIGH** | SemesterSubjectsController.kt:91 | Full-list reconcile from possibly-empty `subjects.value` → **mass soft-delete of a semester's subjects** (S3) |
| 2 | **HIGH** | SessionTimetableController.kt:66 + Snapshot:139 + RepoImpl:100 | Effective-dated periods share one storage key (`buildId` omits effective dates); overlap validator permits date-disjoint same-slot periods → **older period silently overwritten**. Fix needs DTO/PK/migration decision |
| 3 | **HIGH** | SessionStudentsController.kt:55 | `addStudent` double-wraps error in `RuntimeException` → `isSafeValidationError` rejects it → user **always** sees generic "Something went wrong" instead of "Roll X already enrolled"/"Session full". Easy fix: drop the wrapper |
| 4 | HIGH | SessionTimetableController.kt:86 | Non-atomic edit-move: save-new then delete-old; if delete throws → **duplicate/orphaned period**, no rollback |
| 5 | MED | MarksEntryController.kt:156 | `requestMarkEdit` never validates `requestedScore` bounds (save path does) → out-of-range edit request persisted |
| 6 | MED | MarksEntryController.kt:123 | Invalid/out-of-range scores silently dropped in `mapNotNull`, yet `save()` reports **Success** → teacher's typed value lost with no signal |
| 7 | MED | SemesterResultsController.kt:110 | `record()` persists non-normalized `result` casing (validates uppercased, stores raw) → downstream `== "PASS"` misclassifies |
| 8 | MED | MarkEditRequestsController.kt:90 | Approve/reject toast always shows "Roll N", never student name (`removeResolvedRequest` deletes details before the notice is built) |
| 9 | MED | SessionFeesController.kt:48 | Slow initial `load()` can overwrite a just-saved structure with stale/null → saved fees vanish on screen |
| 10 | MED | NotificationsController.kt:116 | Student with no session → `refreshNow` never called → **loading spinner stuck forever** (the guard that would clear it is upstream-filtered dead code) |
| 11 | MED | NotificationsController.kt:145 | Admin can set dept **and** session on one student notice; DAO matches with `AND` → silently narrower audience (intersection) than intended |
| 12 | MED | PeopleHubController.kt:71 | Student count is the only source never `sync()`'d before a one-shot `.first()` → **cold launch reports 0 students** until another screen syncs |

## Rated clean / correct where it counts
- **ExamsHub, MoreHub, RecordsHub, PeopleHub** aggregation/counting math — correct and well-guarded (`distinctBy` dedup, divide-by-zero guards, partial-failure degradation).
- **Insights** analytics (avg/%/pass-rate) — correct, division-by-zero safe; only state-transition races.
- **SemesterResults** — does not compute GPA itself (arrives pre-computed); no rounding/division risk here.
- **MyStudents** flow assembly, **LinkRequests** identity/TOCTOU handling (re-fetches + server re-check), **Notifications** permission gating & audience targeting, **MarkAttendance** term-% & completeness logic — all verified correct.
- `ScreenController` base class itself is correct; the problem is subclasses defeating its cancellation discipline (S1).

## Suggested fix order
1. **S1 + S2 together on `ScreenController`** (one `launchResult`/single-flight helper, migrate ~16 sites) — closes the most bugs.
2. **Bug #3** (SessionStudents error double-wrap) — trivial, high user impact.
3. **Bug #1** (SemesterSubjects mass-delete) and **S3** hardening — guard destructive reconciles against empty/stale `.value`.
4. **Bug #2/#4** (SessionTimetable identity/atomicity) — needs a schema decision first.

---

# Batch 2 (2026-08-29): 17 more controllers + 3 re-runs

The 3 re-runs (MoreHub, MasterTimetable, SemesterResults) **confirmed** their batch-1 findings — no drift. The 17 new controllers reinforce the systemic patterns and add several standalone bugs.

## Pattern prevalence in batch 2
- **S2 (no single-flight guard) — near-universal.** Present in essentially every mutating controller (Student* saves, Teachers/Administrators/Departments CRUD, Calendar/Events/Datesheets create/delete, all refreshes).
- **S1 (cancellation swallowed) — common** where `refresh()` uses `runCatching` over sync calls (StudentAttendance, StudentHome, StudentExamsHub, TeacherSchedule, MasterTimetable). Several are "narrow S1" that self-heal via a later suspend point but still enter work during a cancelled scope.
- **S4 (refresh race + unguarded `_loading`) — common.**
- **S3 — mostly ABSENT in batch 2** (student controllers use plain `MutableStateFlow` or `Eagerly`, not `WhileSubscribed.value`). Good — S3 stays contained to batch-1's SemesterSubjects/SessionStudents/MarksEntry (+ a casing-variant in SemesterResults).
- **Silent sync-failure / dead error channel — common** (StudentHome, StudentAttendance, StudentExamsHub, MasterTimetable, TeacherSchedule): `runCatching`-wrapped syncs never populate `error`, and some screens observe only `refreshError`/`refreshState`, never the base `error` — so DB/network failures render as blank/stale screens with no message.

## New standalone bugs (batch 2)

| # | Severity | File:line | Bug |
|---|---|---|---|
| B1 | **HIGH** | DepartmentsActionController.kt:22 | `create()` has no code-uniqueness check; repo `upsert` is keyed on `deptId` derived from code → creating a dept with an existing code **silently overwrites** it (name/hodEmail/**createdAt/createdBy reset**), UI shows success. Plus code stored verbatim while `deptId` is lowercased → case collisions |
| B2 | **HIGH** | DatesheetsController.kt:83 | `updateDatesheet` forwards `published=true` after only title/type validation — **bypasses the `datesheetScheduleQuality` gate** that `setPublished` enforces → can publish a datesheet with zero papers or overlapping slots |
| B3 | MED-HIGH | AdministratorsController.kt:50 | Email validated with `required=false` → `emailError("")` returns null → **empty/blank email reaches `createAdministrator("")`**. One-word fix (`required=true`) |
| B4 | MED | Calendar/Events/Datesheets (create/delete) | **Ineffective single-flight guard (S2b):** `if (_busy.value) return` is checked in the caller but `_busy=true` is set *inside* the launched coroutine → check-and-set straddles dispatch → double-submit still fires duplicate create/delete. (These controllers *tried* to guard and got it wrong — set `_busy` synchronously before `launch`.) |
| B5 | MED | DepartmentDetailController.kt:33 | `sessions` flow reuses `departmentDetailSnapshot(...).sessions` which filters `isActive && archivedAt==null` → archived/deactivated sessions are **invisible and unrecoverable** from the detail screen |
| B6 | MED | Repo contract (Teachers, Dashboard, DepartmentDetail) | `observeActiveTeachers()` / `observeActiveDepartments()` return the **whole** cache and `sync()` selects all rows (no active/status filter) despite the name → "active" rosters/counts include inactive/disabled records |
| B7 | LOW-MED | StudentHomeController.kt:80, TeacherScheduleWorkspace.kt:73 | "Next class" / schedule sorts `startTime` as a **string** → `"9:00"` sorts after `"10:00"`; non-zero-padded times also throw in `LocalTime.parse` → silently dropped |
| B8 | LOW | StudentProfileEditController.kt:78 | Validates trimmed fields but persists the raw `edited` object → names/addresses stored with leading/trailing whitespace |

## Rated clean (batch 2)
- **DashboardController — fully clean** on all axes: pure declarative `combine(...).stateIn()`, no imperative refresh/`_loading`/`runCatching`/`.value` — structurally immune to S1–S4. Counts correct.
- **StudentMarks, StudentAttendance, StudentResults, StudentTimetable, StudentFeeChallan** — the display/derivation/percentage/GPA math is correct and division-safe; issues are confined to S1/S2/S4 in `refresh()`.
- Calendar date/audience validation, Datesheet schedule-quality logic (where invoked), Teachers/Administrators field validation — all correct.

## Fix-order update
The batch-1 recommendation still dominates: a **central `ScreenController.launchResult`/single-flight helper** closes S1+S2+S2b+S4 across ~30 controllers. Add to the standalone list: **B1** (dept overwrite — needs a uniqueness pre-check), **B2** (datesheet publish gate), **B3** (admin email `required=true` — trivial), and confirm the **B6** "active" repo-filter intent.

---

# Batch 3 (2026-08-31): the data layer — 18 repositories + StudentIdCodec + TeacherAssignmentsProvider

This layer is **materially more broken than the controllers.** Several controller "depends on whether the repo is idempotent" questions are now answered — and the answer is usually "no". Six new systemic patterns (R1–R6), plus two features that don't work against the real schema at all.

## New systemic patterns

### R1 — Dropped soft-delete flag → server deletes NEVER propagate (systemic, HIGH, ~8 repos)
The `dtoToEntity = domainToEntity(dtoToDomain(dto))` round-trip loses `isDeleted`/`deletedAt`/`deletedBy` because the **domain models have no such fields**. So `sync()`'s `entities.partition { it.isDeleted }` yields an **always-empty `deleted` list**, and a server-soft-deleted row is re-upserted locally as **active**.
Confirmed in: **SessionAttendance** (tallies stay permanently inflated), **AcademicSession** (sessions and students reappear), **Teacher**, **Department**, **ExamPaperSubmission**, **StudentLinkRequest**, **SessionFee**. Net effect app-wide: deletions only take effect on the device that made them; every other device keeps showing deleted data forever.

### R2 — encodeDefaults=false drops default-valued fields on upsert → "turning something off" never persists (systemic, HIGH)
The shared `Json` sets `encodeDefaults=false`, so any DTO field equal to its Kotlin default is omitted from the PostgREST payload, and ON CONFLICT DO UPDATE leaves that column unchanged.
- **Teacher** — revoking a permission (true→false) or re-activating (isActive false→true) is **silently not saved**; next sync reverts local to the stale server value.
- **Curriculum** — turning an elective off (isElective true→false) or clearing an outline silently reverts.
- Zero-value **insert** failures: **Fine** (amount=0.0 omitted → NOT-NULL violation → issuing a zero fine throws), **SessionFee** (position/amount).

### R3 — Written against a schema the table doesn't have → the feature is broken (CRITICAL)
- **NotificationRepositoryImpl (mobile) is entirely non-functional.** It filters/orders `updated_at` and writes `is_deleted` — columns the `notifications` table does not have — and uses camelCase filter names (targetRole, createdByEmail) instead of snake_case. Every sync()/observeForRole/delete() returns a PostgREST 400 → notifications never load on mobile. The desktop repo does it correctly (orders created_at, hard-deletes) — this is a mobile regression.
- **SessionTimetableRepositoryImpl.removePeriod is broken** — `update({ set("is_deleted", true) })` on `timetable_periods`, which has no is_deleted column → every delete throws at runtime. savePeriod also fails when createdBy/updatedBy are non-null.

### R4 — Non-atomic multi-write mutations (no transaction/RPC) → partial-failure corruption
- **StudentLinkRequest.approveRequest** — 4 independent writes (unlink previous, set roster linked_email, set requester profile, set status). Crash mid-way → student linked but request still PENDING, or previous holder cleared but new link failed.
- **MarkEditRequest.approveRequest** — 2 tables (marks + request status) non-atomically.
- **SessionFee** head replace and **Curriculum**/**SessionMarks** local reconcile — delete-then-insert with no @Transaction; a crash between them loses all heads / empties the local scope.

### R5 — Approve paths: no status=PENDING guard, no bounds check, silent no-op, lost update (HIGH)
**MarkEditRequest** and **StudentLinkRequest** approve: (a) no `eq("status","PENDING")` → a REJECTED request can be flipped to APPROVED, double-approve re-applies; (b) MarkEdit writes requestedScore into session_marks with **zero bounds validation** — this is where the unbounded-999 request from the controller review actually lands; (c) the marks update can match **0 rows yet still mark the request APPROVED** (silent no-op); (d) ignores currentScore → silent lost update if the mark changed since submit. Best fixed as a single server-side RPC.

### R6 — insert-not-upsert → duplicate rows on double-submit/retry (confirms controller S2)
Plain insert, no conflict key, no DB unique constraint: **SessionAttendance.markAttendance** (duplicate attendance), **ExamPaperSubmission** (duplicate rows + orphaned storage files — timestamped path makes upsert=true dead), **Calendar.createEvent**, **Datesheet.addSlot**, **Fine.issueFine**. The controller S2 double-submits therefore produce real server-side duplicates here.

## Top standalone bugs (batch 3)
| Sev | File | Bug |
|---|---|---|
| CRITICAL | NotificationRepositoryImpl (mobile) | Whole feature 400s against real schema — notifications never load (R3) |
| HIGH | CurriculumRepositoryImpl:75 | Full-list reconcile from empty subjects.value wipes a semester — CONFIRMED at the repo |
| HIGH | SessionMarksRepositoryImpl:154 | Partial save deleteFor whole scope + re-insert only new rows → locked/saved marks vanish locally, not recovered by delta sync |
| HIGH | AcademicSessionRepositoryImpl | createSession overwrites an existing session (resets semester/active); new session gets maxStudents=0 → "Session is full (0 max)", no student can be added; isActive local(false)/remote(true) mismatch |
| HIGH | UserRepositoryImpl | Cached role never invalidated on backend role change; no email normalization → case-mismatch login → "No profile found"; teacher perms silently all-false if teacher row unsynced; observeCurrentUserRole never emits a signed-out value |
| HIGH | TeacherAssignmentsProvider | Stored teacherEmail un-normalized vs normalized accountKey (case-sensitive SQLite =) → a teacher can see zero assignments (entire schedule silently empty) |
| MED-HIGH | SessionFeeRepositoryImpl | Non-atomic remote head replace (crash → all heads lost); indexOf position bug mis-numbers duplicate heads; negative amount unvalidated |
| MED | CalendarRepositoryImpl (mobile) | getEvents missing is_deleted filter + deleteEvent HARD-deletes — diverges from desktop soft-delete on the same table |
| MED | InsightsRepositoryImpl | Kotlin is clean; session_overview.avg_cgpa SQL view fan-out over-counts CGPA (weights students by course count) — fix in the view |
| LOW-MED | StudentIdCodec | Correct for well-formed ids (even underscores in deptId), but silent-wrong on an underscore in a roll/deptId (unconstrained text), and no trim/case normalization vs repos that trim |

## Cross-cutting observation
Many DTOs model **phantom audit/soft-delete columns that don't exist** in the actual Postgres schema (Fine, Datesheet slot, Administrator, Notification), so domain createdAt/entityId/isDeleted are always Epoch/0/false — audit metadata is fabricated. And **desktop repos are frequently correct where the mobile twin is broken** (Notification, Calendar) — the mobile data layer looks like a regression against an older soft-delete schema.

## Rated clean (batch 3)
- **AdministratorRepositoryImpl** — no material bugs (DB unique constraint + edge function backstop create); only "should disabled admins be listed?" to confirm.
- **DatesheetRepositoryImpl** — substantially correct; drafts gated by RLS server-side; published flag persists correctly.
- Kotlin mapping/snake_case/enum handling verified faithful across the board. CancellationException is NOT swallowed in most repos (the runCatchings wrap synchronous enum/date parsing) — the exceptions that DO swallow it on suspend calls are SessionAttendance, SessionMarks, SessionFee, ExamPaperSubmission, UserRepository.

## Fix-order update (whole effort)
1. **R3 first** — mobile Notification + timetable removePeriod are broken features, not edge cases. Port the desktop repo shapes.
2. **R1** — restore isDeleted through the mapper (add to domain or map DTO→entity directly) so deletes propagate; single highest-leverage data bug.
3. **R2** — stop dropping default-valued fields on upsert (explicit column writes for permission/active/elective toggles).
4. **R4/R5** — move approve paths (link-request, mark-edit) into server-side RPCs (atomic + status guard + bounds).
5. Then the controller-layer ScreenController.launchResult/single-flight helper (S1/S2/S4).

---

# Batch 4 (2026-08-31): 10 mappers + 2 thin controllers + 8 app-layer files

## Mappers — R1 confirmed near-universal, plus mapper-specific issues
**8 of 10 mappers drop the soft-delete triple** (`isDeleted`/`deletedAt`/`deletedBy`) in dtoToEntity because the domain model has no such fields — AcademicStructure, Teacher, SessionFee, StudentLinkRequest, Department, Datesheet, ExamPaperSubmission, Records (Calendar+Fine). Exceptions: **NotificationMapper** correctly never sets it (the table lacks the column), **MarkEditRequestEntityMapper** actually maps it correctly. This is the definitive confirmation of R1 as an architectural gap: the fix is to add the fields to the domain models (or map DTO→entity directly), not per-repo.

New mapper-specific patterns/bugs:
- **R7 — case-sensitive enum parse silently mis-defaults.** `enumValueOf`/`valueOf` is case-sensitive; a lowercase/legacy server value (`"annual"`, `"urgent"`) fails and silently falls to the default (SEMESTER / NORMAL / null role) with no signal. Seen in Notification (priority/role), SessionFee (cadence), ExamPaperSubmission (examType→MIDTERM), MarkEdit (examType→MIDTERM). Only safe because app-written data is uppercase.
- **null→"" collapse** loses the null/empty distinction on `createdBy`/`updatedBy`/`requestedBy`/`reason` in several mappers (Teacher, StudentLinkRequest, MarkEdit, Records) — asymmetric between the dto and entity paths.
- **AcademicStructureMapper**: no DTO mappers at all (entity↔domain only); **missing reverse mappers** for student & period; period `deptId` dropped; subject `id` reconstructed as a composite (breaks if the stored PK ever differed); **`currentSemester` NOT coerced to 1..8 here** (the repo coerces, the mapper doesn't); effective-date strings lossily parse to null on non-ISO input.
- **SessionFeeMapper**: `headLocalId = sessionId + "_" + label.trim().lowercase()` — case/whitespace-different head labels collapse onto one Room PK (fewer local heads, total-amount loss).
- **DepartmentMapper**: `deptId ?: ""` → an empty-string primary key hazard (two null-deptId rows collide); no code/deptId normalization; createdAt local/remote divergence on create.
- **ExamPaperSubmissionMapper**: `semester` and `fileSizeBytes` dropped outright (quiet data loss, not a wrong value).

## Thin controllers
- **StudentMoreController** — S1 (runCatching swallows cancellation) couples into **S4 stuck-`_loading`**: on the cancellation path `userMessage()` rethrows before `_loading=false`, and there is no `finally` → spinner never clears. S2 present, S3 absent.
- **StudentProfileController** — S2/S4 confirmed, plus a **real bug**: `getStudentProfile` is nullable and `profileLoad.getOrNull()?.let { _profile.value = it }` means a **successful load returning null** (profile deleted server-side) leaves the previous profile on screen forever. Fix: distinguish "load failed" from "loaded null".

## A1 — NEW systemic gap: app ViewModel↔Screen state contract drops error & loading (student app)
Across **all four** student ViewModels reviewed (AttendanceSummary, FeeChallan, MyMarks, MyTimetable) the same pattern:
- The screen **hardcodes `errorMessage = null`** and never observes the controller's `error`/`refreshing` channel → **sync failures are completely invisible** (stale/empty data, no message, retry gives no feedback).
- `loading = (snapshot == null)` **conflates three states** — "still loading", "no linked student" (context null → emits null forever), and "error" — so an unlinked/logged-out student gets a **permanent spinner** instead of an empty/"not enrolled" state.
- `refresh()` is frequently **dead or no-op** — controller assigned only as a side-effect inside `flatMapLatest` (null before first subscription), or not wired into the screen at all.
The core math and StateFlow scoping in these ViewModels is correct and properly delegated; the defect is entirely the VM↔screen observability contract. Worth fixing once as a shared pattern.

## Real standalone bugs (app layer)
| Sev | File | Bug |
|---|---|---|
| **HIGH** | AttendanceHistoryViewModel:59 | `loadMonth` has `try/finally` with **no catch** → a `marksBetween` throw is uncaught in `viewModelScope` → **app crash**. Export coroutines likewise uncaught (IOException / ActivityNotFoundException on share → crash) |
| **HIGH** | AttendanceExporter:44 | CSV escaping checks only `,` and `"`, **not newline/CR** → a student name with a line break writes a raw newline → row splits, every subsequent column misaligns → corrupted CSV |
| MED-HIGH | RecordsExporter:71 | PDF **hard-truncates every cell** (`take(24/6)`) → money `125,000`→`125,00`, date `2026-08-31`→`2026-0` → the PDF shows wrong numbers (CSV is correct); also assumes row length == header.size (extra cells drawn off-page and lost) |
| MED | AttendanceHistoryViewModel:97 | Export reads `roster.value`/`session.value` off WhileSubscribed flows → export fired on screen-open produces **empty roster / blank metadata**; ExportMeta never fills creditHours/timeslots; refresh race on month nav |

## Rated clean (batch 4)
- **CurrentStudentProvider** — verified correct: identity resolution, flow combine, null/empty edges all sound; the un-normalized-email bug is upstream (at link time), not here. Good, since most student screens depend on it.
- **AttendanceSummary/FeeChallan/MyMarks/MyTimetable ViewModels** — their own logic (wiring, delegation, StateFlow scoping, and — notably — MyTimetable does NOT have the string-sort bug because it delegates to the LocalTime-based core snapshot) is correct; only the A1 contract gap applies.
- **MarkEditRequestEntityMapper, NotificationMapper** — soft-delete handling correct; mapping faithful.

## State: 78/110 reviewed. Remaining ~32 = app-module ViewModels that wrap already-reviewed controllers (auth/home/notifications/hub/profile/link-request across the 3 apps). Expect mostly A1-contract confirmations and wiring checks.

---

# Batch 5 (2026-08-31/09-01): 20 app-module ViewModels (all admin + student + MarkAttendance)

Not all thin/clean — several ViewModels bypass or mis-wire their already-reviewed controllers, which produces the worst single-screen breakages of this batch. Two new app patterns (A2, A3) join A1.

## New patterns
- **A2 — `distinctUntilChangedBy { it?.studentId }` freezes context-derived fields.** The student Home/Profile VMs key controller re-creation on `studentId` only, but `CurrentStudentProvider.observeContext()` re-emits when name/gpa/cgpa/session change (same studentId). Those emissions are suppressed → **the profile/home header GPA, CGPA, name, and semester stay stale after a sync/refresh** until re-login (attendance/timetable tiles, which flow through the controller, do update — so the screen is half-stale).
- **A3 — sign-out doesn't clear local cache + is fire-and-forget.** `SessionManager.signOut()` only calls `auth.signOut()` in a detached scope wrapped in `runCatching` (swallowed), and clears no Room/singleton cache. Confirmed in Profile (admin + student) and AppRoot (admin): **user B signs in and sees user A's cached data** (the admin list is a `@Singleton` StateFlow that leaks whole admin emails); a failed sign-out is invisible; no signed-out value is ever emitted.

## Real bugs (ranked)
| Sev | File | Bug |
|---|---|---|
| **HIGH** | AppRootViewModel (admin) | Sign-out never propagates to the role gate → `role` stays `Admin` (observeCurrentUserRole `filterNotNull` retains the stale value; `observed ?: startup` lets it win) → app **hangs on "LOADING ADMIN DATA" splash forever**, neither signed out nor in. Also: cached/observed role never invalidated on a backend change → a **de-provisioned admin keeps full access** for the whole session. Plus a brief LoginScreen flash on the network-resolve path (authChecked/role-emit race). |
| **HIGH** | NotificationsViewModel (student) | VM never injects `CurrentStudentProvider` / never passes `audienceContext` → controller gets sessionId=null → init filter drops the only emission → **infinite spinner, `sync()` never runs, and session-scoped notices are filtered out even from cache**. Screen is non-functional for students; wiring bug in this file. |
| **HIGH** | StudentExamsHubViewModel | **Bypasses the reviewed `StudentExamsHubController` entirely** and re-implements aggregation with `observe…().first()` + `runCatching{}.getOrDefault(emptyList())`: no remote sync (stale marks forever), no live updates, **entire error channel discarded** (failures look like an empty hub), and infinite spinner when unlinked. Fix = delegate to the controller. |
| **HIGH** | NotificationsBadgeViewModel (admin + student) | Badge shows **0 permanently** — inherits the R3 `NotificationRepositoryImpl.sync()` 400 (queries non-existent `updated_at` + camelCase `targetRole`), `init`-only sync swallowed in `runCatching`, and the count is read from a local table only `sync()` fills. Confirmed both apps. |
| MED-HIGH | ProfileViewModel (admin + student) | A3 (no cache clear on sign-out; fire-and-forget swallowed) + load errors dropped via `runCatching{}.getOrNull()` (A1) + infinite-spinner-when-unlinked (student) + A2 frozen header fields (student) + `error`/`actionMessage` never reset (re-fires on recomposition). |
| MED | HomeViewModel (student) | A2 (header name/gpa/cgpa/session frozen after refresh) + A1 (controller `error` never observed; infinite spinner when unlinked). |
| MED | AuthViewModel (student) | Successful registration with email-confirmation → no session → `accountKey` null → `error(...)` thrown → user sees generic "Sign-in failed" instead of "check your email" (registration looks broken). Also: no double-submit guard; partial-failure (auth succeeds but `provisionUnlinkedStudent`/`resolveRole` throws → authenticated at auth layer but stuck on login with a generic error). |
| MED | DepartmentDetailViewModel / DepartmentsViewModel (admin) | VM-side observed streams (`teachers`, `observeStudentCount`, `departmentStats`) have **no `.catch`** → their failures bypass the controller `error` channel and silently freeze (A1 variant); `createdBy`/`editedBy` snapshotted once at construction (blank if built pre-auth). |
| MED | DashboardViewModel (admin) | A1 confirmed at the design level: `DashboardState` has no `loading`/`error` field, and any of the 5 `combine`d flows throwing terminates the stream → the screen shows **all-zeros indistinguishable from loading / empty / error**, and never recovers. |
| LOW | LoginViewModel (admin) | Correct; minor: no VM-level re-entry guard (relies on the button's `enabled`), and wrong-role `signOut()` inside the try can mask the specific message + leave the non-admin token uncleared (root gate still blocks access). |

## Rated clean (proper thin passthroughs, error/loading all surfaced)
**AdministratorsViewModel, LoginViewModel(admin), LinkRequestsViewModel(admin), NotificationsViewModel(admin), TeachersViewModel(admin), MarkAttendanceViewModel(teacher)** — full delegation, correct identity source, `Eagerly` flows avoid the WhileSubscribed `.value` trap, every controller signal exposed. (LinkRequestViewModel-student is structurally sound; its only real item is a VM-level double-submit guard.)

## Coverage note
**AppRootViewModel (student)** did not actually run — its session was interrupted (0 turns, a stale task-notification), so it is NOT reviewed. The admin AppRoot findings (sign-out hang, no role invalidation) almost certainly apply to it too; it should be re-run to confirm.

## State: 97 of 110 reviewed (student AppRoot pending re-run). Remaining ~13 = the 12 teacher ViewModels (Login, ExamPaperSubmission, Home, Menu, LinkRequests, Marks, NotificationsBadge, Notifications, Profile, AppRoot, Schedule, MyStudents) + student AppRoot re-run. All expected to confirm A1/A2/A3 and the R3 notification breakage.

---

# ⚠️ CORRECTIONS (2026-09-01) — schema-dependent findings read a STALE schema

Several sub-agents (batch 3 repos, batch 4 mappers, batch 5 notification VMs) verified against only the base schema `supabase/migrations/20260714000001_schema.sql` and **missed migration `20260830000001_incremental_sync_updated_at_triggers.sql`** (dated 2026-08-30). That migration `alter table ... add column if not exists` for **created_at, created_by, updated_at, updated_by, is_deleted, deleted_at, deleted_by** + an `updated_at` index + a `fn_touch_updated_at` BEFORE-UPDATE trigger, across **every** synced table — including `notifications`, `timetable_periods`, `fines`, `datesheet_slots`, `calendar_events`, `exam_paper_submissions`, `session_fees`, `session_fee_heads`, `datesheets`, and the rest. Confirmed against current repo code.

## RETRACTED (false positives against current code + schema)
- **R3 — mobile NotificationRepositoryImpl "entirely 400s / feature non-functional": RETRACTED.** The columns now exist, and the *current* code uses snake_case filters throughout (`eq("target_role", …)`, `eq("created_by_email", uid)`, `gte("updated_at", …)`, `set("is_deleted", true)` — verified at `NotificationRepositoryImpl.kt:70-151`). The camelCase citation (`targetRole`/`createdByEmail`) in the batch-3/5 reports does not match the current file. Mobile notifications sync/read/delete are fine. Codex correctly deferred this.
  - Consequently **RETRACTED:** "NotificationsBadgeViewModel badge = 0 forever" (both apps) and the R3-attributed half of the student Notifications finding. **STILL VALID (separate, not schema):** `NotificationsViewModel` (student) never wires `audienceContext` from `CurrentStudentProvider` → sessionId null → infinite spinner + session notices filtered. That is a VM wiring omission, unrelated to R3.
- **SessionTimetableRepositoryImpl.removePeriod "is_deleted column doesn't exist → every delete throws": RETRACTED** — `timetable_periods` now has `is_deleted`. (The *other* SessionTimetable finding — the `uq_session_slot` unique key omitting effective dates → same-slot date-disjoint periods overwrite — is a **separate unique-index issue that this migration does NOT change, so it remains open.**)
- **"Phantom audit columns" findings (Fine, Datesheet-slot, Administrator DTOs modeling non-existent columns): RETRACTED** — `fines`, `datesheet_slots`, `profiles`, etc. now carry those columns, so the DTOs are no longer misrepresenting the schema and the domain audit fields can be populated.

## STILL VALID (not schema-dependent, or now MORE relevant)
- **R1 — mappers drop `isDeleted`/`deletedAt`/`deletedBy` in dtoToEntity: STILL VALID, and now the *primary* delete-sync bug.** The DB now has the tombstone columns and the delta-sync `partition { it.isDeleted }` genuinely needs them — but the mappers still never copy them (the domain models lack the fields), so every soft-deleted row is still re-materialized locally as active. The infrastructure exists; the mapper omission defeats it. (Codex's Semester-Subjects fix sidesteps this for curriculum by switching to targeted delete; the general R1 gap across the other ~7 mappers remains.)
- **R2** (encodeDefaults drops default-valued fields on upsert — e.g. permission revocation), **R4/R5** (non-atomic approve, no status-guard/bounds), **R6** (insert-not-upsert → duplicates; the migration adds no unique constraints), **R7** (case-sensitive enum parse) — all serialization/logic, not schema. Still valid.
- All controller patterns **S1–S4** and the specific logic bugs (SemesterSubjects mass-delete, SessionStudents error-wrap, AcademicSession createSession/maxStudents, UserRepository role-cache/normalization, TeacherAssignmentsProvider email match, etc.) — still valid.
- App-layer **A1/A2/A3** and the batch-5 HIGH bugs (AppRoot sign-out hang, StudentExamsHub bypass, student Notifications audienceContext) — still valid.

## Lesson for the remaining verification
Any *schema-dependent* claim in batches 3–5 should be re-checked against migration `20260830000001` before acting. Logic/serialization/wiring findings are unaffected.

---

# Batch 6 (2026-09-01, final wave): 12 teacher ViewModels + student AppRoot re-run

Closes the sweep. Four genuinely new items beyond A1/A2/A3 confirmations.

## A3 (sign-out) is confirmed on ALL THREE apps — with a nuance and two new sub-findings
Root cause is identical everywhere: `observeCurrentUserRole() = roleResolver.observeRole().filterNotNull()` makes "signed out" **inexpressible** (the null is filtered), and `role = observed ?: startup` pins the stale role. `signOut()` nulls `startupRole` and clears Room, but the gate never sees it.
- **Admin:** hangs on the "LOADING…" splash forever. **Teacher & Student:** strand the user in **stale authenticated content** (the Scaffold keeps rendering) because `authChecked` stays true — same root cause, different symptom.
- **NEW — RoleResolver NPE risk (all apps):** after `userDao.clear()`, `observeCurrent()` (typed non-null `Flow<UserEntity>`) emits `null` from the empty `SELECT … LIMIT 1` → the combine lambda dereferences `user.uid` → NPE → the role stream dies frozen at its last value. Reinforces the sign-out-never-takes-effect defect.
- **NEW — student app lacks the 1.1s min-duration splash** that admin/teacher have (`AppRoot.kt` gates on `!authChecked` only) → a returning student gets a **one-frame LoginScreen flash** on cold start before `role` resolves. Admin/teacher mask this with the splash floor.
- De-provisioned/role-revoked account is never re-resolved mid-session (all apps; `resolveRole` runs once in init).

## Other new bugs
| Sev | File | Bug |
|---|---|---|
| **HIGH** | MyStudentsViewModel (teacher) | **Bypasses MyStudentsController and drops the sync**: `selectAssignment` sets `_selected` but never calls `syncStudents`/`syncSummary` (the controller's `select()` does) → on a fresh install / evicted cache the roster + attendance tallies render **empty and never populate from remote**. Also duplicates the controller's flow logic and drops the error/loading channel (A1). Same "VM re-implements its controller" anti-pattern as StudentExamsHub. |
| **HIGH** | LinkRequestsViewModel (teacher) | **No `permissionCheck` passed → `access` hardcoded GRANTED** → a teacher with `canApproveLinkRequests=false` (menu shows "Restricted") can still open the screen and approve/reject student link requests. Desktop-teacher gates this (`TeacherNavHost.kt:279`); mobile teacher has no gate on the path. Client-side authorization bypass (server RLS may still block, but the intended gate is absent). |
| MED | ScheduleViewModel (teacher) | `refresh()` reads `periods.value` off a `WhileSubscribed(5000)` flow → on first-launch/unsubscribed it's the `emptyList()` seed → **syncs nothing yet reports `Outcome.Success`** (silent no-op). And every `syncSession` is individually `runCatching`-swallowed inside the loop, so the outer `Outcome.Error` is **unreachable** → refresh reports success even when all syncs fail. (Also can't discover a brand-new assignment: only syncs sessions already in local cache.) |
| MED | ExamPaperSubmissionScreen (via VM) | `outcome = uploadState ?: Outcome.Success(Unit)` → the idle state renders the green "**Paper uploaded successfully**" before any upload (and again on every assignment switch, since `select()` resets to null). Plus the file byte-read runs on the **main thread** (no `Dispatchers.IO`) → ANR risk near the 5 MB cap; null input-stream / null filename paths are silent no-ops / unguarded NPE. All in the Screen, not the VM. |

## A1 confirmations (teacher Home/Menu/Profile)
`TeacherHomeSnapshot`/`teacherMenuSnapshot` carry **no loading/error field**, and the VMs seed `stateIn` with a fully-formed placeholder snapshot → "loading" is byte-identical to "loaded but empty," and any upstream `combine` throw freezes the flow with no error surfaced. Teacher Profile adds the A3 sign-out pattern + un-reset `error`/`actionMessage` (re-fires on recomposition) + swallowed department-fetch error. A recurring seam across all three: **`teacherId = accountKey.orEmpty()` snapshotted at construction** while `TeacherAssignmentsProvider` re-reads it live → if built pre-auth, `teacherId=""` can wedge the snapshot permanently.

## Clean
- **MarksEntryViewModel (teacher)** — faithful thin passthrough; only the construction-time `teacherId` snapshot note.

## Not individually run (pure duplicates, covered by sibling reviews)
`NotificationsBadgeViewModel`, `NotificationsViewModel`, `LoginViewModel` (teacher) were not spawned — they are near-identical to the admin/student versions already reviewed (badge inherits the — now RETRACTED — R3 concern; Login mirrors the shared `RoleLoginViewModel` verified via admin). Teacher `NotificationsViewModel` should be spot-checked for the same `audienceContext`-wiring question that broke the student one, but teachers use a role/session context that likely differs.

## SWEEP COMPLETE: 108/110 individually reviewed (2 teacher dup VMs deferred as sibling-covered). All findings in `Documentation/business-logic-findings.md` (Batches 1–6 + Corrections).
