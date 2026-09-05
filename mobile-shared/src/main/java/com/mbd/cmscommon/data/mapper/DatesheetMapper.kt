package com.mbd.cmscommon.data.mapper

import com.mbd.cmscommon.data.local.entity.DatesheetEntity
import com.mbd.cmscommon.data.local.entity.DatesheetSlotEntity
import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.dto.DatesheetDto
import com.mbd.cmscommon.data.remote.dto.DatesheetSlotDto
import com.mbd.cmscommon.domain.model.Datesheet
import com.mbd.cmscommon.domain.model.DatesheetSlot
import java.time.Instant

object DatesheetMapper {
    fun dtoToEntity(dto: DatesheetDto): DatesheetEntity = DatesheetEntity(
        datesheetId = dto.id ?: "",
        title = dto.title ?: "",
        examType = dto.examType ?: "",
        sessionId = dto.sessionId,
        published = dto.published,
        instructions = dto.instructions,
        createdAt = PgTime.parseOrEpoch(dto.createdAt).toEpochMilli(),
        createdBy = dto.createdBy,
        updatedAt = PgTime.parseOrEpoch(dto.updatedAt).toEpochMilli(),
        updatedBy = dto.updatedBy,
        isDeleted = dto.isDeleted,
        deletedAt = PgTime.parse(dto.deletedAt)?.toEpochMilli(),
        deletedBy = dto.deletedBy,
    )

    fun entityToDomain(entity: DatesheetEntity): Datesheet = Datesheet(
        id = entity.datesheetId,
        title = entity.title,
        examType = entity.examType,
        sessionId = entity.sessionId,
        published = entity.published,
        instructions = entity.instructions,
        createdAt = Instant.ofEpochMilli(entity.createdAt),
        createdBy = entity.createdBy,
        updatedAt = Instant.ofEpochMilli(entity.updatedAt),
        updatedBy = entity.updatedBy,
    )

    fun slotDtoToEntity(dto: DatesheetSlotDto): DatesheetSlotEntity = DatesheetSlotEntity(
        slotId = dto.id ?: "",
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
        createdAt = PgTime.parseOrEpoch(dto.createdAt).toEpochMilli(),
        createdBy = dto.createdBy,
        updatedAt = PgTime.parseOrEpoch(dto.updatedAt).toEpochMilli(),
        updatedBy = dto.updatedBy,
        isDeleted = dto.isDeleted,
        deletedAt = PgTime.parse(dto.deletedAt)?.toEpochMilli(),
        deletedBy = dto.deletedBy,
    )

    fun slotEntityToDomain(entity: DatesheetSlotEntity): DatesheetSlot = DatesheetSlot(
        id = entity.slotId,
        datesheetId = entity.datesheetId,
        examDate = entity.examDate,
        startTime = entity.startTime,
        endTime = entity.endTime,
        durationMinutes = entity.durationMinutes,
        courseCode = entity.courseCode,
        subjectName = entity.subjectName,
        roomNo = entity.roomNo,
        building = entity.building,
        invigilatorEmail = entity.invigilatorEmail,
        createdAt = Instant.ofEpochMilli(entity.createdAt),
        createdBy = entity.createdBy,
        updatedAt = Instant.ofEpochMilli(entity.updatedAt),
        updatedBy = entity.updatedBy,
    )
}
