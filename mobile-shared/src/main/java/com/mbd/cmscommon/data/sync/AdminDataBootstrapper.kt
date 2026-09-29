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
import com.mbd.cmscommon.util.LoadFailure
import com.mbd.cmscommon.util.SyncReport
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
    suspend fun refreshAll(onTaskDone: () -> Unit = {}): Boolean = refreshAllReport(onTaskDone).successful

    /** Same refresh as [refreshAll], but says WHICH parts failed and why ([SyncReport.message]) so an
     * interactive refresh can tell the user "Couldn't refresh fees (no connection)" instead of nothing. */
    suspend fun refreshAllReport(onTaskDone: () -> Unit = {}): SyncReport {
        // Every table below is fetched with ONE global "WHERE updated_at >= checkpoint" delta query
        // instead of one call per department/session -- RLS (see 20260714000002_rls.sql) already
        // restricts each row to what the calling admin/teacher/student is allowed to see, so scoping
        // the client-side query by session/department bought nothing but N extra round trips. This
        // is also why `syncAllSessions()` runs in its own stage first: `syncAllStudents()` and the
        // per-table `syncAll()`s below resolve each row's deptId via a local lookup on the session
        // that row belongs to, which only works once that session has landed in the local cache.
        val failures = mutableListOf<LoadFailure>()

        failures += supervisorScope {
            listOf(
                async { step("administrators", "sync.administrators", onTaskDone) { administratorRepository.sync() } },
                async { step("departments", "sync.departments", onTaskDone) { departmentRepository.sync() } },
                async { step("buildings", "sync.buildings", onTaskDone) { buildingRepository.sync() } },
                async { step("rooms", "sync.rooms", onTaskDone) { roomRepository.sync() } },
                async { step("teachers", "sync.teachers", onTaskDone) { teacherRepository.sync() } },
                async { step("calendar", "sync.calendar", onTaskDone) { calendarRepository.sync() } },
                async { step("datesheets", "sync.datesheets", onTaskDone) { datesheetRepository.sync() } },
                async { step("insights", "sync.insights", onTaskDone) { insightsRepository.sync() } },
                async { step("edit requests", "sync.markEditRequests", onTaskDone) { markEditRequestRepository.sync() } },
                async { step("sessions", "sync.sessions", onTaskDone) { sessionRepository.syncAllSessions() } },
            ).awaitAll().filterNotNull()
        }

        failures += supervisorScope {
            listOf(
                async { step("students", "sync.sessionStudents", onTaskDone) { sessionRepository.syncAllStudents() } },
                async { step("curriculum", "sync.curriculum", onTaskDone) { curriculumRepository.syncAll() } },
                async { step("timetables", "sync.timetable", onTaskDone) { timetableRepository.syncAll() } },
                async { step("attendance", "sync.attendance", onTaskDone) { attendanceRepository.syncAll() } },
                async { step("marks", "sync.marks", onTaskDone) { marksRepository.syncAll() } },
                async { step("fees", "sync.fees", onTaskDone) { feeRepository.syncAll() } },
                async { step("fines", "sync.fines", onTaskDone) { fineRepository.syncAll() } },
                async { step("exam papers", "sync.examPapers", onTaskDone) { examPaperRepository.syncAll() } },
                async { step("datesheet slots", "sync.datesheetSlots", onTaskDone) { datesheetRepository.syncAllSlots() } },
            ).awaitAll().filterNotNull()
        }

        failures += supervisorScope {
            listOf(
                async { step("link requests", "sync.linkRequests", onTaskDone) { linkRequestRepository.sync() } },
                async { step("notifications", "sync.notifications.admin", onTaskDone) { notificationRepository.sync(NotificationTargetRole.ADMIN) } },
                async { step("notifications", "sync.notifications.teacher", onTaskDone) { notificationRepository.sync(NotificationTargetRole.TEACHER) } },
                async { step("notifications", "sync.notifications.student", onTaskDone) { notificationRepository.sync(NotificationTargetRole.STUDENT) } },
            ).awaitAll().filterNotNull()
        }

        // Flush buffered crash/critical logs alongside the normal sync cycle. Never allowed to
        // affect the report or throw -- see AppLogRepositoryImpl.flush().
        runCatching { appLogRepository.flush() }
        onTaskDone()

        return SyncReport(failures)
    }

    /** Runs one sync task; a failure is logged (CRITICAL ones) under [tag] and returned, named [label], for the report. */
    private suspend fun step(label: String, tag: String, onTaskDone: () -> Unit, block: suspend () -> Unit): LoadFailure? {
        val result = runCatching { block() }
        result.isSuccessLogged(tag)
        onTaskDone()
        return result.exceptionOrNull()?.let { LoadFailure(label, it) }
    }

    companion object {
        /** Must track the exact number of `onTaskDone()` calls in [refreshAllReport] -- 10 + 9 + 4 sync
         * tasks plus the final log flush. Drives the refresh progress dialog's determinate bar. */
        const val TOTAL_SYNC_TASKS = 24
    }
}
