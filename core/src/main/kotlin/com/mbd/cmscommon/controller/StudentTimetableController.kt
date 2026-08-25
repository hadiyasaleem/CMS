package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

class StudentTimetableController(
    private val sessionId: String,
    private val timetableRepository: SessionTimetableRepository,
    scope: CoroutineScope,
) : ScreenController(scope) {

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    val periods: StateFlow<List<SessionPeriod>> =
        timetableRepository.observeWeek(sessionId).stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refresh()
    }

    fun refresh() {
        clearError()
        _refreshing.value = true
        launch {
            try {
                timetableRepository.syncSession(sessionId)
            } finally {
                _refreshing.value = false
            }
        }
    }
}
