package com.mbd.cmscommon.domain.model

import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StudentTermAttendanceTest {

    private fun mark(date: String, status: AttendanceStatus, late: Boolean = false) =
        DailyAttendanceMark("IT-21-09", LocalDate.parse(date), status, isLate = late)

    @Test
    fun termMonthsRunsFromTermStartToEarlierOfTermEndAndToday() {
        val months = termMonths(LocalDate.parse("2026-03-10"), LocalDate.parse("2026-08-01"), LocalDate.parse("2026-05-20"), emptyList())
        assertEquals(listOf(YearMonth.of(2026, 3), YearMonth.of(2026, 4), YearMonth.of(2026, 5)), months)
    }

    @Test
    fun termMonthsFallsBackToMonthsWithMarksWhenTermIsUnknown() {
        val marks = listOf(mark("2026-05-02", AttendanceStatus.PRESENT), mark("2026-03-09", AttendanceStatus.ABSENT))
        assertEquals(listOf(YearMonth.of(2026, 3), YearMonth.of(2026, 5)), termMonths(null, null, LocalDate.parse("2026-06-01"), marks))
    }

    @Test
    fun summaryTalliesEachMonthAndOverall() {
        val marks = listOf(
            mark("2026-04-01", AttendanceStatus.PRESENT, late = true),
            mark("2026-04-02", AttendanceStatus.ABSENT),
            mark("2026-05-04", AttendanceStatus.PRESENT),
            mark("2026-05-05", AttendanceStatus.LEAVE),
        )
        val summary = studentTermAttendance(marks, listOf(YearMonth.of(2026, 4), YearMonth.of(2026, 5), YearMonth.of(2026, 6)))
        assertEquals(AttendanceCounts(present = 2, absent = 1, leave = 1, late = 1), summary.overall)
        assertEquals(50, summary.months[0].tally.percentage)
        assertEquals(0, summary.months[2].tally.total)
        assertTrue(summary.months[0].tally.isAtRisk)
        assertFalse(summary.months[2].tally.isAtRisk)
    }

    @Test
    fun sundayIsHolidayOnlyWhenNothingWasRecorded() {
        val sunday = LocalDate.parse("2026-09-27")
        assertTrue(isRegisterHoliday(sunday, emptySet()))
        assertFalse(isRegisterHoliday(sunday, setOf(sunday)))
        assertFalse(isRegisterHoliday(sunday.plusDays(1), emptySet()))
    }
}
