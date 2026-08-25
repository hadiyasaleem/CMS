package com.mbd.cmscommon.data.mapper

import com.mbd.cmscommon.data.local.entity.CalendarEventEntity
import com.mbd.cmscommon.data.local.entity.FineEntity
import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.dto.CalendarEventDto
import com.mbd.cmscommon.data.remote.dto.FineDto
import com.mbd.cmscommon.domain.model.CalendarEvent
import com.mbd.cmscommon.domain.model.Fine
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
        entityId = dto.entityId ?: 0L,
        createdAt = PgTime.parseOrEpoch(dto.createdAt).toEpochMilli(),
        createdBy = dto.createdBy,
        updatedAt = PgTime.parseOrEpoch(dto.updatedAt).toEpochMilli(),
        updatedBy = dto.updatedBy,
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
        entityId = entity.entityId,
        createdAt = Instant.ofEpochMilli(entity.createdAt),
        createdBy = entity.createdBy,
        updatedAt = Instant.ofEpochMilli(entity.updatedAt),
        updatedBy = entity.updatedBy,
    )
}

object FineMapper {
    fun dtoToEntity(dto: FineDto): FineEntity = FineEntity(
        fineId = dto.id ?: "",
        sessionId = dto.sessionId ?: "",
        rollNumber = dto.rollNumber ?: "",
        category = dto.category ?: "",
        amount = dto.amount,
        reason = dto.reason,
        issuedBy = dto.issuedBy,
        issuedAt = PgTime.parse(dto.issuedAt)?.toEpochMilli(),
        entityId = dto.entityId ?: 0L,
        createdAt = PgTime.parseOrEpoch(dto.createdAt).toEpochMilli(),
        createdBy = dto.createdBy,
        updatedAt = PgTime.parseOrEpoch(dto.updatedAt).toEpochMilli(),
        updatedBy = dto.updatedBy,
    )

    fun entityToDomain(entity: FineEntity): Fine = Fine(
        id = entity.fineId,
        sessionId = entity.sessionId,
        rollNumber = entity.rollNumber,
        category = entity.category,
        amount = entity.amount,
        reason = entity.reason ?: "",
        issuedBy = entity.issuedBy,
        issuedAt = entity.issuedAt?.let { Instant.ofEpochMilli(it) },
        entityId = entity.entityId,
        createdAt = Instant.ofEpochMilli(entity.createdAt),
        createdBy = entity.createdBy,
        updatedAt = Instant.ofEpochMilli(entity.updatedAt),
        updatedBy = entity.updatedBy,
    )
}
