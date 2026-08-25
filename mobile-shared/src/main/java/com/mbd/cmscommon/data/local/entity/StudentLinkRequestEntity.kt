package com.mbd.cmscommon.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "student_link_requests")
data class StudentLinkRequestEntity(
    @PrimaryKey val requestId: String,
    val requestedByUid: String,
    val sessionIdClaimed: String?,
    val rollNumberClaimed: String?,
    val nameClaimed: String?,
    val cnicClaimed: String?,
    val dobClaimed: String?,
    val universityRollClaimed: String?,
    val registrationNoClaimed: String?,
    val message: String?,
    val status: String,
    val reviewedBy: String?,
    val reviewedAt: Long?,
    val rejectionReason: String?,
    val attemptCount: Int = 0,
    val createdAt: Long = 0L,
    val entityId: Long = 0L,
    val createdBy: String? = null,
    val updatedAt: Long = 0L,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val deletedBy: String? = null,
)
