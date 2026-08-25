package com.mbd.cmscommon.data.mapper

import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.dto.DepartmentDto
import com.mbd.cmscommon.domain.model.Department
import java.time.Instant

/**
 * Direct DTO<->Domain mapping for the desktop apps (no local Room cache, so there is no
 * Entity intermediate here — just the same field logic mobile's dtoToEntity+entityToDomain
 * pair does, composed into one step).
 */
object DesktopDepartmentMapper {
    fun dtoToDomain(dto: DepartmentDto): Department = Department(
        deptId = dto.deptId ?: "",
        name = dto.name ?: "",
        code = dto.code ?: "",
        hodEmail = dto.hodEmail,
        description = dto.description,
        isActive = dto.isActive,
        archivedAt = PgTime.parse(dto.archivedAt),
        entityId = dto.entityId ?: 0L,
        createdAt = PgTime.parseOrEpoch(dto.createdAt),
        createdBy = dto.createdBy ?: "",
        updatedAt = PgTime.parseOrEpoch(dto.updatedAt),
        updatedBy = dto.updatedBy ?: "",
    )

    fun domainToDto(domain: Department): DepartmentDto = DepartmentDto(
        deptId = domain.deptId,
        name = domain.name,
        code = domain.code,
        hodEmail = domain.hodEmail,
        description = domain.description,
        isActive = domain.isActive,
        createdBy = domain.createdBy,
        updatedBy = domain.updatedBy,
    )
}
