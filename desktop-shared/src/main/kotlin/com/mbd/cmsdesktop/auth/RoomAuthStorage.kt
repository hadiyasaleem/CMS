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
