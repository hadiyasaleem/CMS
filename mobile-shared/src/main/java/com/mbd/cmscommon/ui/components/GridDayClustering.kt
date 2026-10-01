package com.mbd.cmscommon.ui.components

import com.mbd.cmscommon.domain.model.SessionPeriod
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

val WeekOrder = listOf(
    DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY,
)

/** A run of consecutive-in-the-week days that all show the identical set of periods for one row
 * (e.g. an evening class meets "Mon & Tue" with one subject, "Wed & Thu" with another — exactly how
 * the printed timetables lay it out). A row that repeats the same subjects every day collapses to a
 * single cluster covering all of them, so no day label is shown at all. */
data class DayCluster(val days: List<DayOfWeek>, val periods: List<SessionPeriod>)

fun dayClustersFor(periods: List<SessionPeriod>): List<DayCluster> {
    val byDay = periods.groupBy { it.day }
    if (byDay.isEmpty()) return listOf(DayCluster(emptyList(), emptyList()))
    fun signature(dayPeriods: List<SessionPeriod>) = dayPeriods
        .sortedBy { it.startTime }
        .joinToString("|") { "${it.courseCode}@${it.startTime}-${it.endTime}" }
    return byDay.entries
        .groupBy({ signature(it.value) }, { it.key to it.value })
        .values
        .map { entries ->
            DayCluster(
                days = entries.map { it.first }.sortedBy { WeekOrder.indexOf(it) },
                periods = entries.first().second,
            )
        }
        .sortedBy { WeekOrder.indexOf(it.days.first()) }
}

fun dayRangeLabel(days: List<DayOfWeek>): String =
    days.joinToString(" & ") { it.getDisplayName(TextStyle.SHORT, Locale.ENGLISH) }

/** Every day (within [periods], typically one session's whole week) that repeats the exact same
 * lecture as [period] -- same course, time and teacher -- so an edit dialog opened from any one of
 * those days can pre-check all of them instead of just the day that was clicked. */
fun siblingDaysFor(period: SessionPeriod, periods: List<SessionPeriod>): Set<DayOfWeek> =
    periods.asSequence()
        .filter {
            it.courseCode == period.courseCode && it.startTime == period.startTime &&
                it.endTime == period.endTime && it.hasSameTeachersAs(period)
        }
        .map { it.day }
        .toSet() + period.day
