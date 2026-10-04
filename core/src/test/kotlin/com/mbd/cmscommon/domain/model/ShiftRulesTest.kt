package com.mbd.cmscommon.domain.model

import com.mbd.cmscommon.util.StudentIdCodec
import java.time.DayOfWeek
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class ShiftRulesTest {

    private fun session(mode: ShiftMode, max: Int) =
        AcademicSession(AcademicSession.buildId("IT", 2022), "IT", 2022, 2026, mode, 1, maxStudents = max)

    @Test
    fun idsNoLongerCarryTheShift() {
        assertEquals("IT_2022", AcademicSession.buildId("IT", 2022))
        assertEquals("IT", StudentIdCodec.deptIdOf("IT_2022"))
        assertEquals("IT_2022", StudentIdCodec.sessionIdOf(SessionStudent.buildId("IT_2022", "IT-22-09")))
        // Morning and Evening can use the same slot, so the period id includes the shift.
        assertEquals("IT_2022_EVENING_MONDAY_08:00", SessionPeriod.buildId("IT_2022", Session.EVENING, DayOfWeek.MONDAY, "08:00"))
    }

    @Test
    fun shiftModeFromTickedShifts() {
        assertEquals(ShiftMode.BOTH, ShiftMode.of(setOf(Session.MORNING, Session.EVENING)))
        assertEquals(ShiftMode.EVENING, ShiftMode.of(setOf(Session.EVENING)))
        assertNull(ShiftMode.of(emptySet()))
        assertEquals(listOf(Session.MORNING, Session.EVENING), ShiftMode.BOTH.shifts)
        assertFalse(ShiftMode.MORNING.allows(Session.EVENING))
        assertEquals("Morning & Evening", ShiftMode.BOTH.label)
    }

    @Test
    fun defaultCapacityIs50PerShift() {
        assertEquals(50, AcademicSession.defaultMaxStudents(ShiftMode.MORNING))
        assertEquals(50, AcademicSession.defaultMaxStudents(ShiftMode.EVENING))
        assertEquals(100, AcademicSession.defaultMaxStudents(ShiftMode.BOTH))
    }

    @Test
    fun shiftCapacityIsHalfForBothOtherwiseTheWhole() {
        assertEquals(50, shiftCapacity(session(ShiftMode.BOTH, 100)))
        assertEquals(50, shiftCapacity(session(ShiftMode.MORNING, 50)))
        assertEquals(50, shiftCapacity(session(ShiftMode.EVENING, 50)))
    }

    @Test
    fun rollPrefixIsDeptCodeAndTwoDigitYear() {
        assertEquals("IT-22-", rollPrefix("it", 2022))
    }
}
