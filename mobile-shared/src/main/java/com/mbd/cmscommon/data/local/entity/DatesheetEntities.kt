package com.mbd.cmscommon.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "datesheets")
data class DatesheetEntity(
    @PrimaryKey val datesheetId: String,
    val title: String,
    val examType: String,
    val sessionId: String?,
    val published: Boolean = false,
    val instructions: String?,
    val entityId: Long = 0L,
    val createdAt: Long = 0L,
    val createdBy: String? = null,
    val updatedAt: Long = 0L,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val deletedBy: String? = null,
)

@Entity(tableName = "datesheet_slots", indices = [Index(value = ["datesheetId", "examDate"])])
data class DatesheetSlotEntity(
    @PrimaryKey val slotId: String,
    val datesheetId: String,
    val examDate: String,
    val startTime: String?,
    val endTime: String?,
    val durationMinutes: Int?,
    val courseCode: String?,
    val subjectName: String?,
    val roomNo: String?,
    val building: String?,
    val invigilatorEmail: String?,
    val entityId: Long = 0L,
    val createdAt: Long = 0L,
    val createdBy: String? = null,
    val updatedAt: Long = 0L,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val deletedBy: String? = null,
)
