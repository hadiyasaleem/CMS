package com.mbd.cmsadmin.feature.hub

import com.mbd.cmscommon.domain.model.ShiftScope
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmsadmin.R
import com.mbd.cmscommon.controller.RecordsHubController
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.CalendarRepository
import com.mbd.cmscommon.domain.repository.DatesheetRepository
import com.mbd.cmscommon.domain.repository.InsightsRepository
import com.mbd.cmscommon.ui.components.RecordsDestination
import com.mbd.cmscommon.ui.components.RecordsHubWorkspace
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RecordsHubViewModel @Inject constructor(
    sessionRepository: AcademicSessionRepository,
    calendarRepository: CalendarRepository,
    datesheetRepository: DatesheetRepository,
    insightsRepository: InsightsRepository,
    departmentRepository: DepartmentRepository,
) : ViewModel() {
    private val controller = RecordsHubController(
        sessionRepository,
        calendarRepository,
        datesheetRepository,
        insightsRepository,
        viewModelScope,
        departmentRepository = departmentRepository,
    )

    val snapshot = controller.snapshot
    val loading = controller.loading
    val error = controller.loadError
    val filterScope = controller.filterScope
    val filterOptions = controller.filterOptions
    fun refresh() = controller.refresh()
    fun setFilterScope(scope: ShiftScope) = controller.setFilterScope(scope)
}

@Composable
fun RecordsHubScreen(
    onOpen: (RecordsDestination) -> Unit,
    viewModel: RecordsHubViewModel = hiltViewModel(),
) {
    val snapshot by viewModel.snapshot.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val filterScope by viewModel.filterScope.collectAsState()
    val filterOptions by viewModel.filterOptions.collectAsState()

    RecordsHubWorkspace(
        heroPainter = painterResource(R.drawable.admin_records_hero),
        snapshot = snapshot,
        loading = loading,
        errorMessage = error,
        onRetry = viewModel::refresh,
        filterScope = filterScope,
        filterOptions = filterOptions,
        onFilterScope = viewModel::setFilterScope,
        onOpen = onOpen,
    )
}
