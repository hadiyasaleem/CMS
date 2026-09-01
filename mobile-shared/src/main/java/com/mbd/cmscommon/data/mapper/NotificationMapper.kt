package com.mbd.cmscommon.data.mapper

import com.mbd.cmscommon.data.local.entity.NotificationEntity
import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.dto.NotificationDto
import com.mbd.cmscommon.domain.model.Notification
import com.mbd.cmscommon.domain.model.NotificationPriority
import com.mbd.cmscommon.domain.model.NotificationTargetRole
import java.time.Instant

object NotificationMapper {
    private fun parseRole(raw: String?): NotificationTargetRole? =
        raw?.let { runCatching { NotificationTargetRole.valueOf(it.trim().uppercase()) }.getOrNull() }

    private fun parsePriority(raw: String?): NotificationPriority =
        runCatching { NotificationPriority.valueOf(raw?.trim()?.uppercase() ?: "") }.getOrDefault(NotificationPriority.NORMAL)

    fun dtoToDomain(dto: NotificationDto): Notification = Notification(
        notificationId = dto.id ?: "",
        title = dto.title ?: "",
        body = dto.body ?: "",
        targetRole = parseRole(dto.targetRole),
        targetOfferingId = dto.targetSessionId,
        createdByUid = dto.createdByEmail ?: "",
        priority = parsePriority(dto.priority),
        targetDeptId = dto.targetDeptId,
        attachmentPath = dto.attachmentPath,
        expiresAt = PgTime.parse(dto.expiresAt),
        entityId = dto.entityId ?: 0L,
        createdAt = PgTime.parseOrEpoch(dto.createdAt),
        createdBy = dto.createdBy,
        updatedAt = PgTime.parseOrEpoch(dto.updatedAt),
        updatedBy = dto.updatedBy,
    )

    fun domainToDto(domain: Notification): NotificationDto = NotificationDto(
        title = domain.title,
        body = domain.body,
        targetRole = domain.targetRole?.name,
        targetDeptId = domain.targetDeptId,
        targetSessionId = domain.targetOfferingId,
        priority = domain.priority.name,
        attachmentPath = domain.attachmentPath,
        expiresAt = domain.expiresAt?.toString(),
        createdByEmail = domain.createdByUid,
        createdBy = domain.createdBy,
        updatedBy = domain.updatedBy,
    )

    fun domainToEntity(domain: Notification): NotificationEntity = NotificationEntity(
        notificationId = domain.notificationId,
        title = domain.title,
        body = domain.body,
        targetRole = domain.targetRole?.name ?: "",
        targetOfferingId = domain.targetOfferingId,
        createdByUid = domain.createdByUid,
        priority = domain.priority.name,
        targetDeptId = domain.targetDeptId,
        attachmentPath = domain.attachmentPath,
        expiresAt = domain.expiresAt?.toEpochMilli(),
        createdAt = domain.createdAt.toEpochMilli(),
        entityId = domain.entityId,
        createdBy = domain.createdBy,
        updatedAt = domain.updatedAt.toEpochMilli(),
        updatedBy = domain.updatedBy,
    )

    fun entityToDomain(entity: NotificationEntity): Notification = Notification(
        notificationId = entity.notificationId,
        title = entity.title,
        body = entity.body ?: "",
        targetRole = parseRole(entity.targetRole),
        targetOfferingId = entity.targetOfferingId,
        createdByUid = entity.createdByUid ?: "",
        priority = parsePriority(entity.priority),
        targetDeptId = entity.targetDeptId,
        attachmentPath = entity.attachmentPath,
        expiresAt = entity.expiresAt?.let { Instant.ofEpochMilli(it) },
        entityId = entity.entityId,
        createdAt = Instant.ofEpochMilli(entity.createdAt),
        createdBy = entity.createdBy,
        updatedAt = Instant.ofEpochMilli(entity.updatedAt),
        updatedBy = entity.updatedBy,
    )

    fun dtoToEntity(dto: NotificationDto): NotificationEntity = domainToEntity(dtoToDomain(dto)).copy(
        isDeleted = dto.isDeleted,
        deletedAt = PgTime.parse(dto.deletedAt)?.toEpochMilli(),
        deletedBy = dto.deletedBy,
    )
}
