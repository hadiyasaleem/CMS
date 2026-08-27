package com.mbd.cmsdesktop.ui.student

import androidx.compose.runtime.getValue

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.painterResource
import com.mbd.cmscommon.controller.StudentMoreController
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.CalendarRepository
import com.mbd.cmscommon.domain.repository.NotificationRepository
import com.mbd.cmscommon.domain.repository.SessionFeeRepository
import com.mbd.cmscommon.ui.components.StudentMoreDestination
import com.mbd.cmscommon.ui.components.StudentMoreWorkspace

@Composable
fun StudentMoreScreen(
    sessionId: String,
    departmentId: String,
    rollNumber: String,
    calendarRepository: CalendarRepository,
    feeRepository: SessionFeeRepository,
    notificationRepository: NotificationRepository,
    sessionRepository: AcademicSessionRepository,
    onOpen: (StudentMoreDestination) -> Unit,
    onSignOut: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(sessionId, departmentId, rollNumber) {
        StudentMoreController(
            sessionId, departmentId, rollNumber,
            calendarRepository, feeRepository, notificationRepository, sessionRepository,
            scope,
        )
    }
    val snapshot by controller.snapshot.collectAsState()
    val loading by controller.loading.collectAsState()
    val loadError by controller.loadError.collectAsState()

    StudentMoreWorkspace(
        heroPainter = painterResource("splash_postgraduate_block.jpg"),
        snapshot = snapshot,
        loading = loading,
        errorMessage = loadError,
        onRetry = controller::refresh,
        onOpen = onOpen,
        onSignOut = onSignOut,
    )
}
