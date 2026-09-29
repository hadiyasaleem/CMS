package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.PeriodType
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.teacher.ResolvedAssignment
import com.mbd.cmscommon.util.parseClock
import java.time.DayOfWeek
import java.time.Duration

/** A teacher's computed weekly teaching load -- derived purely from their own periods/assignments,
 * not stored anywhere, so it's always current with whatever the timetable says right now. */
data class TeacherWorkloadStats(
    val totalPeriods: Int,
    val totalWeeklyMinutes: Int,
    val distinctSubjects: Int,
    val distinctClasses: Int,
    val distinctDepartments: Int,
    val teachingDays: Int,
    val busiestDay: DayOfWeek?,
)

fun computeTeacherWorkloadStats(periods: List<SessionPeriod>, assignments: List<ResolvedAssignment>): TeacherWorkloadStats {
    val teaching = periods.filter { it.periodType != PeriodType.BREAK && it.courseCode.isNotBlank() }
    val totalMinutes = teaching.sumOf { p ->
        val start = parseClock(p.startTime)
        val end = parseClock(p.endTime)
        if (start != null && end != null && end.isAfter(start)) Duration.between(start, end).toMinutes().toInt() else 0
    }
    val busiestDay = teaching.groupingBy { it.day }.eachCount().entries.maxByOrNull { it.value }?.key
    return TeacherWorkloadStats(
        totalPeriods = teaching.size,
        totalWeeklyMinutes = totalMinutes,
        distinctSubjects = teaching.map { it.courseCode }.distinct().size,
        distinctClasses = assignments.map { it.classKey }.distinct().size,
        distinctDepartments = assignments.map { it.deptId }.filter { it.isNotBlank() }.distinct().size,
        teachingDays = teaching.map { it.day }.distinct().size,
        busiestDay = busiestDay,
    )
}

/** "6h 20m" / "45m" / "6h" -- shared by every screen that shows a weekly-minutes stat. */
fun formatWeeklyMinutes(minutes: Int): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return when {
        hours == 0 -> "${mins}m"
        mins == 0 -> "${hours}h"
        else -> "${hours}h ${mins}m"
    }
}
