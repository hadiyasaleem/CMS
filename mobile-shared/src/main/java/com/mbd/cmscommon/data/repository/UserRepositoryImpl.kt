package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.util.CmsException
import com.mbd.cmscommon.util.orLogCritical
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
        val user = userDao.getByUid(uid) ?: throw CmsException.Auth("Your saved sign-in details are missing. Please sign in again.")
        return roleResolver.resolveRoleFromEntities(user.uid, user.role, user.teacherId, user.linkedStudentId)
            ?: throw CmsException.Auth("Your account has no role assigned yet. Contact an administrator.")
    }

    override suspend fun resolveRole(uid: String): UserRole {
        val profile = postgrest.from(SupabaseTables.PROFILES).select {
            filter {
                eq("email", uid)
                eq("is_deleted", false)
            }
        }.decodeList<ProfileDto>().firstOrNull()
            ?: throw CmsException.NotFound("No CMS profile exists for this account. Contact an administrator to set it up.")

        val linkedStudentId = combineStudentId(profile.linkedSessionId, profile.linkedRoll)
        userDao.deleteOthers(uid)
        val teacherEmail = profile.teacherEmail?.takeIf { it.isNotBlank() }
        userDao.upsert(UserEntity(uid, profile.role ?: "", teacherEmail, linkedStudentId, System.currentTimeMillis()))
        return roleResolver.resolveRoleFromEntities(uid, profile.role ?: "", teacherEmail, linkedStudentId)
            ?: throw CmsException.Auth("Your account has no role assigned yet. Contact an administrator.")
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
        }.orLogCritical("UserRepositoryImpl.provisionAdmin") // resolveRole below reports the failure if the profile was not promoted
        resolveRole(uid)
    }

    override suspend fun provisionTeacher(uid: String, teacherId: String) {
        resolveRole(uid)
    }

    override suspend fun provisionUnlinkedStudent(uid: String) {
        resolveRole(uid)
    }

    override suspend fun deleteUser(uid: String) {
        runCatching {
            postgrest.from(SupabaseTables.PROFILES).update({ set("is_deleted", true) }) {
                filter { eq("email", uid) }
            }
        }.orLogCritical("UserRepositoryImpl.deleteUser") // best-effort remote soft-delete; the local session is cleared either way
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
