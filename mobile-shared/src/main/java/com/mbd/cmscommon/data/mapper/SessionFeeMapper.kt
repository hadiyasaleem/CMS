package com.mbd.cmscommon.data.mapper

import com.mbd.cmscommon.data.local.entity.SessionFeeEntity
import com.mbd.cmscommon.data.local.entity.SessionFeeHeadEntity
import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.dto.SessionFeeDto
import com.mbd.cmscommon.data.remote.dto.SessionFeeHeadDto
import com.mbd.cmscommon.domain.model.FeeHead
import com.mbd.cmscommon.domain.model.FeeType
import com.mbd.cmscommon.domain.model.SessionFeeStructure
import java.time.Instant
import java.util.Locale

object SessionFeeMapper {
    fun feeDtoToEntity(dto: SessionFeeDto): SessionFeeEntity = SessionFeeEntity(
        sessionId = dto.sessionId ?: "",
        cadence = dto.cadence ?: "",
        academicYear = dto.academicYear,
        dueDate = dto.dueDate,
        lateFineNote = dto.lateFineNote,
        paymentNote = dto.paymentNote,
        entityId = dto.entityId ?: 0L,
        createdAt = PgTime.parseOrEpoch(dto.createdAt).toEpochMilli(),
        createdBy = dto.createdBy,
        updatedAt = PgTime.parseOrEpoch(dto.updatedAt).toEpochMilli(),
        updatedBy = dto.updatedBy,
        isDeleted = dto.isDeleted,
        deletedAt = PgTime.parse(dto.deletedAt)?.toEpochMilli(),
        deletedBy = dto.deletedBy,
    )

    fun headDtoToEntity(dto: SessionFeeHeadDto): SessionFeeHeadEntity = SessionFeeHeadEntity(
        id = headLocalId(dto.sessionId ?: "", dto.label ?: ""),
        sessionId = dto.sessionId ?: "",
        label = dto.label ?: "",
        amount = dto.amount,
        position = dto.position,
        entityId = dto.entityId ?: 0L,
        createdAt = PgTime.parseOrEpoch(dto.createdAt).toEpochMilli(),
        createdBy = dto.createdBy,
        updatedAt = PgTime.parseOrEpoch(dto.updatedAt).toEpochMilli(),
        updatedBy = dto.updatedBy,
        isDeleted = dto.isDeleted,
        deletedAt = PgTime.parse(dto.deletedAt)?.toEpochMilli(),
        deletedBy = dto.deletedBy,
    )

    fun toDomain(fee: SessionFeeEntity, heads: List<SessionFeeHeadEntity>): SessionFeeStructure {
        val feeType = runCatching { FeeType.valueOf(fee.cadence) }.getOrDefault(FeeType.SEMESTER)
        val domainHeads = heads.map { head ->
            FeeHead(
                label = head.label,
                amount = head.amount,
                entityId = head.entityId,
                createdAt = Instant.ofEpochMilli(head.createdAt),
                createdBy = head.createdBy,
                updatedAt = Instant.ofEpochMilli(head.updatedAt),
                updatedBy = head.updatedBy,
            )
        }
        return SessionFeeStructure(
            sessionId = fee.sessionId,
            cadence = feeType,
            heads = domainHeads,
            academicYear = fee.academicYear,
            dueDate = fee.dueDate,
            lateFineNote = fee.lateFineNote,
            paymentNote = fee.paymentNote,
            entityId = fee.entityId,
            createdAt = Instant.ofEpochMilli(fee.createdAt),
            createdBy = fee.createdBy,
            updatedAt = Instant.ofEpochMilli(fee.updatedAt),
            updatedBy = fee.updatedBy,
        )
    }

    fun headLocalId(sessionId: String, label: String): String = "${sessionId}_${label.trim().lowercase(Locale.ROOT)}"
}
