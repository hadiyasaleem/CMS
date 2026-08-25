package com.mbd.cmscommon.domain.model

import java.time.Instant

data class ExamPaperSubmission(
    val submissionId: String,
    val offeringId: String,
    val subjectId: String,
    val examType: ExamType,
    val teacherId: String,
    val storagePath: String,
    val fileName: String,
    val uploadedAt: Instant,
    override val entityId: Long = 0L,
    override val createdAt: Instant = Instant.EPOCH,
    override val createdBy: String,
    override val updatedAt: Instant = Instant.EPOCH,
    override val updatedBy: String? = null,
) : BaseEntity()
