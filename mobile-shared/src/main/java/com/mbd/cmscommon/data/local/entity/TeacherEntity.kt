package com.mbd.cmscommon.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teachers")
data class TeacherEntity(
    @PrimaryKey val teacherId: String,
    val entityId: Long = 0L,
    val name: String,
    val email: String,
    val phone: String?,
    val deptId: String?,
    val designation: String?,
    val qualification: String?,
    val specialization: String?,
    val officeRoom: String?,
    val gender: String?,
    val canApproveLinkRequests: Boolean = false,
    val canEditTimetable: Boolean = false,
    val canSendNotifications: Boolean = false,
    val canManageDatesheets: Boolean = false,
    val status: String,
    val isActive: Boolean = true,
    val archivedAt: Long? = null,
    val createdAt: Long = 0L,
    val createdBy: String? = null,
    val updatedAt: Long = 0L,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val deletedBy: String? = null,
)
