package com.mbd.cmscommon.domain.model

import java.time.Instant

enum class ExamPaperReviewStatus { SUBMITTED, REVIEWED }

data class ExamPaperSubmission(
    val submissionId: String,
    val offeringId: String,
    val subjectId: String,
    val examType: ExamType,
    val teacherId: String,
    val storagePath: String,
    val fileName: String,
    val uploadedAt: Instant,
    val mimeType: String? = null,
    val reviewStatus: ExamPaperReviewStatus = ExamPaperReviewStatus.SUBMITTED,
    val reviewedBy: String? = null,
    val reviewedAt: Instant? = null,
    val teacherNotes: String? = null,
    val keyStoragePath: String? = null,
    override val createdAt: Instant = Instant.EPOCH,
    override val createdBy: String,
    override val updatedAt: Instant = Instant.EPOCH,
    override val updatedBy: String? = null,
) : BaseEntity()
