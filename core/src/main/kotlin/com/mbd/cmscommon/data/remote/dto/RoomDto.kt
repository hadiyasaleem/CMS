package com.mbd.cmscommon.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RoomDto(
    val roomId: String? = null,
    val buildingId: String? = null,
    val roomNo: String? = null,
    val name: String? = null,
    val capacity: Int? = null,
    val isOffice: Boolean = false,
    val isActive: Boolean = true,
    val createdAt: String? = null,
    val createdBy: String? = null,
    val updatedAt: String? = null,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: String? = null,
    val deletedBy: String? = null,
)
