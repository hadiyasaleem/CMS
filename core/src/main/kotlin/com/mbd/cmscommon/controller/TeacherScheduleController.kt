package com.mbd.cmscommon.controller

import com.mbd.cmscommon.util.LoadFailure
import com.mbd.cmscommon.util.FailureSummary
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import com.mbd.cmscommon.util.Outcome
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** One semester+shift grid narrowed to a single teacher: only the department rows they teach in,
 * only their own periods within those rows, plus that grid's break gap (if any) so it can still be
 * shown even though no period of the teacher's own ever falls in it. */
data class TeacherGrid(
    val grid: MasterGrid,
    val breakSlot: Pair<String, String>?,
)

class TeacherScheduleController(
    private val teacherId: String,
    private val departmentRepository: DepartmentRepository,
    private val sessionRepository: AcademicSessionRepository,
    private val timetableRepository: SessionTimetableRepository,
    scope: CoroutineScope,
) : ScreenController(scope) {

    val periods: StateFlow<List<SessionPeriod>> =
        timetableRepository.observeMyPeriods(teacherId).stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sessions: StateFlow<List<AcademicSession>> =
        sessionRepository.observeAllSessions().stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val departments: StateFlow<List<Department>> =
        departmentRepository.observeActiveDepartments().stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val allPeriods: StateFlow<List<SessionPeriod>> =
        timetableRepository.observeAll().stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Every semester+shift grid the whole college runs (all departments), used only to find each
     * grid's break gap — the teacher's own grid below only has their own periods, so the gap has to
     * be located from the full picture. */
    private val allGrids: StateFlow<List<MasterGrid>> = combine(sessions, departments, allPeriods) { s, d, p ->
        buildMasterGrids(s, d, p)
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** [allGrids] narrowed to just this teacher: rows with none of their own periods are dropped, and
     * every remaining row keeps only the periods that are actually theirs. */
    val myGrids: StateFlow<List<TeacherGrid>> = allGrids.map { grids ->
        grids.mapNotNull { grid ->
            val myRows = grid.rows.mapNotNull { row ->
                val mine = row.periods.filter { it.teacherId == teacherId }
                if (mine.isEmpty()) null else row.copy(periods = mine)
            }
            if (myRows.isEmpty()) null else TeacherGrid(grid.copy(rows = myRows), detectBreakSlot(grid.rows.flatMap { it.periods }))
        }
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _refreshState = MutableStateFlow<Outcome<Unit>?>(null)
    val refreshState: StateFlow<Outcome<Unit>?> = _refreshState.asStateFlow()

    fun refresh() = launch("refresh your schedule") {
        _refreshState.value = Outcome.Loading
        val failures = mutableListOf<LoadFailure>()

        runCatching { departmentRepository.sync() }.onFailure { failures += LoadFailure("departments", it) }
        val depts = runCatching { departmentRepository.observeActiveDepartments().first() }
            .onFailure { failures += LoadFailure("the department list", it) }
            .getOrDefault(emptyList())
        depts.forEach { dept ->
            runCatching { sessionRepository.syncSessionsForDept(dept.deptId) }.onFailure { failures += LoadFailure("sessions", it) }
        }
        val sessionIds = runCatching { sessionRepository.observeAllSessions().first().map { it.sessionId }.distinct() }
            .onFailure { failures += LoadFailure("the session list", it) }
            .getOrDefault(emptyList())
        sessionIds.forEach { sessionId ->
            runCatching { timetableRepository.syncSession(sessionId) }.onFailure { failures += LoadFailure("timetables", it) }
        }

        _refreshState.value = FailureSummary.describe(failures, "TeacherScheduleController", prefix = "Couldn't refresh")
            ?.let { Outcome.Error(it, failures.first().cause) }
            ?: Outcome.Success(Unit)
    }

    fun clearRefreshState() {
        _refreshState.value = null
    }
}
