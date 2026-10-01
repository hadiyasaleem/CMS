package com.mbd.cmscommon.auth

import com.mbd.cmscommon.util.CmsException
import com.mbd.cmscommon.util.LogContext
import com.mbd.cmscommon.util.cmsExceptionHandler
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.exception.AuthRestException
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
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.launch

fun String.normalizeEmail(): String = trim().lowercase(Locale.ROOT)

@Singleton
class SessionManager @Inject constructor(
    private val auth: Auth,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default + cmsExceptionHandler("SessionManager"))

    /**
     * [changePassword] re-signs in to check the current password, which would look like a brand-new sign-in to
     * [newlyAuthenticatedAccountKey]; sessions that start before this time are not announced there. Cleared by any
     * real sign-in, registration or sign-out.
     */
    @Volatile private var quietUntil = 0L

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
        .filter { it.isNew && System.currentTimeMillis() >= quietUntil }
        .mapNotNull { it.session.user?.email?.normalizeEmail() }

    suspend fun awaitInitialization(): String? {
        auth.awaitInitialization()
        return accountKey.also { LogContext.accountEmail = it }
    }

    suspend fun signIn(email: String, password: String) {
        quietUntil = 0L
        auth.signInWith(Email) {
            this.email = email.normalizeEmail()
            this.password = password
        }
        LogContext.accountEmail = accountKey
    }

    suspend fun registerStudent(email: String, password: String) {
        quietUntil = 0L
        auth.signUpWith(Email, redirectUrl = EMAIL_REDIRECT_URL) {
            this.email = email.normalizeEmail()
            this.password = password
        }
        LogContext.accountEmail = accountKey
    }

    suspend fun sendPasswordReset(email: String) {
        auth.resetPasswordForEmail(email.normalizeEmail())
    }

    /**
     * Changes the signed-in user's password after checking the current one by signing in with it. A wrong current
     * password is a [CmsException.Validation] saying so, not the generic "email or password is incorrect".
     */
    suspend fun changePassword(currentPassword: String, newPassword: String) {
        val email = accountKey ?: throw CmsException.Auth("You're not signed in. Sign in again.")
        quietUntil = System.currentTimeMillis() + CHANGE_PASSWORD_QUIET_MS
        try {
            try {
                auth.signInWith(Email) {
                    this.email = email
                    this.password = currentPassword
                }
            } catch (c: CancellationException) {
                throw c
            } catch (t: Throwable) {
                if (t.isWrongPassword()) throw CmsException.Validation("Your current password is incorrect.", "currentPassword", t)
                throw t
            }
            auth.updateUser { password = newPassword }
        } finally {
            quietUntil = System.currentTimeMillis() + CHANGE_PASSWORD_QUIET_MS
        }
    }

    fun signOut() {
        quietUntil = 0L
        LogContext.accountEmail = null
        scope.launch {
            // Best-effort: the local session is already cleared; a failed server-side revoke (e.g. offline) must not block signing out.
            runCatching { auth.signOut() }
        }
    }

    private fun Throwable.isWrongPassword(): Boolean {
        val text = generateSequence(this) { it.cause }.joinToString(" ") {
            "${it.message.orEmpty()} ${(it as? AuthRestException)?.errorCode?.value.orEmpty()}"
        }.lowercase(Locale.ROOT)
        return "invalid_credentials" in text || "invalid login credentials" in text || "invalid credentials" in text
    }

    companion object {
        const val EMAIL_REDIRECT_URL = "cms://login-callback"
        private const val CHANGE_PASSWORD_QUIET_MS = 15_000L
    }
}
