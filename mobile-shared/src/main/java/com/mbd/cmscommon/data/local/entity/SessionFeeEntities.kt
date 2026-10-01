package com.mbd.cmscommon.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** One shift's fee structure: keyed by (sessionId, shift), matching the database. */
@Entity(tableName = "session_fees", primaryKeys = ["sessionId", "shift"])
data class SessionFeeEntity(
    val sessionId: String,
    val shift: String,
    val cadence: String,
    val academicYear: String?,
    val dueDate: String?,
    val paymentNote: String?,
    val createdAt: Long = 0L,
    val createdBy: String? = null,
    val updatedAt: Long = 0L,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val deletedBy: String? = null,
)

@Entity(tableName = "session_fee_heads", indices = [Index(value = ["sessionId", "shift", "position"])])
data class SessionFeeHeadEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val shift: String,
    val label: String,
    val amount: Double,
    val position: Int,
    val createdAt: Long = 0L,
    val createdBy: String? = null,
    val updatedAt: Long = 0L,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val deletedBy: String? = null,
)
