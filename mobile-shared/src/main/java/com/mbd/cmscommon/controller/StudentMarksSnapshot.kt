package com.mbd.cmscommon.controller

data class StudentMarksSnapshot(
    val rows: List<SubjectMarksRow>,
    val earnedMarks: Int,
    val availableMarks: Int,
    val assessmentsEntered: Int,
    val absentAssessments: Int,
    val fullyRecordedSubjects: Int,
) {
    val percentage: Float? get() = availableMarks.takeIf { it > 0 }?.let { (earnedMarks * 100f) / it }
    val strongestSubject: SubjectMarksRow? get() = rows.filter { it.percentage != null }.maxByOrNull { it.percentage ?: 0f }
}
