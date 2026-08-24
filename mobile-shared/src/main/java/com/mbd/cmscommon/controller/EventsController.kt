package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.CalendarEvent
import com.mbd.cmscommon.domain.repository.CalendarRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EventsController(
    private val repo: CalendarRepository,
    scope: CoroutineScope,
) : ScreenController(scope) {

    private val _events = MutableStateFlow<List<CalendarEvent>?>(null)
    val events: StateFlow<List<CalendarEvent>?> = _events.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        clearError()
        launch {
            _loading.value = true
            try {
                _events.value = repo.getEvents()
            } finally {
                _loading.value = false
            }
        }
    }
}
