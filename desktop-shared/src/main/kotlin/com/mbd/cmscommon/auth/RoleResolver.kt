package com.mbd.cmscommon.auth

import com.mbd.cmscommon.domain.model.TeacherPermissions
import com.mbd.cmscommon.domain.model.UserRole
import com.mbd.cmscommon.domain.repository.TeacherRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Desktop has no local Room cache to observe (mobile's `RoleResolver` combines `UserDao`/`TeacherDao`
 * flows) — role resolution here is a plain suspend lookup against [TeacherRepository], called
 * directly by [com.mbd.cmscommon.data.repository.DesktopUserRepository] after every profile fetch.
 */
@Singleton
class RoleResolver @Inject constructor(
    private val teacherRepository: TeacherRepository,
) {
    suspend fun resolveRoleFromEntities(uid: String, role: String, teacherId: String?, linkedStudentId: String?): UserRole? =
        when (role) {
            "STUDENT" -> if (linkedStudentId != null) UserRole.LinkedStudent(uid, linkedStudentId) else UserRole.UnlinkedStudent(uid)
            "TEACHER" -> {
                if (teacherId != null) {
                    val permissions = runCatching { teacherRepository.getTeacher(teacherId)?.permissions }
                        .getOrNull() ?: TeacherPermissions()
                    UserRole.Teacher(uid, teacherId, permissions)
                } else {
                    null
                }
            }
            "ADMIN" -> UserRole.Admin(uid)
            else -> null
        }
}
