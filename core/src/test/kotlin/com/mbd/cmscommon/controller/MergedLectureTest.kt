package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.domain.model.ShiftMode
import com.mbd.cmscommon.domain.model.ShiftScope
import com.mbd.cmscommon.teacher.resolveAssignments
import java.time.DayOfWeek
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MergedLectureTest {

    private val morningA = AcademicSession("CH_2022", "CH", 2022, 2026, ShiftMode.MORNING, 3, maxStudents = 100)
    private val morningB = AcademicSession("UR_2022", "UR", 2022, 2026, ShiftMode.MORNING, 3, maxStudents = 100)
    private val eveningOnly = AcademicSession("PH_2022", "PH", 2022, 2026, ShiftMode.EVENING, 3, maxStudents = 100)
    private val inactive = AcademicSession("ZZ_2022", "ZZ", 2022, 2026, ShiftMode.MORNING, 3, maxStudents = 100, isActive = false)
    private val dept = Department("CH", "Chemistry", "CH", createdAt = Instant.EPOCH, createdBy = "", updatedAt = Instant.EPOCH, updatedBy = "")

    private fun period(linked: Set<String> = emptySet()) = SessionPeriod(
        "p1", "CH_2022", Session.MORNING, DayOfWeek.MONDAY, "08:00", "09:00", "GE-101", "English", "t@x", "Teacher",
        linkedSessionIds = linked,
    )

    @Test
    fun aMergedLectureIsOneClassSpanningEverySession() {
        val classes = resolveAssignments(listOf(period(setOf("UR_2022"))), listOf(morningA, morningB), listOf(dept))
        assertEquals(1, classes.size)
        assertTrue(classes.single().isMerged)
        assertEquals(setOf("CH_2022", "UR_2022"), classes.single().sessionIds)
    }

    @Test
    fun anUnmergedLectureCoversOnlyItsOwnSession() {
        val single = resolveAssignments(listOf(period()), listOf(morningA), listOf(dept)).single()
        assertFalse(single.isMerged)
        assertEquals(setOf("CH_2022"), single.sessionIds)
    }

    @Test
    fun onlyActiveSameShiftUnlinkedSessionsCanBeMerged() {
        val eligible = eligibleMergeSessions(period(), listOf(morningA, morningB, eveningOnly, inactive))
        assertEquals(listOf("UR_2022"), eligible.map { it.sessionId })
        assertTrue(eligibleMergeSessions(period(setOf("UR_2022")), listOf(morningA, morningB)).isEmpty())
    }

    @Test
    fun aScopeMatchesAMergedClassThroughItsLinkedSession() {
        val merged = resolveAssignments(listOf(period(setOf("UR_2022"))), listOf(morningA, morningB), listOf(dept)).single()
        assertTrue(ShiftScope(sessionId = "UR_2022").matches(merged))
        assertEquals(setOf("CH_2022", "UR_2022"), listOf(merged).taughtClasses().map { it.sessionId }.toSet())
    }
}
