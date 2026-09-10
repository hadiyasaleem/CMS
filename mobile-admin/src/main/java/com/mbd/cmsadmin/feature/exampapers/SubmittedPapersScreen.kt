package com.mbd.cmsadmin.feature.exampapers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.controller.SubmittedPapersController
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.ExamPaperSubmissionRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.ui.components.SubmittedPapersWorkspace
import com.mbd.cmscommon.util.FileOpener
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SubmittedPapersViewModel @Inject constructor(
    repository: ExamPaperSubmissionRepository,
    teacherRepository: TeacherRepository,
    departmentRepository: DepartmentRepository,
    sessionRepository: AcademicSessionRepository,
) : ViewModel() {
    val controller = SubmittedPapersController(
        repo = repository,
        teacherRepository = teacherRepository,
        departmentRepository = departmentRepository,
        sessionRepository = sessionRepository,
        scope = viewModelScope,
    )
}

@Composable
fun SubmittedPapersScreen(viewModel: SubmittedPapersViewModel = hiltViewModel()) {
    val controller = viewModel.controller
    val context = LocalContext.current
    val grouped by controller.grouped.collectAsState()
    val teachers by controller.teachers.collectAsState()
    val departments by controller.departments.collectAsState()
    val filters by controller.filters.collectAsState()
    val loading by controller.loading.collectAsState()
    val notice by controller.notice.collectAsState()

    SubmittedPapersWorkspace(
        grouped = grouped,
        teachers = teachers,
        departments = departments,
        filters = filters,
        loading = loading,
        notice = notice,
        onSetTeacherFilter = controller::setTeacherFilter,
        onSetDeptFilter = controller::setDeptFilter,
        onSetSemesterFilter = controller::setSemesterFilter,
        onSetShiftFilter = controller::setShiftFilter,
        onClearFilters = controller::clearFilters,
        onDownload = { submission ->
            controller.downloadAndOpen(submission, context.cacheDir) { file ->
                FileOpener.open(context, file, "application/pdf")
            }
        },
        onConsumeNotice = controller::consumeNotice,
        onRefresh = controller::refresh,
    )
}
