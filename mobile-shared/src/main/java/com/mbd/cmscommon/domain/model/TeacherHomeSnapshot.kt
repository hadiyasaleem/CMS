package com.mbd.cmscommon.domain.model

import com.mbd.cmscommon.teacher.ResolvedAssignment
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val TeachingWeek = listOf(
    DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY,
)

data class TeacherDayLoad(
    val day: DayOfWeek,
    val label: String,
    val count: Int,
    val isToday: Boolean,
)

data class TeacherHomeSnapshot(
    val name: String,
    val dateLabel: String,
    val todaysClasses: List<SessionPeriod>,
    val nextClass: SessionPeriod?,
    val weeklyLoad: List<TeacherDayLoad>,
    val weeklyLectures: Int,
    val assignedSubjects: Int,
    val assignedSessions: Int,
    val busiestDay: String,
)

fun teacherHomeSnapshot(
    teacherId: String,
    periods: List<SessionPeriod>,
    assignments: List<ResolvedAssignment>,
    date: LocalDate,
    time: LocalTime,
): TeacherHomeSnapshot {
    val teachingPeriods = periods
        .filter { it.periodType != PeriodType.BREAK && it.courseCode.isNotBlank() }
        .distinctBy { it.id }

    val todaysClasses = teachingPeriods
        .filter { it.day == date.dayOfWeek }
        .sortedBy { it.startTime }

    val loads = TeachingWeek.map { day ->
        val label = day.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).uppercase(Locale.ROOT).take(3)
        val count = teachingPeriods.count { it.day == day }
        TeacherDayLoad(day, label, count, day == date.dayOfWeek)
    }
    val maxLoad = loads.maxOfOrNull { it.count } ?: 0

    val resolvedAssignments = assignments.distinctBy { it.sessionId to it.courseCode }

    val namePart = teacherId.substringBefore("@")
    val displayName = if (namePart.isNotEmpty()) {
        namePart[0].uppercase(Locale.ROOT) + namePart.substring(1)
    } else {
        namePart
    }.ifBlank { "Professor" }

    val dateLabel = date.format(DateTimeFormatter.ofPattern("EEEE · d MMM yyyy", Locale.ENGLISH))

    val nextClass = todaysClasses.firstOrNull { period ->
        val start = runCatching { LocalTime.parse(period.startTime) }.getOrNull()
        start != null && !start.isBefore(time)
    }

    val busiestDay = loads.firstOrNull { it.count == maxLoad && maxLoad > 0 }?.label ?: "None"

    return TeacherHomeSnapshot(
        name = displayName,
        dateLabel = dateLabel,
        todaysClasses = todaysClasses,
        nextClass = nextClass,
        weeklyLoad = loads,
        weeklyLectures = teachingPeriods.size,
        assignedSubjects = resolvedAssignments.size,
        assignedSessions = resolvedAssignments.map { it.sessionId }.distinct().size,
        busiestDay = busiestDay,
    )
}
