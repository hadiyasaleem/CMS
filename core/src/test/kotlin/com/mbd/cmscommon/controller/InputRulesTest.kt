package com.mbd.cmscommon.controller

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InputRulesTest {

    @Test
    fun blankTermDatesAreAllowed() {
        assertNull(termDatesError("", ""))
        assertNull(termDatesError("  ", "2027-01-15"))
        assertNull(termDatesError("2026-09-01", ""))
    }

    @Test
    fun aValidTermIsAccepted() {
        assertNull(termDatesError("2026-09-01", "2027-01-15"))
        assertNull(termDatesError(" 2026-09-01 ", " 2026-09-01 "))
    }

    @Test
    fun aBadDateSaysWhichFieldAndTheFormat() {
        assertEquals("Enter the start date as YYYY-MM-DD (for example 2026-09-01).", termDatesError("01/09/2026", "2027-01-15"))
        assertEquals("Enter the end date as YYYY-MM-DD (for example 2027-01-15).", termDatesError("2026-09-01", "next year"))
        // Both bad: the start is reported first, matching the order on the form.
        assertEquals("Enter the start date as YYYY-MM-DD (for example 2026-09-01).", termDatesError("x", "y"))
    }

    @Test
    fun aTermCannotEndBeforeItStarts() {
        assertEquals("The term can't end (2026-08-01) before it starts (2026-09-01).", termDatesError("2026-09-01", "2026-08-01"))
    }

    @Test
    fun fineAmountsMustBePositiveNumbers() {
        assertNull(fineAmountError(500.0))
        assertNull(fineAmountError(0.5))
        assertEquals("Fine amount must be greater than zero.", fineAmountError(0.0))
        assertEquals("Fine amount must be greater than zero.", fineAmountError(-3.0))
        assertEquals("Fine amount must be greater than zero.", fineAmountError(Double.NaN))
        assertEquals("Fine amount must be greater than zero.", fineAmountError(Double.POSITIVE_INFINITY))
        assertEquals("Enter the fine amount as a number, for example 500.", fineAmountError(null))
    }
}
