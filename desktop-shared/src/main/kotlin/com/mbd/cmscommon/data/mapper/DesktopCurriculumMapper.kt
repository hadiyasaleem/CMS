package com.mbd.cmscommon.data.mapper

import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.dto.SemesterSubjectDto
import com.mbd.cmscommon.data.remote.dto.SemesterTermDto
import com.mbd.cmscommon.domain.model.SemesterSubject
import com.mbd.cmscommon.domain.model.SemesterTerm
import com.mbd.cmscommon.domain.model.SubjectType
import java.time.LocalDate

/**
 * Direct DTO<->Domain mapping for the desktop apps (no local Room cache, so there is no
 * Entity intermediate here — just the same field logic mobile's dtoToEntity+entityToDomain
 * pair does, composed into one step).
 */
object DesktopCurriculumMapper {
    fun subjectDtoToDomain(dto: SemesterSubjectDto): SemesterSubject = SemesterSubject(
        sessionId = dto.sessionId ?: "",
        semester = dto.semester,
        courseCode = dto.courseCode ?: "",
        name = dto.name ?: "",
        creditHours = dto.creditHours,
        subjectType = runCatching { SubjectType.valueOf(dto.subjectType ?: "") }.getOrDefault(SubjectType.THEORY),
        isElective = dto.isElective,
        outline = dto.outline,
        entityId = dto.entityId ?: 0L,
        createdAt = PgTime.parseOrEpoch(dto.createdAt),
        createdBy = dto.createdBy,
        updatedAt = PgTime.parseOrEpoch(dto.updatedAt),
        updatedBy = dto.updatedBy,
    )

    fun subjectDomainToDto(domain: SemesterSubject, sessionId: String, semester: Int): SemesterSubjectDto = SemesterSubjectDto(
        sessionId = sessionId,
        semester = semester,
        courseCode = domain.courseCode,
        name = domain.name,
        creditHours = domain.creditHours,
        subjectType = domain.subjectType.name,
        isElective = domain.isElective,
        outline = domain.outline,
        createdBy = domain.createdBy,
        updatedBy = domain.updatedBy,
    )

    fun termDtoToDomain(dto: SemesterTermDto, sessionId: String, semester: Int): SemesterTerm = SemesterTerm(
        sessionId = sessionId,
        semester = semester,
        startDate = dto.startDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
        endDate = dto.endDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
    )

    fun termDomainToDto(domain: SemesterTerm): SemesterTermDto = SemesterTermDto(
        sessionId = domain.sessionId,
        semester = domain.semester,
        startDate = domain.startDate?.toString(),
        endDate = domain.endDate?.toString(),
    )
}
