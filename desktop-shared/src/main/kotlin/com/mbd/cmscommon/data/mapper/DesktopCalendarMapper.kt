package com.mbd.cmscommon.data.mapper

import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.dto.CalendarEventDto
import com.mbd.cmscommon.domain.model.CalendarEvent

/**
 * Direct DTO<->Domain mapping for the desktop apps (no local Room cache, so there is no
 * Entity intermediate here — just the same field logic mobile's dtoToEntity+entityToDomain
 * pair does, composed into one step).
 */
object DesktopCalendarMapper {
    fun dtoToDomain(dto: CalendarEventDto): CalendarEvent = CalendarEvent(
        id = dto.id ?: "",
        title = dto.title ?: "",
        eventType = dto.eventType ?: "",
        startDate = dto.startDate ?: "",
        endDate = dto.endDate,
        startTime = dto.startTime,
        endTime = dto.endTime,
        description = dto.description,
        venue = dto.venue,
        audience = dto.audience ?: "ALL",
        deptId = dto.deptId,
        sessionId = dto.sessionId,
        entityId = dto.entityId ?: 0L,
        createdAt = PgTime.parseOrEpoch(dto.createdAt),
        createdBy = dto.createdBy,
        updatedAt = PgTime.parseOrEpoch(dto.updatedAt),
        updatedBy = dto.updatedBy,
    )

    fun domainToDto(domain: CalendarEvent, createdBy: String): CalendarEventDto = CalendarEventDto(
        title = domain.title.trim(),
        eventType = domain.eventType.ifBlank { "EVENT" },
        startDate = domain.startDate,
        endDate = domain.endDate?.trim()?.takeIf { it.isNotBlank() },
        startTime = domain.startTime?.trim()?.takeIf { it.isNotBlank() },
        endTime = domain.endTime?.trim()?.takeIf { it.isNotBlank() },
        description = domain.description?.trim()?.takeIf { it.isNotBlank() },
        venue = domain.venue?.trim()?.takeIf { it.isNotBlank() },
        audience = domain.audience.ifBlank { "ALL" },
        deptId = domain.deptId?.trim()?.takeIf { it.isNotBlank() },
        sessionId = domain.sessionId?.trim()?.takeIf { it.isNotBlank() },
        createdBy = createdBy,
    )
}
