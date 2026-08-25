package com.mbd.cmscommon.data.mapper

import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.dto.AdministratorAccountDto
import com.mbd.cmscommon.domain.model.AdministratorAccount

/**
 * Direct DTO<->Domain mapping for the desktop apps (no local Room cache, so there is no
 * Entity intermediate here — just the same field logic mobile's dtoToEntity+entityToDomain
 * pair does, composed into one step).
 */
object DesktopAdministratorMapper {
    fun dtoToDomain(dto: AdministratorAccountDto): AdministratorAccount = AdministratorAccount(
        id = dto.id ?: "",
        entityId = dto.entityId ?: 0L,
        email = dto.email ?: "",
        status = dto.status ?: "",
        createdAt = PgTime.parse(dto.createdAt),
        lastLoginAt = PgTime.parse(dto.lastLoginAt),
        createdBy = dto.createdBy,
        updatedAt = PgTime.parse(dto.updatedAt ?: dto.createdAt),
        updatedBy = dto.updatedBy,
        isDeleted = dto.isDeleted,
        deletedAt = PgTime.parse(dto.deletedAt),
        deletedBy = dto.deletedBy,
    )
}
