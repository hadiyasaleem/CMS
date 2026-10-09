package com.mbd.cmsadmin.feature.hub

import com.mbd.cmscommon.domain.model.DeptSemesterScope
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmsadmin.R
import com.mbd.cmscommon.controller.PeopleHubController
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.ExamPaperSubmissionRepository
import com.mbd.cmscommon.domain.repository.MarkEditRequestRepository
import com.mbd.cmscommon.domain.repository.StudentLinkRequestRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.ui.components.PeopleDestination
import com.mbd.cmscommon.ui.components.PeopleHubWorkspace
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PeopleHubViewModel @Inject constructor(
    teacherRepository: TeacherRepository,
    sessionRepository: AcademicSessionRepository,
    linkRequestRepository: StudentLinkRequestRepository,
    markEditRequestRepository: MarkEditRequestRepository,
    examPaperSubmissionRepository: ExamPaperSubmissionRepository,
    departmentRepository: DepartmentRepository,
) : ViewModel() {
    private val controller = PeopleHubController(
        teacherRepository,
        sessionRepository,
        linkRequestRepository,
        markEditRequestRepository,
        examPaperSubmissionRepository,
        viewModelScope,
        departmentRepository,
    )

    val snapshot = controller.snapshot
    val loading = controller.loading
    val error = controller.loadError
    val filterScope = controller.filterScope
    val filterOptions = controller.filterOptions
    val availableSemesters = controller.availableSemesters
    fun refresh() = controller.refresh()
    fun setFilterScope(scope: DeptSemesterScope) = controller.setFilterScope(scope)
}

@Composable
fun PeopleHubScreen(
    onOpen: (PeopleDestination) -> Unit,
    viewModel: PeopleHubViewModel = hiltViewModel(),
) {
    val snapshot by viewModel.snapshot.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val filterScope by viewModel.filterScope.collectAsState()
    val filterOptions by viewModel.filterOptions.collectAsState()
    val availableSemesters by viewModel.availableSemesters.collectAsState()

    PeopleHubWorkspace(
        heroPainter = painterResource(R.drawable.admin_people_hero),
        snapshot = snapshot,
        loading = loading,
        errorMessage = error,
        onRetry = viewModel::refresh,
        filterScope = filterScope,
        filterOptions = filterOptions,
        availableSemesters = availableSemesters,
        onFilterScope = viewModel::setFilterScope,
        onOpen = onOpen,
    )
}
