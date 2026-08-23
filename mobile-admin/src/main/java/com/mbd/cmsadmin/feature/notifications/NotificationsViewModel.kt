package com.mbd.cmsadmin.feature.notifications

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
) : ViewModel() {
    val controller = NotificationsController(
        repository = repository,
        viewerRole = NotificationTargetRole.ADMIN,
        accountKey = sessionManager.accountKey.orEmpty(),
        sessionRepository = sessionRepository,
        departmentRepository = departmentRepository,
        publisherKind = NotificationPublisherKind.ADMIN,
        scope = viewModelScope,
    )
}
