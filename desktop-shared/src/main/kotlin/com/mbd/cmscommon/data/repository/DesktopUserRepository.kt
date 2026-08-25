package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.auth.RoleResolver
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.ProfileDto
import com.mbd.cmscommon.domain.model.UserRole
import com.mbd.cmscommon.domain.repository.UserRepository
import io.github.jan.supabase.postgrest.Postgrest
import java.time.Instant
import java.util.prefs.Preferences
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull

/**
 * Desktop has no Room-backed local cache, so "cached role" here means a small [Preferences] entry
 * (per desktop app, keyed by `cms.desktop.appId`) written on every successful [resolveRole] — just
 * enough to prime the UI instantly on the next launch before [resolveRole] re-validates against
 * Supabase. Unlike mobile's `UserRepositoryImpl`, [getCachedRole] never throws: each app's `Main.kt`
 * calls it unguarded (`as? UserRole.Admin`), so an unresolved/missing cache falls back to a harmless
 * [UserRole.UnlinkedStudent] the role-specific cast simply turns into `null`.
 */
@Singleton
class DesktopUserRepository @Inject constructor(
    private val postgrest: Postgrest,
    private val roleResolver: RoleResolver,
) : UserRepository {

    private val roleCache = MutableStateFlow<UserRole?>(null)
    private val prefs: Preferences by lazy {
        Preferences.userRoot().node("com/mbd/cmsdesktop/${System.getProperty("cms.desktop.appId", "app")}/lastrole")
    }

    override fun observeCurrentUserRole(): Flow<UserRole> = roleCache.filterNotNull()

    override suspend fun getCachedRole(uid: String): UserRole {
        roleCache.value?.let { return it }
        val role = prefs.get(key(uid, "role"), null) ?: return UserRole.UnlinkedStudent(uid)
        val teacherId = prefs.get(key(uid, "teacherId"), null)
        val linkedStudentId = prefs.get(key(uid, "linkedStudentId"), null)
        val resolved = roleResolver.resolveRoleFromEntities(uid, role, teacherId, linkedStudentId) ?: UserRole.UnlinkedStudent(uid)
        roleCache.value = resolved
        return resolved
    }

    override suspend fun resolveRole(uid: String): UserRole {
        val profile = postgrest.from(SupabaseTables.PROFILES).select {
            filter {
                eq("email", uid)
                eq("is_deleted", false)
            }
        }.decodeList<ProfileDto>().firstOrNull() ?: error("No profile found for $uid")

        val linkedStudentId = combineStudentId(profile.linkedSessionId, profile.linkedRoll)
        val teacherEmail = profile.teacherEmail?.takeIf { it.isNotBlank() }
        persistCache(uid, profile.role ?: "", teacherEmail, linkedStudentId)
        val resolved = roleResolver.resolveRoleFromEntities(uid, profile.role ?: "", teacherEmail, linkedStudentId)
            ?: error("Unable to resolve role for $uid")
        roleCache.value = resolved
        return resolved
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
        clearLocalCache()
    }

    override suspend fun clearLocalCache() {
        roleCache.value = null
        prefs.clear()
    }

    private fun persistCache(uid: String, role: String, teacherId: String?, linkedStudentId: String?) {
        prefs.put(key(uid, "role"), role)
        if (teacherId != null) prefs.put(key(uid, "teacherId"), teacherId) else prefs.remove(key(uid, "teacherId"))
        if (linkedStudentId != null) prefs.put(key(uid, "linkedStudentId"), linkedStudentId) else prefs.remove(key(uid, "linkedStudentId"))
    }

    private fun key(uid: String, field: String) = "$uid.$field"

    private fun combineStudentId(sessionId: String?, roll: String?): String? {
        if (sessionId.isNullOrBlank() || roll.isNullOrBlank()) return null
        return "${sessionId}_$roll"
    }
}
