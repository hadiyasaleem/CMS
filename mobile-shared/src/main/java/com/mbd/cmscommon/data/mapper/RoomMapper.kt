package com.mbd.cmscommon.data.mapper

import com.mbd.cmscommon.data.local.entity.RoomEntity
import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.dto.RoomDto
import com.mbd.cmscommon.domain.model.Room
import java.time.Instant

object RoomMapper {
    fun dtoToEntity(dto: RoomDto): RoomEntity = RoomEntity(
        roomId = dto.roomId ?: "",
        buildingId = dto.buildingId ?: "",
        roomNo = dto.roomNo ?: "",
        name = dto.name,
        capacity = dto.capacity,
        isOffice = dto.isOffice,
        isActive = dto.isActive,
        createdAt = PgTime.parseOrEpoch(dto.createdAt).toEpochMilli(),
        createdBy = dto.createdBy ?: "",
        updatedAt = PgTime.parseOrEpoch(dto.updatedAt).toEpochMilli(),
        updatedBy = dto.updatedBy ?: "",
        isDeleted = dto.isDeleted,
        deletedAt = PgTime.parse(dto.deletedAt)?.toEpochMilli(),
        deletedBy = dto.deletedBy,
    )

    fun entityToDomain(entity: RoomEntity): Room = Room(
        roomId = entity.roomId,
        buildingId = entity.buildingId,
        roomNo = entity.roomNo,
        name = entity.name,
        capacity = entity.capacity,
        isOffice = entity.isOffice,
        isActive = entity.isActive,
        createdAt = Instant.ofEpochMilli(entity.createdAt),
        createdBy = entity.createdBy ?: "",
        updatedAt = Instant.ofEpochMilli(entity.updatedAt),
        updatedBy = entity.updatedBy ?: "",
    )

    fun domainToEntity(domain: Room): RoomEntity = RoomEntity(
        roomId = domain.roomId,
        buildingId = domain.buildingId,
        roomNo = domain.roomNo,
        name = domain.name,
        capacity = domain.capacity,
        isOffice = domain.isOffice,
        isActive = domain.isActive,
        createdAt = domain.createdAt.toEpochMilli(),
        createdBy = domain.createdBy,
        updatedAt = domain.updatedAt.toEpochMilli(),
        updatedBy = domain.updatedBy,
    )

    fun domainToDto(domain: Room): RoomDto = RoomDto(
        roomId = domain.roomId,
        buildingId = domain.buildingId,
        roomNo = domain.roomNo,
        name = domain.name,
        capacity = domain.capacity,
        isOffice = domain.isOffice,
        isActive = domain.isActive,
        createdBy = domain.createdBy,
        updatedBy = domain.updatedBy,
    )
}
