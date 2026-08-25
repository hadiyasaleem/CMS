package com.mbd.cmscommon.domain.model

import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters

data class StudentScheduledPeriod(
    val period: SessionPeriod,
    val date: LocalDate,
    val start: LocalTime?,
    val end: LocalTime?,
)

data class StudentTimetableSnapshot(
    val weekStart: LocalDate,
    val weekEnd: LocalDate,
    val periods: List<StudentScheduledPeriod>,
    val lectureCount: Int,
    val classDays: Int,
    val weeklyMinutes: Int,
    val todayPeriods: Int,
    val nextLecture: StudentScheduledPeriod?,
)

fun studentTimetableSnapshot(periods: List<SessionPeriod>, today: LocalDate, now: LocalTime): StudentTimetableSnapshot {
    val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val weekEnd = weekStart.plusDays(6)

    val scheduled = periods
        .groupBy { it.id }
        .map { (_, entries) -> entries.last() }
        .mapNotNull { period ->
            val date = weekStart.plusDays((period.day.value - DayOfWeek.MONDAY.value).toLong())
            val beforeStart = period.effectiveFrom?.let { date.isBefore(it) } ?: false
            val afterEnd = period.effectiveTo?.let { date.isAfter(it) } ?: false
            if (beforeStart || afterEnd) return@mapNotNull null
            StudentScheduledPeriod(period, date, toTimeOrNull(period.startTime), toTimeOrNull(period.endTime))
        }
        .sortedWith(compareBy<StudentScheduledPeriod> { it.date }.thenBy { it.start ?: LocalTime.MAX })

    val lectures = scheduled.filter { it.period.periodType == PeriodType.LECTURE && it.period.courseCode.isNotBlank() }

    val nextLecture = lectures.firstOrNull { item ->
        when {
            item.date.isAfter(today) -> true
            item.date == today -> item.end == null || item.end.isAfter(now)
            else -> false
        }
    }

    val weeklyMinutes = lectures.sumOf { item ->
        val start = item.start
        val end = item.end
        if (start != null && end != null && end.isAfter(start)) Duration.between(start, end).toMinutes().toInt() else 0
    }

    return StudentTimetableSnapshot(
        weekStart = weekStart,
        weekEnd = weekEnd,
        periods = scheduled,
        lectureCount = lectures.size,
        classDays = lectures.map { it.date }.distinct().size,
        weeklyMinutes = weeklyMinutes,
        todayPeriods = lectures.count { it.date == today },
        nextLecture = nextLecture,
    )
}

private fun toTimeOrNull(value: String?): LocalTime? =
    value?.let { runCatching { LocalTime.parse(it.trim()) }.getOrNull() }
