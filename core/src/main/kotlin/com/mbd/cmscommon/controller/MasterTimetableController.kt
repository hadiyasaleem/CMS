package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

class MasterTimetableController(
    private val departmentRepository: DepartmentRepository,
    private val sessionRepository: AcademicSessionRepository,
    private val timetableRepository: SessionTimetableRepository,
    scope: CoroutineScope,
) : ScreenController(scope) {

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _refreshError = MutableStateFlow<String?>(null)
    val refreshError: StateFlow<String?> = _refreshError.asStateFlow()

    val departments: StateFlow<List<Department>> =
        departmentRepository.observeActiveDepartments().stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sessions: StateFlow<List<AcademicSession>> =
        sessionRepository.observeAllSessions().stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedDeptId = MutableStateFlow<String?>(null)
    val selectedDeptId: StateFlow<String?> = _selectedDeptId.asStateFlow()

    private val _selectedStartYear = MutableStateFlow<Int?>(null)
    val selectedStartYear: StateFlow<Int?> = _selectedStartYear.asStateFlow()

    private val _selectedShift = MutableStateFlow<Session?>(null)
    val selectedShift: StateFlow<Session?> = _selectedShift.asStateFlow()

    /** Sessions in the selected department, offered as choices for the "Session" dropdown. */
    val sessionsInDepartment: StateFlow<List<AcademicSession>> = combine(sessions, _selectedDeptId) { all, deptId ->
        if (deptId == null) emptyList() else all.filter { it.deptId == deptId }
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Shifts available for the selected department + intake year, offered as choices for the "Shift" dropdown. */
    val shiftsForSelection: StateFlow<List<Session>> = combine(sessionsInDepartment, _selectedStartYear) { inDept, year ->
        if (year == null) emptyList() else inDept.filter { it.startYear == year }.map { it.shift }.distinct()
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** The single session identified once department + intake year + shift are all chosen. */
    val resolvedSession: StateFlow<AcademicSession?> = combine(sessions, _selectedDeptId, _selectedStartYear, _selectedShift) { all, deptId, year, shift ->
        if (deptId == null || year == null || shift == null) {
            null
        } else {
            all.firstOrNull { it.deptId == deptId && it.startYear == year && it.shift == shift }
        }
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    val periods: StateFlow<List<SessionPeriod>> = resolvedSession
        .flatMapLatest { session -> if (session != null) timetableRepository.observeWeek(session.sessionId) else flowOf(emptyList()) }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        _loading.value = false
    }

    fun refresh() = launch {
        _loading.value = true
        try {
            val failures = mutableListOf<Throwable>()

            runCatching { departmentRepository.sync() }.onFailure { failures += it }
            val depts = departmentRepository.observeActiveDepartments().first()
            depts.forEach { dept ->
                runCatching { sessionRepository.syncSessionsForDept(dept.deptId) }.onFailure { failures += it }
            }
            val sessionIds = sessionRepository.observeAllSessions().first().map { it.sessionId }.distinct()
            sessionIds.forEach { sessionId ->
                runCatching { timetableRepository.syncSession(sessionId) }.onFailure { failures += it }
            }

            _refreshError.value = failures.firstOrNull()?.userMessageLogged("Could not refresh the master timetable.")
        } finally {
            _loading.value = false
        }
    }

    fun selectDepartment(deptId: String?) {
        _selectedDeptId.value = deptId
        _selectedStartYear.value = null
        _selectedShift.value = null
    }

    fun selectStartYear(year: Int?) {
        _selectedStartYear.value = year
        _selectedShift.value = null
    }

    fun selectShift(shift: Session?) {
        _selectedShift.value = shift
    }
}
