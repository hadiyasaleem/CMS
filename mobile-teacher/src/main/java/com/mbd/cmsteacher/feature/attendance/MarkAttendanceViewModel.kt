package com.mbd.cmsteacher.feature.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.controller.MarkAttendanceController
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.NotificationRepository
import com.mbd.cmscommon.domain.repository.SessionAttendanceRepository
import com.mbd.cmscommon.teacher.TeacherAssignmentsProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class MarkAttendanceViewModel @Inject constructor(
    sessionManager: SessionManager,
    attendanceRepository: SessionAttendanceRepository,
    sessionRepository: AcademicSessionRepository,
    notificationRepository: NotificationRepository,
    assignmentsProvider: TeacherAssignmentsProvider,
) : ViewModel() {

    val controller = MarkAttendanceController(
        attendanceRepository = attendanceRepository,
        sessionRepository = sessionRepository,
        notificationRepository = notificationRepository,
        teacherId = sessionManager.accountKey.orEmpty(),
        scope = viewModelScope,
    )

    val assignments = assignmentsProvider.observeMyAssignments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
