package com.mbd.cmscommon.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class DatesheetDto(
    val id: String? = null,
    val sessionId: String? = null,
    val semester: Int = 0,
    val defaultStartTime: String? = null,
    val defaultEndTime: String? = null,
    val defaultBuildingId: String? = null,
    val published: Boolean = false,
    val instructions: String? = null,
    val createdBy: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: String? = null,
    val deletedBy: String? = null,
)

@Serializable
data class DatesheetSlotDto(
    val id: String? = null,
    val datesheetId: String? = null,
    val courseCode: String? = null,
    val subjectName: String? = null,
    val examDate: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val buildingId: String? = null,
    val building: String? = null,
    val roomId: String? = null,
    val roomNo: String? = null,
    val invigilatorEmail: String? = null,
    val createdAt: String? = null,
    val createdBy: String? = null,
    val updatedAt: String? = null,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: String? = null,
    val deletedBy: String? = null,
)
