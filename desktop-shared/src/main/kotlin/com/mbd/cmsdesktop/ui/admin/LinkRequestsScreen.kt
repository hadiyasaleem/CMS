package com.mbd.cmsdesktop.ui.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.mbd.cmscommon.controller.LinkRequestsController
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.StudentLinkRequestRepository
import com.mbd.cmscommon.ui.components.LinkRequestReviewWorkspace

@Composable
fun LinkRequestsScreen(
    repository: StudentLinkRequestRepository,
    sessionRepository: AcademicSessionRepository,
    departmentRepository: DepartmentRepository,
    reviewedBy: String?,
    permissionCheck: (suspend () -> Boolean)? = null,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(repository, sessionRepository, departmentRepository, reviewedBy, permissionCheck) {
        LinkRequestsController(repository, sessionRepository, departmentRepository, reviewedBy.orEmpty(), permissionCheck, scope)
    }
    val requests by controller.requests.collectAsState()
    val sessions by controller.sessions.collectAsState()
    val departments by controller.departments.collectAsState()
    val verifications by controller.verifications.collectAsState()
    val access by controller.access.collectAsState()
    val loading by controller.loading.collectAsState()
    val busyRequestId by controller.busyRequestId.collectAsState()
    val rowErrors by controller.rowErrors.collectAsState()
    val notice by controller.notice.collectAsState()
    val errorMessage by controller.error.collectAsState()

    LinkRequestReviewWorkspace(
        requests = requests,
        sessions = sessions,
        departments = departments,
        verifications = verifications,
        access = access,
        loading = loading,
        busyRequestId = busyRequestId,
        rowErrors = rowErrors,
        notice = notice,
        errorMessage = errorMessage,
        onRefresh = controller::refresh,
        onApprove = controller::approve,
        onReject = controller::reject,
        onConsumeNotice = controller::consumeNotice,
        onClearError = controller::clearError,
    )
}
