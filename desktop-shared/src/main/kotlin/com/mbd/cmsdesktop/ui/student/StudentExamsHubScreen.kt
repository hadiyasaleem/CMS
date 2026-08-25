package com.mbd.cmsdesktop.ui.student

import androidx.compose.runtime.getValue

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.painterResource
import com.mbd.cmscommon.controller.StudentExamsHubController
import com.mbd.cmscommon.domain.repository.DatesheetRepository
import com.mbd.cmscommon.domain.repository.SessionMarksRepository
import com.mbd.cmscommon.ui.components.StudentExamsDestination
import com.mbd.cmscommon.ui.components.StudentExamsHubWorkspace

@Composable
fun StudentExamsHubScreen(
    sessionId: String,
    rollNumber: String,
    marksRepository: SessionMarksRepository,
    datesheetRepository: DatesheetRepository,
    onOpen: (StudentExamsDestination) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(sessionId, rollNumber) {
        StudentExamsHubController(sessionId, rollNumber, marksRepository, datesheetRepository, scope)
    }
    val snapshot by controller.snapshot.collectAsState()
    val loading by controller.loading.collectAsState()

    StudentExamsHubWorkspace(
        heroPainter = painterResource("splash_postgraduate_block.jpg"),
        snapshot = snapshot,
        loading = loading,
        errorMessage = null,
        onRetry = controller::refresh,
        onOpen = onOpen,
    )
}
