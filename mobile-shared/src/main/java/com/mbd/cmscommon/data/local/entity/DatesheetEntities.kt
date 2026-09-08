package com.mbd.cmscommon.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "datesheets")
data class DatesheetEntity(
    @PrimaryKey val datesheetId: String,
    val sessionId: String,
    val semester: Int,
    val defaultStartTime: String?,
    val defaultEndTime: String?,
    val defaultBuildingId: String?,
    val published: Boolean = false,
    val instructions: String?,
    val createdAt: Long = 0L,
    val createdBy: String? = null,
    val updatedAt: Long = 0L,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val deletedBy: String? = null,
)

@Entity(
    tableName = "datesheet_slots",
    indices = [Index(value = ["datesheetId", "examDate"]), Index(value = ["datesheetId", "courseCode"])],
)
data class DatesheetSlotEntity(
    @PrimaryKey val slotId: String,
    val datesheetId: String,
    val courseCode: String,
    val subjectName: String,
    val examDate: String?,
    val startTime: String?,
    val endTime: String?,
    val buildingId: String?,
    val building: String?,
    val roomId: String?,
    val roomNo: String?,
    val invigilatorEmail: String?,
    val createdAt: Long = 0L,
    val createdBy: String? = null,
    val updatedAt: Long = 0L,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val deletedBy: String? = null,
)
