package com.mbd.cmscommon.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val notificationId: String,
    val title: String,
    val body: String?,
    val targetRole: String,
    val targetOfferingId: String?,
    val createdByUid: String?,
    val priority: String,
    val targetDeptId: String?,
    val attachmentPath: String?,
    val expiresAt: Long?,
    val createdAt: Long = 0L,
    val entityId: Long = 0L,
    val createdBy: String? = null,
    val updatedAt: Long = 0L,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val deletedBy: String? = null,
)
