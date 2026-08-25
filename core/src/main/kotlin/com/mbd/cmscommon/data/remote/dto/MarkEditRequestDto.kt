package com.mbd.cmscommon.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MarkEditRequestDto(
    val id: String? = null,
    val entityId: Long? = null,
    val sessionId: String? = null,
    val semester: Int = 0,
    val courseCode: String? = null,
    val examType: String? = null,
    val rollNumber: String? = null,
    val currentScore: Int? = null,
    val requestedScore: Int = 0,
    val reason: String? = null,
    val status: String? = null,
    val requestedBy: String? = null,
    val reviewedBy: String? = null,
    val requestedAt: String? = null,
    val reviewedAt: String? = null,
    val createdAt: String? = null,
    val createdBy: String? = null,
    val updatedAt: String? = null,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: String? = null,
    val deletedBy: String? = null,
)
