package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

class StudentTimetableController(
    private val sessionId: String,
    private val timetableRepository: SessionTimetableRepository,
    scope: CoroutineScope,
    /** The student's shift: they see their own shift's periods only. Null (unknown) shows the whole session. */
    shift: Flow<Session?> = flowOf(null),
) : ScreenController(scope) {

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    val periods: StateFlow<List<SessionPeriod>> =
        combine(timetableRepository.observeWeek(sessionId), shift) { periods, own -> periodsForShift(periods, own) }
            .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun refresh() {
        clearError()
        _refreshing.value = true
        launch("refresh your timetable") {
            try {
                timetableRepository.syncSession(sessionId)
            } finally {
                _refreshing.value = false
            }
        }
    }
}
