package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.SemesterSubject
import com.mbd.cmscommon.domain.model.SubjectType

data class SemesterCurriculumSnapshot(
    val subjectCount: Int,
    val totalCredits: Int,
    val theoryCount: Int,
    val labCount: Int,
    val electiveCount: Int,
    val outlinedCount: Int,
    val missingOutlines: Int,
)

fun semesterCurriculumSnapshot(subjects: List<SemesterSubject>): SemesterCurriculumSnapshot {
    val validSubjects = subjects.filter { it.courseCode.isNotBlank() && it.name.isNotBlank() }
    val outlinedCount = validSubjects.count { !it.outline.isNullOrBlank() }
    val totalCredits = validSubjects.sumOf { it.creditHours.coerceAtLeast(0) }
    return SemesterCurriculumSnapshot(
        subjectCount = validSubjects.size,
        totalCredits = totalCredits,
        theoryCount = validSubjects.count { it.subjectType == SubjectType.THEORY },
        labCount = validSubjects.count { it.subjectType == SubjectType.LAB },
        electiveCount = validSubjects.count { it.isElective },
        outlinedCount = outlinedCount,
        missingOutlines = validSubjects.size - outlinedCount,
    )
}
