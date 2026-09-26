package com.mbd.cmscommon.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShiftScopeTest {

    private val it22 = AcademicSession("IT_2022", "IT", 2022, 2026, ShiftMode.BOTH, 1, maxStudents = 100)
    private val it23 = AcademicSession("IT_2023", "IT", 2023, 2027, ShiftMode.MORNING, 1)
    private val cs22 = AcademicSession("CS_2022", "CS", 2022, 2026, ShiftMode.EVENING, 1)
    private val sessions = listOf(it22, it23, cs22)

    @Test
    fun nothingSelectedMatchesEverything() {
        assertTrue(ShiftScope.ALL.isEmpty)
        assertTrue(ShiftScope().matches("CS", "CS_2022", Session.EVENING))
        assertEquals(3, sessions.count { ShiftScope().matches(it) })
    }

    @Test
    fun levelsNarrowProgressively() {
        val dept = ShiftScope(deptId = "IT")
        assertTrue(dept.matches("IT", "IT_2022", Session.EVENING))
        assertFalse(dept.matches("CS", "CS_2022", Session.EVENING))

        val session = ShiftScope(deptId = "IT", sessionId = "IT_2022")
        assertTrue(session.matches("IT", "IT_2022", Session.MORNING))
        assertTrue(session.matches("IT", "IT_2022", Session.EVENING))
        assertFalse(session.matches("IT", "IT_2023", Session.MORNING))

        val shift = session.withShift(Session.EVENING)
        assertTrue(shift.matches("IT", "IT_2022", Session.EVENING))
        assertFalse(shift.matches("IT", "IT_2022", Session.MORNING))
    }

    @Test
    fun onlyChosenLevelsApply() {
        // A shift alone narrows across every department and session.
        val eveningOnly = ShiftScope(shift = Session.EVENING)
        assertEquals(listOf("IT_2022", "CS_2022"), sessions.filter { eveningOnly.matches(it) }.map { it.sessionId })
        // Shared items with no shift (e.g. subjects) match any shift filter.
        assertTrue(eveningOnly.matches("IT", "IT_2022", null))
    }

    @Test
    fun narrowingDropsSelectionsThatNoLongerFit() {
        val picked = ShiftScope().withSession(it22).withShift(Session.EVENING)
        assertEquals(ShiftScope("IT", "IT_2022", Session.EVENING), picked)
        // it23 runs Morning only, so the Evening shift is dropped.
        assertEquals(ShiftScope("IT", "IT_2023", null), picked.withSession(it23))
        // Another department clears the session and shift; the same department keeps them.
        assertEquals(ShiftScope("CS"), picked.withDept("CS", sessions))
        assertEquals(picked, picked.withDept("IT", sessions))
    }

    @Test
    fun optionsFollowTheSelection() {
        assertEquals(listOf(it22, it23), ShiftScope.sessionOptions(ShiftScope(deptId = "IT"), sessions))
        assertEquals(sessions, ShiftScope.sessionOptions(ShiftScope(), sessions))
        assertEquals(listOf(Session.MORNING), ShiftScope.shiftOptions(ShiftScope(sessionId = "IT_2023"), sessions))
        assertEquals(Session.entries, ShiftScope.shiftOptions(ShiftScope(), sessions))
    }
}
