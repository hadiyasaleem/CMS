package com.mbd.cmsteacher.feature.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.controller.TeacherScheduleController
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    sessionManager: SessionManager,
    departmentRepository: DepartmentRepository,
    sessionRepository: AcademicSessionRepository,
    timetableRepository: SessionTimetableRepository,
) : ViewModel() {
    private val controller = TeacherScheduleController(
        sessionManager.accountKey.orEmpty(),
        departmentRepository,
        sessionRepository,
        timetableRepository,
        viewModelScope,
    )

    val periods = controller.periods
    val sessions = controller.sessions
    val grids = controller.myGrids
    val outcome = controller.refreshState

    fun refresh() = controller.refresh()
    fun clearOutcome() = controller.clearRefreshState()
}
