package com.mbd.cmscommon.util

/**
 * For code that isn't a [com.mbd.cmscommon.controller.ScreenController] subclass (ViewModels,
 * bootstrap/sync code) and deliberately swallows a failure with `runCatching { }.getOrNull()` /
 * `.getOrDefault(...)` rather than surfacing it — often correct for best-effort, offline-first
 * work where cached data should keep driving the UI. That swallow should not also mean the
 * failure vanishes entirely: this logs it to [CmsLog] when [Severity.CRITICAL] first, exactly
 * like [com.mbd.cmscommon.controller.ScreenController.userMessageLogged] does for controllers,
 * then behaves like [Result.getOrNull] / [Result.getOrDefault].
 */
fun <T> Result<T>.orLogCritical(tag: String): T? {
    exceptionOrNull()?.let { logIfCritical(tag, it) }
    return getOrNull()
}

fun <T> Result<T>.orLogCritical(tag: String, default: T): T {
    exceptionOrNull()?.let { logIfCritical(tag, it) }
    return getOrDefault(default)
}

/** Same idea as [orLogCritical], for call sites that only care about `Result.isSuccess` (e.g. `AdminDataBootstrapper`'s per-table sync fan-out). */
fun Result<*>.isSuccessLogged(tag: String): Boolean {
    exceptionOrNull()?.let { logIfCritical(tag, it) }
    return isSuccess
}

/**
 * Free-function counterpart to [com.mbd.cmscommon.controller.ScreenController.userMessageLogged]
 * for code that isn't a `ScreenController` subclass (ViewModels) but still catches a `Throwable`
 * directly (rather than via `runCatching`) and wants both the classified message and CmsLog
 * logging for CRITICAL failures. `tag` is explicit here since there's no enclosing controller
 * class name to infer it from.
 */
fun Throwable.userMessageLogged(tag: String, fallback: String = "Something went wrong. Please try again."): String {
    val classified = ErrorClassifier.classify(this, fallback)
    if (classified.severity == Severity.CRITICAL) {
        CmsLog.critical(tag, classified.userMessage, this)
    }
    return classified.userMessage
}

private fun logIfCritical(tag: String, error: Throwable) {
    val classified = ErrorClassifier.classify(error)
    if (classified.severity == Severity.CRITICAL) {
        CmsLog.critical(tag, classified.userMessage, error)
    }
}
