package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import com.mbd.cmscommon.util.Outcome
import com.mbd.cmscommon.util.userMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn

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

    private val _refreshState = MutableStateFlow<Outcome<Unit>?>(null)
    val refreshState: StateFlow<Outcome<Unit>?> = _refreshState.asStateFlow()

    fun refresh() = launch {
        _refreshState.value = Outcome.Loading
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

        _refreshState.value = failures.firstOrNull()
            ?.let { Outcome.Error(it.userMessage("Could not refresh your schedule."), it) }
            ?: Outcome.Success(Unit)
    }
}
