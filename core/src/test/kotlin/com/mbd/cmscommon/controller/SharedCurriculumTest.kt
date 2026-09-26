package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.ShiftMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SharedCurriculumTest {

    private fun session(mode: ShiftMode, semester: Int) =
        AcademicSession("IT_2022", "IT", 2022, 2026, mode, semester, maxStudents = AcademicSession.defaultMaxStudents(mode))

    @Test
    fun twoShiftSessionsSayTheCurriculumIsShared() {
        assertEquals(
            "Shared by the Morning and Evening shifts: subjects, topics and term dates are entered once.",
            sharedCurriculumNote(session(ShiftMode.BOTH, 3)),
        )
        assertNull(sharedCurriculumNote(session(ShiftMode.MORNING, 3)))
        assertNull(sharedCurriculumNote(null))
    }

    @Test
    fun onePromotionMovesEveryShift() {
        assertEquals(
            "This promotes both the Morning and Evening shifts from semester 3 to 4 and removes that semester's exam papers.",
            promotionConfirmText(session(ShiftMode.BOTH, 3)),
        )
        assertEquals(
            "This promotes the whole class from semester 3 to 4 and removes that semester's exam papers.",
            promotionConfirmText(session(ShiftMode.EVENING, 3)),
        )
        assertEquals(
            "This marks both the Morning and Evening shifts as graduated and archives the session. This cannot be undone.",
            promotionConfirmText(session(ShiftMode.BOTH, 8)),
        )
    }
}
