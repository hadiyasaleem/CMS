package com.mbd.cmsdesktop.ui.student

import androidx.compose.runtime.getValue

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.painterResource
import com.mbd.cmscommon.controller.StudentAttendanceController
import com.mbd.cmscommon.controller.studentAttendanceSnapshot
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.SessionAttendanceRepository
import com.mbd.cmscommon.ui.components.StudentAttendanceWorkspace

@Composable
fun StudentAttendanceWorkspaceScreen(
    sessionId: String,
    rollNumber: String,
    attendanceRepository: SessionAttendanceRepository,
    curriculumRepository: CurriculumRepository,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(sessionId, rollNumber) {
        StudentAttendanceController(sessionId, rollNumber, attendanceRepository, curriculumRepository, scope)
    }
    val rows by controller.rows.collectAsState()

    StudentAttendanceWorkspace(
        heroPainter = painterResource("splash_postgraduate_block.jpg"),
        snapshot = if (rows.isEmpty()) null else studentAttendanceSnapshot(rows),
        loading = rows.isEmpty(),
    )
}
