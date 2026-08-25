package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.SessionFeeStructure
import com.mbd.cmscommon.domain.repository.SessionFeeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StudentFeeChallanController(
    private val sessionId: String,
    private val feeRepository: SessionFeeRepository,
    scope: CoroutineScope,
) : ScreenController(scope) {

    private val _fee = MutableStateFlow<SessionFeeStructure?>(null)
    val fee: StateFlow<SessionFeeStructure?> = _fee.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    init {
        load()
    }

    private fun load() {
        clearError()
        launch {
            _loading.value = true
            try {
                _fee.value = feeRepository.getSessionFee(sessionId)
            } finally {
                _loading.value = false
            }
        }
    }

    fun refresh() {
        load()
    }
}
