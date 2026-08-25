package com.mbd.cmsdesktop.ui.student

import androidx.compose.runtime.getValue

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.mbd.cmscommon.controller.StudentMarksController
import com.mbd.cmscommon.controller.studentMarksSnapshot
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.SessionMarksRepository
import com.mbd.cmscommon.ui.components.StudentMarksWorkspace

@Composable
fun StudentMarksScreen(
    sessionId: String,
    rollNumber: String,
    marksRepository: SessionMarksRepository,
    curriculumRepository: CurriculumRepository,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(sessionId, rollNumber) {
        StudentMarksController(sessionId, rollNumber, marksRepository, curriculumRepository, scope)
    }
    val rows by controller.rows.collectAsState()
    val refreshing by controller.refreshing.collectAsState()

    StudentMarksWorkspace(
        snapshot = studentMarksSnapshot(rows),
        loading = refreshing && rows.isEmpty(),
        errorMessage = null,
        onRetry = controller::refresh,
    )
}
