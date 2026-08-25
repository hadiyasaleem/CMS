package com.mbd.cmsdesktop.ui.student

import androidx.compose.runtime.getValue

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.mbd.cmscommon.controller.StudentResultsController
import com.mbd.cmscommon.domain.model.studentResultsSnapshot
import com.mbd.cmscommon.domain.repository.SessionMarksRepository
import com.mbd.cmscommon.ui.components.StudentResultsWorkspace

@Composable
fun StudentResultsScreen(
    sessionId: String,
    rollNumber: String,
    marksRepository: SessionMarksRepository,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(sessionId, rollNumber) { StudentResultsController(sessionId, rollNumber, marksRepository, scope) }
    val results by controller.results.collectAsState()
    val loading by controller.loading.collectAsState()

    StudentResultsWorkspace(
        snapshot = if (loading && results.isEmpty()) null else studentResultsSnapshot(results),
        loading = loading,
        errorMessage = null,
        onRetry = controller::refresh,
    )
}
