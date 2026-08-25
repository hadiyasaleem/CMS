package com.mbd.cmscommon.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProfileDto(
    val id: String? = null,
    val email: String? = null,
    val role: String? = null,
    val teacherEmail: String? = null,
    val linkedSessionId: String? = null,
    val linkedRoll: String? = null,
    val status: String? = null,
    val entityId: Long? = null,
    val createdAt: String? = null,
    val createdBy: String? = null,
    val updatedAt: String? = null,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: String? = null,
    val deletedBy: String? = null,
)
