package com.mbd.cmscommon.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TeacherDto(
    val entityId: Long? = null,
    val email: String? = null,
    val name: String? = null,
    val phone: String? = null,
    val deptId: String? = null,
    val designation: String? = null,
    val qualification: String? = null,
    val specialization: String? = null,
    val officeRoom: String? = null,
    val gender: String? = null,
    val authUid: String? = null,
    val isAdmin: Boolean = false,
    val isHod: Boolean = false,
    val photoPath: String? = null,
    val canApproveLinkRequests: Boolean = false,
    val canEditTimetable: Boolean = false,
    val canSendNotifications: Boolean = false,
    val canManageDatesheets: Boolean = false,
    val status: String? = null,
    val isActive: Boolean = true,
    val createdAt: String? = null,
    val createdBy: String? = null,
    val updatedAt: String? = null,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: String? = null,
    val deletedBy: String? = null,
)
