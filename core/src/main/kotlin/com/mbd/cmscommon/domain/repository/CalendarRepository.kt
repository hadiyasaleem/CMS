package com.mbd.cmscommon.domain.repository

import com.mbd.cmscommon.domain.model.CalendarEvent

interface CalendarRepository {
    suspend fun getEvents(): List<CalendarEvent>
    suspend fun createEvent(event: CalendarEvent, createdBy: String)
    suspend fun deleteEvent(id: String)
}
