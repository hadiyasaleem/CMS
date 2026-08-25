package com.mbd.cmscommon.domain.model

data class SemesterResultsSummary(
    val totalStudents: Int,
    val recordedStudents: Int,
    val missingStudents: Int,
    val promotedStudents: Int,
    val attentionStudents: Int,
    val supplyStudents: Int,
    val averageGpa: Double?,
    val averageCgpa: Double?,
)

fun semesterResultsSummary(roster: List<SessionStudent>, results: Map<String, SemesterGpa>): SemesterResultsSummary {
    val rosterRolls = roster.map { it.rollNumber }.toSet()
    val current = results.filterKeys { it in rosterRolls }.values

    return SemesterResultsSummary(
        totalStudents = roster.size,
        recordedStudents = current.size,
        missingStudents = (roster.size - current.size).coerceAtLeast(0),
        promotedStudents = current.count { it.resultStatus.equals("PROMOTED", ignoreCase = true) },
        attentionStudents = current.count {
            it.resultStatus.equals("REPEATED", ignoreCase = true) || it.resultStatus.equals("PROBATION", ignoreCase = true)
        },
        supplyStudents = current.count { it.supplyCourses.isNotEmpty() },
        averageGpa = current.map { it.gpa }.takeIf { it.isNotEmpty() }?.average(),
        averageCgpa = current.map { it.cgpa }.takeIf { it.isNotEmpty() }?.average(),
    )
}

fun gradePointValidationMessage(raw: String): String? {
    if (raw.isBlank()) return "Required"
    val value = raw.trim().toDoubleOrNull() ?: return "Enter a number"
    return if (value in 0.0..4.0) null else "Use 0.00 to 4.00"
}
