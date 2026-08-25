package com.mbd.cmscommon.util

import java.util.Locale
import java.util.concurrent.CancellationException
import kotlin.sequences.generateSequence

object UserFacingErrorHandler {
    fun message(error: Throwable, fallback: String = "Something went wrong. Please try again."): String {
        if (error is CancellationException) throw error

        val causes = generateSequence(error) { it.cause }.take(6).toList()
        val raw = causes.mapNotNull { it.message }.joinToString(" ")
        val normalized = (causes.joinToString(" ") { it::class.qualifiedName ?: "" } + " " + raw).lowercase(Locale.ROOT)

        return when {
            normalized.contains("invalid login credentials") || normalized.contains("invalid credentials") ->
                "The email or password is incorrect."
            normalized.contains("email not confirmed") -> "Confirm your email address before signing in."
            normalized.contains("user already registered") || normalized.contains("already been registered") ->
                "An account with this email already exists."
            normalized.contains("password should be") || normalized.contains("weak password") ->
                "Choose a stronger password and try again."
            normalized.contains("jwt expired") || normalized.contains("refresh token") || normalized.contains("session expired") ->
                "Your session has expired. Sign in again."
            hasStatus(normalized, 401) || normalized.contains("unauthorized") -> "Your session is no longer valid. Sign in again."
            hasStatus(normalized, 403) || normalized.contains("42501") || normalized.contains("row-level security") || normalized.contains("permission denied") ->
                "You do not have permission to perform this action."
            normalized.contains("23505") || normalized.contains("duplicate key") || normalized.contains("already exists") ->
                "This record already exists. Check the details and try again."
            normalized.contains("23503") || normalized.contains("foreign key") || normalized.contains("still referenced") ->
                "This action cannot be completed because related records still exist."
            hasStatus(normalized, 404) || normalized.contains("pgrst116") -> "The requested information could not be found."
            hasStatus(normalized, 409) -> "This information was changed elsewhere. Refresh and try again."
            hasStatus(normalized, 413) || normalized.contains("payload too large") || normalized.contains("file too large") ->
                "The selected file is too large."
            hasStatus(normalized, 429) || normalized.contains("rate limit") || normalized.contains("too many requests") ->
                "Too many attempts. Please wait a moment and try again."
            normalized.contains("timeout") || normalized.contains("timed out") ->
                "The request took too long. Check your connection and try again."
            isNetworkFailure(normalized) -> "Unable to connect. Check your internet connection and try again."
            (500..599).any { hasStatus(normalized, it) } || normalized.contains("service unavailable") ->
                "The service is temporarily unavailable. Please try again shortly."
            isSafeValidationError(error, raw) -> raw.trim().lineSequence().first().take(180)
            else -> fallback.ifBlank { "Something went wrong. Please try again." }
        }
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

fun Throwable.userMessage(fallback: String = "Something went wrong. Please try again."): String =
    UserFacingErrorHandler.message(this, fallback)
