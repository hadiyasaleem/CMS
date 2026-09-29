package com.mbd.cmscommon.util

import io.github.jan.supabase.auth.exception.AuthRestException
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

/**
 * [reference] is a short, deterministic code (e.g. `A1B2`) set only for [ErrorKind.UNEXPECTED]
 * failures: it is appended to [userMessage] and written to [CmsLog] with the full throwable, so a
 * user can quote it and support can find the exact failure.
 */
data class ClassifiedError(
    val kind: ErrorKind,
    val severity: Severity,
    val userMessage: String,
    val cause: Throwable,
    val reference: String? = null,
)

/**
 * Classifies a [Throwable] into a typed [ClassifiedError]. Order of precedence: a [CmsException]
 * anywhere in the cause chain (a typed decision made at the throw site); then a recognised Postgres
 * error ([PostgresErrorParser]: `RAISE EXCEPTION` text, unique/foreign-key/check/not-null
 * violations, RLS denials) turned into plain words by [ConstraintMessages]; then the legacy
 * string-matching for raw Ktor/GoTrue/etc throwables. A truly unrecognised failure gets the
 * caller's action-aware `fallback` plus a reference code.
 */
object ErrorClassifier {

    const val DEFAULT_FALLBACK = "Something went wrong. Please try again."

    /** "Couldn't save the teacher." for an action phrase like "save the teacher"; [DEFAULT_FALLBACK] when there is none. */
    fun fallbackFor(action: String?): String = action?.takeIf { it.isNotBlank() }?.let { "Couldn't ${it.trim().trimEnd('.')}." } ?: DEFAULT_FALLBACK

    fun classify(error: Throwable, fallback: String = DEFAULT_FALLBACK): ClassifiedError {
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
            val text = typed.message ?: fallback
            return if (kind == ErrorKind.UNEXPECTED) {
                unexpected(error, text)
            } else {
                ClassifiedError(kind, Severity.EXPECTED, text, error)
            }
        }

        causes.filterIsInstance<AuthRestException>().firstOrNull()?.let { auth ->
            AuthErrorMessages.forException(auth)?.let { (kind, message) ->
                return ClassifiedError(kind, Severity.EXPECTED, message, error)
            }
        }

        PostgresErrorParser.parse(error)?.let { pg ->
            postgresMessage(pg)?.let { (kind, message) ->
                return ClassifiedError(kind, Severity.EXPECTED, message, error)
            }
        }

        val raw = causes.mapNotNull { it.message }.joinToString(" ")
        val normalized = (causes.joinToString(" ") { it::class.qualifiedName ?: "" } + " " + raw).lowercase(Locale.ROOT)

        AuthErrorMessages.messageFor(code = null, text = normalized)?.let { (kind, message) ->
            return ClassifiedError(kind, Severity.EXPECTED, message, error)
        }

        val (kind, message) = when {
            normalized.contains("invalid login credentials") || normalized.contains("invalid credentials") ->
                ErrorKind.AUTH to "The email or password is incorrect."
            normalized.contains("email not confirmed") ->
                ErrorKind.AUTH to "Confirm your email address before signing in."
            normalized.contains("user already registered") || normalized.contains("already been registered") ->
                ErrorKind.CONFLICT to "An account with this email already exists."
            normalized.contains("password should be") || normalized.contains("weak password") ->
                ErrorKind.VALIDATION to "Choose a stronger password and try again."
            normalized.contains("jwt expired") || normalized.contains("refresh token") || normalized.contains("session expired") ||
                normalized.contains("anonymous access is disabled") ->
                ErrorKind.AUTH to "Your session has expired. Sign in again."
            hasStatus(normalized, 401) || normalized.contains("unauthorized") ->
                ErrorKind.AUTH to "Your session is no longer valid. Sign in again."
            hasStatus(normalized, 403) || normalized.contains("42501") || normalized.contains("row-level security") || normalized.contains("permission denied") ->
                ErrorKind.PERMISSION to "You do not have permission to perform this action."
            normalized.contains("23505") || normalized.contains("duplicate key") || normalized.contains("already exists") ->
                ErrorKind.CONFLICT to "This record already exists. Check the details and try again."
            normalized.contains("23503") || normalized.contains("foreign key") || normalized.contains("still referenced") ->
                ErrorKind.CONFLICT to "This action cannot be completed because related records still exist."
            normalized.contains("already booked overlapping") || normalized.contains("already has an overlapping") || normalized.contains("already booked for an overlapping") ->
                ErrorKind.CONFLICT to raw.trim().lineSequence().first().take(180)
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
                return unexpected(error, fallback)
        }

        return ClassifiedError(kind = kind, severity = Severity.EXPECTED, userMessage = message, cause = error)
    }

    /** A failure nobody anticipated: the action-aware [text] plus a reference code that is also logged. */
    private fun unexpected(error: Throwable, text: String): ClassifiedError {
        val reference = referenceFor(error)
        return ClassifiedError(
            kind = ErrorKind.UNEXPECTED,
            severity = Severity.CRITICAL,
            userMessage = "${text.ifBlank { DEFAULT_FALLBACK }} (Ref $reference)",
            cause = error,
            reference = reference,
        )
    }

    /** Same failure => same code, so repeated reports of one problem can be grouped. */
    private fun referenceFor(error: Throwable): String {
        val basis = (error::class.qualifiedName ?: "") + "|" + (error.message ?: "").take(200)
        return String.format(Locale.ROOT, "%04X", basis.hashCode() and 0xFFFF)
    }

    private fun postgresMessage(pg: PostgresError): Pair<ErrorKind, String>? = when (pg.code) {
        "P0001" -> safeRaisedMessage(pg.message)?.let { text ->
            val kind = when {
                PERMISSION_HINTS.any { text.contains(it, ignoreCase = true) } -> ErrorKind.PERMISSION
                text.contains("no longer exists", ignoreCase = true) -> ErrorKind.NOT_FOUND
                CONFLICT_HINTS.any { text.contains(it, ignoreCase = true) } -> ErrorKind.CONFLICT
                else -> ErrorKind.VALIDATION
            }
            kind to text
        }
        "23505" -> ErrorKind.CONFLICT to ConstraintMessages.uniqueViolation(pg)
        "23503" -> ErrorKind.CONFLICT to ConstraintMessages.foreignKeyViolation(pg)
        "23514" -> ErrorKind.VALIDATION to ConstraintMessages.checkViolation(pg)
        "23502" -> ErrorKind.VALIDATION to ConstraintMessages.notNullViolation(pg)
        "22001" -> ErrorKind.VALIDATION to ConstraintMessages.tooLong(pg)
        "22P02", "22007", "22008", "22003" -> ErrorKind.VALIDATION to ConstraintMessages.invalidFormat()
        "42501" -> ErrorKind.PERMISSION to ConstraintMessages.permissionDenied(pg)
        // Transient database conditions: the same request usually works a moment later.
        "40001", "40P01" -> ErrorKind.CONFLICT to "Someone else changed this at the same time. Refresh and try again."
        "53300", "53400", "08000", "08003", "08006", "57P01", "57P03" ->
            ErrorKind.NETWORK to "The server is temporarily unavailable. Try again in a moment."
        else -> null
    }

    /**
     * `RAISE EXCEPTION` text is written by us for end users, so it is shown as-is (first line only,
     * capped) -- unless it contains anything that looks like an internal detail, in which case the
     * caller falls through to the generic path.
     */
    private fun safeRaisedMessage(message: String): String? {
        val line = message.trim().lineSequence().firstOrNull()?.trim().orEmpty()
        if (line.isBlank() || line.length > 220) return null
        return if (UNSAFE_MARKERS.none { line.contains(it, ignoreCase = true) }) line else null
    }

    private val PERMISSION_HINTS = listOf("don't have permission", "only an admin", "set by an admin", "ask an admin")
    private val CONFLICT_HINTS = listOf("already", "overlapping", "is full", "no longer", "still has", "still have")
    private val UNSAFE_MARKERS = listOf(
        "http://", "https://", "supabase", "postgrest", "exception", "request url",
        "apikey", "authorization", "bearer ", "select=", "stacktrace", "{", "}",
    )

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
