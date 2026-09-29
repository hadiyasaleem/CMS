package com.mbd.cmsdesktop.ui.student

import androidx.compose.runtime.getValue

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.painterResource
import com.mbd.cmscommon.controller.StudentTimetableController
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.studentTimetableSnapshot
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import com.mbd.cmscommon.ui.components.StudentTimetableWorkspace
import com.mbd.cmsdesktop.platform.rememberDocumentExport
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Composable
fun StudentTimetableScreen(
    sessionId: String,
    timetableRepository: SessionTimetableRepository,
    /** The student's shift; they see their own shift's periods only. */
    shift: Flow<Session?> = flowOf(null),
) {
    val scope = rememberCoroutineScope()
    val controller = remember(sessionId) { StudentTimetableController(sessionId, timetableRepository, scope, shift) }
    val periods by controller.periods.collectAsState()
    val refreshing by controller.refreshing.collectAsState()
    val errorMessage by controller.error.collectAsState()

    StudentTimetableWorkspace(
        heroPainter = painterResource("splash_postgraduate_block.jpg"),
        snapshot = studentTimetableSnapshot(periods, LocalDate.now(), LocalTime.now()),
        loading = refreshing && periods.isEmpty(),
        errorMessage = errorMessage,
        onRetry = controller::refresh,
        onClearError = controller::clearError,
        onExport = rememberDocumentExport(),
    )
}
