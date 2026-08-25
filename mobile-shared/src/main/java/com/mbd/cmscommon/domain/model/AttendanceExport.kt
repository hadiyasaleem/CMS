package com.mbd.cmscommon.domain.model

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

enum class AttendanceReportKind {
    SEMESTER,
    MONTHLY,
    FULL,
}

data class AttendanceExportPayload(
    val fileBase: String,
    val title: List<String>,
    val header: List<String>,
    val rows: List<List<String>>,
)

fun buildAttendanceExportPayload(
    kind: AttendanceReportKind,
    departmentName: String,
    departmentId: String?,
    year: Int?,
    semester: Int?,
    shift: Session?,
    raw: List<DailyAttendanceMark>,
    roster: List<SessionStudent>,
    months: List<YearMonth>,
    month: YearMonth?,
    courseCode: String?,
    full: Map<String, Map<LocalDate, DailyAttendanceMark>>,
): AttendanceExportPayload? {
    if (departmentId == null || year == null || semester == null || shift == null) return null

    val base = "attendance_${departmentId}_${year}_${shift.name}_sem$semester"
    val title = listOf(
        "Attendance Records",
        "Department: $departmentName",
        "Session: $year–${year + 4} · ${shift.name} · Sem $semester",
    )
    val names = roster.associate { it.rollNumber to it.name }

    return when (kind) {
        AttendanceReportKind.SEMESTER -> {
            if (raw.isEmpty()) return null
            val byRoll = raw.groupBy { it.rollNumber }
            val rolls = (roster.map { it.rollNumber } + byRoll.keys).distinct().sorted()
            val header = listOf("Roll", "Name") + months.map { monthName(it) } + "Overall"
            val rows = rolls.map { roll ->
                val rollMarks = byRoll[roll].orEmpty()
                val monthCells = months.map { selectedMonth ->
                    percentageText(rollMarks.filter { YearMonth.from(it.date) == selectedMonth })
                }
                listOf(roll, names[roll] ?: roll) + monthCells + percentageText(rollMarks)
            }
            AttendanceExportPayload("${base}_semester", title + "Report: Semester summary", header, rows)
        }

        AttendanceReportKind.MONTHLY -> {
            if (month == null) return null
            val monthMarks = raw.filter { YearMonth.from(it.date) == month }
            val byRoll = monthMarks.groupBy { it.rollNumber }
            val rolls = (roster.map { it.rollNumber } + byRoll.keys).distinct().sorted()
            val header = listOf("Roll", "Name", "Present", "Absent", "Leave", "%")
            val rows = rolls.map { roll ->
                val rollMarks = byRoll[roll].orEmpty()
                listOf(
                    roll,
                    names[roll] ?: roll,
                    rollMarks.count { it.status == AttendanceStatus.PRESENT }.toString(),
                    rollMarks.count { it.status == AttendanceStatus.ABSENT }.toString(),
                    rollMarks.count { it.status == AttendanceStatus.LEAVE }.toString(),
                    percentageText(rollMarks),
                )
            }
            AttendanceExportPayload(
                "${base}_${month}_monthly",
                title + "Report: Monthly summary · ${monthLabel(month)}",
                header,
                rows,
            )
        }

        AttendanceReportKind.FULL -> {
            if (month == null || courseCode == null) return null
            val days = (1..month.lengthOfMonth()).map { month.atDay(it) }
            val rolls = (roster.map { it.rollNumber } + full.keys).distinct().sorted()
            val header = listOf("Roll", "Name") + days.map { it.dayOfMonth.toString() } + "%"
            val rows = rolls.map { roll ->
                val dayMap = full[roll].orEmpty()
                val dayCells = days.map { day ->
                    val mark = dayMap[day]
                    val letter = mark?.status?.let { exportLetter(it) } ?: ""
                    letter + if (mark?.isLate == true) "*" else ""
                }
                listOf(roll, names[roll] ?: roll) + dayCells + percentageText(dayMap.values.toList())
            }
            AttendanceExportPayload(
                "${base}_${courseCode}_${month}_full",
                title + "Report: Monthly full · $courseCode · ${monthLabel(month)}",
                header,
                rows,
            )
        }
    }
}

private fun percentageText(marks: List<DailyAttendanceMark>): String {
    if (marks.isEmpty()) return "–"
    val present = marks.count { it.status == AttendanceStatus.PRESENT }
    return "${(present * 100) / marks.size}%"
}

private fun exportLetter(status: AttendanceStatus): String = when (status) {
    AttendanceStatus.PRESENT -> "P"
    AttendanceStatus.ABSENT -> "A"
    AttendanceStatus.LEAVE -> "L"
}

private fun monthName(month: YearMonth): String =
    month.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)

private fun monthLabel(month: YearMonth): String =
    "${monthName(month)} ${month.year}"
