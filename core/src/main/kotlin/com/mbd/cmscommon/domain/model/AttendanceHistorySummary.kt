package com.mbd.cmscommon.domain.model

import java.time.LocalDate

data class StudentAttendanceHistorySummary(
    val student: SessionStudent,
    val marks: List<DailyAttendanceMark>,
    val present: Int,
    val absent: Int,
    val leave: Int,
    val late: Int,
) {
    val total: Int get() = present + absent + leave
    val percentage: Int get() = if (total == 0) 0 else (present * 100) / total
    val isAtRisk: Boolean get() = total > 0 && percentage < 65
}

data class AttendanceHistorySummary(
    val students: List<StudentAttendanceHistorySummary>,
    val present: Int,
    val absent: Int,
    val leave: Int,
    val late: Int,
    val averagePercentage: Int,
    val atRiskStudents: Int,
    val studentsWithoutRecords: Int,
)

fun attendanceHistorySummary(
    roster: List<SessionStudent>,
    marks: Map<String, Map<LocalDate, DailyAttendanceMark>>,
): AttendanceHistorySummary {
    val students = roster.map { student ->
        val studentMarks = marks[student.rollNumber].orEmpty().values.sortedBy { it.date }
        StudentAttendanceHistorySummary(
            student = student,
            marks = studentMarks,
            present = studentMarks.count { it.status == AttendanceStatus.PRESENT },
            absent = studentMarks.count { it.status == AttendanceStatus.ABSENT },
            leave = studentMarks.count { it.status == AttendanceStatus.LEAVE },
            late = studentMarks.count { it.isLate },
        )
    }

    val recorded = students.filter { it.total > 0 }
    val present = students.sumOf { it.present }
    val absent = students.sumOf { it.absent }
    val leave = students.sumOf { it.leave }
    val late = students.sumOf { it.late }
    val averagePercentage = if (recorded.isEmpty()) 0 else recorded.map { it.percentage }.average().toInt()
    val atRiskStudents = students.count { it.isAtRisk }
    val studentsWithoutRecords = students.count { it.total == 0 }

    return AttendanceHistorySummary(students, present, absent, leave, late, averagePercentage, atRiskStudents, studentsWithoutRecords)
}
