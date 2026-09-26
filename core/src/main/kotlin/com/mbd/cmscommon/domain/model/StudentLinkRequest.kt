package com.mbd.cmscommon.domain.model

import java.time.Instant

enum class LinkRequestStatus {
    PENDING,
    APPROVED,
    REJECTED,
}

data class StudentLinkRequest(
    val requestId: String,
    val requestedByUid: String,
    val sessionIdClaimed: String?,
    val rollNumberClaimed: String,
    val nameClaimed: String?,
    val cnicClaimed: String?,
    val dobClaimed: String?,
    val universityRollClaimed: String? = null,
    val registrationNoClaimed: String? = null,
    val message: String? = null,
    val status: LinkRequestStatus,
    val reviewedBy: String?,
    val reviewedAt: Instant?,
    val rejectionReason: String? = null,
    val attemptCount: Int = 1,
    override val createdAt: Instant,
    override val createdBy: String? = null,
    override val updatedAt: Instant = Instant.EPOCH,
    override val updatedBy: String? = null,
) : BaseEntity() {
    /** Session ids are "{deptId}_{startYear}"; the shift is not part of the id any more. */
    val deptIdClaimed: String? get() = sessionIdClaimed?.substringBeforeLast('_')
}
