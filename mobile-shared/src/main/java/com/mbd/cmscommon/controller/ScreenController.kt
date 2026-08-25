package com.mbd.cmscommon.controller

import com.mbd.cmscommon.util.userMessage
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
                _error.value = t.userMessage()
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
