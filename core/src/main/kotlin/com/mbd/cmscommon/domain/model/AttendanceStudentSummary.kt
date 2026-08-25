package com.mbd.cmscommon.domain.model

import java.time.YearMonth

data class AttendanceMonthRate(
    val month: YearMonth,
    val marked: Int,
    val percentage: Int?,
)

data class AttendanceStudentSummary(
    val rollNumber: String,
    val name: String,
    val marked: Int,
    val present: Int,
    val absent: Int,
    val leave: Int,
    val late: Int,
    val percentage: Int?,
    val monthlyRates: List<AttendanceMonthRate>,
) {
    val belowTarget: Boolean get() = percentage != null && percentage < 75
}

fun attendanceStudentSummaries(
    marks: List<DailyAttendanceMark>,
    roster: List<SessionStudent>,
    months: List<YearMonth> = emptyList(),
): List<AttendanceStudentSummary> {
    val names = roster.associate { it.rollNumber to it.name }
    val byRoll = marks.groupBy { it.rollNumber }
    val rolls = (roster.map { it.rollNumber } + byRoll.keys).distinct().sorted()

    return rolls.map { roll ->
        val studentMarks = byRoll[roll].orEmpty()
        val present = studentMarks.count { it.status == AttendanceStatus.PRESENT }
        val name = names[roll]?.takeIf { it.isNotBlank() } ?: "Student record unavailable"
        val monthlyRates = months.map { month ->
            val monthMarks = studentMarks.filter { YearMonth.from(it.date) == month }
            val monthPresent = monthMarks.count { it.status == AttendanceStatus.PRESENT }
            AttendanceMonthRate(month, monthMarks.size, percentageOf(monthPresent, monthMarks.size))
        }
        AttendanceStudentSummary(
            rollNumber = roll,
            name = name,
            marked = studentMarks.size,
            present = present,
            absent = studentMarks.count { it.status == AttendanceStatus.ABSENT },
            leave = studentMarks.count { it.status == AttendanceStatus.LEAVE },
            late = studentMarks.count { it.isLate },
            percentage = percentageOf(present, studentMarks.size),
            monthlyRates = monthlyRates,
        )
    }
}

private fun percentageOf(count: Int, total: Int): Int? =
    if (total == 0) null else (count * 100) / total
