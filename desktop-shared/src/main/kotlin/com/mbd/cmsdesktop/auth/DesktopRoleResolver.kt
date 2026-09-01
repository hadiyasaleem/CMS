package com.mbd.cmsdesktop.auth

import com.mbd.cmscommon.data.remote.dto.ProfileDto
import com.mbd.cmscommon.data.remote.dto.TeacherDto
import com.mbd.cmscommon.domain.model.TeacherPermissions
import com.mbd.cmscommon.domain.model.UserRole
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resolves the current server role during sign-in. The Room-backed [UserRepository] persists the
 * resolved role and the mobile-compatible repository layer serves subsequent cached reads.
 */
@Singleton
class DesktopRoleResolver @Inject constructor(
    private val postgrest: Postgrest,
) {
    suspend fun resolveRole(uid: String): UserRole? {
        val profile = postgrest.from("profiles").select {
            filter { eq("email", uid) }
        }.decodeList<ProfileDto>().firstOrNull() ?: return null

        return when (profile.role) {
            "STUDENT" -> {
                val linkedStudentId = combineStudentId(profile.linkedSessionId, profile.linkedRoll)
                if (linkedStudentId != null) UserRole.LinkedStudent(uid, linkedStudentId) else UserRole.UnlinkedStudent(uid)
            }
            "TEACHER" -> {
                val teacherEmail = profile.teacherEmail?.trim()?.takeIf { it.isNotBlank() } ?: return null
                val teacher = postgrest.from("teachers").select {
                    filter { eq("email", teacherEmail) }
                }.decodeList<TeacherDto>().firstOrNull()
                val permissions = if (teacher != null) {
                    TeacherPermissions(
                        canApproveLinkRequests = teacher.canApproveLinkRequests,
                        canEditTimetable = teacher.canEditTimetable,
                        canSendNotifications = teacher.canSendNotifications,
                    )
                } else {
                    TeacherPermissions()
                }
                UserRole.Teacher(uid, teacherEmail, permissions)
            }
            "ADMIN" -> UserRole.Admin(uid)
            else -> null
        }
    }

    private fun combineStudentId(sessionId: String?, roll: String?): String? {
        if (sessionId.isNullOrBlank() || roll.isNullOrBlank()) return null
        return "${sessionId}_$roll"
    }
}
