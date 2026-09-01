package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.auth.RoleResolver
import com.mbd.cmscommon.data.local.dao.UserDao
import com.mbd.cmscommon.data.local.entity.UserEntity
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.ProfileDto
import com.mbd.cmscommon.domain.model.UserRole
import com.mbd.cmscommon.domain.repository.UserRepository
import io.github.jan.supabase.postgrest.Postgrest
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull

class UserRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val userDao: UserDao,
    private val roleResolver: RoleResolver,
) : UserRepository {

    override fun observeCurrentUserRole(): Flow<UserRole?> = roleResolver.observeRole()

    override suspend fun getCachedRole(uid: String): UserRole {
        val user = userDao.getByUid(uid) ?: error("No cached user for $uid")
        return roleResolver.resolveRoleFromEntities(user.uid, user.role, user.teacherId, user.linkedStudentId)
            ?: error("Unable to resolve role for $uid")
    }

    override suspend fun resolveRole(uid: String): UserRole {
        val profile = postgrest.from(SupabaseTables.PROFILES).select {
            filter {
                eq("email", uid)
                eq("is_deleted", false)
            }
        }.decodeList<ProfileDto>().firstOrNull() ?: error("No profile found for $uid")

        val linkedStudentId = combineStudentId(profile.linkedSessionId, profile.linkedRoll)
        userDao.deleteOthers(uid)
        val teacherEmail = profile.teacherEmail?.takeIf { it.isNotBlank() }
        userDao.upsert(UserEntity(uid, profile.role ?: "", teacherEmail, linkedStudentId, System.currentTimeMillis()))
        return roleResolver.resolveRoleFromEntities(uid, profile.role ?: "", teacherEmail, linkedStudentId)
            ?: error("Unable to resolve role for $uid")
    }

    override suspend fun touchLastLogin(uid: String) {
        postgrest.from(SupabaseTables.PROFILES).update({ set("last_login_at", Instant.now().toString()) }) {
            filter {
                eq("email", uid)
                eq("is_deleted", false)
            }
        }
    }

    override suspend fun provisionAdmin(uid: String) {
        runCatching {
            postgrest.from(SupabaseTables.PROFILES).update({
                set("role", "ADMIN")
                set("is_deleted", false)
            }) {
                filter { eq("email", uid) }
            }
        }
        resolveRole(uid)
    }

    override suspend fun provisionTeacher(uid: String, teacherId: String) {
        resolveRole(uid)
    }

    override suspend fun provisionUnlinkedStudent(uid: String) {
        resolveRole(uid)
    }

    override suspend fun linkStudent(uid: String, studentId: String) {
        val sessionId = studentId.substringBeforeLast('_')
        val roll = studentId.substringAfterLast('_')
        postgrest.from(SupabaseTables.PROFILES).update({
            set("linked_session_id", sessionId)
            set("linked_roll", roll)
            set("is_deleted", false)
        }) {
            filter { eq("email", uid) }
        }
    }

    override suspend fun unlinkStudent(uid: String) {
        postgrest.from(SupabaseTables.PROFILES).update({
            set("linked_session_id", null as String?)
            set("linked_roll", null as String?)
        }) {
            filter { eq("email", uid) }
        }
    }

    override suspend fun deleteUser(uid: String) {
        runCatching {
            postgrest.from(SupabaseTables.PROFILES).update({ set("is_deleted", true) }) {
                filter { eq("email", uid) }
            }
        }
        userDao.clear()
    }

    override suspend fun clearLocalCache() {
        userDao.clear()
    }

    private fun combineStudentId(sessionId: String?, roll: String?): String? {
        if (sessionId.isNullOrBlank() || roll.isNullOrBlank()) return null
        return "${sessionId}_$roll"
    }
}
