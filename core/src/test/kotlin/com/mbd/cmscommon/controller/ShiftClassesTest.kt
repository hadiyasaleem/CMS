package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Datesheet
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.domain.model.ShiftMode
import com.mbd.cmscommon.export.attendanceRegisterExport
import com.mbd.cmscommon.export.timetableExport
import com.mbd.cmscommon.teacher.resolveAssignments
import java.time.DayOfWeek
import java.time.Instant
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ShiftClassesTest {

    private val both = AcademicSession("IT_2022", "IT", 2022, 2026, ShiftMode.BOTH, 3, maxStudents = 100)
    private val it = Department("IT", "Information Technology", "IT", createdAt = Instant.EPOCH, createdBy = "", updatedAt = Instant.EPOCH, updatedBy = "")

    private fun period(shift: Session, course: String, day: DayOfWeek = DayOfWeek.MONDAY, start: String = "08:00", end: String = "09:00") =
        SessionPeriod(SessionPeriod.buildId("IT_2022", shift, day, start), "IT_2022", shift, day, start, end, course, "Subject $course", "t@x", "Teacher")

    private fun student(roll: String, shift: Session) =
        SessionStudent(SessionStudent.buildId("IT_2022", roll), "IT_2022", "IT", roll, roll, shift)

    @Test
    fun oneSubjectTaughtToBothShiftsIsTwoClasses() {
        val periods = listOf(
            period(Session.MORNING, "IT-301"),
            period(Session.MORNING, "IT-301", DayOfWeek.TUESDAY),
            period(Session.EVENING, "IT-301", start = "15:00", end = "16:00"),
            period(Session.EVENING, "IT-302", start = "16:00", end = "17:00"),
        )
        val classes = resolveAssignments(periods, listOf(both), listOf(it))
        assertEquals(3, classes.size)
        assertEquals(
            listOf("IT · 2022–2026 (E)" to "IT-301", "IT · 2022–2026 (E)" to "IT-302", "IT · 2022–2026 (M)" to "IT-301"),
            classes.map { c -> c.sessionLabel to c.courseCode },
        )
        val evening301 = classes.first { c -> c.courseCode == "IT-301" && c.classShift == Session.EVENING }
        assertEquals("Evening", evening301.shift)
        assertEquals("IT_2022@EVENING", evening301.classKey)
    }

    @Test
    fun aClassRosterIsItsShiftsStudents() {
        val roster = listOf(student("IT-22-01", Session.MORNING), student("IT-22-51", Session.EVENING), student("IT-22-02", Session.MORNING))
        assertEquals(listOf("IT-22-51"), studentsForTab(roster, Session.EVENING).map { s -> s.rollNumber })
        assertEquals(listOf("IT-22-01", "IT-22-02"), studentsForTab(roster, Session.MORNING).map { s -> s.rollNumber })
        // A class whose shift is unknown keeps the whole session.
        assertEquals(3, studentsForTab(roster, null).size)
    }

    @Test
    fun classKeysRoundTrip() {
        assertEquals("IT_2022@MORNING", shiftClassKey("IT_2022", Session.MORNING))
        assertEquals("IT_2022" to Session.EVENING, parseShiftClassKey("IT_2022@EVENING"))
        assertEquals("IT_2022" to null, parseShiftClassKey("IT_2022"))
        assertEquals(
            listOf("IT_2022@MORNING" to "IT 2022–2026 (M)", "IT_2022@EVENING" to "IT 2022–2026 (E)", "IT_2023@EVENING" to "IT 2023–2027 (Evening)"),
            shiftClassOptions(listOf(both, both.copy(sessionId = "IT_2023", startYear = 2023, endYear = 2027, shiftMode = ShiftMode.EVENING))) { s -> "IT ${s.label}" },
        )
    }

    @Test
    fun timetableTabsAreTheSessionsShifts() {
        assertEquals(listOf(Session.MORNING, Session.EVENING), shiftTabs(both))
        assertEquals(listOf(Session.EVENING), shiftTabs(both.copy(shiftMode = ShiftMode.EVENING)))
        assertEquals(Session.EVENING, shiftTab(both, Session.EVENING))
        assertEquals(Session.EVENING, shiftTab(both.copy(shiftMode = ShiftMode.EVENING), Session.MORNING))
    }

    @Test
    fun morningAndEveningShareSlotTimesWithoutOverlap() {
        val morning = period(Session.MORNING, "IT-301")
        val evening = period(Session.EVENING, "IT-302")
        assertTrue(sessionTimetableSnapshot(listOf(morning, evening)).conflicts.isEmpty())
        assertNull(validateTimetablePeriod(evening, null, listOf(morning)))
        // Within one shift the same slot still overlaps.
        val clash = period(Session.EVENING, "IT-303", start = "08:30", end = "09:30")
        assertEquals("Overlaps IT-302 (08:00–09:00) on Monday.", validateTimetablePeriod(clash, null, listOf(morning, evening)))
        assertEquals(listOf(evening), periodsForShift(listOf(morning, evening), Session.EVENING))
        assertEquals("2022–2026 (E)", periodSessionLabel(both, evening))
    }

    @Test
    fun studentsSeeTheirOwnShiftsDatesheet() {
        val morning = Datesheet("d1", "IT_2022", Session.MORNING, 3, published = true)
        val evening = Datesheet("d2", "IT_2022", Session.EVENING, 3, published = true)
        val draft = Datesheet("d3", "IT_2021", Session.EVENING, 3, published = false)
        assertEquals("d2", studentDatesheet(listOf(morning, evening, draft), "IT_2022", 3, Session.EVENING)?.id)
        assertEquals("d1", studentDatesheet(listOf(evening, morning), "IT_2022", 3, Session.MORNING)?.id)
        assertNull(studentDatesheet(listOf(morning), "IT_2022", 3, Session.EVENING))
        assertNull(studentDatesheet(listOf(draft), "IT_2021", 3, Session.EVENING))
    }

    @Test
    fun exportsNameTheShift() {
        val register = attendanceRegisterExport("IT-301", both, YearMonth.of(2026, 9), emptyList(), emptyMap(), shift = Session.EVENING)
        assertTrue(register.title.any { line -> line.contains("Shift: Evening") })
        assertEquals("attendance_IT-301_evening_2026-09", register.fileBase)
        val timetable = timetableExport(both, listOf(period(Session.EVENING, "IT-302")), Session.EVENING)
        assertEquals(listOf("Class Timetable", "IT 2022–2026 · Evening · Semester 3"), timetable.title)
        assertEquals("timetable_IT_2022_evening", timetable.fileBase)
    }
}
