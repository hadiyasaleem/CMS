package com.mbd.cmsstudent.feature.common

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.UserRole
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.SessionAttendanceRepository
import com.mbd.cmscommon.domain.repository.SessionMarksRepository
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import com.mbd.cmscommon.domain.repository.UserRepository
import com.mbd.cmscommon.util.StudentIdCodec
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flatMapLatest

data class StudentContext(
    val studentId: String,
    val sessionId: String,
    val deptId: String,
    val rollNumber: String,
    val name: String,
    val session: AcademicSession?,
    val gpa: Double? = null,
    val cgpa: Double? = null,
)

@Singleton
class CurrentStudentProvider @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionRepository: AcademicSessionRepository,
    private val curriculumRepository: CurriculumRepository,
    private val timetableRepository: SessionTimetableRepository,
    private val attendanceRepository: SessionAttendanceRepository,
    private val marksRepository: SessionMarksRepository,
) {
    fun observeContext(): Flow<StudentContext?> =
        userRepository.observeCurrentUserRole().flatMapLatest { role ->
            val linked = role as? UserRole.LinkedStudent ?: return@flatMapLatest flowOf(null)
            val sessionId = StudentIdCodec.sessionIdOf(linked.studentId)
            val deptId = StudentIdCodec.deptIdOf(sessionId)
            val rollNumber = StudentIdCodec.rollOf(linked.studentId)
            sessionRepository.observeStudents(sessionId).flatMapLatest { students ->
                val student = students.firstOrNull { it.rollNumber == rollNumber }
                sessionRepository.observeSession(sessionId).let { sessionFlow ->
                    kotlinx.coroutines.flow.combine(sessionFlow, flowOf(student)) { session, matched ->
                        StudentContext(
                            studentId = linked.studentId,
                            sessionId = sessionId,
                            deptId = deptId,
                            rollNumber = rollNumber,
                            name = matched?.name ?: rollNumber,
                            session = session,
                            gpa = matched?.gpa,
                            cgpa = matched?.cgpa,
                        )
                    }
                }
            }
        }

    suspend fun syncMySession(studentId: String): Boolean {
        val sessionId = StudentIdCodec.sessionIdOf(studentId)
        val deptId = StudentIdCodec.deptIdOf(sessionId)
        var successful = runCatching { sessionRepository.syncSessionsForDept(deptId) }.isSuccess
        successful = runCatching { curriculumRepository.syncSession(sessionId) }.isSuccess && successful
        successful = runCatching { sessionRepository.syncStudents(sessionId) }.isSuccess && successful
        successful = runCatching { timetableRepository.syncSession(sessionId) }.isSuccess && successful
        successful = runCatching { attendanceRepository.syncSession(sessionId) }.isSuccess && successful
        successful = runCatching { marksRepository.syncSession(sessionId) }.isSuccess && successful
        return successful
    }

    suspend fun hasCachedSession(studentId: String): Boolean {
        val sessionId = StudentIdCodec.sessionIdOf(studentId)
        val rollNumber = StudentIdCodec.rollOf(studentId)
        val session = runCatching { sessionRepository.observeSession(sessionId).first() }.getOrNull()
        val students = runCatching { sessionRepository.observeStudents(sessionId).first() }.getOrDefault(emptyList())
        return session != null && students.any { it.rollNumber == rollNumber }
    }
}
