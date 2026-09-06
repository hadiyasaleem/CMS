package com.mbd.cmscommon.util

/**
 * Typed exception hierarchy for errors we raise ourselves (validation, permission checks,
 * conflicts, etc). Throwing one of these instead of a raw [IllegalArgumentException] lets
 * [ErrorClassifier] make a typed decision about severity and user messaging instead of
 * sniffing the message text.
 *
 * Raw throwables from Ktor/Postgrest/etc keep flowing through unchanged — [ErrorClassifier]
 * falls back to the existing string-based [UserFacingErrorHandler] matching for anything that
 * is not a [CmsException].
 */
sealed class CmsException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    /** A field failed validation before any network call was made. [field] is optional, for logging. */
    class Validation(message: String, val field: String? = null, cause: Throwable? = null) :
        CmsException(message, cause)

    /** The current user is not allowed to perform this action. */
    class Permission(message: String = "You do not have permission to perform this action.", cause: Throwable? = null) :
        CmsException(message, cause)

    /** The requested record could not be found. */
    class NotFound(message: String = "The requested information could not be found.", cause: Throwable? = null) :
        CmsException(message, cause)

    /** The action conflicts with existing data (duplicate, stale write, still-referenced row). */
    class Conflict(message: String, cause: Throwable? = null) : CmsException(message, cause)

    /** A connectivity or timeout failure. */
    class Network(message: String = "Unable to connect. Check your internet connection and try again.", cause: Throwable? = null) :
        CmsException(message, cause)

    /** Sign-in/session related failure. */
    class Auth(message: String, cause: Throwable? = null) : CmsException(message, cause)

    /** Anything else we did not anticipate — always CRITICAL severity. */
    class Unexpected(message: String, cause: Throwable? = null) : CmsException(message, cause)
}
