package com.mbd.cmscommon.domain.model

import java.time.Instant

data class AdministratorAccount(
    val id: String,
    val email: String,
    val status: String,
    val createdAt: Instant?,
    val lastLoginAt: Instant?,
    val createdBy: String? = null,
    val updatedAt: Instant? = null,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: Instant? = null,
    val deletedBy: String? = null,
)
