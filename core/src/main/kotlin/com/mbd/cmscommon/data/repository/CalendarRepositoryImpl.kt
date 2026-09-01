package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.CalendarEventDto
import com.mbd.cmscommon.domain.model.CalendarEvent
import com.mbd.cmscommon.domain.repository.CalendarRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject

class CalendarRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
) : CalendarRepository {

    override suspend fun getEvents(): List<CalendarEvent> {
        val rows = postgrest.from(SupabaseTables.CALENDAR_EVENTS).select {
            filter { eq("is_deleted", false) }
            order("start_date", Order.ASCENDING)
        }.decodeList<CalendarEventDto>()
        return rows.map { it.toDomain() }
    }

    override suspend fun createEvent(event: CalendarEvent, createdBy: String) {
        val dto = CalendarEventDto(
            title = event.title.trim(),
            eventType = event.eventType.ifBlank { "EVENT" },
            startDate = event.startDate,
            endDate = event.endDate?.trim()?.takeIf { it.isNotBlank() },
            startTime = event.startTime?.trim()?.takeIf { it.isNotBlank() },
            endTime = event.endTime?.trim()?.takeIf { it.isNotBlank() },
            description = event.description?.trim()?.takeIf { it.isNotBlank() },
            venue = event.venue?.trim()?.takeIf { it.isNotBlank() },
            audience = event.audience.ifBlank { "ALL" },
            deptId = event.deptId?.trim()?.takeIf { it.isNotBlank() },
            sessionId = event.sessionId?.trim()?.takeIf { it.isNotBlank() },
            createdBy = createdBy,
        )
        postgrest.from(SupabaseTables.CALENDAR_EVENTS).insert(dto)
    }

    override suspend fun deleteEvent(id: String) {
        postgrest.from(SupabaseTables.CALENDAR_EVENTS).update({ set("is_deleted", true) }) {
            filter { eq("id", id) }
        }
    }

    private fun CalendarEventDto.toDomain() = CalendarEvent(
        id = id.orEmpty(),
        title = title.orEmpty(),
        eventType = eventType.orEmpty(),
        startDate = startDate.orEmpty(),
        endDate = endDate,
        startTime = startTime,
        endTime = endTime,
        description = description,
        venue = venue,
        audience = audience ?: "ALL",
        deptId = deptId,
        sessionId = sessionId,
        entityId = entityId ?: 0L,
        createdAt = PgTime.parseOrEpoch(createdAt),
        createdBy = createdBy,
        updatedAt = PgTime.parseOrEpoch(updatedAt),
        updatedBy = updatedBy,
    )
}
