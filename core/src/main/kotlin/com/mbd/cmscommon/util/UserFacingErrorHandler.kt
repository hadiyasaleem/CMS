package com.mbd.cmscommon.util

/**
 * Backward-compatible facade over [ErrorClassifier]. Kept so the ~200 existing call sites of
 * [Throwable.userMessage] do not need to change; the actual classification (typed [CmsException]
 * first, legacy string matching as fallback) now lives in [ErrorClassifier].
 */
object UserFacingErrorHandler {
    fun message(error: Throwable, fallback: String = ErrorClassifier.DEFAULT_FALLBACK): String =
        ErrorClassifier.classify(error, fallback).userMessage
}

fun Throwable.userMessage(fallback: String = ErrorClassifier.DEFAULT_FALLBACK): String =
    UserFacingErrorHandler.message(this, fallback)
