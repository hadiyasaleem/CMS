package com.mbd.cmscommon.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class FineDto(
    val id: String? = null,
    val sessionId: String? = null,
    val rollNumber: String? = null,
    val category: String? = null,
    val amount: Double = 0.0,
    val reason: String? = null,
    val issuedBy: String? = null,
    val issuedAt: String? = null,
    val createdAt: String? = null,
    val createdBy: String? = null,
    val updatedAt: String? = null,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: String? = null,
    val deletedBy: String? = null,
)
