package com.mbd.cmscommon.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SessionFeeDto(
    val sessionId: String? = null,
    val cadence: String? = null,
    val academicYear: String? = null,
    val dueDate: String? = null,
    val lateFineNote: String? = null,
    val paymentNote: String? = null,
    val updatedBy: String? = null,
    val createdAt: String? = null,
    val createdBy: String? = null,
    val updatedAt: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: String? = null,
    val deletedBy: String? = null,
)

@Serializable
data class SessionFeeHeadDto(
    val sessionId: String? = null,
    val label: String? = null,
    val amount: Double = 0.0,
    val position: Int = 0,
    val createdAt: String? = null,
    val createdBy: String? = null,
    val updatedAt: String? = null,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: String? = null,
    val deletedBy: String? = null,
)
