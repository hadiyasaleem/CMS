package com.mbd.cmsdesktop.ui.student

import androidx.compose.runtime.getValue

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.painterResource
import com.mbd.cmscommon.controller.StudentHomeController
import com.mbd.cmscommon.domain.model.studentHomeSnapshot
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.SessionAttendanceRepository
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import com.mbd.cmscommon.ui.components.StudentHomeDestination
import com.mbd.cmscommon.ui.components.StudentHomeWorkspace

@Composable
fun StudentHomeWorkspaceScreen(
    sessionId: String,
    rollNumber: String,
    sessionRepository: AcademicSessionRepository,
    attendanceRepository: SessionAttendanceRepository,
    timetableRepository: SessionTimetableRepository,
    onOpen: (StudentHomeDestination) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(sessionId, rollNumber) {
        StudentHomeController(sessionId, rollNumber, sessionRepository, attendanceRepository, timetableRepository, scope)
    }
    val me by controller.me.collectAsState()
    val session by controller.session.collectAsState()
    val ui by controller.ui.collectAsState()

    StudentHomeWorkspace(
        heroPainter = painterResource("splash_postgraduate_block.jpg"),
        snapshot = me?.let {
            studentHomeSnapshot(
                name = it.name,
                rollNumber = rollNumber,
                session = session,
                gpa = it.gpa,
                cgpa = it.cgpa,
                overallAttendance = ui.overallPercent,
                subjectCount = ui.subjectCount,
                lecturesToday = ui.lecturesToday,
                nextClass = ui.nextClass,
                weakestSubject = ui.weakestSubject,
            )
        },
        loading = me == null,
        onOpen = onOpen,
    )
}
