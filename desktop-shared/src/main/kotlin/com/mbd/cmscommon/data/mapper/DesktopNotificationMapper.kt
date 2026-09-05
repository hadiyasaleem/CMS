package com.mbd.cmscommon.data.mapper

import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.dto.NotificationDto
import com.mbd.cmscommon.domain.model.Notification
import com.mbd.cmscommon.domain.model.NotificationPriority
import com.mbd.cmscommon.domain.model.NotificationTargetRole

/**
 * Direct DTO<->Domain mapping for the desktop apps — copied near-verbatim from mobile's
 * NotificationMapper.dtoToDomain/domainToDto (which are already direct, Entity-free there too).
 */
object DesktopNotificationMapper {
    private fun parseRole(raw: String?): NotificationTargetRole? =
        raw?.let { runCatching { NotificationTargetRole.valueOf(it) }.getOrNull() }

    private fun parsePriority(raw: String?): NotificationPriority =
        runCatching { NotificationPriority.valueOf(raw ?: "") }.getOrDefault(NotificationPriority.NORMAL)

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
}
