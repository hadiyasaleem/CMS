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
                val classified = ErrorClassifier.classify(t)
                _error.value = classified.userMessage
                if (classified.severity == Severity.CRITICAL) {
                    CmsLog.critical(this@ScreenController::class.simpleName ?: "ScreenController", classified.userMessage, t)
                }
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
