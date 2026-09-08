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
        sessionId = dto.sessionId ?: "",
        semester = dto.semester,
        defaultStartTime = dto.defaultStartTime,
        defaultEndTime = dto.defaultEndTime,
        defaultBuildingId = dto.defaultBuildingId,
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
        sessionId = entity.sessionId,
        semester = entity.semester,
        defaultStartTime = entity.defaultStartTime,
        defaultEndTime = entity.defaultEndTime,
        defaultBuildingId = entity.defaultBuildingId,
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
        courseCode = dto.courseCode ?: "",
        subjectName = dto.subjectName ?: "",
        examDate = dto.examDate,
        startTime = dto.startTime,
        endTime = dto.endTime,
        buildingId = dto.buildingId,
        building = dto.building,
        roomId = dto.roomId,
        roomNo = dto.roomNo,
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
        courseCode = entity.courseCode,
        subjectName = entity.subjectName,
        examDate = entity.examDate,
        startTime = entity.startTime,
        endTime = entity.endTime,
        buildingId = entity.buildingId,
        building = entity.building,
        roomId = entity.roomId,
        roomNo = entity.roomNo,
        invigilatorEmail = entity.invigilatorEmail,
        createdAt = Instant.ofEpochMilli(entity.createdAt),
        createdBy = entity.createdBy,
        updatedAt = Instant.ofEpochMilli(entity.updatedAt),
        updatedBy = entity.updatedBy,
    )
}
