package com.mbd.cmscommon.domain.model

data class TeacherRosterSummary(
    val totalStudents: Int,
    val linkedStudents: Int,
    val studentsWithGrades: Int,
    val studentsWithAttendance: Int,
    val atRiskStudents: Int,
    val averageAttendance: Float?,
    val averageCgpa: Double?,
)

fun teacherRosterSummary(
    students: List<SessionStudent>,
    tallies: Map<String, AttendanceTally>,
    riskThreshold: Float = 65.0f,
): TeacherRosterSummary {
    val recordedAttendance = students.mapNotNull { student ->
        tallies[student.rollNumber]?.takeIf { it.total > 0 }
    }
    val recordedCgpa = students.mapNotNull { it.cgpa }

    return TeacherRosterSummary(
        totalStudents = students.size,
        linkedStudents = students.count { it.linkedEmail.isNotBlank() },
        studentsWithGrades = students.count { it.gpa != null || it.cgpa != null },
        studentsWithAttendance = recordedAttendance.size,
        atRiskStudents = recordedAttendance.count { it.percentage < riskThreshold },
        averageAttendance = recordedAttendance.takeIf { it.isNotEmpty() }?.map { it.percentage }?.average()?.toFloat(),
        averageCgpa = recordedCgpa.takeIf { it.isNotEmpty() }?.average(),
    )
}
