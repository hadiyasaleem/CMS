package com.mbd.cmsdesktop.data.repository

import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.domain.model.UserRole
import com.mbd.cmscommon.domain.repository.UserRepository
import com.mbd.cmsdesktop.auth.DesktopRoleResolver
import com.mbd.cmsdesktop.data.cache.DesktopBootstrapSnapshotStore
import io.github.jan.supabase.postgrest.Postgrest
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull

/**
 * Desktop has no Room-backed local cache. [DesktopBootstrapSnapshotStore] persists the last
 * resolved [UserRole] to a small JSON file so the UI can prime instantly on next launch; the real
 * "resolve" work (querying `profiles`/`teachers`/`session_students`) is delegated entirely to
 * [DesktopRoleResolver] rather than being re-implemented here.
 */
@Singleton
class DesktopUserRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val roleResolver: DesktopRoleResolver,
    private val snapshotStore: DesktopBootstrapSnapshotStore,
) : UserRepository {

    private val roleCache = MutableStateFlow<UserRole?>(snapshotStore.readRole())

    override fun observeCurrentUserRole(): Flow<UserRole?> = roleCache

    override suspend fun getCachedRole(uid: String): UserRole {
        val cached = snapshotStore.readRole()
        if (cached == null || cached.uid != uid) return UserRole.UnlinkedStudent(uid)
        return cached
    }

    override suspend fun resolveRole(uid: String): UserRole {
        val role = roleResolver.resolveRole(uid) ?: error("Unable to resolve role for $uid")
        roleCache.value = role
        snapshotStore.writeRole(role)
        return role
    }

    override suspend fun touchLastLogin(uid: String) {
        runCatching {
            postgrest.from(SupabaseTables.PROFILES).update({ set("last_login_at", Instant.now().toString()) }) {
                filter { eq("email", uid) }
            }
        }
    }

    override suspend fun provisionAdmin(uid: String) {
        runCatching {
            postgrest.from(SupabaseTables.PROFILES).update({ set("role", "ADMIN") }) {
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
            postgrest.from(SupabaseTables.PROFILES).delete { filter { eq("email", uid) } }
        }
        clearLocalCache()
    }

    override suspend fun clearLocalCache() {
        roleCache.value = null
        snapshotStore.writeRole(null)
    }
}
