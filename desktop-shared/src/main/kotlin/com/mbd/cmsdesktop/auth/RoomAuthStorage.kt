package com.mbd.cmsdesktop.auth

import com.mbd.cmsdesktop.data.local.dao.DesktopAuthCodeVerifierDao
import com.mbd.cmsdesktop.data.local.dao.DesktopAuthSessionDao
import com.mbd.cmsdesktop.data.local.entity.DesktopAuthCodeVerifierEntity
import com.mbd.cmsdesktop.data.local.entity.DesktopAuthSessionEntity
import io.github.jan.supabase.auth.CodeVerifierCache
import io.github.jan.supabase.auth.SessionManager as SupabaseSessionManager
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.datetime.Instant
import kotlinx.serialization.json.put

/** Supabase Auth adapters whose only durable backing store is the desktop Room database. */
class RoomAuthSessionManager(private val dao: DesktopAuthSessionDao) : SupabaseSessionManager {
    override suspend fun saveSession(session: UserSession) {
        dao.upsert(
            DesktopAuthSessionEntity(
                accessToken = session.accessToken,
                refreshToken = session.refreshToken,
                providerRefreshToken = session.providerRefreshToken,
                providerToken = session.providerToken,
                expiresIn = session.expiresIn,
                tokenType = session.tokenType,
                sessionType = session.type,
                expiresAtEpochMillis = session.expiresAt.toEpochMilliseconds(),
                userId = session.user?.id,
                userAud = session.user?.aud,
                userEmail = session.user?.email,
            ),
        )
    }

    override suspend fun loadSession(): UserSession? = dao.get()?.let { stored ->
        UserSession(
            accessToken = stored.accessToken,
            refreshToken = stored.refreshToken,
            providerRefreshToken = stored.providerRefreshToken,
            providerToken = stored.providerToken,
            expiresIn = stored.expiresIn,
            tokenType = stored.tokenType,
            type = stored.sessionType,
            expiresAt = Instant.fromEpochMilliseconds(stored.expiresAtEpochMillis),
            user = stored.userId?.let { userId ->
                UserInfo(id = userId, aud = stored.userAud.orEmpty(), email = stored.userEmail)
            },
        )
    }

    override suspend fun deleteSession() = dao.delete()
}

class RoomAuthCodeVerifierCache(private val dao: DesktopAuthCodeVerifierDao) : CodeVerifierCache {
    override suspend fun saveCodeVerifier(codeVerifier: String) =
        dao.upsert(DesktopAuthCodeVerifierEntity(value = codeVerifier))

    override suspend fun loadCodeVerifier(): String? = dao.get()

    override suspend fun deleteCodeVerifier() = dao.delete()
}

/**
 * Keeps the login in a small file of its own, next to (not inside) the Room database. The Room database is a wipeable
 * cache -- it is dropped and rebuilt whenever its schema changes (see DesktopRoomModule) -- and a login stored inside it
 * was lost with it, so every app update or schema change signed everyone out. A session left in the old Room table by a
 * previous version is adopted once and moved into the file.
 */
class FileAuthSessionManager(
    private val file: java.io.File,
    private val legacy: SupabaseSessionManager? = null,
) : SupabaseSessionManager {

    override suspend fun saveSession(session: UserSession) {
        val json = kotlinx.serialization.json.buildJsonObject {
            put("accessToken", session.accessToken)
            put("refreshToken", session.refreshToken)
            session.providerRefreshToken?.let { put("providerRefreshToken", it) }
            session.providerToken?.let { put("providerToken", it) }
            put("expiresIn", session.expiresIn)
            put("tokenType", session.tokenType)
            put("sessionType", session.type)
            put("expiresAtEpochMillis", session.expiresAt.toEpochMilliseconds())
            session.user?.let { u ->
                put("userId", u.id)
                put("userAud", u.aud)
                u.email?.let { put("userEmail", it) }
            }
        }
        file.parentFile?.mkdirs()
        // Write beside the target and swap in one step, so a crash mid-write can never leave a half-written login.
        val tmp = java.io.File(file.parentFile, file.name + ".tmp")
        tmp.writeText(json.toString())
        java.nio.file.Files.move(
            tmp.toPath(), file.toPath(),
            java.nio.file.StandardCopyOption.REPLACE_EXISTING, java.nio.file.StandardCopyOption.ATOMIC_MOVE,
        )
    }

    override suspend fun loadSession(): UserSession? {
        readFile()?.let { return it }
        // First launch after the upgrade: bring the login over from the Room table, if it still has one.
        val old = runCatching { legacy?.loadSession() }.getOrNull() ?: return null
        runCatching { saveSession(old) }
        return old
    }

    override suspend fun deleteSession() {
        runCatching { file.delete() }
        runCatching { legacy?.deleteSession() }
    }

    private fun readFile(): UserSession? = runCatching {
        if (!file.isFile) return null
        val o = kotlinx.serialization.json.Json.parseToJsonElement(file.readText()) as kotlinx.serialization.json.JsonObject
        fun str(k: String) = (o[k] as? kotlinx.serialization.json.JsonPrimitive)?.takeIf { it.isString }?.content
        fun lng(k: String) = (o[k] as? kotlinx.serialization.json.JsonPrimitive)?.content?.toLongOrNull()
        UserSession(
            accessToken = str("accessToken") ?: return null,
            refreshToken = str("refreshToken") ?: return null,
            providerRefreshToken = str("providerRefreshToken"),
            providerToken = str("providerToken"),
            expiresIn = lng("expiresIn") ?: 0L,
            tokenType = str("tokenType") ?: "bearer",
            type = str("sessionType") ?: "",
            expiresAt = Instant.fromEpochMilliseconds(lng("expiresAtEpochMillis") ?: return null),
            user = str("userId")?.let { id -> UserInfo(id = id, aud = str("userAud").orEmpty(), email = str("userEmail")) },
        )
    }.getOrNull()
}
