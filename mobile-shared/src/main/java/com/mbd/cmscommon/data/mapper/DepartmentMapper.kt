package com.mbd.cmscommon.data.mapper

import com.mbd.cmscommon.data.local.entity.DepartmentEntity
import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.dto.DepartmentDto
import com.mbd.cmscommon.domain.model.Department
import java.time.Instant

object DepartmentMapper {
    fun dtoToEntity(dto: DepartmentDto): DepartmentEntity = DepartmentEntity(
        deptId = dto.deptId ?: "",
        entityId = dto.entityId ?: 0L,
        name = dto.name ?: "",
        code = dto.code ?: "",
        hodEmail = dto.hodEmail,
        description = dto.description,
        createdAt = PgTime.parseOrEpoch(dto.createdAt).toEpochMilli(),
        createdBy = dto.createdBy ?: "",
        updatedAt = PgTime.parseOrEpoch(dto.updatedAt).toEpochMilli(),
        updatedBy = dto.updatedBy ?: "",
        isDeleted = dto.isDeleted,
        deletedAt = PgTime.parse(dto.deletedAt)?.toEpochMilli(),
        deletedBy = dto.deletedBy,
    )

    fun entityToDomain(entity: DepartmentEntity): Department = Department(
        deptId = entity.deptId,
        name = entity.name,
        code = entity.code,
        hodEmail = entity.hodEmail,
        description = entity.description,
        entityId = entity.entityId,
        createdAt = Instant.ofEpochMilli(entity.createdAt),
        createdBy = entity.createdBy ?: "",
        updatedAt = Instant.ofEpochMilli(entity.updatedAt),
        updatedBy = entity.updatedBy ?: "",
    )

    fun domainToEntity(domain: Department): DepartmentEntity = DepartmentEntity(
        deptId = domain.deptId,
        entityId = domain.entityId,
        name = domain.name,
        code = domain.code,
        hodEmail = domain.hodEmail,
        description = domain.description,
        createdAt = domain.createdAt.toEpochMilli(),
        createdBy = domain.createdBy,
        updatedAt = domain.updatedAt.toEpochMilli(),
        updatedBy = domain.updatedBy,
    )

    fun domainToDto(domain: Department): DepartmentDto = DepartmentDto(
        deptId = domain.deptId,
        name = domain.name,
        code = domain.code,
        hodEmail = domain.hodEmail,
        description = domain.description,
        createdBy = domain.createdBy,
        updatedBy = domain.updatedBy,
    )
}
