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
    fun suggestedRollContinuesTheShiftsBlock() {
        assertEquals("IT-22-03", suggestedRollNumber(both, "IT", Session.MORNING, roster))
        assertEquals("IT-22-52", suggestedRollNumber(both, "IT", Session.EVENING, roster))
        assertNull(suggestedRollNumber(both, null, Session.MORNING, roster))
    }

    @Test
    fun addingChecksSeatsDuplicatesAndTheBlock() {
        assertNull(addStudentError(both, Session.MORNING, "IT-22-03", roster))
        assertNull(addStudentError(both, Session.EVENING, "it-22-52", roster))
        assertEquals("Roll number IT-22-02 is already enrolled in this session.", addStudentError(both, Session.MORNING, "IT-22-02", roster))
        assertEquals("Morning roll numbers use serials 1-50; IT-22-60 is outside that range.", addStudentError(both, Session.MORNING, "IT-22-60", roster))
        assertEquals("Evening roll numbers start after serial 50; IT-22-10 is in the Morning range.", addStudentError(both, Session.EVENING, "IT-22-10", roster))
        assertEquals("This session does not run the Evening shift.", addStudentError(morningOnly, Session.EVENING, "IT-22-60", roster))
        assertEquals("This session is full (3 students).", addStudentError(both.copy(maxStudents = 3), Session.MORNING, "IT-22-03", roster))
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
    fun shiftFollowsTheRollNumber() {
        val profile = StudentProfile(sessionId = "IT_2022", rollNumber = "IT-22-03", name = "A", shift = Session.EVENING)
        assertEquals("Evening roll numbers start after serial 50; IT-22-03 is in the Morning range.", profileShiftError(both, profile))
        assertNull(profileShiftError(both, profile.copy(shift = Session.MORNING)))
        assertEquals("Morning uses serials 01–50; Evening starts at 51.", rollBlockHint(both))
        assertEquals("Morning uses serials 01–50.", rollBlockHint(morningOnly))
        assertEquals("Evening uses serials from 51.", rollBlockHint(both.copy(shiftMode = ShiftMode.EVENING, maxStudents = 50)))
    }

    @Test
    fun importReadsAnOptionalShiftColumn() {
        val result = StudentImportParser.parseCsv(
            "Roll,Name,Shift\nIT-22-01,Ali,Morning\nIT-22-51,Sara,E\nIT-22-02,Omar,\nIT-22-03,Zoya,Night\n",
        )
        assertEquals(listOf(Session.MORNING, Session.EVENING, null), result.rows.map { it.shift })
        assertEquals(listOf("Row 5: shift 'Night' must be Morning or Evening — skipped."), result.errors)
        // Files without the column still import; the roll number's block decides the shift later.
        assertEquals(listOf<Session?>(null), StudentImportParser.parseCsv("Roll,Name\nIT-22-01,Ali\n").rows.map { it.shift })
    }
}
