package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.domain.model.ShiftMode
import java.time.DayOfWeek
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class MergedMasterGridTest {

    private val a = AcademicSession("CH_2022", "CH", 2022, 2026, ShiftMode.MORNING, 3, maxStudents = 100)
    private val b = AcademicSession("UR_2022", "UR", 2022, 2026, ShiftMode.MORNING, 3, maxStudents = 100)

    @Test
    fun aMergedLectureAppearsUnderBothSessionsButIsStoredOnce() {
        val owned = SessionPeriod(
            "p1", "CH_2022", Session.MORNING, DayOfWeek.MONDAY, "08:00", "09:00", "GE-101", "English", "t@x", "Teacher",
            linkedSessionIds = setOf("UR_2022"),
        )
        val rows = buildMasterGrids(listOf(a, b), emptyList(), listOf(owned)).single().rows.associateBy { it.session.sessionId }
        val own = rows.getValue("CH_2022").periods.single()
        val linked = rows.getValue("UR_2022").periods.single()
        assertEquals("p1", own.id)
        assertEquals("p1::UR_2022", linked.id)
        assertFalse(linked.isOwnRow)
        assertEquals(setOf("CH_2022"), linked.linkedSessionIds)
    }
}
