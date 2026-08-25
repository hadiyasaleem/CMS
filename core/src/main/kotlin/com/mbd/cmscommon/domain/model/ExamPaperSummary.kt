package com.mbd.cmscommon.domain.model

import java.time.Instant

data class ExamPaperSummary(
    val totalSubmissions: Int,
    val selectedTypeSubmissions: Int,
    val coveredExamTypes: Int,
    val latestUpload: Instant?,
)

fun examPaperSummary(submissions: List<ExamPaperSubmission>, selectedType: ExamType): ExamPaperSummary =
    ExamPaperSummary(
        totalSubmissions = submissions.size,
        selectedTypeSubmissions = submissions.count { it.examType == selectedType },
        coveredExamTypes = submissions.map { it.examType }.distinct().size,
        latestUpload = submissions.maxOfOrNull { it.uploadedAt },
    )
