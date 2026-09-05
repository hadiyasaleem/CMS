package com.mbd.cmscommon.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "departments")
data class DepartmentEntity(
    @PrimaryKey val deptId: String,
    val entityId: Long = 0L,
    val name: String,
    val code: String,
    val hodEmail: String?,
    val description: String?,
    val createdAt: Long = 0L,
    val createdBy: String? = null,
    val updatedAt: Long = 0L,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val deletedBy: String? = null,
)
