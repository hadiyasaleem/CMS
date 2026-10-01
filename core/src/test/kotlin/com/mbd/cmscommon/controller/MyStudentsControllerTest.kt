package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AttendanceTally
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.SessionAttendanceRepository
import com.mbd.cmscommon.teacher.ResolvedAssignment
import java.lang.reflect.Proxy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Test

/** "My Students" with nothing picked shows every student across every class this teacher has, like the admin directory. */
class MyStudentsControllerTest {

    private inline fun <reified T : Any> fake(crossinline handler: (String, Array<Any?>) -> Any?): T =
        Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, method, args ->
            handler(method.name, args ?: emptyArray())
        } as T

    private val scope = CoroutineScope(Dispatchers.Unconfined)

    private fun student(sessionId: String, roll: String, name: String) =
        SessionStudent(SessionStudent.buildId(sessionId, roll), sessionId, "it", roll, name, Session.MORNING)

    // Two subjects (BS-101, BS-102) taught to the same class (it_2022, Morning) -- a real "two classKeys collapse
    // to one roster" case -- plus a second, unrelated class (it_2023).
    private val classA1 = ResolvedAssignment("it_2022", "IT 2022 (M)", "BS-101", "Programming", classShift = Session.MORNING)
    private val classA2 = ResolvedAssignment("it_2022", "IT 2022 (M)", "BS-102", "Databases", classShift = Session.MORNING)
    private val classB = ResolvedAssignment("it_2023", "IT 2023 (M)", "BS-201", "Networks", classShift = Session.MORNING)

    private val rosterA = listOf(student("it_2022", "01", "Ali"), student("it_2022", "02", "Sara"))
    private val rosterB = listOf(student("it_2023", "01", "Omar"))

    private fun controller(): MyStudentsController {
        val sessionRepo = fake<AcademicSessionRepository> { name, args ->
            if (name == "observeStudents") {
                when (args[0]) {
                    "it_2022" -> flowOf(rosterA)
                    "it_2023" -> flowOf(rosterB)
                    else -> flowOf(emptyList<SessionStudent>())
                }
            } else {
                null
            }
        }
        val attendanceRepo = fake<SessionAttendanceRepository> { name, args ->
            if (name == "observeTallies") {
                val sessionId = args[0]; val course = args[1]
                when {
                    sessionId == "it_2022" && course == "BS-101" -> flowOf(listOf(AttendanceTally("01", 9, 1, 0, "BS-101"))) // Ali 90%
                    sessionId == "it_2022" && course == "BS-102" -> flowOf(listOf(AttendanceTally("01", 4, 6, 0, "BS-102"))) // Ali 40% -- the flaggable one
                    else -> flowOf(emptyList<AttendanceTally>())
                }
            } else {
                null
            }
        }
        return MyStudentsController(sessionRepo, attendanceRepo, scope)
    }

    @Test
    fun withNothingPickedTheRosterIsEveryClassCombinedAndDeduped() {
        val c = controller()
        c.setAssignments(listOf(classA1, classA2, classB))
        scope.launch { c.roster.collect {} }
        // Ali and Sara come from it_2022 once each (not twice, despite two subjects there), plus Omar from it_2023.
        assertEquals(setOf("it_2022_01", "it_2022_02", "it_2023_01"), c.roster.value.map { it.id }.toSet())
    }

    @Test
    fun aStudentTaughtInTwoSubjectsKeepsTheLowerAttendancePercentage() {
        val c = controller()
        c.setAssignments(listOf(classA1, classA2, classB))
        scope.launch { c.tallies.collect {} }
        assertEquals(40f, c.tallies.value.getValue("it_2022_01").percentage)
    }

    @Test
    fun selectingOneClassNarrowsToItAndSelectAllGoesBack() {
        val c = controller()
        c.setAssignments(listOf(classA1, classA2, classB))
        scope.launch { c.roster.collect {} }

        c.select(classB)
        assertEquals(setOf("it_2023_01"), c.roster.value.map { it.id }.toSet())

        c.selectAll()
        assertEquals(setOf("it_2022_01", "it_2022_02", "it_2023_01"), c.roster.value.map { it.id }.toSet())
    }

    @Test
    fun withNoClassesTheRosterIsEmpty() {
        val c = controller()
        scope.launch { c.roster.collect {} }
        assertEquals(emptyList<SessionStudent>(), c.roster.value)
    }
}
