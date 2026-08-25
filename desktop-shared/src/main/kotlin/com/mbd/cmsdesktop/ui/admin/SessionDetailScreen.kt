package com.mbd.cmsdesktop.ui.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.mbd.cmscommon.controller.SessionDetailController
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.SessionFeeRepository
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.ui.components.SessionOperationsWorkspace

@Composable
fun SessionDetailScreen(
    sessionId: String,
    sessionRepository: AcademicSessionRepository,
    curriculumRepository: CurriculumRepository,
    timetableRepository: SessionTimetableRepository,
    feeRepository: SessionFeeRepository,
    teacherRepository: TeacherRepository,
    onOpenStudents: (String) -> Unit,
    onOpenTimetable: (String) -> Unit,
    onOpenSemester: (String, Int) -> Unit,
    onOpenFees: (String) -> Unit,
    onDeleted: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(sessionId, sessionRepository, curriculumRepository, timetableRepository, feeRepository) {
        SessionDetailController(sessionId, sessionRepository, curriculumRepository, timetableRepository, feeRepository, scope)
    }
    val session by controller.session.collectAsState()
    val students by controller.students.collectAsState()
    val subjectCounts by controller.subjectCounts.collectAsState()
    val periods by controller.periods.collectAsState()
    val fee by controller.fee.collectAsState()
    val feeLoading by controller.feeLoading.collectAsState()
    val errorMessage by controller.error.collectAsState()
    val teachers by teacherRepository.observeActiveTeachers().collectAsState(initial = emptyList())

    SessionOperationsWorkspace(
        session = session,
        students = students,
        subjectCounts = subjectCounts,
        periods = periods,
        fee = fee,
        feeLoading = feeLoading,
        errorMessage = errorMessage,
        teachers = teachers,
        onSetSemester = controller::setSemester,
        onUpdateDetails = { programName, inchargeEmail, maxStudents ->
            controller.updateDetails(programName, inchargeEmail, maxStudents)
        },
        onOpenStudents = { onOpenStudents(sessionId) },
        onOpenTimetable = { onOpenTimetable(sessionId) },
        onOpenSemester = { semester -> onOpenSemester(sessionId, semester) },
        onOpenFees = { onOpenFees(sessionId) },
        onDeleteSession = { controller.deleteSession(onDeleted) },
        onClearError = controller::clearError,
    )
}
