package com.mbd.cmsdesktop.ui.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.mbd.cmscommon.controller.SessionTimetableController
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.ui.components.SessionTimetableWorkspace

@Composable
fun SessionTimetableScreen(
    sessionId: String,
    sessionRepository: AcademicSessionRepository,
    curriculumRepository: CurriculumRepository,
    teacherRepository: TeacherRepository,
    timetableRepository: SessionTimetableRepository,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(sessionId, timetableRepository, sessionRepository, curriculumRepository, teacherRepository) {
        SessionTimetableController(sessionId, timetableRepository, sessionRepository, curriculumRepository, teacherRepository, scope)
    }
    val session by controller.session.collectAsState()
    val periods by controller.periods.collectAsState()
    val subjects by controller.subjects.collectAsState()
    val teachers by controller.teachers.collectAsState()
    val errorMessage by controller.error.collectAsState()

    SessionTimetableWorkspace(
        session = session,
        periods = periods,
        subjects = subjects,
        teachers = teachers,
        errorMessage = errorMessage,
        onSavePeriod = controller::savePeriod,
        onRemovePeriod = controller::removePeriod,
        onClearError = controller::clearError,
    )
}
