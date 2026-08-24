package com.mbd.cmscommon.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "calendar_events", indices = [Index(value = ["startDate"])])
data class CalendarEventEntity(
    @PrimaryKey val eventId: String,
    val title: String,
    val eventType: String,
    val startDate: String,
    val endDate: String?,
    val startTime: String?,
    val endTime: String?,
    val description: String?,
    val venue: String?,
    val audience: String,
    val deptId: String?,
    val sessionId: String?,
    val entityId: Long = 0L,
    val createdAt: Long = 0L,
    val createdBy: String? = null,
    val updatedAt: Long = 0L,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val deletedBy: String? = null,
)

@Entity(tableName = "fines", indices = [Index(value = ["sessionId", "rollNumber"])])
data class FineEntity(
    @PrimaryKey val fineId: String,
    val sessionId: String,
    val rollNumber: String,
    val category: String,
    val amount: Double,
    val reason: String?,
    val issuedBy: String?,
    val issuedAt: Long?,
    val entityId: Long = 0L,
    val createdAt: Long = 0L,
    val createdBy: String? = null,
    val updatedAt: Long = 0L,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val deletedBy: String? = null,
)

@Entity(
    tableName = "mark_edit_requests",
    indices = [
        Index(value = ["sessionId", "courseCode", "examType", "status", "rollNumber"]),
        Index(value = ["sessionId", "courseCode", "examType", "status", "updatedAt", "entityId"]),
        Index(value = ["status", "requestedAt"]),
        Index(value = ["status", "updatedAt", "entityId"]),
    ],
)
data class MarkEditRequestEntity(
    @PrimaryKey val requestId: String,
    val sessionId: String,
    val semester: Int,
    val courseCode: String,
    val examType: String,
    val rollNumber: String,
    val currentScore: Int?,
    val requestedScore: Int,
    val reason: String?,
    val status: String,
    val requestedBy: String,
    val reviewedBy: String?,
    val requestedAt: Long,
    val reviewedAt: Long?,
    val entityId: Long = 0L,
    val createdAt: Long = 0L,
    val createdBy: String? = null,
    val updatedAt: Long = 0L,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val deletedBy: String? = null,
)
