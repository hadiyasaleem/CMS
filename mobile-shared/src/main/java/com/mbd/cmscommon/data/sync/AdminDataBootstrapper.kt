package com.mbd.cmscommon.data.sync

import com.mbd.cmscommon.domain.model.NotificationTargetRole
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.AdministratorRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.NotificationRepository
import com.mbd.cmscommon.domain.repository.SessionAttendanceRepository
import com.mbd.cmscommon.domain.repository.SessionMarksRepository
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import com.mbd.cmscommon.domain.repository.StudentLinkRequestRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.supervisorScope

@Singleton
class AdminDataBootstrapper @Inject constructor(
    private val administratorRepository: AdministratorRepository,
    private val departmentRepository: DepartmentRepository,
    private val teacherRepository: TeacherRepository,
    private val sessionRepository: AcademicSessionRepository,
    private val curriculumRepository: CurriculumRepository,
    private val timetableRepository: SessionTimetableRepository,
    private val attendanceRepository: SessionAttendanceRepository,
    private val marksRepository: SessionMarksRepository,
    private val linkRequestRepository: StudentLinkRequestRepository,
    private val notificationRepository: NotificationRepository,
) {
    suspend fun hasCachedData(): Boolean {
        val departments = runCatching { departmentRepository.observeActiveDepartments().first() }.getOrDefault(emptyList())
        val teachers = runCatching { teacherRepository.observeActiveTeachers().first() }.getOrDefault(emptyList())
        val sessions = runCatching { sessionRepository.observeAllSessions().first() }.getOrDefault(emptyList())
        return departments.isNotEmpty() && teachers.isNotEmpty() && sessions.isNotEmpty()
    }

    suspend fun refreshAll(): Boolean {
        var successful = supervisorScope {
            listOf(
                async { runCatching { administratorRepository.sync() }.isSuccess },
                async { runCatching { departmentRepository.sync() }.isSuccess },
                async { runCatching { teacherRepository.sync() }.isSuccess },
            ).awaitAll().all { it }
        }

        val departments = runCatching { departmentRepository.observeActiveDepartments().first() }.getOrDefault(emptyList())
        successful = supervisorScope {
            departments.map { department ->
                async { runCatching { sessionRepository.syncSessionsForDept(department.deptId) }.isSuccess }
            }.awaitAll().all { it }
        } && successful

        val sessions = runCatching { sessionRepository.observeAllSessions().first() }.getOrDefault(emptyList())
        successful = supervisorScope {
            sessions.map { session ->
                async {
                    supervisorScope {
                        listOf(
                            async { runCatching { sessionRepository.syncStudents(session.sessionId) }.isSuccess },
                            async { runCatching { curriculumRepository.syncSession(session.sessionId) }.isSuccess },
                            async { runCatching { timetableRepository.syncSession(session.sessionId) }.isSuccess },
                            async { runCatching { attendanceRepository.syncSession(session.sessionId) }.isSuccess },
                            async { runCatching { marksRepository.syncSession(session.sessionId) }.isSuccess },
                        ).awaitAll().all { it }
                    }
                }
            }.awaitAll().all { it }
        } && successful

        successful = supervisorScope {
            listOf(
                async { runCatching { linkRequestRepository.sync() }.isSuccess },
                async { runCatching { notificationRepository.sync(NotificationTargetRole.ADMIN) }.isSuccess },
            ).awaitAll().all { it }
        } && successful

        return successful
    }
}
