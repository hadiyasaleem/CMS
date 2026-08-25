package com.mbd.cmscommon.domain.model

data class StudentSemesterResult(
    val result: SemesterGpa,
    val gpaChange: Double?,
)

data class StudentResultsSnapshot(
    val semesters: List<StudentSemesterResult>,
    val currentGpa: Double?,
    val currentCgpa: Double?,
    val strongestSemester: SemesterGpa?,
    val cgpaChange: Double?,
    val activeSupplyCourses: List<String>,
    val promotedSemesters: Int,
)

fun studentResultsSnapshot(results: List<SemesterGpa>): StudentResultsSnapshot {
    val sorted = results
        .filter { it.semester > 0 }
        .groupBy { it.semester }
        .map { (_, entries) -> entries.last() }
        .sortedBy { it.semester }

    val semesters = sorted.mapIndexed { index, result ->
        val previous = sorted.getOrNull(index - 1)
        val gpaChange = previous?.let { result.gpa - it.gpa }
        StudentSemesterResult(result, gpaChange)
    }

    val latest = sorted.lastOrNull()
    val earliest = sorted.firstOrNull()
    val strongestSemester = sorted.maxByOrNull { it.gpa }
    val cgpaChange = if (sorted.size > 1) latest!!.cgpa - earliest!!.cgpa else null

    val activeSupplyCourses = (latest?.supplyCourses ?: emptyList())
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinctBy { it.lowercase() }

    val promotedSemesters = sorted.count { it.resultStatus.equals("PROMOTED", ignoreCase = true) }

    return StudentResultsSnapshot(
        semesters = semesters,
        currentGpa = latest?.gpa,
        currentCgpa = latest?.cgpa,
        strongestSemester = strongestSemester,
        cgpaChange = cgpaChange,
        activeSupplyCourses = activeSupplyCourses,
        promotedSemesters = promotedSemesters,
    )
}
