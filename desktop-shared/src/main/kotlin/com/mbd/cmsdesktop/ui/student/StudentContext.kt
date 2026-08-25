package com.mbd.cmsdesktop.ui.student

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.util.StudentIdCodec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Desktop-local stand-in for mobile-student's `CurrentStudentProvider` (that class lives in the
 * mobile-student module, which desktop-shared does not depend on). Derives sessionId/deptId/
 * rollNumber from the combined `UserRole.LinkedStudent.studentId` via [StudentIdCodec] and exposes
 * the small bit of session/roster context (name, session, gpa, cgpa) that the Home/Profile
 * workspaces need. Screen-specific data (attendance, marks, timetable, ...) is still owned by each
 * leaf's own controller, constructed directly from [sessionId]/[rollNumber] below - mirroring how
 * every mobile-student `XViewModel` builds its `StudentXController`.
 */
data class DesktopStudentContext(
    val studentId: String,
    val sessionId: String,
    val deptId: String,
    val rollNumber: String,
    val name: String,
    val session: AcademicSession?,
    val gpa: Double?,
    val cgpa: Double?,
)

class StudentContextHolder(
    sessionRepository: AcademicSessionRepository,
    val studentId: String,
    private val scope: CoroutineScope,
) {
    val sessionId: String = StudentIdCodec.sessionIdOf(studentId)
    val deptId: String = StudentIdCodec.deptIdOf(sessionId)
    val rollNumber: String = StudentIdCodec.rollOf(studentId)

    val context: StateFlow<DesktopStudentContext?> = combine(
        sessionRepository.observeSession(sessionId),
        sessionRepository.observeStudents(sessionId),
    ) { session, students ->
        val student = students.firstOrNull { it.rollNumber == rollNumber }
        DesktopStudentContext(
            studentId = studentId,
            sessionId = sessionId,
            deptId = deptId,
            rollNumber = rollNumber,
            name = student?.name ?: rollNumber,
            session = session,
            gpa = student?.gpa,
            cgpa = student?.cgpa,
        )
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    init {
        scope.launch { runCatching { sessionRepository.syncStudents(sessionId) } }
    }
}
