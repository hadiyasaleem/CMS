package com.mbd.cmscommon.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rooms")
data class RoomEntity(
    @PrimaryKey val roomId: String,
    val buildingId: String,
    val roomNo: String,
    val name: String?,
    val capacity: Int?,
    val isOffice: Boolean = false,
    val isActive: Boolean = true,
    val createdAt: Long = 0L,
    val createdBy: String? = null,
    val updatedAt: Long = 0L,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val deletedBy: String? = null,
)
