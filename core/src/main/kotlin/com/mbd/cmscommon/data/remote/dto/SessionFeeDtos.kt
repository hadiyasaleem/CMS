package com.mbd.cmscommon.data.remote.dto

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class SessionFeeDto(
    val sessionId: String? = null,
    val shift: String? = null,
    val cadence: String? = null,
    val academicYear: String? = null,
    val dueDate: String? = null,
    val paymentNote: String? = null,
    val updatedBy: String? = null,
    val createdAt: String? = null,
    val createdBy: String? = null,
    val updatedAt: String? = null,
    @EncodeDefault val isDeleted: Boolean = false,
    val deletedAt: String? = null,
    val deletedBy: String? = null,
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class SessionFeeHeadDto(
    val sessionId: String? = null,
    val shift: String? = null,
    val label: String? = null,
    val amount: Double = 0.0,
    val position: Int = 0,
    val createdAt: String? = null,
    val createdBy: String? = null,
    val updatedAt: String? = null,
    val updatedBy: String? = null,
    @EncodeDefault val isDeleted: Boolean = false,
    val deletedAt: String? = null,
    val deletedBy: String? = null,
)

/** The college-wide base fee structure for one shift (see college_fees). */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class CollegeFeeDto(
    val shift: String? = null,
    val cadence: String? = null,
    val academicYear: String? = null,
    val dueDate: String? = null,
    val paymentNote: String? = null,
    val updatedBy: String? = null,
    val createdAt: String? = null,
    val createdBy: String? = null,
    val updatedAt: String? = null,
    @EncodeDefault val isDeleted: Boolean = false,
    val deletedAt: String? = null,
    val deletedBy: String? = null,
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class CollegeFeeHeadDto(
    val shift: String? = null,
    val label: String? = null,
    val amount: Double = 0.0,
    val position: Int = 0,
    val createdAt: String? = null,
    val createdBy: String? = null,
    val updatedAt: String? = null,
    val updatedBy: String? = null,
    @EncodeDefault val isDeleted: Boolean = false,
    val deletedAt: String? = null,
    val deletedBy: String? = null,
)
