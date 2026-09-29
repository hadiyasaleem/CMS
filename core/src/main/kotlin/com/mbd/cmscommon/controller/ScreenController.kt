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

    /**
     * Runs [block] and routes any failure to [error]. [action] is a short verb phrase in lower case
     * ("save the teacher", "delete the department"): when the failure is one we cannot classify, the
     * user sees "Couldn't save the teacher. (Ref A1B2)" instead of a bare "Something went wrong".
     * Failures we *can* classify (validation, conflicts, database constraints, network, ...) show
     * their specific message regardless.
     */
    protected fun launch(action: String? = null, block: suspend () -> Unit) {
        scope.launch {
            try {
                block()
            } catch (c: CancellationException) {
                throw c
            } catch (t: Throwable) {
                _error.value = t.userMessageLogged(ErrorClassifier.fallbackFor(action))
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
    protected fun Throwable.userMessageLogged(fallback: String = ErrorClassifier.DEFAULT_FALLBACK): String {
        val classified = ErrorClassifier.classify(this, fallback)
        if (classified.severity == Severity.CRITICAL) {
            CmsLog.critical(this@ScreenController::class.simpleName ?: "ScreenController", classified.userMessage, this)
        }
        return classified.userMessage
    }

    fun clearError() {
        _error.value = null
    }

    /** Shows an already-worded message (e.g. a [com.mbd.cmscommon.util.FailureSummary] that names what failed) as the screen's error. */
    protected fun showError(message: String?) {
        _error.value = message
    }

    /** For failures that happen outside [launch], e.g. a platform export step run by the screen. */
    fun reportFailure(t: Throwable, fallback: String) {
        _error.value = t.userMessageLogged(fallback)
    }
}
