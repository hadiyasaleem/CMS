package com.mbd.cmscommon.data.sync

import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import com.mbd.cmscommon.domain.repository.StudentLinkRequestRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class SyncEngine @Inject constructor(
    private val departmentRepository: DepartmentRepository,
    private val curriculumRepository: CurriculumRepository,
    private val sessionRepository: AcademicSessionRepository,
    private val timetableRepository: SessionTimetableRepository,
    private val teacherRepository: TeacherRepository,
    private val studentLinkRequestRepository: StudentLinkRequestRepository,
) {
    suspend fun hasCachedReferenceData(): Boolean {
        val departments = runCatching { departmentRepository.observeActiveDepartments().first() }.getOrDefault(emptyList())
        val sessions = runCatching { sessionRepository.observeAllSessions().first() }.getOrDefault(emptyList())
        return departments.isNotEmpty() && sessions.isNotEmpty()
    }

    suspend fun refreshReferenceData(): Boolean {
        var successful = runCatching { departmentRepository.sync() }.isSuccess
        val departments = runCatching { departmentRepository.observeActiveDepartments().first() }.getOrDefault(emptyList())

        for (department in departments) {
            successful = runCatching { sessionRepository.syncSessionsForDept(department.deptId) }.isSuccess && successful
        }

        val sessions = runCatching { sessionRepository.observeAllSessions().first() }.getOrDefault(emptyList())
        for (session in sessions) {
            successful = runCatching { sessionRepository.syncStudents(session.sessionId) }.isSuccess && successful
            successful = runCatching { curriculumRepository.syncSession(session.sessionId) }.isSuccess && successful
            successful = runCatching { timetableRepository.syncSession(session.sessionId) }.isSuccess && successful
        }

        successful = runCatching { teacherRepository.sync() }.isSuccess && successful
        return successful
    }

    suspend fun refreshAdminReferenceData(): Boolean {
        val referenceSuccessful = refreshReferenceData()
        val linkRequestSuccessful = runCatching { studentLinkRequestRepository.sync() }.isSuccess
        return referenceSuccessful && linkRequestSuccessful
    }
}
