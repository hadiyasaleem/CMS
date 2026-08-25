package com.mbd.cmscommon.auth

import com.mbd.cmscommon.data.local.dao.TeacherDao
import com.mbd.cmscommon.data.local.dao.UserDao
import com.mbd.cmscommon.data.mapper.TeacherMapper
import com.mbd.cmscommon.domain.model.TeacherPermissions
import com.mbd.cmscommon.domain.model.UserRole
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@Singleton
class RoleResolver @Inject constructor(
    private val userDao: UserDao,
    private val teacherDao: TeacherDao,
) {
    fun observeRole(): Flow<UserRole?> =
        combine(userDao.observeCurrent(), teacherDao.observeActive()) { user, _ ->
            resolveRoleFromEntities(user.uid, user.role, user.teacherId, user.linkedStudentId)
        }

    suspend fun resolveRoleFromEntities(uid: String, role: String, teacherId: String?, linkedStudentId: String?): UserRole? =
        when (role) {
            "STUDENT" -> if (linkedStudentId != null) UserRole.LinkedStudent(uid, linkedStudentId) else UserRole.UnlinkedStudent(uid)
            "TEACHER" -> {
                if (teacherId != null) {
                    val teacherEntity = teacherDao.getById(teacherId)
                    val permissions = teacherEntity?.let { TeacherMapper.entityToDomain(it).permissions } ?: TeacherPermissions()
                    UserRole.Teacher(uid, teacherId, permissions)
                } else {
                    null
                }
            }
            "ADMIN" -> UserRole.Admin(uid)
            else -> null
        }
}
