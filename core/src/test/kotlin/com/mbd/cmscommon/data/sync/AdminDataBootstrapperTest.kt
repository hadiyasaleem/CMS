package com.mbd.cmscommon.data.sync

import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.AdministratorRepository
import com.mbd.cmscommon.domain.repository.AppLogRepository
import com.mbd.cmscommon.domain.repository.BuildingRepository
import com.mbd.cmscommon.domain.repository.CalendarRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.DatesheetRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.ExamPaperSubmissionRepository
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
import java.lang.reflect.Proxy
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/**
 * The refresh that every role's refresh button runs. Repositories are fakes whose sync calls succeed unless a test makes
 * a named one throw, so what is checked is the report the user sees: which parts failed and why, and that a cancelled
 * refresh is passed on instead of being reported as a failure.
 */
class AdminDataBootstrapperTest {

    private val offline = { RuntimeException("Unable to resolve host \"example.supabase.co\": No address associated with hostname") }

    /** [failing] names a repository interface's simple name (optionally `Name.method`) that should throw when synced. */
    private fun bootstrapper(failing: Map<String, () -> Throwable> = emptyMap()): AdminDataBootstrapper {
        fun <T : Any> fake(type: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return Proxy.newProxyInstance(type.classLoader, arrayOf(type)) { _, method, _ ->
                val name = type.simpleName
                (failing["$name.${method.name}"] ?: failing[name])?.let { throw it() }
                Unit
            } as T
        }
        return AdminDataBootstrapper(
            administratorRepository = fake(AdministratorRepository::class.java),
            calendarRepository = fake(CalendarRepository::class.java),
            datesheetRepository = fake(DatesheetRepository::class.java),
            insightsRepository = fake(InsightsRepository::class.java),
            markEditRequestRepository = fake(MarkEditRequestRepository::class.java),
            examPaperRepository = fake(ExamPaperSubmissionRepository::class.java),
            departmentRepository = fake(DepartmentRepository::class.java),
            buildingRepository = fake(BuildingRepository::class.java),
            roomRepository = fake(RoomRepository::class.java),
            teacherRepository = fake(TeacherRepository::class.java),
            sessionRepository = fake(AcademicSessionRepository::class.java),
            curriculumRepository = fake(CurriculumRepository::class.java),
            timetableRepository = fake(SessionTimetableRepository::class.java),
            attendanceRepository = fake(SessionAttendanceRepository::class.java),
            feeRepository = fake(SessionFeeRepository::class.java),
            marksRepository = fake(SessionMarksRepository::class.java),
            linkRequestRepository = fake(StudentLinkRequestRepository::class.java),
            notificationRepository = fake(NotificationRepository::class.java),
            appLogRepository = fake(AppLogRepository::class.java),
        )
    }

    @Test
    fun aCleanRefreshIsSuccessfulAndReportsEveryTask() = runBlocking {
        val done = AtomicInteger()
        val report = bootstrapper().refreshAllReport { done.incrementAndGet() }
        assertTrue(report.successful)
        assertNull(report.message)
        assertEquals("the progress bar total must match the number of tasks", AdminDataBootstrapper.TOTAL_SYNC_TASKS, done.get())
    }

    @Test
    fun theBooleanRefreshStillWorks() = runBlocking {
        assertTrue(bootstrapper().refreshAll())
        assertFalse(bootstrapper(mapOf("SessionMarksRepository" to offline)).refreshAll())
    }

    @Test
    fun aFailedTableIsNamedWithItsReason() = runBlocking {
        val report = bootstrapper(mapOf("SessionFeeRepository" to offline)).refreshAllReport()
        assertFalse(report.successful)
        assertEquals("Couldn't refresh fees (no connection).", report.message)
    }

    @Test
    fun failuresSharingAReasonReadAsOneSentence() = runBlocking {
        val report = bootstrapper(mapOf("SessionFeeRepository" to offline, "SessionMarksRepository" to offline)).refreshAllReport()
        assertEquals("Couldn't refresh marks and fees (no connection).", report.message)
    }

    @Test
    fun aFailureDoesNotStopTheOtherTablesOrTheProgressCount() = runBlocking {
        val done = AtomicInteger()
        val report = bootstrapper(mapOf("CalendarRepository" to offline)).refreshAllReport { done.incrementAndGet() }
        assertEquals(1, report.failures.size)
        assertEquals(AdminDataBootstrapper.TOTAL_SYNC_TASKS, done.get())
    }

    @Test
    fun aFailedLogFlushNeverAffectsTheReport() = runBlocking {
        val report = bootstrapper(mapOf("AppLogRepository" to { RuntimeException("disk full") })).refreshAllReport()
        assertTrue(report.successful)
    }

    @Test
    fun notificationsForAllThreeRolesAreOneNamedPart() = runBlocking {
        val report = bootstrapper(mapOf("NotificationRepository" to offline)).refreshAllReport()
        assertEquals("Couldn't refresh notifications (no connection).", report.message)
    }

    @Test
    fun aCancelledRefreshIsPassedOnNotReportedAsAFailure() {
        try {
            runBlocking { bootstrapper(mapOf("SessionFeeRepository" to { CancellationException("left the screen") })).refreshAllReport() }
            fail("cancellation should propagate")
        } catch (expected: CancellationException) {
            assertEquals("left the screen", expected.message)
        }
    }
}
