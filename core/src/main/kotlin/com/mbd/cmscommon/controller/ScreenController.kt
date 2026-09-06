package com.mbd.cmscommon.controller

import com.mbd.cmscommon.util.CmsLog
import com.mbd.cmscommon.util.ErrorClassifier
import com.mbd.cmscommon.util.Severity
import java.util.concurrent.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class ScreenController(protected val scope: CoroutineScope) {
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    protected fun launch(block: suspend () -> Unit) {
        scope.launch {
            try {
                block()
            } catch (c: CancellationException) {
                throw c
            } catch (t: Throwable) {
                _error.value = t.userMessageLogged()
            }
        }
    }

    /**
     * Like [com.mbd.cmscommon.util.userMessage], but also logs to [CmsLog] when the failure is
     * [Severity.CRITICAL]. Many controllers catch a [Throwable] locally (into an `Outcome` or a
     * per-row error `StateFlow`) instead of letting it propagate to [launch]'s own catch above —
     * those local catches bypass the logging [launch] does, so they should call this instead of
     * the plain [com.mbd.cmscommon.util.userMessage] extension to still get it.
     */
    protected fun Throwable.userMessageLogged(fallback: String = "Something went wrong. Please try again."): String {
        val classified = ErrorClassifier.classify(this, fallback)
        if (classified.severity == Severity.CRITICAL) {
            CmsLog.critical(this@ScreenController::class.simpleName ?: "ScreenController", classified.userMessage, this)
        }
        return classified.userMessage
    }

    fun clearError() {
        _error.value = null
    }
}
