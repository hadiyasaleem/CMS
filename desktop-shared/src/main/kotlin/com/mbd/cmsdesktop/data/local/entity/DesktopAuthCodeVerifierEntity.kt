package com.mbd.cmsdesktop.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Room-backed PKCE verifier used only while completing an OAuth login. */
@Entity(tableName = "desktop_auth_code_verifier")
data class DesktopAuthCodeVerifierEntity(
    @PrimaryKey val id: String = DEFAULT_ID,
    val value: String,
) {
    companion object { const val DEFAULT_ID = "default" }
}
