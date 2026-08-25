package com.mbd.cmscommon.data.mapper

import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.dto.DatesheetDto
import com.mbd.cmscommon.data.remote.dto.DatesheetSlotDto
import com.mbd.cmscommon.domain.model.Datesheet
import com.mbd.cmscommon.domain.model.DatesheetSlot

/**
 * Direct DTO<->Domain mapping for the desktop apps (no local Room cache, so there is no
 * Entity intermediate here — just the same field logic mobile's dtoToEntity+entityToDomain
 * pair does, composed into one step).
 */
object DesktopDatesheetMapper {
    fun dtoToDomain(dto: DatesheetDto): Datesheet = Datesheet(
        id = dto.id ?: "",
        title = dto.title ?: "",
        examType = dto.examType,
        sessionId = dto.sessionId,
        published = dto.published,
        instructions = dto.instructions,
        entityId = dto.entityId ?: 0L,
        createdAt = PgTime.parseOrEpoch(dto.createdAt),
        createdBy = dto.createdBy,
        updatedAt = PgTime.parseOrEpoch(dto.updatedAt),
        updatedBy = dto.updatedBy,
    )

    fun slotDtoToDomain(dto: DatesheetSlotDto): DatesheetSlot = DatesheetSlot(
        id = dto.id ?: "",
        datesheetId = dto.datesheetId ?: "",
        examDate = dto.examDate ?: "",
        startTime = dto.startTime,
        endTime = dto.endTime,
        durationMinutes = dto.durationMinutes,
        courseCode = dto.courseCode,
        subjectName = dto.subjectName,
        roomNo = dto.roomNo,
        building = dto.building,
        invigilatorEmail = dto.invigilatorEmail,
        entityId = dto.entityId ?: 0L,
        createdAt = PgTime.parseOrEpoch(dto.createdAt),
        createdBy = dto.createdBy,
        updatedAt = PgTime.parseOrEpoch(dto.updatedAt),
        updatedBy = dto.updatedBy,
    )

    fun slotDomainToDto(domain: DatesheetSlot): DatesheetSlotDto = DatesheetSlotDto(
        datesheetId = domain.datesheetId,
        examDate = domain.examDate,
        startTime = domain.startTime,
        endTime = domain.endTime,
        durationMinutes = domain.durationMinutes,
        courseCode = domain.courseCode,
        subjectName = domain.subjectName,
        roomNo = domain.roomNo,
        building = domain.building,
        invigilatorEmail = domain.invigilatorEmail,
    )
}
