package com.mbd.cmscommon.domain.model

import java.time.Instant

data class ExamPaperSubmission(
    val submissionId: String,
    val datesheetSlotId: String,
    val offeringId: String,
    val semester: Int,
    val subjectId: String,
    val teacherId: String,
    val storagePath: String,
    val fileName: String,
    val fileSizeBytes: Long? = null,
    val uploadedAt: Instant,
    val mimeType: String? = null,
    val description: String? = null,
    override val createdAt: Instant = Instant.EPOCH,
    override val createdBy: String,
    override val updatedAt: Instant = Instant.EPOCH,
    override val updatedBy: String? = null,
) : BaseEntity()
