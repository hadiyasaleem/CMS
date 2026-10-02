package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Building
import com.mbd.cmscommon.domain.model.Datesheet
import com.mbd.cmscommon.domain.model.DatesheetSlot
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.ProgramType
import com.mbd.cmscommon.domain.model.Room
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.domain.model.datesheetExternalConflicts
import com.mbd.cmscommon.domain.model.normalized
import com.mbd.cmscommon.domain.model.validationMessage
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.BuildingRepository
import com.mbd.cmscommon.domain.repository.DatesheetRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.RoomRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.util.CmsException
import com.mbd.cmscommon.util.FailureSummary
import com.mbd.cmscommon.util.LoadFailure
import com.mbd.cmscommon.util.orThrowValidation
import com.mbd.cmscommon.util.requireValid
import com.mbd.cmscommon.util.rethrowCancellation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** One department's row within a semester+shift exam grid. [sheet] is null when this department
 * has no datesheet yet for its current semester+shift -- the row still renders (mirroring
 * MasterTimetableController showing every session regardless of whether periods exist), just with
 * no editable cells. */
data class MasterDatesheetGridRow(
    val session: AcademicSession,
    val department: Department?,
    val sheet: Datesheet?,
    val slots: List<DatesheetSlot>,
)

/** One titled exam grid, e.g. "Semester 3 Morning". Mirrors [MasterGrid]'s (semester x programType
 * x shift) identity -- a BS and an MA-Replacement session never share a grid even at the same
 * semester number and shift. */
data class MasterDatesheetGrid(
    val semester: Int,
    val programType: ProgramType,
    val shift: Session,
    val rows: List<MasterDatesheetGridRow>,
) {
    val title: String get() = "Semester $semester${if (programType == ProgramType.MA_REPLACEMENT) " (Intake)" else ""} ${shift.label}"
}

/** Every semester+shift exam grid the college runs, department rows sorted by code. A row's key is
 * deliberately [AcademicSession.currentSemester] -- the department's CURRENT exam schedule, the same
 * "current state" philosophy [buildMasterGrids] already uses for class timetables, not every
 * historical datesheet a session has ever had. */
fun buildMasterDatesheetGrids(
    sessionList: List<AcademicSession>,
    deptList: List<Department>,
    datesheets: List<Datesheet>,
    slots: List<DatesheetSlot>,
): List<MasterDatesheetGrid> {
    val deptById = deptList.associateBy { it.deptId }
    val slotsByDatesheetId = slots.groupBy { it.datesheetId }
    val sheetByKey = datesheets.associateBy { Triple(it.sessionId, it.semester, it.shift) }

    return sessionList
        .flatMap { session -> session.shifts.map { shift -> session to shift } }
        .groupBy({ (session, shift) -> Triple(session.currentSemester, session.programType, shift) }) { (session, shift) ->
            val sheet = sheetByKey[Triple(session.sessionId, session.currentSemester, shift)]
            MasterDatesheetGridRow(
                session = session,
                department = deptById[session.deptId],
                sheet = sheet,
                slots = sheet?.let { slotsByDatesheetId[it.id].orEmpty() }.orEmpty(),
            )
        }
        .map { (key, rows) ->
            MasterDatesheetGrid(
                semester = key.first,
                programType = key.second,
                shift = key.third,
                rows = rows.sortedBy { it.department?.code ?: it.session.deptId },
            )
        }
        .sortedWith(compareBy({ it.semester }, { it.programType }, { it.shift }))
}

/** The admin-facing "every department's exam schedule at once" screen, mirroring
 * [MasterTimetableController]. Never creates a new [DatesheetSlot] -- every slot already exists
 * (curriculum-prefilled, possibly unscheduled) once a datesheet is created from Session Detail ->
 * Datesheet; this controller only assigns an existing slot to a date (or moves/edits one already
 * scheduled). */
class MasterDatesheetController(
    private val departmentRepository: DepartmentRepository,
    private val sessionRepository: AcademicSessionRepository,
    private val datesheetRepository: DatesheetRepository,
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

    val departments: StateFlow<List<Department>> =
        departmentRepository.observeActiveDepartments().stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Graduated sessions are excluded -- same rule DatesheetBrowseController already applies:
     * there is no legitimate reason to create or browse datesheets for a batch that has finished. */
    val sessions: StateFlow<List<AcademicSession>> = sessionRepository.observeAllSessions()
        .map { it.filter(AcademicSession::isActive) }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val datesheets: StateFlow<List<Datesheet>> =
        datesheetRepository.observeDatesheets().stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSlots: StateFlow<List<DatesheetSlot>> =
        datesheetRepository.observeAllSlots().stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _refreshError = MutableStateFlow<String?>(null)
    val refreshError: StateFlow<String?> = _refreshError.asStateFlow()

    /** Every distinct semester number with an active session, low to high. */
    val availableSemesters: StateFlow<List<Int>> = sessions
        .map { list -> list.map { it.currentSemester }.distinct().sorted() }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedSemester = MutableStateFlow<Int?>(null)
    val selectedSemester: StateFlow<Int?> = _selectedSemester.asStateFlow()

    private val _selectedShift = MutableStateFlow<Session?>(null)
    val selectedShift: StateFlow<Session?> = _selectedShift.asStateFlow()

    private val _selectedDeptId = MutableStateFlow<String?>(null)
    val selectedDeptId: StateFlow<String?> = _selectedDeptId.asStateFlow()

    private val _selectedProgramType = MutableStateFlow<ProgramType?>(null)
    val selectedProgramType: StateFlow<ProgramType?> = _selectedProgramType.asStateFlow()

    val grids: StateFlow<List<MasterDatesheetGrid>> = combine(sessions, departments, datesheets, allSlots) { s, d, ds, sl ->
        buildMasterDatesheetGrids(s, d, ds, sl)
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredGrids: StateFlow<List<MasterDatesheetGrid>> =
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

    fun refresh() = launch("refresh the master datesheet") {
        _loading.value = true
        try {
            val failures = mutableListOf<LoadFailure>()
            runCatching { departmentRepository.sync() }.rethrowCancellation().onFailure { failures += LoadFailure("departments", it) }
            val depts = runCatching { departmentRepository.observeActiveDepartments().first() }
                .rethrowCancellation()
                .onFailure { failures += LoadFailure("the department list", it) }
                .getOrDefault(emptyList())
            depts.forEach { dept ->
                runCatching { sessionRepository.syncSessionsForDept(dept.deptId) }.rethrowCancellation().onFailure { failures += LoadFailure("sessions", it) }
            }
            runCatching { datesheetRepository.sync() }.rethrowCancellation().onFailure { failures += LoadFailure("datesheets", it) }
            runCatching { datesheetRepository.syncAllSlots() }.rethrowCancellation().onFailure { failures += LoadFailure("exam papers", it) }
            _refreshError.value = FailureSummary.describe(failures, "MasterDatesheetController", prefix = "Couldn't refresh")
        } finally {
            _loading.value = false
        }
    }

    fun selectSemester(semester: Int?) { _selectedSemester.value = semester }
    fun selectShift(shift: Session?) { _selectedShift.value = shift }
    fun selectDepartment(deptId: String?) { _selectedDeptId.value = deptId }
    fun selectProgramType(programType: ProgramType?) { _selectedProgramType.value = programType }

    fun clearFilters() {
        _selectedSemester.value = null
        _selectedShift.value = null
        _selectedDeptId.value = null
        _selectedProgramType.value = null
    }

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    /**
     * Saves [slot] -- one of [sheet]'s own rows, either previously unscheduled (curriculum
     * prefill) or already scheduled and being moved/edited -- with whatever date/time/room/
     * invigilator the caller has already set on it (mirrors [DatesheetEditorController.updatePaper]'s
     * own shape). Checked against this controller's full in-memory snapshot (same pre-emptive,
     * computed-once philosophy as [MasterTimetableController.savePeriod]), since the admin may be
     * scheduling a clash against a department outside the grid's current filters.
     */
    fun assignPaperToDate(sheet: Datesheet, slot: DatesheetSlot) {
        if (_busy.value) return
        launch("schedule the paper") {
            clearError()
            _busy.value = true
            try {
                val updated = normalized(slot)
                validationMessage(updated).orThrowValidation()

                val siblings = allSlots.value.filter { it.datesheetId == sheet.id }
                if (updated.examDate != null) {
                    siblings.firstOrNull { it.id != updated.id && it.examDate == updated.examDate }?.let {
                        throw CmsException.Validation("${it.subjectName.ifBlank { it.courseCode }} is already scheduled on ${updated.examDate} in this datesheet.")
                    }
                }

                val date = updated.examDate
                if (date != null) {
                    val sheetsById = datesheets.value.associateBy { it.id }
                    val otherPapers = allSlots.value
                        .filter { it.datesheetId != sheet.id && it.examDate == date }
                        .mapNotNull { paper -> sheetsById[paper.datesheetId]?.let { paper to it } }
                    val conflicts = datesheetExternalConflicts(updated, sheet, otherPapers)
                    requireValid(conflicts.isEmpty()) { conflicts.joinToString(" ") }
                }

                datesheetRepository.updateSlot(updated)
            } finally {
                _busy.value = false
            }
        }
    }
}
