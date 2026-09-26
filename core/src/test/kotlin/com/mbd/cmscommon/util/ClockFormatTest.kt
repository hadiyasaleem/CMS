package com.mbd.cmscommon.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClockFormatTest {

    @Test
    fun dropsSecondsButLeavesOtherValuesAlone() {
        assertEquals("09:00", clockDisplay("09:00:00"))
        assertEquals("14:30", clockDisplay(" 14:30:59.123 "))
        assertEquals("08:15", clockDisplay("08:15"))
        assertEquals("", clockDisplay(null))
        assertEquals("TBA", clockDisplay("TBA"))
    }

    @Test
    fun rangesRejectReversedOrZeroLengthButAllowBlankEnds() {
        assertTrue(isDateRangeReversed("2026-09-10", "2026-09-09"))
        assertFalse(isDateRangeReversed("2026-09-10", "2026-09-10"))
        assertFalse(isDateRangeReversed("2026-09-10", ""))
        assertTrue(isTimeRangeInvalid("09:00:00", "09:00"))
        assertTrue(isTimeRangeInvalid("10:00", "09:59"))
        assertFalse(isTimeRangeInvalid("09:00", "09:01:00"))
        assertFalse(isTimeRangeInvalid("09:00", null))
    }
}
