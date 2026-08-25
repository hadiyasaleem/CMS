package com.mbd.cmscommon.teacher

import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf

@Singleton
class TeacherAssignmentsProvider @Inject constructor(
    private val sessionManager: SessionManager,
    private val timetableRepository: SessionTimetableRepository,
    private val sessionRepository: AcademicSessionRepository,
    private val departmentRepository: DepartmentRepository,
) {
    fun observeMyAssignments(): Flow<List<ResolvedAssignment>> {
        val teacherId = sessionManager.accountKey ?: return flowOf(emptyList())
        return observeAssignmentsFor(teacherId)
    }

    fun observeAssignmentsFor(teacherId: String): Flow<List<ResolvedAssignment>> =
        combine(
            timetableRepository.observeMyPeriods(teacherId),
            sessionRepository.observeAllSessions(),
            departmentRepository.observeActiveDepartments(),
        ) { periods, sessions, departments ->
            periods
                .filter { it.courseCode.isNotBlank() }
                .groupBy { it.sessionId to it.courseCode }
                .map { (key, group) ->
                    val (sessionId, courseCode) = key
                    val session = sessions.firstOrNull { it.sessionId == sessionId }
                    val department = departments.firstOrNull { it.deptId == session?.deptId }
                    val deptName = department?.code ?: session?.deptId?.uppercase(Locale.ROOT) ?: sessionId
                    val shiftShort = if (session?.shift == Session.EVENING) "E" else "M"
                    val sessionLabel = if (session != null) "$deptName · ${session.label} ($shiftShort)" else sessionId
                    ResolvedAssignment(
                        sessionId = sessionId,
                        sessionLabel = sessionLabel,
                        courseCode = courseCode,
                        subjectLabel = "$courseCode — ${group.first().subjectName}",
                    )
                }
                .sortedWith(compareBy({ it.sessionLabel }, { it.courseCode }))
        }
}
