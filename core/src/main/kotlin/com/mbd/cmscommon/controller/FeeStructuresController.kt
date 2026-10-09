package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.ProgramType
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionFeeStructure
import com.mbd.cmscommon.domain.model.ShiftScope
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.SessionFeeRepository
import com.mbd.cmscommon.export.ExportDocument
import com.mbd.cmscommon.export.feeStructuresExport
import com.mbd.cmscommon.util.FailureSummary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Where a class's fee structure comes from. */
enum class FeeSource { CUSTOM, COLLEGE, NONE }

/** One titled grid of fee rows for a single semester+program+shift, one row per department --
 * mirrors the master timetable's grids (see [buildMasterGrids]): splitting the shift into the
 * grid's own identity (rather than a row or column) is what keeps a department from appearing
 * twice in the same grid when its session runs both shifts. */
data class FeeGrid(
    val semester: Int,
    val programType: ProgramType,
    val shift: Session,
    val rows: List<FeeRow>,
) {
    val title: String get() = "Semester $semester${if (programType == ProgramType.MA_REPLACEMENT) " (Intake)" else ""} ${shift.label}"
}

/** Groups flat fee [rows] into one grid per semester+program+shift, department rows sorted by name. */
fun buildFeeGrids(rows: List<FeeRow>): List<FeeGrid> =
    rows.groupBy { Triple(it.session.currentSemester, it.session.programType, it.shift) }
        .map { (key, grouped) -> FeeGrid(key.first, key.second, key.third, grouped.sortedBy { it.departmentName }) }
        .sortedWith(compareBy({ it.semester }, { it.programType }, { it.shift }))

/** One class (session + shift) in the fee overview, with the structure it pays. */
data class FeeRow(
    val session: AcademicSession,
    val departmentName: String,
    val shift: Session,
    val structure: SessionFeeStructure?,
) {
    val source: FeeSource
        get() = when {
            structure == null -> FeeSource.NONE
            structure.inherited -> FeeSource.COLLEGE
            else -> FeeSource.CUSTOM
        }
}

/**
 * The classes in the overview: every active session (inside [scope], [programType] and [semester]) once per shift it
 * runs (and the scope's shift, if one is chosen), each with its own structure or else the college base for the shift.
 */
fun feeRows(
    sessions: List<AcademicSession>,
    departments: List<Department>,
    own: List<SessionFeeStructure>,
    base: List<SessionFeeStructure>,
    scope: ShiftScope,
    programType: ProgramType?,
    semester: Int?,
): List<FeeRow> {
    val ownByKey = own.associateBy { it.sessionId to it.shift }
    val baseByShift = base.associateBy { it.shift }
    val deptNames = departments.associate { it.deptId to it.name }
    return sessions
        .filter { it.isActive && scope.matches(it) }
        .filter { programType == null || it.programType == programType }
        .filter { semester == null || it.currentSemester == semester }
        .sortedWith(compareBy({ deptNames[it.deptId] ?: it.deptId }, { -it.startYear }, { it.programType }))
        .flatMap { session ->
            session.shifts.filter { scope.shift == null || it == scope.shift }.map { shift ->
                val structure = ownByKey[session.sessionId to shift]
                    ?: baseByShift[shift]?.copy(sessionId = session.sessionId, inherited = true)
                FeeRow(session, deptNames[session.deptId] ?: session.deptId, shift, structure)
            }
        }
}

/** Every class's fees on one screen: the college-wide base per shift at the top, then a filterable grid. */
class FeeStructuresController(
    private val feeRepository: SessionFeeRepository,
    private val sessionRepository: AcademicSessionRepository,
    private val departmentRepository: DepartmentRepository,
    scope: CoroutineScope,
) : ScreenController(scope) {

    private val sessions: StateFlow<List<AcademicSession>> =
        sessionRepository.observeAllSessions().stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val departments: StateFlow<List<Department>> =
        departmentRepository.observeActiveDepartments().stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _filterScope = MutableStateFlow(ShiftScope.ALL)
    val filterScope: StateFlow<ShiftScope> = _filterScope.asStateFlow()
    private val _programType = MutableStateFlow<ProgramType?>(null)
    val programType: StateFlow<ProgramType?> = _programType.asStateFlow()

    fun setFilterScope(value: ShiftScope) { _filterScope.value = value }
    fun setProgramType(value: ProgramType?) { _programType.value = value }

    val filterOptions: StateFlow<ScopeFilterOptions> = combine(departments, sessions) { d, s -> ScopeFilterOptions.of(d, s) }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), ScopeFilterOptions())

    private val _own = MutableStateFlow<List<SessionFeeStructure>>(emptyList())
    private val _base = MutableStateFlow<List<SessionFeeStructure>>(emptyList())

    /** The college-wide base, Morning first; a shift with no base yet is simply absent. */
    val base: StateFlow<List<SessionFeeStructure>> = _base.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private data class Filters(val scope: ShiftScope, val program: ProgramType?)

    val rows: StateFlow<List<FeeRow>> = combine(
        sessions, departments, _own, _base,
        combine(_filterScope, _programType) { s, p -> Filters(s, p) },
    ) { s, d, own, base, f -> feeRows(s, d, own, base, f.scope, f.program, semester = null) }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** [rows] grouped into one grid per semester+program+shift -- see [FeeGrid]. */
    val grids: StateFlow<List<FeeGrid>> = rows.map { buildFeeGrids(it) }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refresh(fetchRemote = false)
    }

    fun refresh(fetchRemote: Boolean = true) {
        launch("load the fee structures") {
            _loading.value = true
            try {
                val sync = if (fetchRemote) runCatching { feeRepository.syncAll() } else Result.success(Unit)
                val own = runCatching { feeRepository.getAllSessionFees() }
                val base = runCatching { feeRepository.getCollegeFees() }
                own.getOrNull()?.let { _own.value = it }
                base.getOrNull()?.let { _base.value = it.sortedBy { fee -> fee.shift } }
                showError(
                    FailureSummary.describe(
                        FailureSummary.of(listOf("fee structures" to sync, "saved class fees" to own, "saved college fees" to base)),
                        "FeeStructuresController",
                    ),
                )
            } finally {
                _loading.value = false
            }
        }
    }

    /** The grid as one document: the college base per shift, then a row per class with a column per fee head. */
    fun exportDocument(): ExportDocument = feeStructuresExport(_base.value, rows.value)
}
