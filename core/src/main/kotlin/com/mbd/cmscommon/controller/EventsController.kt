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
                _events.value = repo.getEvents()
            } finally {
                _loading.value = false
            }
        }
    }

    /** Adds one or more events in a single busy-guarded operation -- more than one once a calendar
     * form's Department/Semester/Shift/Program audience narrows to several matching classes at once. */
    fun createEvent(events: List<CalendarEvent>, createdBy: String) {
        if (events.isEmpty()) return
        if (_busy.value) return
        _busy.value = true // set synchronously before launch so the guard actually blocks a double-tap
        launch("add the event") {
            clearError()
            _actionMessage.value = null
            try {
                events.forEach { repo.createEvent(it, createdBy) }
                _events.value = repo.getEvents()
                _actionMessage.value = if (events.size > 1) "${events.size} events added." else "Event added."
            } finally {
                _busy.value = false
            }
        }
    }

    fun deleteEvent(id: String) {
        if (_busy.value) return
        _busy.value = true // set synchronously before launch so the guard actually blocks a double-tap
        launch("remove the event") {
            clearError()
            _actionMessage.value = null
            try {
                repo.deleteEvent(id)
                _events.value = repo.getEvents()
                _actionMessage.value = "Event removed."
            } finally {
                _busy.value = false
            }
        }
    }
}
