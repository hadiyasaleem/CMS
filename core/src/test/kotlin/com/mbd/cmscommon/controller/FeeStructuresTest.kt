package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.FeeHead
import com.mbd.cmscommon.domain.model.FeeType
import com.mbd.cmscommon.domain.model.ProgramType
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionFeeStructure
import com.mbd.cmscommon.domain.model.ShiftMode
import com.mbd.cmscommon.domain.model.ShiftScope
import com.mbd.cmscommon.export.feeStructuresExport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FeeStructuresTest {
    private val itBoth = AcademicSession("it_2026", "it", 2026, 2030, ShiftMode.BOTH, 1)
    private val engMorning = AcademicSession("eng_2025", "eng", 2025, 2029, ShiftMode.MORNING, 3)
    private val maEvening = AcademicSession("eng_2026_ma", "eng", 2026, 2028, ShiftMode.EVENING, 5, programType = ProgramType.MA_REPLACEMENT)
    private val sessions = listOf(itBoth, engMorning, maEvening)

    private fun fee(session: String, shift: Session, vararg heads: Pair<String, Double>, cadence: FeeType = FeeType.ANNUAL) =
        SessionFeeStructure(session, shift, cadence, heads.map { FeeHead(it.first, it.second) })

    private val morningBase = fee("", Session.MORNING, "Tuition" to 10000.0, "Library" to 500.0)
    private val eveningBase = fee("", Session.EVENING, "Tuition" to 20000.0, cadence = FeeType.SEMESTER)

    private fun rows(own: List<SessionFeeStructure> = emptyList(), scope: ShiftScope = ShiftScope.ALL, program: ProgramType? = null, semester: Int? = null) =
        feeRows(sessions, emptyList(), own, listOf(morningBase, eveningBase), scope, program, semester)

    @Test
    fun everyClassFollowsTheCollegeBaseForItsShiftUntilItHasItsOwn() {
        val result = rows()
        // IT both shifts, ENG morning, ENG MA evening.
        assertEquals(4, result.size)
        val itMorning = result.first { it.session.sessionId == "it_2026" && it.shift == Session.MORNING }
        assertEquals(FeeSource.COLLEGE, itMorning.source)
        assertEquals(10500.0, itMorning.structure!!.totalAmount, 0.0)
        assertEquals("it_2026", itMorning.structure!!.sessionId)
        val itEvening = result.first { it.session.sessionId == "it_2026" && it.shift == Session.EVENING }
        assertEquals(20000.0, itEvening.structure!!.totalAmount, 0.0)
        assertEquals(FeeType.SEMESTER, itEvening.structure!!.cadence)
    }

    @Test
    fun aClassOwnStructureOverridesTheBase() {
        val own = fee("it_2026", Session.MORNING, "Tuition" to 15000.0)
        val itMorning = rows(listOf(own)).first { it.session.sessionId == "it_2026" && it.shift == Session.MORNING }
        assertEquals(FeeSource.CUSTOM, itMorning.source)
        assertEquals(15000.0, itMorning.structure!!.totalAmount, 0.0)
        // The other shift of the same session is unaffected.
        val itEvening = rows(listOf(own)).first { it.session.sessionId == "it_2026" && it.shift == Session.EVENING }
        assertEquals(FeeSource.COLLEGE, itEvening.source)
    }

    @Test
    fun withNoBaseAndNoOwnTheClassHasNoFees() {
        val result = feeRows(sessions, emptyList(), emptyList(), emptyList(), ShiftScope.ALL, null, null)
        assertTrue(result.all { it.source == FeeSource.NONE })
    }

    @Test
    fun filtersNarrowTheGrid() {
        assertEquals(2, rows(scope = ShiftScope(deptId = "eng")).size)
        assertEquals(listOf(Session.EVENING), rows(scope = ShiftScope(deptId = "eng"), program = ProgramType.MA_REPLACEMENT).map { it.shift })
        assertEquals(2, rows(scope = ShiftScope(shift = Session.EVENING)).size)
        assertEquals(listOf("eng_2025"), rows(semester = 3).map { it.session.sessionId })
    }

    @Test
    fun theExportHasABaseSectionAndAColumnPerFeeHead() {
        val doc = feeStructuresExport(listOf(morningBase, eveningBase), rows())
        assertEquals(listOf("College-wide base", "Fee structure by class"), doc.sections.map { it.name })
        val grid = doc.sections.last()
        assertEquals(listOf("Department", "Session", "Semester", "Program", "Shift", "Plan", "Tuition", "Library", "Total", "Source"), grid.header)
        val itMorning = grid.rows.first { it[1] == "2026–2030" && it[4] == "Morning" }
        assertEquals(listOf("10,000", "500", "10,500", "College base"), itMorning.drop(6))
    }
}
