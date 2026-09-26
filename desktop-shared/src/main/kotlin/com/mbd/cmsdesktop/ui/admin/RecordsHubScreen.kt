package com.mbd.cmsdesktop.ui.admin

import com.mbd.cmscommon.domain.repository.DepartmentRepository
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.painterResource
import com.mbd.cmscommon.controller.RecordsHubController
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.CalendarRepository
import com.mbd.cmscommon.domain.repository.DatesheetRepository
import com.mbd.cmscommon.domain.repository.InsightsRepository
import com.mbd.cmscommon.ui.components.RecordsDestination
import com.mbd.cmscommon.ui.components.RecordsHubWorkspace

@Composable
fun RecordsHubScreen(
    sessionRepository: AcademicSessionRepository,
    calendarRepository: CalendarRepository,
    datesheetRepository: DatesheetRepository,
    insightsRepository: InsightsRepository,
    departmentRepository: DepartmentRepository? = null,
    onOpen: (RecordsDestination) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(sessionRepository, calendarRepository, datesheetRepository, insightsRepository) {
        RecordsHubController(sessionRepository, calendarRepository, datesheetRepository, insightsRepository, scope, departmentRepository = departmentRepository)
    }
    val snapshot by controller.snapshot.collectAsState()
    val loading by controller.loading.collectAsState()
    val errorMessage by controller.loadError.collectAsState()
    val filterScope by controller.filterScope.collectAsState()
    val filterOptions by controller.filterOptions.collectAsState()

    RecordsHubWorkspace(
        heroPainter = painterResource("admin-records-hero.jpg"),
        snapshot = snapshot,
        loading = loading,
        errorMessage = errorMessage,
        onRetry = controller::refresh,
        filterScope = filterScope,
        filterOptions = filterOptions,
        onFilterScope = controller::setFilterScope,
        onOpen = onOpen,
    )
}
