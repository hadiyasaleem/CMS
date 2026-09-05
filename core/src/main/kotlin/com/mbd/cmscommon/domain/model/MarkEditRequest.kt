package com.mbd.cmscommon.domain.model

import java.time.Instant

enum class MarkEditStatus {
    PENDING,
    APPROVED,
    REJECTED,
}

data class MarkEditRequest(
    val id: String,
    val sessionId: String,
    val semester: Int,
    val courseCode: String,
    val examType: ExamType,
    val rollNumber: String,
    val currentScore: Int?,
    val requestedScore: Int,
    val reason: String?,
    val status: MarkEditStatus,
    val requestedBy: String?,
    val reviewedBy: String?,
    val requestedAt: Instant,
    val reviewedAt: Instant?,
    override val createdAt: Instant = Instant.EPOCH,
    override val createdBy: String? = null,
    override val updatedAt: Instant = Instant.EPOCH,
    override val updatedBy: String? = null,
) : BaseEntity()
