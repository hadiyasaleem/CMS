package com.mbd.cmscommon.util

import java.util.Locale
import java.util.concurrent.CancellationException

/** What kind of failure this was, independent of the exact message shown to the user. */
enum class ErrorKind {
    VALIDATION, PERMISSION, NOT_FOUND, CONFLICT, NETWORK, AUTH, UNEXPECTED
}

/**
 * Whether a failure of this [ErrorKind] is worth uploading to central logging.
 * Validation mistakes, permission denials, missing records, network hiccups and auth/session
 * expiry are all expected, user-recoverable outcomes — they stay on-screen only. Anything we
 * did not anticipate ([ErrorKind.UNEXPECTED]) is CRITICAL and gets logged.
 */
enum class Severity { EXPECTED, CRITICAL }

data class ClassifiedError(
    val kind: ErrorKind,
    val severity: Severity,
    val userMessage: String,
    val cause: Throwable,
)

/**
 * Classifies a [Throwable] into a typed [ClassifiedError]. Prefers a [CmsException] found
 * anywhere in the cause chain (a typed decision made at the throw site); falls back to the
 * legacy string-matching in [UserFacingErrorHandler] for raw Ktor/Postgrest/etc throwables so
 * existing call sites keep behaving exactly as before.
 */
object ErrorClassifier {

    fun classify(error: Throwable, fallback: String = "Something went wrong. Please try again."): ClassifiedError {
        if (error is CancellationException) throw error

        val causes = generateSequence(error) { it.cause }.take(6).toList()

        val typed = causes.filterIsInstance<CmsException>().firstOrNull()
        if (typed != null) {
            val kind = when (typed) {
                is CmsException.Validation -> ErrorKind.VALIDATION
                is CmsException.Permission -> ErrorKind.PERMISSION
                is CmsException.NotFound -> ErrorKind.NOT_FOUND
                is CmsException.Conflict -> ErrorKind.CONFLICT
                is CmsException.Network -> ErrorKind.NETWORK
                is CmsException.Auth -> ErrorKind.AUTH
                is CmsException.Unexpected -> ErrorKind.UNEXPECTED
            }
            return ClassifiedError(
                kind = kind,
                severity = if (kind == ErrorKind.UNEXPECTED) Severity.CRITICAL else Severity.EXPECTED,
                userMessage = typed.message ?: fallback,
                cause = error,
            )
        }

        val raw = causes.mapNotNull { it.message }.joinToString(" ")
        val normalized = (causes.joinToString(" ") { it::class.qualifiedName ?: "" } + " " + raw).lowercase(Locale.ROOT)

        val (kind, message) = when {
            normalized.contains("invalid login credentials") || normalized.contains("invalid credentials") ->
                ErrorKind.AUTH to "The email or password is incorrect."
            normalized.contains("email not confirmed") ->
                ErrorKind.AUTH to "Confirm your email address before signing in."
            normalized.contains("user already registered") || normalized.contains("already been registered") ->
                ErrorKind.CONFLICT to "An account with this email already exists."
            normalized.contains("password should be") || normalized.contains("weak password") ->
                ErrorKind.VALIDATION to "Choose a stronger password and try again."
            normalized.contains("jwt expired") || normalized.contains("refresh token") || normalized.contains("session expired") ->
                ErrorKind.AUTH to "Your session has expired. Sign in again."
            hasStatus(normalized, 401) || normalized.contains("unauthorized") ->
                ErrorKind.AUTH to "Your session is no longer valid. Sign in again."
            hasStatus(normalized, 403) || normalized.contains("42501") || normalized.contains("row-level security") || normalized.contains("permission denied") ->
                ErrorKind.PERMISSION to "You do not have permission to perform this action."
            normalized.contains("23505") || normalized.contains("duplicate key") || normalized.contains("already exists") ->
                ErrorKind.CONFLICT to "This record already exists. Check the details and try again."
            normalized.contains("23503") || normalized.contains("foreign key") || normalized.contains("still referenced") ->
                ErrorKind.CONFLICT to "This action cannot be completed because related records still exist."
            hasStatus(normalized, 404) || normalized.contains("pgrst116") ->
                ErrorKind.NOT_FOUND to "The requested information could not be found."
            hasStatus(normalized, 409) ->
                ErrorKind.CONFLICT to "This information was changed elsewhere. Refresh and try again."
            hasStatus(normalized, 413) || normalized.contains("payload too large") || normalized.contains("file too large") ->
                ErrorKind.VALIDATION to "The selected file is too large."
            hasStatus(normalized, 429) || normalized.contains("rate limit") || normalized.contains("too many requests") ->
                ErrorKind.NETWORK to "Too many attempts. Please wait a moment and try again."
            normalized.contains("timeout") || normalized.contains("timed out") ->
                ErrorKind.NETWORK to "The request took too long. Check your connection and try again."
            isNetworkFailure(normalized) ->
                ErrorKind.NETWORK to "Unable to connect. Check your internet connection and try again."
            (500..599).any { hasStatus(normalized, it) } || normalized.contains("service unavailable") ->
                ErrorKind.NETWORK to "The service is temporarily unavailable. Please try again shortly."
            isSafeValidationError(error, raw) ->
                ErrorKind.VALIDATION to raw.trim().lineSequence().first().take(180)
            else ->
                ErrorKind.UNEXPECTED to fallback.ifBlank { "Something went wrong. Please try again." }
        }

        return ClassifiedError(
            kind = kind,
            severity = if (kind == ErrorKind.UNEXPECTED) Severity.CRITICAL else Severity.EXPECTED,
            userMessage = message,
            cause = error,
        )
    }

    private fun hasStatus(text: String, status: Int): Boolean {
        val value = status.toString()
        return Regex("(?:status|code|http)[^0-9]{0,8}$value\\b").containsMatchIn(text) ||
            Regex("\\b$value(?:\\s|:|-)").containsMatchIn(text)
    }

    private fun isNetworkFailure(text: String): Boolean {
        val markers = listOf(
            "unknownhost", "unresolvedaddress", "connectexception", "connection refused",
            "network is unreachable", "no route to host", "failed to connect", "socketexception",
            "connection reset", "unable to resolve host",
        )
        return markers.any { text.contains(it) }
    }

    private fun isSafeValidationError(error: Throwable, raw: String): Boolean {
        if ((error !is IllegalArgumentException && error !is IllegalStateException) || raw.isBlank() || raw.length > 180) return false
        val unsafe = listOf(
            "http://", "https://", "supabase", "postgrest", "exception", "request url",
            "apikey", "authorization", "bearer ", "select=", "stacktrace", "{", "}",
        )
        return unsafe.none { raw.contains(it, ignoreCase = true) }
    }
}
