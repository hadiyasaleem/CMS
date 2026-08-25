package com.mbd.cmsadmin.feature.hub

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
import com.mbd.cmscommon.domain.repository.DocumentRepository
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
    documentRepository: DocumentRepository,
    insightsRepository: InsightsRepository,
) : ViewModel() {
    private val controller = RecordsHubController(
        sessionRepository,
        calendarRepository,
        datesheetRepository,
        documentRepository,
        insightsRepository,
        viewModelScope,
    )

    val snapshot = controller.snapshot
    val loading = controller.loading
    val error = controller.loadError
    fun refresh() = controller.refresh()
}

@Composable
fun RecordsHubScreen(
    onOpen: (RecordsDestination) -> Unit,
    viewModel: RecordsHubViewModel = hiltViewModel(),
) {
    val snapshot by viewModel.snapshot.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    RecordsHubWorkspace(
        heroPainter = painterResource(R.drawable.admin_records_hero),
        snapshot = snapshot,
        loading = loading,
        errorMessage = error,
        onRetry = viewModel::refresh,
        onOpen = onOpen,
    )
}
