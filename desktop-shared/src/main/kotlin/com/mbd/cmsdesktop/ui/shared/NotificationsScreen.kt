package com.mbd.cmsdesktop.ui.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.mbd.cmscommon.controller.NotificationPublisherKind
import com.mbd.cmscommon.controller.NotificationsController
import com.mbd.cmscommon.domain.model.NotificationTargetRole
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.NotificationAudienceContext
import com.mbd.cmscommon.domain.repository.NotificationRepository
import com.mbd.cmscommon.teacher.ResolvedAssignment
import com.mbd.cmscommon.ui.components.NotificationControllerWorkspace
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Self-contained Notifications screen shared by every desktop role: builds its own
 * [NotificationsController] and hands it straight to [NotificationControllerWorkspace]. [publisherKind]
 * / [hasAssignmentsCheck] / [assignmentsFlow] only matter for roles that can compose+publish (admin,
 * and teachers with permission) - a plain student/read-only viewer leaves them null.
 */
@Composable
fun NotificationsScreen(
    repository: NotificationRepository,
    role: NotificationTargetRole,
    accountKey: String,
    sessionRepository: AcademicSessionRepository,
    departmentRepository: DepartmentRepository,
    publisherKind: NotificationPublisherKind? = null,
    audienceContext: Flow<NotificationAudienceContext>? = null,
    hasAssignmentsCheck: (suspend () -> Boolean)? = null,
    assignmentsFlow: Flow<List<ResolvedAssignment>>? = null,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(repository, role, accountKey) {
        NotificationsController(
            repository = repository,
            viewerRole = role,
            accountKey = accountKey,
            sessionRepository = sessionRepository,
            departmentRepository = departmentRepository,
            audienceContext = audienceContext ?: flowOf(NotificationAudienceContext()),
            publisherKind = publisherKind ?: NotificationPublisherKind.NONE,
            permissionCheck = hasAssignmentsCheck,
            teacherAssignments = assignmentsFlow ?: flowOf(emptyList()),
            scope = scope,
        )
    }
    NotificationControllerWorkspace(controller = controller)
}
