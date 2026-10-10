package com.mbd.cmscommon.controller

import com.mbd.cmscommon.util.orThrowValidation
import com.mbd.cmscommon.util.requireValid

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
        refresh(fetchRemote = false)
    }

    fun refresh(fetchRemote: Boolean = true) {
        clearError()
        launch("load the calendar") {
            _loading.value = true
            try {
                if (fetchRemote) repo.sync()
                _events.value = calendarQueueSnapshot(repo.getEvents()).events
            } finally {
                _loading.value = false
            }
        }
    }

    /**
     * Adds one or more events in a single busy-guarded operation -- more than one once a calendar
     * form's Department/Semester/Shift/Program audience narrows to several matching classes at once,
     * rather than one exact session (there's no single session that alone represents "every 3rd-semester
     * class"), so each resolved class gets its own event row.
     */
    fun createMany(events: List<CalendarEvent>) {
        if (events.isEmpty()) return
        // Single-flight: set _busy synchronously before launch so a double-tap can't fire two inserts.
        if (_busy.value) return
        _busy.value = true
        launch("add the event") {
            clearError()
            _actionMessage.value = null
            try {
                events.forEach { event ->
                    validationMessage(event).orThrowValidation()
                    repo.createEvent(event, createdBy)
                }
                _events.value = calendarQueueSnapshot(repo.getEvents()).events
                _actionMessage.value = if (events.size > 1) "${events.size} events added to the college calendar." else "Event added to the college calendar."
            } finally {
                _busy.value = false
            }
        }
    }

    fun delete(id: String) {
        if (_busy.value) return
        _busy.value = true
        launch("remove the event") {
            clearError()
            _actionMessage.value = null
            try {
            requireValid(id.isNotBlank()) { "This event has no database ID and cannot be removed safely." }
            requireValid(_events.value.orEmpty().any { it.id == id }) {
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
}
