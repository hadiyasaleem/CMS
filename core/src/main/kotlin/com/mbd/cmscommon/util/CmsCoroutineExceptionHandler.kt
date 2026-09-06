package com.mbd.cmscommon.util

import kotlinx.coroutines.CoroutineExceptionHandler

/**
 * A [CoroutineExceptionHandler] for scopes that are not already covered by
 * [com.mbd.cmscommon.controller.ScreenController]'s own try/catch (e.g. a top-level singleton's
 * own scope, or a ViewModel's `viewModelScope`) — classifies the throwable and logs it via
 * [CmsLog] when [Severity.CRITICAL], the same way `ScreenController.launch{}` does.
 *
 * Never attach this to `RoomLogSink`'s own scope — a log-sink failure logging itself is the
 * recursion hazard [CmsLog]'s re-entrancy guard exists to prevent, and doubling up here would
 * only mask it.
 */
fun cmsExceptionHandler(tag: String): CoroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
    val classified = ErrorClassifier.classify(throwable)
    if (classified.severity == Severity.CRITICAL) {
        CmsLog.critical(tag, classified.userMessage, throwable)
    }
}
