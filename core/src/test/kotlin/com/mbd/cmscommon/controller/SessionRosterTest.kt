package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.domain.model.ShiftMode
import com.mbd.cmscommon.domain.model.StudentProfile
import com.mbd.cmscommon.util.StudentImportParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SessionRosterTest {

    private val both = AcademicSession("IT_2022", "IT", 2022, 2026, ShiftMode.BOTH, 1, maxStudents = 100)
    private val morningOnly = both.copy(shiftMode = ShiftMode.MORNING, maxStudents = 50)

    private fun student(roll: String, shift: Session) =
        SessionStudent(SessionStudent.buildId("IT_2022", roll), "IT_2022", "IT", roll, roll, shift)

    private val roster = listOf(
        student("IT-22-01", Session.MORNING),
        student("IT-22-02", Session.MORNING),
        student("IT-22-51", Session.EVENING),
    )

    @Test
    fun tabsAreAllPlusTheSessionsShifts() {
        assertEquals(listOf(null, Session.MORNING, Session.EVENING), rosterTabs(both))
        assertEquals(listOf(null, Session.MORNING), rosterTabs(morningOnly))
        assertEquals(listOf<Session?>(null), rosterTabs(null))
    }

    @Test
    fun tabsFilterByShift() {
        assertEquals(3, studentsForTab(roster, null).size)
        assertEquals(listOf("IT-22-01", "IT-22-02"), studentsForTab(roster, Session.MORNING).map { it.rollNumber })
        assertEquals(listOf("IT-22-51"), studentsForTab(roster, Session.EVENING).map { it.rollNumber })
        assertEquals("All (3)", rosterTabLabel(null, roster))
        assertEquals("Evening (1)", rosterTabLabel(Session.EVENING, roster))
    }

    @Test
    fun newStudentsStartInTheOpenShift() {
        assertEquals(Session.EVENING, defaultShiftForNewStudent(both, Session.EVENING))
        assertEquals(Session.MORNING, defaultShiftForNewStudent(both, null))
        // A single-shift session always gets its only shift, whatever tab was open.
        assertEquals(Session.EVENING, defaultShiftForNewStudent(both.copy(shiftMode = ShiftMode.EVENING), Session.MORNING))
    }

    @Test
    fun addingChecksShiftCapacityAndDuplicates() {
        assertNull(addStudentError(both, Session.MORNING, "IT-22-03", roster))
        assertNull(addStudentError(both, Session.EVENING, "it-22-52", roster))
        assertEquals("Roll number IT-22-02 is already enrolled in this session.", addStudentError(both, Session.MORNING, "IT-22-02", roster))
        assertEquals("This session does not run the Evening shift.", addStudentError(morningOnly, Session.EVENING, "IT-22-60", roster))
        assertEquals("Morning is full (2 students).", addStudentError(both.copy(maxStudents = 4), Session.MORNING, "IT-22-03", roster))
        assertEquals("Enter a roll number.", addStudentError(both, Session.MORNING, "  ", roster))
    }

    @Test
    fun oneCrAndGrPerShift() {
        fun profile(roll: String, shift: Session, cr: Boolean = false, gr: Boolean = false) =
            StudentProfile(sessionId = "IT_2022", rollNumber = roll, name = roll, shift = shift, isCr = cr, isGr = gr)
        val classmates = listOf(profile("IT-22-01", Session.MORNING, cr = true), profile("IT-22-51", Session.EVENING, gr = true))
        // The Evening shift can have its own CR even though Morning has one.
        assertNull(classRoleConflict(profile("IT-22-52", Session.EVENING, cr = true), classmates))
        assertEquals("IT-22-01 is already the Morning CR. Remove that role first.", classRoleConflict(profile("IT-22-02", Session.MORNING, cr = true), classmates))
        assertEquals("IT-22-51 is already the Evening GR. Remove that role first.", classRoleConflict(profile("IT-22-52", Session.EVENING, gr = true), classmates))
        // Re-saving the current CR is fine.
        assertNull(classRoleConflict(profile("IT-22-01", Session.MORNING, cr = true), classmates))
    }

    @Test
    fun shiftCapacityHintReportsSeatsUsed() {
        assertEquals("2 of 50 seats used in Morning.", shiftCapacityHint(both, Session.MORNING, roster))
        assertEquals("1 of 50 seats used in Evening.", shiftCapacityHint(both, Session.EVENING, roster))
        assertNull(shiftCapacityHint(null, Session.MORNING, roster))
    }

    @Test
    fun importReadsAnOptionalShiftColumn() {
        val result = StudentImportParser.parseCsv(
            "Roll,Name,Shift\nIT-22-01,Ali,Morning\nIT-22-51,Sara,E\nIT-22-02,Omar,\nIT-22-03,Zoya,Night\n",
        )
        assertEquals(listOf(Session.MORNING, Session.EVENING, null), result.rows.map { it.shift })
        assertEquals(listOf("Row 5: shift 'Night' must be Morning or Evening — skipped."), result.errors)
        // Files without the column still import; a default shift is applied when the rows are saved.
        assertEquals(listOf<Session?>(null), StudentImportParser.parseCsv("Roll,Name\nIT-22-01,Ali\n").rows.map { it.shift })
    }
}
