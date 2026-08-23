package com.mbd.cmsadmin.feature.markrequests

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.controller.MarkEditRequestsController
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.MarkEditRequestRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.ui.components.MarkEditRequestReviewWorkspace
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MarkEditRequestsViewModel @Inject constructor(
    repository: MarkEditRequestRepository,
    sessionRepository: AcademicSessionRepository,
    curriculumRepository: CurriculumRepository,
    departmentRepository: DepartmentRepository,
    teacherRepository: TeacherRepository,
    sessionManager: SessionManager,
) : ViewModel() {
    val controller = MarkEditRequestsController(
        repository = repository,
        sessionRepository = sessionRepository,
        curriculumRepository = curriculumRepository,
        departmentRepository = departmentRepository,
        teacherRepository = teacherRepository,
        reviewedBy = sessionManager.accountKey.orEmpty(),
        scope = viewModelScope,
    )
}

@Composable
fun MarkEditRequestsScreen(
    refreshVersion: Int = 0,
    viewModel: MarkEditRequestsViewModel = hiltViewModel(),
) {
    val controller = viewModel.controller
    val requests by controller.requests.collectAsState()
    val details by controller.details.collectAsState()
    val sessions by controller.sessions.collectAsState()
    val departments by controller.departments.collectAsState()
    val teachers by controller.teachers.collectAsState()
    val loading by controller.loading.collectAsState()
    val busyRequestId by controller.busyRequestId.collectAsState()
    val rowErrors by controller.rowErrors.collectAsState()
    val notice by controller.notice.collectAsState()
    val error by controller.error.collectAsState()

    LaunchedEffect(refreshVersion) {
        if (refreshVersion > 0) controller.refresh()
    }

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
        errorMessage = error,
        onApprove = controller::approve,
        onReject = controller::reject,
        onConsumeNotice = controller::consumeNotice,
        onClearError = controller::clearError,
        onRetry = controller::refresh,
    )
}
