package com.mbd.cmscommon.domain.model

import java.time.Instant

data class Fine(
    val id: String,
    val sessionId: String,
    val rollNumber: String,
    val category: String,
    val amount: Double,
    val reason: String,
    val issuedBy: String? = null,
    val issuedAt: Instant? = null,
    override val createdAt: Instant = Instant.EPOCH,
    override val createdBy: String? = null,
    override val updatedAt: Instant = Instant.EPOCH,
    override val updatedBy: String? = null,
) : BaseEntity()
