package com.mbd.cmscommon.auth

import com.mbd.cmscommon.util.LogContext
import com.mbd.cmscommon.util.cmsExceptionHandler
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
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default + cmsExceptionHandler("SessionManager"))

    val accountKey: String?
        get() = auth.currentUserOrNull()?.email?.normalizeEmail()

    val currentUid: String?
        get() = auth.currentUserOrNull()?.id

    suspend fun awaitInitialization(): String? {
        auth.awaitInitialization()
        return accountKey.also { LogContext.accountEmail = it }
    }

    suspend fun signIn(email: String, password: String) {
        auth.signInWith(Email) {
            this.email = email.normalizeEmail()
            this.password = password
        }
        LogContext.accountEmail = accountKey
    }

    suspend fun registerStudent(email: String, password: String) {
        auth.signUpWith(Email, redirectUrl = EMAIL_REDIRECT_URL) {
            this.email = email.normalizeEmail()
            this.password = password
        }
        LogContext.accountEmail = accountKey
    }

    suspend fun sendPasswordReset(email: String) {
        auth.resetPasswordForEmail(email.normalizeEmail())
    }

    fun signOut() {
        LogContext.accountEmail = null
        scope.launch {
            runCatching { auth.signOut() }
        }
    }

    companion object {
        const val EMAIL_REDIRECT_URL = "cms://login-callback"
    }
}
