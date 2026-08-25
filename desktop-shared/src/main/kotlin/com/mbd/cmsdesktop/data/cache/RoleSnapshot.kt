package com.mbd.cmsdesktop.data.cache

import com.mbd.cmscommon.domain.model.TeacherPermissions
import com.mbd.cmscommon.domain.model.UserRole
import kotlinx.serialization.Serializable

@Serializable
data class RoleSnapshot(
    val uid: String,
    val role: String,
    val teacherId: String? = null,
    val studentId: String? = null,
    val canApproveLinkRequests: Boolean = false,
    val canEditTimetable: Boolean = false,
    val canSendNotifications: Boolean = false,
    val canManageDatesheets: Boolean = false,
) {
    fun toUserRole(): UserRole? = when (role) {
        "ADMIN" -> UserRole.Admin(uid)
        "TEACHER" -> teacherId?.let {
            UserRole.Teacher(
                uid,
                it,
                TeacherPermissions(canApproveLinkRequests, canEditTimetable, canSendNotifications, canManageDatesheets),
            )
        }
        "LINKED_STUDENT" -> studentId?.let { UserRole.LinkedStudent(uid, it) }
        "UNLINKED_STUDENT" -> UserRole.UnlinkedStudent(uid)
        else -> null
    }

    companion object {
        fun from(role: UserRole): RoleSnapshot = when (role) {
            is UserRole.Admin -> RoleSnapshot(uid = role.uid, role = "ADMIN")
            is UserRole.Teacher -> RoleSnapshot(
                uid = role.uid,
                role = "TEACHER",
                teacherId = role.teacherId,
                canApproveLinkRequests = role.permissions.canApproveLinkRequests,
                canEditTimetable = role.permissions.canEditTimetable,
                canSendNotifications = role.permissions.canSendNotifications,
                canManageDatesheets = role.permissions.canManageDatesheets,
            )
            is UserRole.LinkedStudent -> RoleSnapshot(uid = role.uid, role = "LINKED_STUDENT", studentId = role.studentId)
            is UserRole.UnlinkedStudent -> RoleSnapshot(uid = role.uid, role = "UNLINKED_STUDENT")
        }
    }
}
