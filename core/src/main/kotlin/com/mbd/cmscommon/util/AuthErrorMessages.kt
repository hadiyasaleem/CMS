package com.mbd.cmscommon.util

import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.exception.AuthWeakPasswordException
import java.util.Locale

/**
 * What a GoTrue (Supabase Auth) failure means in plain words: wrong credentials, unconfirmed email,
 * disabled account, rate limits, expired links, weak passwords, ... Matches the structured error code
 * first (supabase-kt's `AuthRestException.errorCode`, or the code string in a text-only failure), then
 * the well-known phrases GoTrue puts in its messages. Returns null when the failure is not a
 * recognised auth outcome, so the classifier carries on with its other rules.
 */
internal object AuthErrorMessages {

    private const val RATE_LIMIT = "Too many attempts. Wait a minute and try again."
    private const val SESSION_EXPIRED = "Your session has expired. Sign in again."

    /** For a typed supabase-kt auth failure. */
    fun forException(error: AuthRestException): Pair<ErrorKind, String>? {
        val reasons = (error as? AuthWeakPasswordException)?.reasons.orEmpty()
        return messageFor(error.errorCode?.value, "${error.error} ${error.errorDescription}", reasons)
    }

    /**
     * [code] is the GoTrue error code (`over_email_send_rate_limit`, ...) when known; [text] is any
     * message text; [reasons] are the weak-password reasons GoTrue lists (`length`, `characters`, `pwned`).
     */
    fun messageFor(code: String?, text: String, reasons: List<String> = emptyList()): Pair<ErrorKind, String>? {
        val t = text.lowercase(Locale.ROOT)
        // A text-only failure may still mention the code ("code: user_banned"); pick it up so both paths behave alike.
        val c = code?.lowercase(Locale.ROOT).orEmpty().ifEmpty { KNOWN_CODES.firstOrNull { t.contains(it) }.orEmpty() }
        fun has(vararg phrases: String) = phrases.any { t.contains(it) }

        return when {
            c == "invalid_credentials" || has("invalid login credentials", "invalid credentials") ->
                ErrorKind.AUTH to "The email or password is incorrect."
            c == "email_not_confirmed" || has("email not confirmed") ->
                ErrorKind.AUTH to "Confirm your email address before signing in. Check your inbox (and spam or junk folder) for the verification link."
            c == "user_banned" || has("user banned", "user is banned") ->
                ErrorKind.AUTH to "This account has been disabled. Contact the college administrator."
            c == "email_exists" || c == "user_already_exists" || has("user already registered", "already been registered") ->
                ErrorKind.CONFLICT to "An account with this email already exists. Try signing in, or use Forgot password."
            c == "signup_disabled" || has("signups not allowed", "signup is disabled", "signups are disabled") ->
                ErrorKind.AUTH to "New sign-ups are turned off right now. Contact the college administrator."
            c == "email_provider_disabled" ->
                ErrorKind.AUTH to "Signing in with email is turned off right now. Contact the college administrator."
            c == "same_password" || has("should be different from the old password", "same as the old password") ->
                ErrorKind.VALIDATION to "Choose a password that is different from your current one."
            c == "weak_password" || has("weak password", "password should be", "password is too weak") ->
                ErrorKind.VALIDATION to weakPasswordMessage(reasons)
            c == "over_email_send_rate_limit" || has("email rate limit exceeded", "over_email_send_rate_limit") ->
                ErrorKind.NETWORK to "Too many emails were requested recently. Wait a while before asking for another."
            c == "over_request_rate_limit" || has("over_request_rate_limit") ->
                ErrorKind.NETWORK to RATE_LIMIT
            c == "otp_expired" || c == "flow_state_expired" || has("token has expired or is invalid", "link is invalid or has expired", "otp_expired") ->
                ErrorKind.AUTH to "That link or code has expired or was already used. Request a new one."
            c == "email_address_not_authorized" ->
                ErrorKind.VALIDATION to "The system can't send email to this address. Use a different email address."
            c == "email_address_invalid" || has("unable to validate email address", "email address is invalid") ->
                ErrorKind.VALIDATION to "That email address isn't valid. Check it and try again."
            c == "user_not_found" || has("user not found") ->
                ErrorKind.NOT_FOUND to "No account exists for that email address."
            c in SESSION_CODES ->
                ErrorKind.AUTH to SESSION_EXPIRED
            c == "captcha_failed" ->
                ErrorKind.AUTH to "The security check didn't pass. Try again."
            c == "request_timeout" || c == "hook_timeout" || c == "hook_timeout_after_retry" ->
                ErrorKind.NETWORK to "The sign-in service took too long to answer. Try again."
            else -> null
        }
    }

    private val KNOWN_CODES = listOf(
        "invalid_credentials", "email_not_confirmed", "user_banned", "email_exists", "user_already_exists", "signup_disabled",
        "email_provider_disabled", "weak_password", "same_password", "over_email_send_rate_limit", "over_request_rate_limit",
        "otp_expired", "flow_state_expired", "email_address_not_authorized", "email_address_invalid", "user_not_found",
        "bad_jwt", "session_expired", "session_not_found", "refresh_token_not_found", "refresh_token_already_used",
        "no_authorization", "captcha_failed", "request_timeout", "hook_timeout_after_retry", "hook_timeout",
    )

    private val SESSION_CODES = setOf(
        "bad_jwt", "session_expired", "session_not_found", "refresh_token_not_found", "refresh_token_already_used", "no_authorization",
    )

    private fun weakPasswordMessage(reasons: List<String>): String {
        val needs = reasons.mapNotNull {
            when (it.lowercase(Locale.ROOT)) {
                "length" -> "be longer"
                "characters" -> "mix lowercase and uppercase letters, numbers and symbols"
                "pwned" -> "not be a password that has leaked in a data breach"
                else -> null
            }
        }
        return if (needs.isEmpty()) "Choose a stronger password and try again." else "That password is too weak. It must ${needs.joinToString(", ")}."
    }
}
