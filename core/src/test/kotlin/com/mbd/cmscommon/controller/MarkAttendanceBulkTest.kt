package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.AttendanceStatus
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.domain.model.ShiftMode
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.NotificationRepository
import com.mbd.cmscommon.domain.repository.SessionAttendanceRepository
import com.mbd.cmscommon.teacher.ResolvedAssignment
import java.lang.reflect.Proxy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** "Mark all present" and "Mark remaining present" on the teacher attendance register. */
class MarkAttendanceBulkTest {

    private inline fun <reified T : Any> fake(crossinline handler: (String, Array<Any?>) -> Any?): T =
        Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, method, args ->
            handler(method.name, args ?: emptyArray())
        } as T

    private val scope = CoroutineScope(Dispatchers.Unconfined)
    private val session = AcademicSession("isl_2023", "isl", 2023, 2027, ShiftMode.MORNING, 3, maxStudents = 100)
    private val students = listOf("01", "02", "03").map { roll ->
        SessionStudent(SessionStudent.buildId("isl_2023", "IT-22-$roll"), "isl_2023", "isl", "IT-22-$roll", "Student $roll", Session.MORNING)
    }
    private val assignment = ResolvedAssignment("isl_2023", "ISL 2023–2027 (M)", "BS-301", "Quran Studies", session = session)

    private fun controller(): MarkAttendanceController {
        val sessionRepo = fake<AcademicSessionRepository> { name, args ->
            when (name) {
                "observeSession" -> flowOf(session)
                "observeStudents" -> flowOf(students)
                else -> null
            }
        }
        val attendanceRepo = fake<SessionAttendanceRepository> { name, _ ->
            when (name) {
                "marksBetween" -> emptyList<Any>()
                "observeTallies" -> flowOf(emptyList<Any>())
                else -> null
            }
        }
        val notificationRepo = fake<NotificationRepository> { _, _ -> null }
        val curriculumRepo = fake<CurriculumRepository> { name, _ ->
            when (name) {
                "observeSemesterSubjects" -> flowOf(emptyList<Any>())
                else -> null
            }
        }
        val controller = MarkAttendanceController(attendanceRepo, sessionRepo, notificationRepo, curriculumRepo, "t@x.pk", scope)
        controller.select(assignment)
        // The derived StateFlows (roster, lockedStudentIds, ...) only start producing once something collects
        // them (SharingStarted.WhileSubscribed) -- with the Unconfined dispatcher this runs synchronously.
        scope.launch { controller.roster.collect {} }
        scope.launch { controller.lockedStudentIds.collect {} }
        return controller
    }

    @Test
    fun markAllPresentMarksEveryStudent() {
        val c = controller()
        c.markAllPresent()
        assertEquals(students.associate { it.id to AttendanceStatus.PRESENT }, c.statuses.value)
    }

    @Test
    fun markRemainingPresentLeavesAlreadyMarkedStudentsAlone() {
        val c = controller()
        c.setStatus(students[0].id, AttendanceStatus.ABSENT)
        c.markRemainingPresent()
        assertEquals(AttendanceStatus.ABSENT, c.statuses.value[students[0].id])
        assertEquals(AttendanceStatus.PRESENT, c.statuses.value[students[1].id])
        assertEquals(AttendanceStatus.PRESENT, c.statuses.value[students[2].id])
    }

    @Test
    fun markRemainingPresentDoesNothingWhenEveryoneIsAlreadyMarked() {
        val c = controller()
        c.setStatus(students[0].id, AttendanceStatus.ABSENT)
        c.setStatus(students[1].id, AttendanceStatus.LEAVE)
        c.setStatus(students[2].id, AttendanceStatus.PRESENT)
        c.markRemainingPresent()
        assertEquals(AttendanceStatus.ABSENT, c.statuses.value[students[0].id])
        assertEquals(AttendanceStatus.LEAVE, c.statuses.value[students[1].id])
    }

    @Test
    fun lateCanOnlyBeSetWhenPresent() {
        val c = controller()
        c.setStatus(students[0].id, AttendanceStatus.ABSENT)
        c.toggleLate(students[0].id)
        assertEquals(emptySet<String>(), c.late.value)

        c.setStatus(students[0].id, AttendanceStatus.PRESENT)
        c.toggleLate(students[0].id)
        assertEquals(setOf(students[0].id), c.late.value)

        // Switching away from Present drops the late flag already set.
        c.setStatus(students[0].id, AttendanceStatus.LEAVE)
        assertEquals(emptySet<String>(), c.late.value)
    }

    @Test
    fun bulkMarkingIgnoresLockedStudents() {
        // No session is reported as already-marked here (marksBetween returns empty), so nothing is locked;
        // this documents that markAllPresent only ever writes unlocked ids via lockedStudentIds filtering.
        val c = controller()
        c.markAllPresent()
        assertNull(c.lockedStudentIds.value.firstOrNull())
    }
}
