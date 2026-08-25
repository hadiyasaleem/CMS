package com.mbd.cmsdesktop.ui.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.mbd.cmscommon.controller.MarkEditRequestsController
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.MarkEditRequestRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.ui.components.MarkEditRequestReviewWorkspace

@Composable
fun MarkEditRequestsScreen(
    repository: MarkEditRequestRepository,
    sessionRepository: AcademicSessionRepository,
    curriculumRepository: CurriculumRepository,
    departmentRepository: DepartmentRepository,
    teacherRepository: TeacherRepository,
    reviewedBy: String?,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(repository, sessionRepository, curriculumRepository, departmentRepository, teacherRepository, reviewedBy) {
        MarkEditRequestsController(repository, sessionRepository, curriculumRepository, departmentRepository, teacherRepository, reviewedBy.orEmpty(), scope)
    }
    val requests by controller.requests.collectAsState()
    val details by controller.details.collectAsState()
    val sessions by controller.sessions.collectAsState()
    val departments by controller.departments.collectAsState()
    val teachers by controller.teachers.collectAsState()
    val loading by controller.loading.collectAsState()
    val busyRequestId by controller.busyRequestId.collectAsState()
    val rowErrors by controller.rowErrors.collectAsState()
    val notice by controller.notice.collectAsState()
    val errorMessage by controller.error.collectAsState()

    MarkEditRequestReviewWorkspace(
        requests = requests,
        details = details,
        sessions = sessions,
        departments = departments,
        teachers = teachers,
        loading = loading,
        busyRequestId = busyRequestId,
        rowErrors = rowErrors,
        notice = notice,
        errorMessage = errorMessage,
        onApprove = controller::approve,
        onReject = controller::reject,
        onRefresh = controller::refresh,
        onConsumeNotice = controller::consumeNotice,
        onClearError = controller::clearError,
    )
}
