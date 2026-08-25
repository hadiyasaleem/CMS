package com.mbd.cmscommon.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class NotificationDto(
    val entityId: Long? = null,
    val id: String? = null,
    val title: String? = null,
    val body: String? = null,
    val targetRole: String? = null,
    val targetDeptId: String? = null,
    val targetSessionId: String? = null,
    val priority: String? = null,
    val attachmentPath: String? = null,
    val expiresAt: String? = null,
    val createdByEmail: String? = null,
    val createdAt: String? = null,
    val createdBy: String? = null,
    val updatedAt: String? = null,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: String? = null,
    val deletedBy: String? = null,
)
