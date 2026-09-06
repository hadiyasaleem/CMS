package com.mbd.cmscommon.data.sync

import com.mbd.cmscommon.domain.model.NotificationTargetRole
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.AdministratorRepository
import com.mbd.cmscommon.domain.repository.AppLogRepository
import com.mbd.cmscommon.domain.repository.BuildingRepository
import com.mbd.cmscommon.domain.repository.CalendarRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.DatesheetRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.ExamPaperSubmissionRepository
import com.mbd.cmscommon.domain.repository.FineRepository
import com.mbd.cmscommon.domain.repository.InsightsRepository
import com.mbd.cmscommon.domain.repository.MarkEditRequestRepository
import com.mbd.cmscommon.domain.repository.NotificationRepository
import com.mbd.cmscommon.domain.repository.RoomRepository
import com.mbd.cmscommon.domain.repository.SessionAttendanceRepository
import com.mbd.cmscommon.domain.repository.SessionFeeRepository
import com.mbd.cmscommon.domain.repository.SessionMarksRepository
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import com.mbd.cmscommon.domain.repository.StudentLinkRequestRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.util.isSuccessLogged
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.supervisorScope

@Singleton
class AdminDataBootstrapper @Inject constructor(
    private val administratorRepository: AdministratorRepository,
    private val calendarRepository: CalendarRepository,
    private val datesheetRepository: DatesheetRepository,
    private val fineRepository: FineRepository,
    private val insightsRepository: InsightsRepository,
    private val markEditRequestRepository: MarkEditRequestRepository,
    private val examPaperRepository: ExamPaperSubmissionRepository,
    private val departmentRepository: DepartmentRepository,
    private val buildingRepository: BuildingRepository,
    private val roomRepository: RoomRepository,
    private val teacherRepository: TeacherRepository,
    private val sessionRepository: AcademicSessionRepository,
    private val curriculumRepository: CurriculumRepository,
    private val timetableRepository: SessionTimetableRepository,
    private val attendanceRepository: SessionAttendanceRepository,
    private val feeRepository: SessionFeeRepository,
    private val marksRepository: SessionMarksRepository,
    private val linkRequestRepository: StudentLinkRequestRepository,
    private val notificationRepository: NotificationRepository,
    private val appLogRepository: AppLogRepository,
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
                async { runCatching { administratorRepository.sync() }.isSuccessLogged("sync.administrators") },
                async { runCatching { departmentRepository.sync() }.isSuccessLogged("sync.departments") },
                async { runCatching { buildingRepository.sync() }.isSuccessLogged("sync.buildings") },
                async { runCatching { roomRepository.sync() }.isSuccessLogged("sync.rooms") },
                async { runCatching { teacherRepository.sync() }.isSuccessLogged("sync.teachers") },
                async { runCatching { calendarRepository.sync() }.isSuccessLogged("sync.calendar") },
                async { runCatching { datesheetRepository.sync() }.isSuccessLogged("sync.datesheets") },
                async { runCatching { insightsRepository.sync() }.isSuccessLogged("sync.insights") },
                async { runCatching { markEditRequestRepository.sync() }.isSuccessLogged("sync.markEditRequests") },
            ).awaitAll().all { it }
        }

        val departments = runCatching { departmentRepository.observeActiveDepartments().first() }.getOrDefault(emptyList())
        successful = supervisorScope {
            departments.map { department ->
                async { runCatching { sessionRepository.syncSessionsForDept(department.deptId) }.isSuccessLogged("sync.sessionsForDept") }
            }.awaitAll().all { it }
        } && successful

        val sessions = runCatching { sessionRepository.observeAllSessions().first() }.getOrDefault(emptyList())
        successful = supervisorScope {
            sessions.map { session ->
                async {
                    supervisorScope {
                        listOf(
                            async { runCatching { sessionRepository.syncStudents(session.sessionId) }.isSuccessLogged("sync.sessionStudents") },
                            async { runCatching { curriculumRepository.syncSession(session.sessionId) }.isSuccessLogged("sync.curriculum") },
                            async { runCatching { timetableRepository.syncSession(session.sessionId) }.isSuccessLogged("sync.timetable") },
                            async { runCatching { attendanceRepository.syncSession(session.sessionId) }.isSuccessLogged("sync.attendance") },
                            async { runCatching { marksRepository.syncSession(session.sessionId) }.isSuccessLogged("sync.marks") },
                            async { runCatching { feeRepository.syncSession(session.sessionId) }.isSuccessLogged("sync.fees") },
                        ).awaitAll().all { it }
                    }
                }
            }.awaitAll().all { it }
        } && successful

        successful = supervisorScope {
            sessions.map { session ->
                async {
                    val students = runCatching { sessionRepository.observeStudents(session.sessionId).first() }
                        .getOrDefault(emptyList())
                    val subjects = runCatching { curriculumRepository.observeSessionSubjects(session.sessionId).first() }
                        .getOrDefault(emptyList())
                    supervisorScope {
                        val fineSyncs = students.map { student ->
                            async { runCatching { fineRepository.sync(session.sessionId, student.rollNumber) }.isSuccessLogged("sync.fines") }
                        }
                        val paperSyncs = subjects.distinctBy { it.courseCode }.map { subject ->
                            async { runCatching { examPaperRepository.sync(session.sessionId, subject.courseCode) }.isSuccessLogged("sync.examPapers") }
                        }
                        (fineSyncs + paperSyncs).awaitAll().all { it }
                    }
                }
            }.awaitAll().all { it }
        } && successful

        val datesheets = runCatching { datesheetRepository.getDatesheets() }.getOrDefault(emptyList())
        successful = supervisorScope {
            datesheets.map { sheet ->
                async { runCatching { datesheetRepository.syncSlots(sheet.id) }.isSuccessLogged("sync.datesheetSlots") }
            }.awaitAll().all { it }
        } && successful
        successful = supervisorScope {
            listOf(
                async { runCatching { linkRequestRepository.sync() }.isSuccessLogged("sync.linkRequests") },
                async { runCatching { notificationRepository.sync(NotificationTargetRole.ADMIN) }.isSuccessLogged("sync.notifications.admin") },
                async { runCatching { notificationRepository.sync(NotificationTargetRole.TEACHER) }.isSuccessLogged("sync.notifications.teacher") },
                async { runCatching { notificationRepository.sync(NotificationTargetRole.STUDENT) }.isSuccessLogged("sync.notifications.student") },
            ).awaitAll().all { it }
        } && successful

        // Flush buffered crash/critical logs alongside the normal sync cycle. Never allowed to
        // affect `successful` or throw -- see AppLogRepositoryImpl.flush().
        runCatching { appLogRepository.flush() }

        return successful
    }
}
