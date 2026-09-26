package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Datesheet
import com.mbd.cmscommon.domain.model.FeeType
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionFeeStructure
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.domain.model.ShiftMode
import java.time.DayOfWeek
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SessionShiftRulesTest {

    private val both = AcademicSession("IT_2022", "IT", 2022, 2026, ShiftMode.BOTH, 1, maxStudents = 100)

    private fun student(roll: String, shift: Session) =
        SessionStudent(SessionStudent.buildId("IT_2022", roll), "IT_2022", "IT", roll, roll, shift)

    private fun period(shift: Session, start: String) =
        SessionPeriod(SessionPeriod.buildId("IT_2022", shift, DayOfWeek.MONDAY, start), "IT_2022", shift, DayOfWeek.MONDAY, start, "09:00", "IT-301", "OS", "t@x", "T")

    private fun fee(shift: Session) = SessionFeeStructure("IT_2022", shift, FeeType.SEMESTER, emptyList())

    private fun sheet(shift: Session) = Datesheet("d-$shift", "IT_2022", shift, 1)

    @Test
    fun capacityRefillsFromTheTickedShifts() {
        assertEquals("50", capacityForShiftSelection(setOf(Session.MORNING), "100"))
        assertEquals("100", capacityForShiftSelection(setOf(Session.MORNING, Session.EVENING), "50"))
        assertEquals("50", capacityForShiftSelection(setOf(Session.EVENING), "100"))
        // Nothing ticked: keep what the admin typed.
        assertEquals("73", capacityForShiftSelection(emptySet(), "73"))
    }

    @Test
    fun capacityMustBe1To200AndFitTheRoster() {
        assertNull(capacityError("100"))
        assertNull(capacityError("200"))
        assertEquals("Student capacity must be between 1 and 200.", capacityError("0"))
        assertEquals("Student capacity must be between 1 and 200.", capacityError("201"))
        assertEquals("Student capacity must be between 1 and 200.", capacityError(""))
        assertEquals("Student capacity can't be below the 60 student(s) already enrolled.", capacityError("50", enrolled = 60))
    }

    @Test
    fun oneSessionPerIntakeYear() {
        val existing = listOf(both)
        assertNull(createSessionError(2023, setOf(Session.EVENING), "50", existing))
        assertEquals("A 2022–2026 session already exists in this department.", createSessionError(2022, setOf(Session.MORNING), "50", existing))
        assertEquals("Tick Morning, Evening, or both.", createSessionError(2023, emptySet(), "50", existing))
        assertEquals("Select the intake year.", createSessionError(null, setOf(Session.MORNING), "50", existing))
        assertEquals("Student capacity must be between 1 and 200.", createSessionError(2023, setOf(Session.MORNING), "500", existing))
    }

    @Test
    fun addingAShiftIsAlwaysAllowed() {
        val morningOnly = both.copy(shiftMode = ShiftMode.MORNING, maxStudents = 50)
        val students = listOf(student("IT-22-01", Session.MORNING), student("IT-22-50", Session.MORNING))
        assertNull(shiftModeChangeError(morningOnly, ShiftMode.BOTH, 100, students, listOf(fee(Session.MORNING)), listOf(period(Session.MORNING, "08:00")), emptyList()))
    }

    @Test
    fun droppingAShiftListsWhatIsLeft() {
        val students = listOf(student("IT-22-01", Session.MORNING), student("IT-22-51", Session.EVENING), student("IT-22-52", Session.EVENING))
        val message = shiftModeChangeError(
            both, ShiftMode.MORNING, 50, students,
            fees = listOf(fee(Session.MORNING), fee(Session.EVENING)),
            periods = listOf(period(Session.EVENING, "08:00"), period(Session.EVENING, "09:00"), period(Session.MORNING, "08:00")),
            datesheets = listOf(sheet(Session.EVENING)),
        )
        assertEquals(
            "Cannot switch to Morning only: the Evening shift still has 2 student(s), a fee structure, 2 timetable period(s), 1 datesheet(s). Move or remove them first.",
            message,
        )
        // Once the Evening shift is empty the switch is allowed.
        assertNull(shiftModeChangeError(both, ShiftMode.MORNING, 50, students.take(1), listOf(fee(Session.MORNING)), emptyList(), emptyList()))
    }

    @Test
    fun capacityChangeMustKeepRollBlocks() {
        val students = listOf(student("IT-22-45", Session.MORNING), student("IT-22-51", Session.EVENING))
        // BOTH with 80 seats -> Morning block 1-40, so IT-22-45 would fall out of it.
        assertEquals(
            "Morning roll numbers use serials 1-40; IT-22-45 is outside that range. Adjust the capacity or renumber that student first.",
            shiftModeChangeError(both, ShiftMode.BOTH, 80, students, emptyList(), emptyList(), emptyList()),
        )
        // Raising it moves the Morning block up (120 seats -> 1-60), which would swallow Evening's IT-22-51.
        assertEquals(
            "Evening roll numbers start after serial 60; IT-22-51 is in the Morning range. Adjust the capacity or renumber that student first.",
            shiftModeChangeError(both, ShiftMode.BOTH, 120, students, emptyList(), emptyList(), emptyList()),
        )
        // 90 seats -> Morning 1-45: both students still fit.
        assertNull(shiftModeChangeError(both, ShiftMode.BOTH, 90, listOf(student("IT-22-30", Session.MORNING), student("IT-22-51", Session.EVENING)), emptyList(), emptyList(), emptyList()))
    }

    @Test
    fun countsPerShift() {
        val counts = studentCountsByShift(listOf(student("IT-22-01", Session.MORNING), student("IT-22-51", Session.EVENING), student("IT-22-52", Session.EVENING)))
        assertEquals(mapOf(Session.MORNING to 1, Session.EVENING to 2), counts)
        assertEquals(mapOf(Session.MORNING to 0, Session.EVENING to 0), studentCountsByShift(emptyList()))
    }
}
