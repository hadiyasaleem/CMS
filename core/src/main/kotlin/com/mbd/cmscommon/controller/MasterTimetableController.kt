package com.mbd.cmscommon.controller

import com.mbd.cmscommon.util.rethrowCancellation
import com.mbd.cmscommon.util.FailureSummary
import com.mbd.cmscommon.util.LoadFailure
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Building
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.PeriodType
import com.mbd.cmscommon.domain.model.ProgramType
import com.mbd.cmscommon.domain.model.Room
import com.mbd.cmscommon.domain.model.SemesterSubject
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.BuildingRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.RoomRepository
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.util.CmsException
import com.mbd.cmscommon.util.clockDisplay
import com.mbd.cmscommon.util.orThrowValidation
import com.mbd.cmscommon.util.parseClock
import com.mbd.cmscommon.util.requireValid
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** One department's row within a semester+shift grid. */
data class MasterGridRow(
    val session: AcademicSession,
    val department: Department?,
    val periods: List<SessionPeriod>,
)

/** One titled grid, e.g. "Semester 1 Morning" — mirrors the college's printed per-semester timetables.
 * A BS session and an MA-Replacement session can share the same [semester] number (MA-Replacement runs
 * 5-8), so [programType] is part of the grid's identity: each program type always gets its own grid,
 * never merged, even at the same semester and shift. */
data class MasterGrid(
    val semester: Int,
    val programType: ProgramType,
    val shift: Session,
    val rows: List<MasterGridRow>,
) {
    val title: String get() = "Semester $semester${if (programType == ProgramType.MA_REPLACEMENT) " (Intake)" else ""} ${shift.label}"
}

/**
 * Pure cascade math for "edit one time column, push overlapping neighbours out of the way": given every
 * distinct (start, end) column in a grid, sorted left to right, re-time [oldKey] to [newStart]-[newEnd]
 * and, if that now overlaps the next column, shift it forward by the same amount (keeping its own
 * duration), and so on rightward until a column no longer overlaps its neighbour. Returns old-column-key
 * -> new-column-key for every column that moved (including [oldKey] itself), or an empty map if
 * [oldKey] isn't one of [columns] or the new range is invalid. No I/O — safe to call for a live preview
 * before anything is saved.
 */
fun timeSlotCascade(
    columns: List<Pair<String, String>>,
    oldKey: Pair<String, String>,
    newStart: String,
    newEnd: String,
): Map<Pair<String, String>, Pair<String, String>> {
    val newStartTime = parseClock(newStart) ?: return emptyMap()
    val newEndTime = parseClock(newEnd) ?: return emptyMap()
    if (!newEndTime.isAfter(newStartTime)) return emptyMap()
    val startIndex = columns.indexOf(oldKey)
    if (startIndex < 0) return emptyMap()

    val shifts = LinkedHashMap<Pair<String, String>, Pair<String, String>>()
    shifts[columns[startIndex]] = clockDisplay(newStart) to clockDisplay(newEnd)
    var frontier: LocalTime = newEndTime
    for (i in (startIndex + 1) until columns.size) {
        val (colStart, colEnd) = columns[i]
        val colStartTime = parseClock(colStart) ?: break
        if (!colStartTime.isBefore(frontier)) break // this column already starts at/after the new frontier — no overlap, stop.
        val duration = Duration.between(colStartTime, parseClock(colEnd) ?: break)
        val shiftedStart = frontier
        val shiftedEnd = shiftedStart.plus(duration)
        shifts[colStart to colEnd] = shiftedStart.toString().take(5) to shiftedEnd.toString().take(5)
        frontier = shiftedEnd
    }
    return shifts
}

/** Every semester+shift grid the college runs, department rows sorted by code. Shared by
 * [MasterTimetableController] (every row) and [TeacherScheduleController] (narrowed to one teacher's own rows). */
fun buildMasterGrids(
    sessionList: List<AcademicSession>,
    deptList: List<Department>,
    periods: List<SessionPeriod>,
): List<MasterGrid> {
    val deptById = deptList.associateBy { it.deptId }
    // A merged lecture is stored once, under its owning session; every linked session's row shows it too.
    val withMerged = periods + periods.filter { it.isOwnRow && it.linkedSessionIds.isNotEmpty() }.flatMap { p ->
        p.linkedSessionIds.map { linked ->
            p.copy(id = "${p.id}::$linked", sessionId = linked, isOwnRow = false, linkedSessionIds = p.linkedSessionIds - linked + p.sessionId)
        }
    }
    val periodsBySessionShift = withMerged.groupBy { it.sessionId to it.shift }
    return sessionList
        .flatMap { session -> session.shifts.map { shift -> session to shift } }
        .groupBy({ (session, shift) -> Triple(session.currentSemester, session.programType, shift) }) { (session, shift) ->
            MasterGridRow(
                session = session,
                department = deptById[session.deptId],
                periods = periodsBySessionShift[session.sessionId to shift].orEmpty(),
            )
        }
        .map { (key, rows) ->
            MasterGrid(
                semester = key.first,
                programType = key.second,
                shift = key.third,
                rows = rows.sortedBy { it.department?.code ?: it.session.deptId },
            )
        }
        .sortedWith(compareBy({ it.semester }, { it.programType }, { it.shift }))
}

/**
 * The one time gap in a grid's day where no lecture is ever scheduled (e.g. the morning 10:40-11:00
 * break) — detected purely from a genuine gap between two adjacent known columns, since breaks aren't
 * stored as their own timetable_periods rows. Returns null when the grid's columns run back-to-back
 * (typically the evening shift, which has no such gap).
 */
fun detectBreakSlot(gridPeriods: List<SessionPeriod>): Pair<String, String>? {
    val columns = gridPeriods.map { clockDisplay(it.startTime) to clockDisplay(it.endTime) }.distinct().sortedBy { parseClock(it.first) }
    for (i in 0 until columns.size - 1) {
        val (_, endA) = columns[i]
        val (startB, _) = columns[i + 1]
        if (endA == startB) continue
        val endTime = parseClock(endA) ?: continue
        val startTime = parseClock(startB) ?: continue
        if (startTime.isAfter(endTime)) return endA to startB
    }
    return null
}

/** Every period in [grid] that the column re-timing [shifts] moves, paired with its re-timed copy. */
fun columnMoves(
    grid: MasterGrid,
    shifts: Map<Pair<String, String>, Pair<String, String>>,
): List<Pair<SessionPeriod, SessionPeriod>> = grid.rows.flatMap { it.periods }.mapNotNull { period ->
    shifts[clockDisplay(period.startTime) to clockDisplay(period.endTime)]?.let { (newStart, newEnd) ->
        period to period.copy(
            id = SessionPeriod.buildId(period.sessionId, period.shift, period.day, newStart),
            startTime = newStart,
            endTime = newEnd,
        )
    }
}

/**
 * The first teacher/room double-booking the [moves] would create, described with the other class and
 * subject -- or null. Judged against the timetable AFTER the move (moved periods at their new times), so
 * two columns swapping places don't flag each other.
 */
fun columnMoveConflict(
    moves: List<Pair<SessionPeriod, SessionPeriod>>,
    allPeriods: List<SessionPeriod>,
    sessions: List<AcademicSession>,
    departments: List<Department>,
): String? {
    val movedIds = moves.map { it.first.id }.toSet()
    val resulting = allPeriods.filterNot { it.id in movedIds } + moves.map { it.second }
    return moves.firstNotNullOfOrNull { (_, moved) -> describeTimetableConflict(moved, resulting, sessions, departments) }
}

class MasterTimetableController(
    private val departmentRepository: DepartmentRepository,
    private val sessionRepository: AcademicSessionRepository,
    private val timetableRepository: SessionTimetableRepository,
    private val curriculumRepository: CurriculumRepository,
    teacherRepository: TeacherRepository,
    buildingRepository: BuildingRepository,
    roomRepository: RoomRepository,
    scope: CoroutineScope,
) : ScreenController(scope) {

    val teachers: StateFlow<List<Teacher>> =
        teacherRepository.observeActiveTeachers().stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val buildings: StateFlow<List<Building>> =
        buildingRepository.observeActiveBuildings().stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rooms: StateFlow<List<Room>> =
        roomRepository.observeActiveRooms().stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _refreshError = MutableStateFlow<String?>(null)
    val refreshError: StateFlow<String?> = _refreshError.asStateFlow()

    /** Confirms a successful [savePeriod] -- without it, saving a period that happens to come out
     * unchanged (e.g. re-picking the subject it already had) looks identical to the save silently
     * doing nothing, since the only other feedback this screen gives is [error] on failure. */
    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    fun consumeActionMessage() {
        _actionMessage.value = null
    }

    val departments: StateFlow<List<Department>> =
        departmentRepository.observeActiveDepartments().stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sessions: StateFlow<List<AcademicSession>> =
        sessionRepository.observeAllSessions().stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val allPeriods: StateFlow<List<SessionPeriod>> =
        timetableRepository.observeAll().stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Every teacher/room double-booking across the whole college, keyed by period id -- see
     * [masterTimetableConflicts]. Computed from every period regardless of the active filters, so a
     * conflict with a period outside the current filter is still detected and explained. */
    val periodConflicts: StateFlow<Map<String, List<PeriodConflict>>> = allPeriods
        .map { masterTimetableConflicts(it) }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyMap())

    /** Every distinct semester number with an active session, low to high (1, 3, 5, 7, …). */
    val availableSemesters: StateFlow<List<Int>> = sessions
        .map { list -> list.map { it.currentSemester }.distinct().sorted() }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** null means "All" — the dropdown's default, unfiltered state. */
    private val _selectedSemester = MutableStateFlow<Int?>(null)
    val selectedSemester: StateFlow<Int?> = _selectedSemester.asStateFlow()

    private val _selectedShift = MutableStateFlow<Session?>(null)
    val selectedShift: StateFlow<Session?> = _selectedShift.asStateFlow()

    private val _selectedDeptId = MutableStateFlow<String?>(null)
    val selectedDeptId: StateFlow<String?> = _selectedDeptId.asStateFlow()

    /** null means "All" -- shows both BS and MA-Replacement grids together. */
    private val _selectedProgramType = MutableStateFlow<ProgramType?>(null)
    val selectedProgramType: StateFlow<ProgramType?> = _selectedProgramType.asStateFlow()

    /** Every semester+shift grid the college runs, department rows sorted by code. */
    val grids: StateFlow<List<MasterGrid>> = combine(sessions, departments, allPeriods) { sessionList, deptList, periods ->
        buildMasterGrids(sessionList, deptList, periods)
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** [grids] narrowed by the active filters. Each dropdown defaults to "All" (null), which lets
     * every grid through; picking a value narrows to matching grids (or rows, for department). */
    val filteredGrids: StateFlow<List<MasterGrid>> =
        combine(grids, _selectedSemester, _selectedShift, _selectedDeptId, _selectedProgramType) { all, semester, shift, deptId, programType ->
            all.asSequence()
                .filter { semester == null || it.semester == semester }
                .filter { shift == null || it.shift == shift }
                .filter { programType == null || it.programType == programType }
                .map { grid -> if (deptId == null) grid else grid.copy(rows = grid.rows.filter { it.session.deptId == deptId }) }
                .filter { it.rows.isNotEmpty() }
                .toList()
        }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        _loading.value = false
    }

    fun refresh() = launch("refresh the master timetable") {
        _loading.value = true
        try {
            val failures = mutableListOf<LoadFailure>()

            runCatching { departmentRepository.sync() }.rethrowCancellation().onFailure { failures += LoadFailure("departments", it) }
            // The local reads can fail too (a broken cache) -- they must show up as a named refresh error, not escape.
            val depts = runCatching { departmentRepository.observeActiveDepartments().first() }
                .rethrowCancellation()
                .onFailure { failures += LoadFailure("the department list", it) }
                .getOrDefault(emptyList())
            depts.forEach { dept ->
                runCatching { sessionRepository.syncSessionsForDept(dept.deptId) }.rethrowCancellation().onFailure { failures += LoadFailure("sessions", it) }
            }
            val sessionIds = runCatching { sessionRepository.observeAllSessions().first().map { it.sessionId }.distinct() }
                .rethrowCancellation()
                .onFailure { failures += LoadFailure("the session list", it) }
                .getOrDefault(emptyList())
            sessionIds.forEach { sessionId ->
                runCatching { timetableRepository.syncSession(sessionId) }.rethrowCancellation().onFailure { failures += LoadFailure("timetables", it) }
            }

            _refreshError.value = FailureSummary.describe(failures, "MasterTimetableController", prefix = "Couldn't refresh")
        } finally {
            _loading.value = false
        }
    }

    fun selectSemester(semester: Int?) {
        _selectedSemester.value = semester
    }

    fun selectShift(shift: Session?) {
        _selectedShift.value = shift
    }

    fun selectDepartment(deptId: String?) {
        _selectedDeptId.value = deptId
    }

    fun selectProgramType(programType: ProgramType?) {
        _selectedProgramType.value = programType
    }

    fun clearFilters() {
        _selectedSemester.value = null
        _selectedShift.value = null
        _selectedDeptId.value = null
        _selectedProgramType.value = null
    }

    /**
     * Persists a set of column edits already staged in the UI (see [timeSlotCascade]): for every
     * original-column-key -> new-column-key pair, every period currently at that original time in
     * [grid] is moved to the new time. Applied in descending order of the new start time — a later
     * target time is never one still occupied by a column that hasn't moved yet — so an edit that
     * cascaded through several columns never trips the no-double-booking check on its own account.
     */
    fun applyShifts(grid: MasterGrid, shifts: Map<Pair<String, String>, Pair<String, String>>) = launch("save the column times") {
        requireValid(shifts.isNotEmpty()) { "Nothing to save." }
        // A cascade re-times whole columns, so it can put a teacher or room on top of a lecture somewhere else in the
        // college. Check the RESULTING timetable before writing anything and name the class it would clash with.
        columnMoveConflict(columnMoves(grid, shifts), allPeriods.value, sessions.value, departments.value)?.let {
            throw CmsException.Conflict("These new times would create a clash, so nothing was changed. $it")
        }
        for ((oldKey, newKeyPair) in shifts.entries.sortedByDescending { parseClock(it.value.first) }) {
            val (newS, newE) = newKeyPair
            for (row in grid.rows) {
                for (period in row.periods) {
                    if (!period.isOwnRow) continue // a linked session's view moves with its owner's row
                    val key = clockDisplay(period.startTime) to clockDisplay(period.endTime)
                    if (key != oldKey) continue
                    val updated = period.copy(
                        id = SessionPeriod.buildId(period.sessionId, period.shift, period.day, newS),
                        startTime = newS,
                        endTime = newE,
                    )
                    timetableRepository.removePeriod(period)
                    timetableRepository.savePeriod(updated)
                }
            }
        }
        _actionMessage.value = "Column times updated."
    }

    /** The subjects offered for one row's own session+semester -- fetched on demand when its edit
     * dialog opens, since the grid spans many sessions and keeping every one's subject list live
     * would be wasted work. */
    suspend fun subjectsFor(sessionId: String, semester: Int): List<SemesterSubject> =
        curriculumRepository.observeSemesterSubjects(sessionId, semester).first()

    /**
     * Edits one existing period from the grid (subject, teacher, room, building, time, notes, or its
     * day). [replaces] is the period being edited; a day removed from [days] deletes its own row, a
     * day added beyond [replaces]'s own inserts a new one alongside it.
     */
    fun savePeriod(
        replaces: SessionPeriod,
        days: Set<DayOfWeek>,
        start: String,
        end: String,
        subject: SemesterSubject?,
        teachers: List<Teacher>,
        periodType: PeriodType,
        roomNo: String?,
        building: String?,
        notes: String?,
        effectiveFrom: LocalDate?,
        effectiveTo: LocalDate?,
    ) = launch("save the period") {
        requireValid(days.isNotEmpty()) { "Choose at least one day." }
        requireValid(periodType == PeriodType.BREAK || subject != null) { "Choose a subject for this period." }

        val normalizedStart = start.trim()
        val normalizedEnd = end.trim()
        val sessionPeriods = timetableRepository.observeWeek(replaces.sessionId).first()
        // Snapshotted once up front: the database's own trigger is still the source of truth for a
        // conflict newly introduced by this same multi-day save, but for the common case this lets us
        // name exactly which other class/subject a teacher or room double-books, instead of the
        // trigger's own bare "already booked" message.
        val collegeWidePeriods = allPeriods.value
        val sessionList = sessions.value
        val deptList = departments.value

        days.forEach { day ->
            // The dialog pre-ticks every sibling day sharing this slot (e.g. "Mon & Tue" collapsed into
            // one row, see siblingDaysFor), so a ticked day other than the one that was clicked is still
            // an edit of its own already-existing period, not a brand-new one -- look it up by the same
            // criteria the dialog used to find it, so it's excluded from the conflict check and updated
            // in place instead of colliding with itself. A day the user ticked manually, with no such
            // sibling, still goes through as a genuinely new period (and still conflicts if one clashes).
            val dayReplaces = if (day == replaces.day) {
                replaces
            } else {
                sessionPeriods.firstOrNull {
                    it.shift == replaces.shift && it.day == day && it.startTime == replaces.startTime &&
                        it.endTime == replaces.endTime && it.courseCode == replaces.courseCode && it.hasSameTeachersAs(replaces)
                }
            }
            val period = SessionPeriod(
                id = SessionPeriod.buildId(replaces.sessionId, replaces.shift, day, normalizedStart),
                sessionId = replaces.sessionId,
                shift = replaces.shift,
                day = day,
                startTime = normalizedStart,
                endTime = normalizedEnd,
                courseCode = subject?.courseCode ?: "BREAK",
                subjectName = subject?.name ?: "Break",
                teacherId = periodTeachers(periodType, teachers).firstOrNull()?.teacherId ?: "",
                teacherName = periodTeachers(periodType, teachers).firstOrNull()?.name ?: "",
                coTeacherIds = periodTeachers(periodType, teachers).drop(1).map { it.teacherId },
                coTeacherNames = periodTeachers(periodType, teachers).drop(1).map { it.name },
                periodType = periodType,
                creditHours = subject?.creditHours,
                roomNo = roomNo?.trim()?.takeIf { it.isNotBlank() },
                building = building?.trim()?.takeIf { it.isNotBlank() },
                notes = notes?.trim()?.takeIf { it.isNotBlank() },
                effectiveFrom = effectiveFrom,
                effectiveTo = effectiveTo,
            )
            validateTimetablePeriod(period, dayReplaces, sessionPeriods).orThrowValidation()
            describeTimetableConflict(period, collegeWidePeriods, sessionList, deptList, excludedId = dayReplaces?.id)?.let {
                throw CmsException.Conflict(it)
            }
            timetableRepository.savePeriod(period)
            if (dayReplaces != null && (dayReplaces.shift != period.shift || dayReplaces.day != period.day || clockDisplay(dayReplaces.startTime) != clockDisplay(period.startTime))) {
                timetableRepository.removePeriod(dayReplaces)
            }
        }
        if (replaces.day !in days) {
            timetableRepository.removePeriod(replaces)
        }
        _actionMessage.value = "Period saved."
    }
}
