package com.mbd.cmsdesktop.ui.admin

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
import com.mbd.cmscommon.domain.repository.DocumentRepository
import com.mbd.cmscommon.domain.repository.InsightsRepository
import com.mbd.cmscommon.ui.components.RecordsDestination
import com.mbd.cmscommon.ui.components.RecordsHubWorkspace

@Composable
fun RecordsHubScreen(
    sessionRepository: AcademicSessionRepository,
    calendarRepository: CalendarRepository,
    datesheetRepository: DatesheetRepository,
    documentRepository: DocumentRepository,
    insightsRepository: InsightsRepository,
    onOpen: (RecordsDestination) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(sessionRepository, calendarRepository, datesheetRepository, documentRepository, insightsRepository) {
        RecordsHubController(sessionRepository, calendarRepository, datesheetRepository, documentRepository, insightsRepository, scope)
    }
    val snapshot by controller.snapshot.collectAsState()
    val loading by controller.loading.collectAsState()
    val errorMessage by controller.loadError.collectAsState()

    RecordsHubWorkspace(
        heroPainter = painterResource("admin-records-hero.jpg"),
        snapshot = snapshot,
        loading = loading,
        errorMessage = errorMessage,
        onRetry = controller::refresh,
        onOpen = onOpen,
    )
}
