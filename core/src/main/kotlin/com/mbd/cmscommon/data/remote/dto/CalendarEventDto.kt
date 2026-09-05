package com.mbd.cmscommon.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CalendarEventDto(
    val id: String? = null,
    val title: String? = null,
    val eventType: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val description: String? = null,
    val venue: String? = null,
    val audience: String? = null,
    val deptId: String? = null,
    val sessionId: String? = null,
    val createdBy: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: String? = null,
    val deletedBy: String? = null,
)
