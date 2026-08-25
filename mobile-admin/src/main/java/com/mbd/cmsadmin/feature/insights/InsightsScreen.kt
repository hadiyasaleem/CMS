package com.mbd.cmsadmin.feature.insights

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.controller.InsightsController
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.InsightsRepository
import com.mbd.cmscommon.ui.components.InsightsViewer
import com.mbd.cmscommon.ui.components.InsightsWorkspace
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class InsightsViewModel @Inject constructor(
    repo: InsightsRepository,
    sessionRepository: AcademicSessionRepository,
    departmentRepository: DepartmentRepository,
) : ViewModel() {
    val controller = InsightsController(repo, viewModelScope)
    val sessions = sessionRepository.observeAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val departments = departmentRepository.observeActiveDepartments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

@Composable
fun InsightsScreen(refreshVersion: Int = 0, viewModel: InsightsViewModel = hiltViewModel()) {
    val controller = viewModel.controller
    val overviews by controller.overviews.collectAsState()
    val atRisk by controller.atRisk.collectAsState()
    val examStats by controller.examStats.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val departments by viewModel.departments.collectAsState()
    val loading by controller.refreshing.collectAsState()
    val error by controller.error.collectAsState()

    LaunchedEffect(refreshVersion) {
        if (refreshVersion > 0) controller.refresh()
    }

    InsightsWorkspace(
        overviews = overviews.orEmpty(),
        atRisk = atRisk.orEmpty(),
        examStats = examStats.orEmpty(),
        sessions = sessions,
        departments = departments,
        viewer = InsightsViewer.ADMIN,
        loading = loading,
        errorMessage = error,
        onRetry = controller::refresh,
    )
}
