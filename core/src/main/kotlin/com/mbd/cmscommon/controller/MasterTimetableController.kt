package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import com.mbd.cmscommon.util.userMessage
import java.time.DayOfWeek
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
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

    private val _day = MutableStateFlow(DayOfWeek.MONDAY)
    val day: StateFlow<DayOfWeek> = _day.asStateFlow()

    private val _shift = MutableStateFlow(Session.MORNING)
    val shift: StateFlow<Session> = _shift.asStateFlow()

    val departments: StateFlow<List<Department>> =
        departmentRepository.observeActiveDepartments().stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sessions: StateFlow<List<AcademicSession>> =
        sessionRepository.observeAllSessions().stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val periods: StateFlow<List<SessionPeriod>> = _day
        .flatMapLatest { timetableRepository.observeAllForDay(it) }
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

            _refreshError.value = failures.firstOrNull()?.userMessage("Could not refresh the master timetable.")
        } finally {
            _loading.value = false
        }
    }

    fun selectDay(day: DayOfWeek) {
        _day.value = day
    }

    fun selectShift(shift: Session) {
        _shift.value = shift
    }
}
