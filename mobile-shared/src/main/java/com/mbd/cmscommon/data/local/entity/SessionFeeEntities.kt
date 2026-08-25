package com.mbd.cmscommon.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "session_fees")
data class SessionFeeEntity(
    @PrimaryKey val sessionId: String,
    val cadence: String,
    val academicYear: String?,
    val dueDate: String?,
    val lateFineNote: String?,
    val paymentNote: String?,
    val entityId: Long = 0L,
    val createdAt: Long = 0L,
    val createdBy: String? = null,
    val updatedAt: Long = 0L,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val deletedBy: String? = null,
)

@Entity(tableName = "session_fee_heads", indices = [Index(value = ["sessionId", "position"])])
data class SessionFeeHeadEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val label: String,
    val amount: Double,
    val position: Int,
    val entityId: Long = 0L,
    val createdAt: Long = 0L,
    val createdBy: String? = null,
    val updatedAt: Long = 0L,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val deletedBy: String? = null,
)
