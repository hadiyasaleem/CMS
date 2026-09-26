package com.mbd.cmscommon.domain.model

import com.mbd.cmscommon.util.StudentIdCodec
import java.time.DayOfWeek
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
    fun rollSerialIsTheTrailingNumber() {
        assertEquals(9, rollSerial("IT-22-09"))
        assertEquals(51, rollSerial("IT-22-51 "))
        assertNull(rollSerial("IT-22-AB"))
        assertNull(rollSerial("IT-22-1234567"))
    }

    @Test
    fun morningAndEveningBlocks() {
        val both = session(ShiftMode.BOTH, 100)
        assertEquals(50, morningCapacity(both))
        assertNull(rollBlockError(both, Session.MORNING, "IT-22-01"))
        assertNull(rollBlockError(both, Session.MORNING, "IT-22-50"))
        assertNull(rollBlockError(both, Session.EVENING, "IT-22-51"))
        assertEquals("Morning roll numbers use serials 1-50; IT-22-60 is outside that range.", rollBlockError(both, Session.MORNING, "IT-22-60"))
        assertEquals("Evening roll numbers start after serial 50; IT-22-10 is in the Morning range.", rollBlockError(both, Session.EVENING, "IT-22-10"))
        assertTrue(rollBlockError(both, Session.MORNING, "IT-22-00")!!.startsWith("Morning roll numbers"))
        assertEquals("Roll number IT-22-AB must end with a serial number, e.g. IT-22-09.", rollBlockError(both, Session.MORNING, "IT-22-AB"))

        // A single-shift session's Morning block is its whole capacity; Evening still starts above it,
        // so the numbering survives a later switch to BOTH.
        val eveningOnly = session(ShiftMode.EVENING, 50)
        assertNull(rollBlockError(eveningOnly, Session.EVENING, "IT-22-51"))
        assertEquals("This session does not run the Morning shift.", rollBlockError(eveningOnly, Session.MORNING, "IT-22-01"))
    }

    @Test
    fun nextRollContinuesEachBlock() {
        val both = session(ShiftMode.BOTH, 100)
        val prefix = rollPrefix("it", 2022)
        assertEquals("IT-22-", prefix)
        assertEquals("IT-22-01", nextRollFor(both, Session.MORNING, emptyList(), prefix))
        assertEquals("IT-22-51", nextRollFor(both, Session.EVENING, emptyList(), prefix))
        val existing = listOf("IT-22-01", "IT-22-02", "IT-22-51")
        assertEquals("IT-22-03", nextRollFor(both, Session.MORNING, existing, prefix))
        assertEquals("IT-22-52", nextRollFor(both, Session.EVENING, existing, prefix))
        // Morning block full.
        assertNull(nextRollFor(both, Session.MORNING, listOf("IT-22-50"), prefix))
        assertNull(nextRollFor(session(ShiftMode.MORNING, 50), Session.EVENING, existing, prefix))
    }

    @Test
    fun shiftForRollFollowsTheBlock() {
        val both = session(ShiftMode.BOTH, 100)
        assertEquals(Session.MORNING, shiftForRoll(both, "IT-22-50"))
        assertEquals(Session.EVENING, shiftForRoll(both, "IT-22-51"))
        assertNull(shiftForRoll(both, "IT-22-XX"))
        assertEquals(Session.EVENING, shiftForRoll(session(ShiftMode.EVENING, 50), "IT-22-XX"))
    }
}
