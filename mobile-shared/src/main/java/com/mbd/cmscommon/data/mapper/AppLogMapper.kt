package com.mbd.cmscommon.data.mapper

import com.mbd.cmscommon.data.local.entity.AppLogEntity
import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.dto.AppLogDto
import com.mbd.cmscommon.util.LogRecord
import java.time.Instant

object AppLogMapper {
    fun recordToEntity(record: LogRecord): AppLogEntity = AppLogEntity(
        logId = record.logId,
        occurredAtMillis = record.occurredAtMillis,
        severity = record.severity,
        kind = record.kind,
        tag = record.tag,
        message = record.message,
        stackTrace = record.stackTrace,
        accountEmail = record.accountEmail,
        appId = record.appId,
        appVersion = record.appVersion,
        platform = record.platform,
        deviceInfo = record.deviceInfo,
    )

    fun entityToDto(entity: AppLogEntity): AppLogDto = AppLogDto(
        logId = entity.logId,
        occurredAt = PgTime.format(Instant.ofEpochMilli(entity.occurredAtMillis)) ?: Instant.ofEpochMilli(entity.occurredAtMillis).toString(),
        severity = entity.severity,
        kind = entity.kind,
        tag = entity.tag,
        message = entity.message,
        stackTrace = entity.stackTrace,
        accountEmail = entity.accountEmail,
        appId = entity.appId,
        appVersion = entity.appVersion,
        platform = entity.platform,
        deviceInfo = entity.deviceInfo,
    )
}
