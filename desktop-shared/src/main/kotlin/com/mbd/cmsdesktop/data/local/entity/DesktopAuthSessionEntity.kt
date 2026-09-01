package com.mbd.cmsdesktop.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Durable Supabase session fields. The session is kept in Room, never a JSON settings file. */
@Entity(tableName = "desktop_auth_session")
data class DesktopAuthSessionEntity(
    @PrimaryKey val id: String = DEFAULT_ID,
    val accessToken: String,
    val refreshToken: String,
    val providerRefreshToken: String?,
    val providerToken: String?,
    val expiresIn: Long,
    val tokenType: String,
    val sessionType: String,
    val expiresAtEpochMillis: Long,
    val userId: String?,
    val userAud: String?,
    val userEmail: String?,
) {
    companion object { const val DEFAULT_ID = "default" }
}
