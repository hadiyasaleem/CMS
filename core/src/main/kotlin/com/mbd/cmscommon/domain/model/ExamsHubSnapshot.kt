package com.mbd.cmscommon.domain.model

import com.mbd.cmscommon.teacher.ResolvedAssignment
import java.time.LocalDate

data class ExamsHubSnapshot(
    val assignedClasses: Int,
    val assignedSessions: Int,
    val paperSubmissions: Int,
    val classesWithPapers: Int,
    val publishedDatesheets: Int,
    val upcomingInvigilationSlots: Int,
) {
    val paperCoveragePercent: Int get() =
        if (assignedClasses == 0) 0 else (classesWithPapers * 100) / assignedClasses
}

fun examsHubSnapshot(
    teacherId: String,
    assignments: List<ResolvedAssignment>,
    submissions: List<ExamPaperSubmission>,
    datesheets: List<Datesheet>,
    slots: List<DatesheetSlot>,
    today: LocalDate,
): ExamsHubSnapshot {
    val distinctAssignments = assignments.distinctBy { it.sessionId to it.courseCode }
    val assignmentKeys = distinctAssignments.map { it.sessionId to it.courseCode }.toSet()

    val teacherPapers = submissions
        .filter { it.teacherId.equals(teacherId, ignoreCase = true) }
        .filter { assignmentKeys.contains(it.offeringId to it.subjectId) }
        .distinctBy { it.submissionId }

    val publishedDatesheets = datesheets.filter { it.published }.distinctBy { it.id }
    val publishedIds = publishedDatesheets.map { it.id }.toSet()

    val upcomingInvigilation = slots.distinctBy { it.id }.count { slot ->
        publishedIds.contains(slot.datesheetId) &&
            slot.invigilatorEmail.equals(teacherId, ignoreCase = true) &&
            runCatching { LocalDate.parse(slot.examDate) }.getOrNull()?.let { !it.isBefore(today) } == true
    }

    return ExamsHubSnapshot(
        assignedClasses = distinctAssignments.size,
        assignedSessions = distinctAssignments.map { it.sessionId }.distinct().size,
        paperSubmissions = teacherPapers.size,
        classesWithPapers = teacherPapers.map { it.offeringId to it.subjectId }.distinct().size,
        publishedDatesheets = publishedDatesheets.size,
        upcomingInvigilationSlots = upcomingInvigilation,
    )
}
