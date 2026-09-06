package com.mbd.cmscommon.auth

import com.mbd.cmscommon.util.LogContext
import com.mbd.cmscommon.util.cmsExceptionHandler
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.mapNotNull
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

    /**
     * Emits the account key whenever a *new* session becomes authenticated -- sign-in, sign-up,
     * or an external session import (`SessionSource.External`, which is exactly what
     * `handleDeeplinks()` uses for the "cms://login-callback" email-verification link) -- but not
     * for the session Supabase restores from local storage on ordinary app startup.
     *
     * A direct call to [signIn]/[registerStudent] already knows its own account key synchronously,
     * so callers there don't need this. It exists for call sites with no such call to hook into:
     * the student app's MainActivity finishes the email-verification deep link entirely inside the
     * Supabase SDK (see handleDeeplinks in MainActivity.kt), so nothing else ever learns a new
     * session appeared unless something observes [Auth.sessionStatus] for it.
     */
    val newlyAuthenticatedAccountKey: Flow<String> = auth.sessionStatus
        .filterIsInstance<SessionStatus.Authenticated>()
        .filter { it.isNew }
        .mapNotNull { it.session.user?.email?.normalizeEmail() }

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
