package com.mbd.cmscommon.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String,
    val role: String,
    val teacherId: String?,
    val linkedStudentId: String?,
    val updatedAt: Long = 0L,
)
