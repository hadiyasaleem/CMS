package com.mbd.cmscommon.auth

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.builtin.Email
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

fun String.normalizeEmail(): String = trim().lowercase(Locale.ROOT)

@Singleton
class SessionManager @Inject constructor(
    private val auth: Auth,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val accountKey: String?
        get() = auth.currentUserOrNull()?.email?.normalizeEmail()

    val currentUid: String?
        get() = auth.currentUserOrNull()?.id

    suspend fun awaitInitialization(): String? {
        auth.awaitInitialization()
        return accountKey
    }

    suspend fun signIn(email: String, password: String) {
        auth.signInWith(Email) {
            this.email = email.normalizeEmail()
            this.password = password
        }
    }

    suspend fun registerStudent(email: String, password: String) {
        auth.signUpWith(Email, redirectUrl = EMAIL_REDIRECT_URL) {
            this.email = email.normalizeEmail()
            this.password = password
        }
    }

    suspend fun sendPasswordReset(email: String) {
        auth.resetPasswordForEmail(email.normalizeEmail())
    }

    fun signOut() {
        scope.launch {
            runCatching { auth.signOut() }
        }
    }

    companion object {
        const val EMAIL_REDIRECT_URL = "cms://login-callback"
    }
}
