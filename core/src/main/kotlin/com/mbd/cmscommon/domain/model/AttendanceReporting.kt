package com.mbd.cmscommon.domain.model

data class AttendanceReportSummary(
    val studentCount: Int,
    val markedEntries: Int,
    val presentEntries: Int,
    val absentEntries: Int,
    val leaveEntries: Int,
    val lateEntries: Int,
    val belowTargetStudents: Int,
) {
    val attendancePercentage: Int? get() =
        if (markedEntries == 0) null else (presentEntries * 100) / markedEntries
}

fun attendanceReportSummary(
    marks: List<DailyAttendanceMark>,
    roster: List<SessionStudent>,
    targetPercentage: Int = 75,
): AttendanceReportSummary {
    val marksByStudent = marks.groupBy { it.rollNumber }
    val studentCount = (roster.map { it.rollNumber } + marksByStudent.keys).distinct().size
    val belowTargetStudents = marksByStudent.values.count { studentMarks ->
        val present = studentMarks.count { it.status == AttendanceStatus.PRESENT }
        present * 100 < studentMarks.size * targetPercentage
    }

    return AttendanceReportSummary(
        studentCount = studentCount,
        markedEntries = marks.size,
        presentEntries = marks.count { it.status == AttendanceStatus.PRESENT },
        absentEntries = marks.count { it.status == AttendanceStatus.ABSENT },
        leaveEntries = marks.count { it.status == AttendanceStatus.LEAVE },
        lateEntries = marks.count { it.isLate },
        belowTargetStudents = belowTargetStudents,
    )
}
