package com.mbd.cmscommon.domain.model

import java.time.DayOfWeek
import java.time.LocalTime
import java.time.temporal.ChronoUnit

data class TeacherScheduleSummary(
    val totalPeriods: Int,
    val teachingDays: Int,
    val uniqueClasses: Int,
    val uniqueRooms: Int,
    val weeklyMinutes: Int,
    val busiestDay: DayOfWeek?,
)

fun teacherScheduleSummary(periods: List<SessionPeriod>): TeacherScheduleSummary {
    val byDay = periods.groupingBy { it.day }.eachCount()

    val uniqueRooms = periods.mapNotNull { period ->
        val building = period.building?.trim()?.takeIf { it.isNotBlank() }
        val roomNo = period.roomNo?.trim()?.takeIf { it.isNotBlank() }
        listOfNotNull(building, roomNo).takeIf { it.isNotEmpty() }?.joinToString(" / ")
    }.distinct().size

    val busiestDay = byDay.entries
        .maxWithOrNull(compareBy<Map.Entry<DayOfWeek, Int>> { it.value }.thenByDescending { it.key.value })
        ?.key

    return TeacherScheduleSummary(
        totalPeriods = periods.size,
        teachingDays = periods.map { it.day }.distinct().size,
        uniqueClasses = periods.map { it.sessionId to it.courseCode }.distinct().size,
        uniqueRooms = uniqueRooms,
        weeklyMinutes = periods.sumOf { periodMinutes(it) },
        busiestDay = busiestDay,
    )
}

private fun periodMinutes(period: SessionPeriod): Int = runCatching {
    val start = LocalTime.parse(period.startTime)
    val end = LocalTime.parse(period.endTime)
    ChronoUnit.MINUTES.between(start, end).toInt().coerceAtLeast(0)
}.getOrDefault(0)
