package com.mbd.cmscommon.domain.model

import java.time.LocalDate

data class StudentExamsHubSnapshot(
    val enteredAssessments: Int,
    val subjectsWithScores: Int,
    val absentAssessments: Int,
    val recordedSemesters: Int,
    val currentCgpa: Double?,
    val activeSupplyCourses: Int,
    val publishedDatesheets: Int,
    val upcomingPapers: Int,
    val nextExamDate: LocalDate?,
)

fun studentExamsHubSnapshot(
    sessionId: String,
    scores: List<SubjectExamScore>,
    results: List<SemesterGpa>,
    datesheets: List<Datesheet>,
    slots: List<DatesheetSlot>,
    today: LocalDate,
): StudentExamsHubSnapshot {
    val distinctScores = scores.distinctBy { it.courseCode.trim().lowercase() to it.examType }
    val latestResult = results.maxByOrNull { it.semester }

    val viewer = DatesheetViewerContext(DatesheetViewerRole.STUDENT, sessionId)
    val visibleDatesheets = datesheets.filter { isVisibleTo(it, viewer) }.distinctBy { it.id }
    val visibleIds = visibleDatesheets.map { it.id }.toSet()

    val upcoming = slots.distinctBy { it.id }.mapNotNull { slot ->
        if (!visibleIds.contains(slot.datesheetId)) return@mapNotNull null
        val date = runCatching { LocalDate.parse(slot.examDate) }.getOrNull() ?: return@mapNotNull null
        if (date.isBefore(today)) null else slot to date
    }

    val supplyCourses = (latestResult?.supplyCourses ?: emptyList())
        .map { it.trim() }
        .filter { it.isNotBlank() }

    return StudentExamsHubSnapshot(
        enteredAssessments = distinctScores.size,
        subjectsWithScores = distinctScores.map { it.courseCode.trim().lowercase() }.filter { it.isNotBlank() }.distinct().size,
        absentAssessments = distinctScores.count { it.wasAbsent },
        recordedSemesters = results.map { it.semester }.distinct().size,
        currentCgpa = latestResult?.cgpa,
        activeSupplyCourses = supplyCourses.distinct().size,
        publishedDatesheets = visibleDatesheets.size,
        upcomingPapers = upcoming.size,
        nextExamDate = upcoming.minOfOrNull { it.second }, // nearest upcoming exam date
    )
}
