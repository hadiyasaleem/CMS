package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.FeeHead
import com.mbd.cmscommon.domain.model.FeeType
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionFeeStructure
import com.mbd.cmscommon.domain.model.ShiftMode
import com.mbd.cmscommon.domain.model.sampleFeeChallanHeader
import com.mbd.cmscommon.export.sessionFeesExport
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionFeeShiftTest {

    private val both = AcademicSession("IT_2022", "IT", 2022, 2026, ShiftMode.BOTH, 1, maxStudents = 100)
    private val eveningOnly = both.copy(shiftMode = ShiftMode.EVENING, maxStudents = 50)

    private val morningFee = SessionFeeStructure("IT_2022", Session.MORNING, FeeType.ANNUAL, listOf(FeeHead("Tuition", 50000.0)))
    private val eveningFee = SessionFeeStructure("IT_2022", Session.EVENING, FeeType.SEMESTER, listOf(FeeHead("Tuition", 30000.0), FeeHead("Lab", 5000.0)))

    @Test
    fun defaultPlanIsAnnualForMorningPerSemesterForEvening() {
        assertEquals(FeeType.ANNUAL, recommendedFeeCadence(Session.MORNING))
        assertEquals(FeeType.SEMESTER, recommendedFeeCadence(Session.EVENING))
    }

    @Test
    fun tabsAreOnlyTheSessionsShifts() {
        assertEquals(listOf(Session.MORNING, Session.EVENING), feeTabs(both))
        assertEquals(listOf(Session.EVENING), feeTabs(eveningOnly))
        assertEquals(Session.EVENING, feeTabShift(both, Session.EVENING))
        assertEquals(Session.MORNING, feeTabShift(both, null))
        // A shift the session doesn't run falls back to its own shift.
        assertEquals(Session.EVENING, feeTabShift(eveningOnly, Session.MORNING))
    }

    @Test
    fun summaryShowsEachShiftsPlan() {
        assertEquals("Morning Rs 50000 (annual) · Evening Rs 35000 (per semester)", feeSummaryLine(both, listOf(eveningFee, morningFee)))
        assertEquals("Morning Rs 50000 (annual) · Evening not set", feeSummaryLine(both, listOf(morningFee)))
        assertEquals("Rs 35000 (per semester)", feeSummaryLine(eveningOnly, listOf(eveningFee)))
        assertEquals("Fee structure not configured", feeSummaryLine(eveningOnly, emptyList()))
    }

    @Test
    fun exportHasASectionPairPerShift() {
        val doc = sessionFeesExport(both, "Information Technology", listOf(eveningFee, morningFee))
        assertEquals(listOf("Morning fee heads", "Morning details", "Evening fee heads", "Evening details"), doc.sections.map { it.name })
        assertEquals(listOf("Total", "35000"), doc.sections[2].rows.last())
        assertEquals(listOf("Cadence", "Semester"), doc.sections[3].rows[1])
        val single = sessionFeesExport(eveningOnly, null, listOf(eveningFee))
        assertEquals(listOf("Fee heads", "Details"), single.sections.map { it.name })
    }

    @Test
    fun challanPrintsTheShiftLabel() {
        assertEquals("Evening", sampleFeeChallanHeader(both, null, Session.EVENING).shift)
    }
}
