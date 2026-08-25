package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.CalendarEvent
import com.mbd.cmscommon.domain.model.calendarQueueSnapshot
import com.mbd.cmscommon.domain.model.validationMessage
import com.mbd.cmscommon.domain.repository.CalendarRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CalendarController(
    private val repo: CalendarRepository,
    private val createdBy: String,
    scope: CoroutineScope,
) : ScreenController(scope) {

    private val _events = MutableStateFlow<List<CalendarEvent>?>(null)
    val events: StateFlow<List<CalendarEvent>?> = _events.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        clearError()
        launch {
            _loading.value = true
            try {
                _events.value = calendarQueueSnapshot(repo.getEvents()).events
            } finally {
                _loading.value = false
            }
        }
    }

    fun create(event: CalendarEvent) = launch {
        clearError()
        _actionMessage.value = null
        validationMessage(event)?.let { throw IllegalArgumentException(it) }
        _busy.value = true
        try {
            repo.createEvent(event, createdBy)
            _events.value = calendarQueueSnapshot(repo.getEvents()).events
            _actionMessage.value = "Event added to the college calendar."
        } finally {
            _busy.value = false
        }
    }

    fun delete(id: String) = launch {
        clearError()
        _actionMessage.value = null
        _busy.value = true
        try {
            require(id.isNotBlank()) { "This event has no database ID and cannot be removed safely." }
            require(_events.value.orEmpty().any { it.id == id }) {
                "This event is no longer in the calendar. Refresh and try again."
            }
            repo.deleteEvent(id)
            _events.value = calendarQueueSnapshot(repo.getEvents()).events
            _actionMessage.value = "Event removed from the college calendar."
        } finally {
            _busy.value = false
        }
    }
}
