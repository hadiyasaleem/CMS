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

    /** [onTaskDone] fires once per completed sync task (see [TOTAL_SYNC_TASKS]) -- purely a UI
     * progress hook, called from whichever task's own coroutine finishes it, so callers must not
     * assume a particular thread. */
    suspend fun refreshAll(onTaskDone: () -> Unit = {}): Boolean {
        // Every table below is fetched with ONE global "WHERE updated_at >= checkpoint" delta query
        // instead of one call per department/session -- RLS (see 20260714000002_rls.sql) already
        // restricts each row to what the calling admin/teacher/student is allowed to see, so scoping
        // the client-side query by session/department bought nothing but N extra round trips. This
        // is also why `syncAllSessions()` runs in its own stage first: `syncAllStudents()` and the
        // per-table `syncAll()`s below resolve each row's deptId via a local lookup on the session
        // that row belongs to, which only works once that session has landed in the local cache.
        var successful = supervisorScope {
            listOf(
                async { runCatching { administratorRepository.sync() }.isSuccessLogged("sync.administrators").also { onTaskDone() } },
                async { runCatching { departmentRepository.sync() }.isSuccessLogged("sync.departments").also { onTaskDone() } },
                async { runCatching { buildingRepository.sync() }.isSuccessLogged("sync.buildings").also { onTaskDone() } },
                async { runCatching { roomRepository.sync() }.isSuccessLogged("sync.rooms").also { onTaskDone() } },
                async { runCatching { teacherRepository.sync() }.isSuccessLogged("sync.teachers").also { onTaskDone() } },
                async { runCatching { calendarRepository.sync() }.isSuccessLogged("sync.calendar").also { onTaskDone() } },
                async { runCatching { datesheetRepository.sync() }.isSuccessLogged("sync.datesheets").also { onTaskDone() } },
                async { runCatching { insightsRepository.sync() }.isSuccessLogged("sync.insights").also { onTaskDone() } },
                async { runCatching { markEditRequestRepository.sync() }.isSuccessLogged("sync.markEditRequests").also { onTaskDone() } },
                async { runCatching { sessionRepository.syncAllSessions() }.isSuccessLogged("sync.sessions").also { onTaskDone() } },
            ).awaitAll().all { it }
        }

        successful = supervisorScope {
            listOf(
                async { runCatching { sessionRepository.syncAllStudents() }.isSuccessLogged("sync.sessionStudents").also { onTaskDone() } },
                async { runCatching { curriculumRepository.syncAll() }.isSuccessLogged("sync.curriculum").also { onTaskDone() } },
                async { runCatching { timetableRepository.syncAll() }.isSuccessLogged("sync.timetable").also { onTaskDone() } },
                async { runCatching { attendanceRepository.syncAll() }.isSuccessLogged("sync.attendance").also { onTaskDone() } },
                async { runCatching { marksRepository.syncAll() }.isSuccessLogged("sync.marks").also { onTaskDone() } },
                async { runCatching { feeRepository.syncAll() }.isSuccessLogged("sync.fees").also { onTaskDone() } },
                async { runCatching { fineRepository.syncAll() }.isSuccessLogged("sync.fines").also { onTaskDone() } },
                async { runCatching { examPaperRepository.syncAll() }.isSuccessLogged("sync.examPapers").also { onTaskDone() } },
                async { runCatching { datesheetRepository.syncAllSlots() }.isSuccessLogged("sync.datesheetSlots").also { onTaskDone() } },
            ).awaitAll().all { it }
        } && successful

        successful = supervisorScope {
            listOf(
                async { runCatching { linkRequestRepository.sync() }.isSuccessLogged("sync.linkRequests").also { onTaskDone() } },
                async { runCatching { notificationRepository.sync(NotificationTargetRole.ADMIN) }.isSuccessLogged("sync.notifications.admin").also { onTaskDone() } },
                async { runCatching { notificationRepository.sync(NotificationTargetRole.TEACHER) }.isSuccessLogged("sync.notifications.teacher").also { onTaskDone() } },
                async { runCatching { notificationRepository.sync(NotificationTargetRole.STUDENT) }.isSuccessLogged("sync.notifications.student").also { onTaskDone() } },
            ).awaitAll().all { it }
        } && successful

        // Flush buffered crash/critical logs alongside the normal sync cycle. Never allowed to
        // affect `successful` or throw -- see AppLogRepositoryImpl.flush().
        runCatching { appLogRepository.flush() }
        onTaskDone()

        return successful
    }

    companion object {
        /** Must track the exact number of `onTaskDone()` calls in [refreshAll] -- 10 + 9 + 4 sync
         * tasks plus the final log flush. Drives the refresh progress dialog's determinate bar. */
        const val TOTAL_SYNC_TASKS = 24
    }
}
