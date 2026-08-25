package com.mbd.cmsdesktop.ui.student

import androidx.compose.runtime.getValue

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.painterResource
import com.mbd.cmscommon.controller.StudentTimetableController
import com.mbd.cmscommon.domain.model.studentTimetableSnapshot
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import com.mbd.cmscommon.ui.components.StudentTimetableWorkspace
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun StudentTimetableScreen(
    sessionId: String,
    timetableRepository: SessionTimetableRepository,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(sessionId) { StudentTimetableController(sessionId, timetableRepository, scope) }
    val periods by controller.periods.collectAsState()
    val refreshing by controller.refreshing.collectAsState()

    StudentTimetableWorkspace(
        heroPainter = painterResource("splash_postgraduate_block.jpg"),
        snapshot = studentTimetableSnapshot(periods, LocalDate.now(), LocalTime.now()),
        loading = refreshing && periods.isEmpty(),
        errorMessage = null,
        onRetry = controller::refresh,
    )
}
