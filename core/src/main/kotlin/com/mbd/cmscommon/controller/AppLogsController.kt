package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AppLogRecord
import com.mbd.cmscommon.domain.model.AppLogStatus
import com.mbd.cmscommon.domain.repository.AppLogRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

class AppLogsController(
    private val repository: AppLogRepository,
    scope: CoroutineScope,
) : ScreenController(scope) {

    val logs: StateFlow<List<AppLogRecord>> = repository.observeLogs()
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    init {
        _loading.value = false
    }

    fun refresh() = launch("refresh the app logs") {
        try {
            _loading.value = true
            repository.sync()
        } finally {
            _loading.value = false
        }
    }

    fun updateStatus(logId: String, status: AppLogStatus) = launch("update the log status") {
        repository.updateStatus(logId, status)
    }

    private val _deleting = MutableStateFlow(false)
    val deleting: StateFlow<Boolean> = _deleting.asStateFlow()

    fun deleteAll() = launch("clear the app logs") {
        _deleting.value = true
        try {
            repository.deleteAll()
        } finally {
            _deleting.value = false
        }
    }
}
