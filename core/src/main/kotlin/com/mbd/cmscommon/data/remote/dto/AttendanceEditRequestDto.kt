package com.mbd.cmscommon.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AttendanceEditRequestDto(
    val id: String? = null,
    val sessionId: String? = null,
    val semester: Int = 0,
    val courseCode: String? = null,
    val date: String? = null,
    val rollNumber: String? = null,
    val currentStatus: String? = null,
    val currentIsLate: Boolean? = null,
    val requestedStatus: String? = null,
    val requestedIsLate: Boolean = false,
    val reason: String? = null,
    val status: String? = null,
    val requestedBy: String? = null,
    val reviewedBy: String? = null,
    val requestedAt: String? = null,
    val reviewedAt: String? = null,
)
