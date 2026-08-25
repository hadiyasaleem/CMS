package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.data.mapper.DesktopCalendarMapper
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.CalendarEventDto
import com.mbd.cmscommon.domain.model.CalendarEvent
import com.mbd.cmscommon.domain.repository.CalendarRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * [CalendarRepository] has no `observe*`/`sync()` pair in its interface — every screen calls
 * [getEvents] directly and gets a fresh full fetch each time. We still keep a small in-memory
 * cache mirroring the last fetch (useful for callers that want a synchronous last-known-good
 * value without re-awaiting network), refreshed as a side effect of [getEvents].
 */
@Singleton
class DesktopCalendarRepository @Inject constructor(
    private val postgrest: Postgrest,
) : CalendarRepository {

    private val cache = MutableStateFlow<List<CalendarEvent>>(emptyList())

    override suspend fun getEvents(): List<CalendarEvent> {
        val rows = postgrest.from(SupabaseTables.CALENDAR_EVENTS).select {
            filter { eq("is_deleted", false) }
            order("start_date", Order.ASCENDING)
        }.decodeList<CalendarEventDto>()
        val events = rows.map { DesktopCalendarMapper.dtoToDomain(it) }
        cache.value = events
        return events
    }

    override suspend fun createEvent(event: CalendarEvent, createdBy: String) {
        postgrest.from(SupabaseTables.CALENDAR_EVENTS).insert(DesktopCalendarMapper.domainToDto(event, createdBy))
        getEvents()
    }

    override suspend fun deleteEvent(id: String) {
        postgrest.from(SupabaseTables.CALENDAR_EVENTS).update({ set("is_deleted", true) }) {
            filter { eq("id", id) }
        }
        getEvents()
    }
}
