package com.mbd.cmscommon.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StudentLinkRequestDto(
    val requestId: String? = null,
    val requestedByEmail: String? = null,
    val rollNumberClaimed: String? = null,
    val sessionId: String? = null,
    val nameClaimed: String? = null,
    @SerialName("cnic_bform_claimed") val cnicClaimed: String? = null,
    val dobClaimed: String? = null,
    val universityRollClaimed: String? = null,
    val registrationNoClaimed: String? = null,
    val message: String? = null,
    val status: String? = null,
    val reviewedBy: String? = null,
    val reviewedAt: String? = null,
    val rejectionReason: String? = null,
    val attemptCount: Int = 0,
    val createdAt: String? = null,
    val createdBy: String? = null,
    val updatedAt: String? = null,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: String? = null,
    val deletedBy: String? = null,
)
