package com.mbd.cmsadmin.feature.students

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.controller.StudentDirectoryController
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.ui.components.StudentDirectoryWorkspace
import com.mbd.cmscommon.util.rememberDocumentExport
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StudentDirectoryViewModel @Inject constructor(
    sessionRepository: AcademicSessionRepository,
    departmentRepository: DepartmentRepository,
) : ViewModel() {
    val controller = StudentDirectoryController(sessionRepository, departmentRepository, viewModelScope)
}

@Composable
fun StudentDirectoryScreen(
    onOpenStudent: (sessionId: String, rollNumber: String) -> Unit,
    viewModel: StudentDirectoryViewModel = hiltViewModel(),
) {
    val controller = viewModel.controller
    val page by controller.page.collectAsState()
    val query by controller.query.collectAsState()
    val departments by controller.departments.collectAsState()
    val sessions by controller.sessions.collectAsState()
    val statuses by controller.enrollmentStatuses.collectAsState()
    val loaded by controller.loaded.collectAsState()
    val error by controller.error.collectAsState()

    StudentDirectoryWorkspace(
        page = page,
        query = query,
        departments = departments,
        sessions = sessions,
        enrollmentStatuses = statuses,
        loaded = loaded,
        errorMessage = error,
        onSearch = controller::setSearch,
        onDepartment = controller::setDepartment,
        onSession = controller::setSession,
        onShift = controller::setShift,
        onEnrollmentStatus = controller::setEnrollmentStatus,
        onAccount = controller::setAccount,
        onSort = controller::setSort,
        onPreviousPage = controller::previousPage,
        onNextPage = controller::nextPage,
        onClearFilters = controller::clearFilters,
        onClearError = controller::clearError,
        onOpenStudent = { row -> onOpenStudent(row.profile.sessionId, row.profile.rollNumber) },
        onExport = rememberDocumentExport(),
        buildExport = controller::exportDocument,
    )
}
