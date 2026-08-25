package com.mbd.cmscommon.data.mapper

import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.dto.TimetablePeriodDto
import com.mbd.cmscommon.domain.model.PeriodType
import com.mbd.cmscommon.domain.model.SessionPeriod
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Direct DTO<->Domain mapping for timetable periods, for the desktop apps (no local Room cache —
 * just the same field logic mobile's toEntity+entityToDomain pair does, composed into one step).
 */
object DesktopSessionTimetableMapper {
    fun dtoToDomain(dto: TimetablePeriodDto): SessionPeriod {
        val day = runCatching { DayOfWeek.valueOf(dto.day ?: "") }.getOrDefault(DayOfWeek.MONDAY)
        return SessionPeriod(
            id = dto.id ?: SessionPeriod.buildId(dto.sessionId ?: "", day, dto.startTime ?: ""),
            sessionId = dto.sessionId ?: "",
            day = day,
            startTime = dto.startTime ?: "",
            endTime = dto.endTime ?: "",
            courseCode = dto.courseCode ?: "",
            subjectName = dto.subjectName ?: "",
            teacherId = dto.teacherEmail ?: "",
            teacherName = dto.teacherName ?: "",
            periodType = runCatching { PeriodType.valueOf(dto.periodType ?: "") }.getOrDefault(PeriodType.LECTURE),
            creditHours = dto.creditHours,
            roomNo = dto.roomNo,
            building = dto.building,
            notes = dto.notes,
            effectiveFrom = dto.effectiveFrom?.let { raw -> runCatching { LocalDate.parse(raw) }.getOrNull() },
            effectiveTo = dto.effectiveTo?.let { raw -> runCatching { LocalDate.parse(raw) }.getOrNull() },
            entityId = dto.entityId ?: 0L,
            createdAt = PgTime.parseOrEpoch(dto.createdAt),
            createdBy = dto.createdBy,
            updatedAt = PgTime.parseOrEpoch(dto.updatedAt),
            updatedBy = dto.updatedBy,
        )
    }

    fun domainToDto(domain: SessionPeriod): TimetablePeriodDto = TimetablePeriodDto(
        sessionId = domain.sessionId,
        day = domain.day.name,
        startTime = domain.startTime,
        endTime = domain.endTime,
        periodType = domain.periodType.name,
        courseCode = domain.courseCode,
        subjectName = domain.subjectName,
        creditHours = domain.creditHours,
        teacherEmail = domain.teacherId.takeIf { it.isNotBlank() },
        teacherName = domain.teacherName,
        roomNo = domain.roomNo,
        building = domain.building,
        notes = domain.notes,
        effectiveFrom = domain.effectiveFrom?.toString(),
        effectiveTo = domain.effectiveTo?.toString(),
        createdBy = domain.createdBy,
        updatedBy = domain.updatedBy,
    )
}
