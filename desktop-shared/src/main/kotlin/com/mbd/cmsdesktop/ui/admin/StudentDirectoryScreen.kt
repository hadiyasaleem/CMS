package com.mbd.cmsdesktop.ui.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.mbd.cmscommon.controller.StudentDirectoryController
import com.mbd.cmscommon.controller.StudentRecordController
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.FineRepository
import com.mbd.cmscommon.domain.repository.SessionAttendanceRepository
import com.mbd.cmscommon.domain.repository.SessionFeeRepository
import com.mbd.cmscommon.domain.repository.SessionMarksRepository
import com.mbd.cmscommon.ui.components.StudentDirectoryWorkspace
import com.mbd.cmscommon.ui.components.StudentRecordWorkspace
import com.mbd.cmsdesktop.platform.rememberDocumentExport

@Composable
fun StudentDirectoryScreen(
    sessionRepository: AcademicSessionRepository,
    departmentRepository: DepartmentRepository,
    onOpenStudent: (sessionId: String, rollNumber: String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(sessionRepository, departmentRepository) { StudentDirectoryController(sessionRepository, departmentRepository, scope) }
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

@Composable
fun StudentRecordScreen(
    sessionId: String,
    rollNumber: String,
    sessionRepository: AcademicSessionRepository,
    departmentRepository: DepartmentRepository,
    curriculumRepository: CurriculumRepository,
    attendanceRepository: SessionAttendanceRepository,
    marksRepository: SessionMarksRepository,
    feeRepository: SessionFeeRepository,
    fineRepository: FineRepository,
    onBack: () -> Unit,
    onEditProfile: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(sessionId, rollNumber) {
        StudentRecordController(
            sessionId, rollNumber, sessionRepository, departmentRepository, curriculumRepository,
            attendanceRepository, marksRepository, feeRepository, fineRepository, scope,
        )
    }
    val record by controller.record.collectAsState()
    val loading by controller.loading.collectAsState()
    val notFound by controller.notFound.collectAsState()
    val error by controller.error.collectAsState()

    StudentRecordWorkspace(
        rollNumber = rollNumber,
        record = record,
        loading = loading,
        notFound = notFound,
        errorMessage = error,
        onBack = onBack,
        onEditProfile = onEditProfile,
        onRetry = controller::refresh,
        onClearError = controller::clearError,
        onExport = rememberDocumentExport(),
    )
}
