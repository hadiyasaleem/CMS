package com.mbd.cmsteacher.feature.notifications

import com.mbd.cmscommon.controller.teacherNotificationAudience
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.teacher.TeacherAssignmentsProvider
import kotlinx.coroutines.flow.combine
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.controller.NotificationPublisherKind
import com.mbd.cmscommon.controller.NotificationsController
import com.mbd.cmscommon.domain.model.NotificationTargetRole
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    repository: NotificationRepository,
    sessionRepository: AcademicSessionRepository,
    departmentRepository: DepartmentRepository,
    sessionManager: SessionManager,
    teacherRepository: TeacherRepository,
    assignmentsProvider: TeacherAssignmentsProvider,
) : ViewModel() {
    private val assignments = assignmentsProvider.observeMyAssignments()

    val controller = NotificationsController(
        repository = repository,
        viewerRole = NotificationTargetRole.TEACHER,
        accountKey = sessionManager.accountKey.orEmpty(),
        sessionRepository = sessionRepository,
        departmentRepository = departmentRepository,
        publisherKind = NotificationPublisherKind.TEACHER,
        // The teacher's own classes: which scoped notices reach them, and which sessions they may notify.
        audienceContext = combine(teacherRepository.observeTeacher(sessionManager.accountKey.orEmpty()), assignments, ::teacherNotificationAudience),
        teacherAssignments = assignments,
        scope = viewModelScope,
    )
}
