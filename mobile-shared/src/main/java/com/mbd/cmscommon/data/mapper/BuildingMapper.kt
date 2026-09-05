package com.mbd.cmscommon.data.mapper

import com.mbd.cmscommon.data.local.entity.BuildingEntity
import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.dto.BuildingDto
import com.mbd.cmscommon.domain.model.Building
import java.time.Instant

object BuildingMapper {
    fun dtoToEntity(dto: BuildingDto): BuildingEntity = BuildingEntity(
        buildingId = dto.buildingId ?: "",
        name = dto.name ?: "",
        code = dto.code,
        isActive = dto.isActive,
        createdAt = PgTime.parseOrEpoch(dto.createdAt).toEpochMilli(),
        createdBy = dto.createdBy ?: "",
        updatedAt = PgTime.parseOrEpoch(dto.updatedAt).toEpochMilli(),
        updatedBy = dto.updatedBy ?: "",
        isDeleted = dto.isDeleted,
        deletedAt = PgTime.parse(dto.deletedAt)?.toEpochMilli(),
        deletedBy = dto.deletedBy,
    )

    fun entityToDomain(entity: BuildingEntity): Building = Building(
        buildingId = entity.buildingId,
        name = entity.name,
        code = entity.code,
        isActive = entity.isActive,
        createdAt = Instant.ofEpochMilli(entity.createdAt),
        createdBy = entity.createdBy ?: "",
        updatedAt = Instant.ofEpochMilli(entity.updatedAt),
        updatedBy = entity.updatedBy ?: "",
    )

    fun domainToEntity(domain: Building): BuildingEntity = BuildingEntity(
        buildingId = domain.buildingId,
        name = domain.name,
        code = domain.code,
        isActive = domain.isActive,
        createdAt = domain.createdAt.toEpochMilli(),
        createdBy = domain.createdBy,
        updatedAt = domain.updatedAt.toEpochMilli(),
        updatedBy = domain.updatedBy,
    )

    fun domainToDto(domain: Building): BuildingDto = BuildingDto(
        buildingId = domain.buildingId,
        name = domain.name,
        code = domain.code,
        isActive = domain.isActive,
        createdBy = domain.createdBy,
        updatedBy = domain.updatedBy,
    )
}
