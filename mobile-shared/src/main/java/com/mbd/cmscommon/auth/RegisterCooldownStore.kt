package com.mbd.cmscommon.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

private val LAST_REGISTER_EMAIL_KEY = stringPreferencesKey("last_register_email")
private val LAST_REGISTER_AT_MILLIS_KEY = longPreferencesKey("last_register_at_millis")

/** Prevents spamming account-registration attempts (and Supabase's confirmation emails) for the same address. */
@Singleton
class RegisterCooldownStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    /** True if [email] already has a pending, unexpired verification email on file. */
    suspend fun isOnCooldown(email: String): Boolean {
        val prefs = dataStore.data.first()
        val lastEmail = prefs[LAST_REGISTER_EMAIL_KEY]
        val lastAt = prefs[LAST_REGISTER_AT_MILLIS_KEY]
        return lastEmail == email && lastAt != null && System.currentTimeMillis() - lastAt < COOLDOWN_MILLIS
    }

    suspend fun recordAttempt(email: String) {
        dataStore.edit {
            it[LAST_REGISTER_EMAIL_KEY] = email
            it[LAST_REGISTER_AT_MILLIS_KEY] = System.currentTimeMillis()
        }
    }

    companion object {
        const val COOLDOWN_MILLIS = 60L * 60L * 1000L
    }
}
