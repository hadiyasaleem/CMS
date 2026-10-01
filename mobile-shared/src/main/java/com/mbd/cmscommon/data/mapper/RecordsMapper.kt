package com.mbd.cmscommon.data.mapper

import com.mbd.cmscommon.domain.model.parseShift
import com.mbd.cmscommon.data.local.entity.CalendarEventEntity
import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.dto.CalendarEventDto
import com.mbd.cmscommon.domain.model.CalendarEvent
import java.time.Instant

object CalendarEventMapper {
    fun dtoToEntity(dto: CalendarEventDto): CalendarEventEntity = CalendarEventEntity(
        eventId = dto.id ?: "",
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
        shift = dto.shift,
        createdAt = PgTime.parseOrEpoch(dto.createdAt).toEpochMilli(),
        createdBy = dto.createdBy,
        updatedAt = PgTime.parseOrEpoch(dto.updatedAt).toEpochMilli(),
        updatedBy = dto.updatedBy,
        isDeleted = dto.isDeleted,
        deletedAt = PgTime.parse(dto.deletedAt)?.toEpochMilli(),
        deletedBy = dto.deletedBy,
    )

    fun entityToDomain(entity: CalendarEventEntity): CalendarEvent = CalendarEvent(
        id = entity.eventId,
        title = entity.title,
        eventType = entity.eventType,
        startDate = entity.startDate,
        endDate = entity.endDate,
        startTime = entity.startTime,
        endTime = entity.endTime,
        description = entity.description,
        venue = entity.venue,
        audience = entity.audience,
        deptId = entity.deptId,
        sessionId = entity.sessionId,
        shift = parseShift(entity.shift),
        createdAt = Instant.ofEpochMilli(entity.createdAt),
        createdBy = entity.createdBy,
        updatedAt = Instant.ofEpochMilli(entity.updatedAt),
        updatedBy = entity.updatedBy,
    )
}
