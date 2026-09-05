package com.mbd.cmscommon.domain.model

import java.time.Instant

enum class TeacherStatus {
    ACTIVE,
    DISABLED,
    BANNED,
}

data class TeacherPermissions(
    val canApproveLinkRequests: Boolean = false,
    val canEditTimetable: Boolean = false,
    val canSendNotifications: Boolean = false,
    val canManageDatesheets: Boolean = false,
)

data class Teacher(
    val teacherId: String,
    val name: String,
    val email: String,
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
    val permissions: TeacherPermissions = TeacherPermissions(),
    val status: TeacherStatus = TeacherStatus.ACTIVE,
    val isActive: Boolean = true,
    override val createdAt: Instant,
    override val createdBy: String?,
    override val updatedAt: Instant,
    override val updatedBy: String?,
) : BaseEntity()
