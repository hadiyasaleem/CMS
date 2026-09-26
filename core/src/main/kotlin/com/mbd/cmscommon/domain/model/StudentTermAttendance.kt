package com.mbd.cmscommon.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

const val ATTENDANCE_AT_RISK_PERCENT = 65

data class AttendanceCounts(
    val present: Int = 0,
    val absent: Int = 0,
    val leave: Int = 0,
    val late: Int = 0,
) {
    val total: Int get() = present + absent + leave
    val percentage: Int get() = if (total == 0) 0 else (present * 100) / total
    val isAtRisk: Boolean get() = total > 0 && percentage < ATTENDANCE_AT_RISK_PERCENT
}

data class MonthlyAttendance(val month: YearMonth, val tally: AttendanceCounts)

data class StudentTermAttendance(val overall: AttendanceCounts, val months: List<MonthlyAttendance>)

fun attendanceCounts(marks: Collection<DailyAttendanceMark>) = AttendanceCounts(
    present = marks.count { it.status == AttendanceStatus.PRESENT },
    absent = marks.count { it.status == AttendanceStatus.ABSENT },
    leave = marks.count { it.status == AttendanceStatus.LEAVE },
    late = marks.count { it.isLate },
)

/**
 * Months the summary should list: every month of the term up to [today] when term dates are known,
 * otherwise just the months that actually have marks.
 */
fun termMonths(termStart: LocalDate?, termEnd: LocalDate?, today: LocalDate, marks: List<DailyAttendanceMark>): List<YearMonth> {
    if (termStart == null) return marks.map { YearMonth.from(it.date) }.distinct().sorted()
    val last = YearMonth.from(listOfNotNull(termEnd, today).min())
    val first = YearMonth.from(termStart)
    if (last < first) return listOf(first)
    return generateSequence(first) { it.plusMonths(1) }.takeWhile { it <= last }.toList()
}

fun studentTermAttendance(marks: List<DailyAttendanceMark>, months: List<YearMonth>): StudentTermAttendance {
    val byMonth = marks.groupBy { YearMonth.from(it.date) }
    return StudentTermAttendance(
        overall = attendanceCounts(marks.filter { YearMonth.from(it.date) in months }),
        months = months.map { MonthlyAttendance(it, attendanceCounts(byMonth[it].orEmpty())) },
    )
}

/** A Sunday column is shown as a holiday unless a class was actually recorded that day. */
fun isRegisterHoliday(date: LocalDate, datesWithMarks: Set<LocalDate>): Boolean =
    date.dayOfWeek == DayOfWeek.SUNDAY && date !in datesWithMarks
