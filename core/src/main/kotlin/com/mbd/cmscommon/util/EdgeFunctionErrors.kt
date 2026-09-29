package com.mbd.cmscommon.util

import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
import java.util.concurrent.CancellationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * Turns a failed Supabase Edge Function call into a typed [CmsException] that carries the server's
 * own explanation.
 *
 * Our functions answer failures with `{ "error": "<user-safe sentence>", "code": "<SHORT_CODE>" }`
 * (see `supabase/functions/_shared/auth.ts`). supabase-kt raises a [RestException] whose `error`
 * is that raw response body and whose `statusCode` is the HTTP status. Without this translation
 * [ErrorClassifier] only sees the status and shows a generic message ("You do not have permission",
 * "service unavailable", ...) instead of e.g. "This session is still active. Deactivate it before
 * archiving and deleting it."
 *
 * Only bodies that follow our contract (a string `error` field) are trusted and shown; anything
 * else -- gateway responses such as `{"code":401,"message":"Invalid JWT"}`, plain text, HTML -- gets
 * a status-based sentence instead, so raw infrastructure text never reaches the user.
 */
object EdgeFunctionErrors {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private val UNSAFE_MARKERS = listOf(
        "http://", "https://", "supabase", "postgrest", "exception", "stacktrace", "apikey", "bearer ", "{", "}",
    )

    /** Builds the exception for an HTTP failure with [status] and response [body]. */
    fun parse(status: Int, body: String?, cause: Throwable? = null): CmsException {
        val parsed = runCatching { json.parseToJsonElement(body.orEmpty()) }.getOrNull() as? JsonObject
        val serverMessage = (parsed?.get("error") as? JsonPrimitive)?.takeIf { it.isString }?.contentOrNull?.trim()
            ?.takeIf { it.isNotBlank() && it.length <= 220 && UNSAFE_MARKERS.none { marker -> it.contains(marker, ignoreCase = true) } }
        val code = (parsed?.get("code") as? JsonPrimitive)?.takeIf { it.isString }?.contentOrNull?.uppercase()

        val kind = kindFor(code, status)
        val message = serverMessage ?: genericFor(status)
        return when (kind) {
            Kind.VALIDATION -> CmsException.Validation(message, cause = cause)
            Kind.AUTH -> CmsException.Auth(message, cause = cause)
            Kind.PERMISSION -> CmsException.Permission(message, cause = cause)
            Kind.NOT_FOUND -> CmsException.NotFound(message, cause = cause)
            Kind.CONFLICT -> CmsException.Conflict(message, cause = cause)
            Kind.RATE_LIMIT -> CmsException.Network(message, cause = cause)
            Kind.UNEXPECTED -> CmsException.Unexpected(message, cause = cause)
        }
    }

    /**
     * The [CmsException] for [error] when it is (or wraps) an edge-function HTTP failure, otherwise
     * null so the caller rethrows the original (offline, timeouts and decoding errors are handled by
     * [ErrorClassifier] as before).
     */
    fun toCmsException(error: Throwable): CmsException? {
        val rest = generateSequence(error) { it.cause }.take(6)
            .firstOrNull { it is RestException && it !is PostgrestRestException } as? RestException ?: return null
        return parse(rest.statusCode, rest.error, rest)
    }

    /** Runs an edge-function call, rethrowing an HTTP failure as a [CmsException] carrying the server's message. */
    suspend fun <T> translate(block: suspend () -> T): T = try {
        block()
    } catch (c: CancellationException) {
        throw c
    } catch (t: Throwable) {
        throw toCmsException(t) ?: t
    }

    private enum class Kind { VALIDATION, AUTH, PERMISSION, NOT_FOUND, CONFLICT, RATE_LIMIT, UNEXPECTED }

    private fun kindFor(code: String?, status: Int): Kind = when (code) {
        "VALIDATION", "WEAK_PASSWORD", "INVALID_EMAIL" -> Kind.VALIDATION
        "UNAUTHORIZED" -> Kind.AUTH
        "FORBIDDEN" -> Kind.PERMISSION
        "NOT_FOUND" -> Kind.NOT_FOUND
        "CONFLICT", "ALREADY_EXISTS", "EMAIL_EXISTS", "SESSION_ACTIVE", "RELATED_RECORDS" -> Kind.CONFLICT
        "RATE_LIMIT" -> Kind.RATE_LIMIT
        "INTERNAL", "DB_FAILED", "AUTH_FAILED", "ARCHIVE_EXPORT_FAILED", "ARCHIVE_UPLOAD_FAILED" -> Kind.UNEXPECTED
        else -> when {
            status == 400 -> Kind.VALIDATION
            status == 401 -> Kind.AUTH
            status == 403 -> Kind.PERMISSION
            status == 404 -> Kind.NOT_FOUND
            status == 409 -> Kind.CONFLICT
            status == 429 -> Kind.RATE_LIMIT
            status >= 500 -> Kind.UNEXPECTED
            else -> Kind.UNEXPECTED
        }
    }

    private fun genericFor(status: Int): String = when {
        status == 400 -> "The request wasn't accepted. Check the details and try again."
        status == 401 -> "Your session has expired. Sign in again."
        status == 403 -> "You don't have permission to do this."
        status == 404 -> "The item you're working on no longer exists. Refresh and try again."
        status == 409 -> "This conflicts with existing data. Refresh and try again."
        status == 429 -> "Too many attempts. Wait a minute and try again."
        status >= 500 -> "The server hit a problem. Try again in a moment."
        else -> "The request couldn't be completed. Try again."
    }
}
