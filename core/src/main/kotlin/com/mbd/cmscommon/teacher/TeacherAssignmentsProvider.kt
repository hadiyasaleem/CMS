package com.mbd.cmscommon.teacher

import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.SessionPeriod
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
        ) { periods, sessions, departments -> resolveAssignments(periods, sessions, departments) }
}

/**
 * A teacher's classes from their timetable periods. A class is (session, subject, shift): teaching IT-301 to
 * both shifts of one session gives two classes, "IT · 2022–2026 (M)" and "IT · 2022–2026 (E)", each with
 * its own roster.
 */
fun resolveAssignments(
    periods: List<SessionPeriod>,
    sessions: List<AcademicSession>,
    departments: List<Department>,
): List<ResolvedAssignment> =
    periods
        .filter { it.courseCode.isNotBlank() }
        .groupBy { Triple(it.sessionId, it.courseCode, it.shift) }
        .map { (key, group) ->
            val (sessionId, courseCode, shift) = key
            val session = sessions.firstOrNull { it.sessionId == sessionId }
            val department = departments.firstOrNull { it.deptId == session?.deptId }
            val deptName = department?.code ?: session?.deptId?.uppercase(Locale.ROOT) ?: sessionId
            val sessionLabel = if (session != null) "$deptName · ${session.label} (${shift.shortLabel})" else sessionId
            ResolvedAssignment(
                sessionId = sessionId,
                sessionLabel = sessionLabel,
                courseCode = courseCode,
                subjectLabel = "$courseCode — ${group.first().subjectName}",
                deptName = department?.code ?: session?.deptId?.uppercase(Locale.ROOT) ?: "",
                sessionName = session?.label ?: "",
                shift = shift.label,
                classShift = shift,
                deptId = session?.deptId ?: "",
                session = session,
            )
        }
        .sortedWith(compareBy({ it.sessionLabel }, { it.courseCode }))
