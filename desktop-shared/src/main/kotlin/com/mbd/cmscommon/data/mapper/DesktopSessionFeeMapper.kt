package com.mbd.cmscommon.data.mapper

import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.dto.SessionFeeDto
import com.mbd.cmscommon.data.remote.dto.SessionFeeHeadDto
import com.mbd.cmscommon.domain.model.FeeHead
import com.mbd.cmscommon.domain.model.FeeType
import com.mbd.cmscommon.domain.model.SessionFeeStructure

/**
 * Direct DTO<->Domain mapping for session fee structures, for the desktop apps (no local Room
 * cache — just the same field logic mobile's toEntity+toDomain pair does, composed into one
 * step). A [SessionFeeStructure] is assembled from two tables (the fee row + its heads), so
 * [dtoToDomain] takes both DTOs, and saving splits back out into [domainToFeeDto] +
 * [domainToHeadDtos] the same way mobile's saveSessionFee does.
 */
object DesktopSessionFeeMapper {
    fun dtoToDomain(fee: SessionFeeDto, heads: List<SessionFeeHeadDto>): SessionFeeStructure {
        val feeType = runCatching { FeeType.valueOf(fee.cadence ?: "") }.getOrDefault(FeeType.SEMESTER)
        val domainHeads = heads.map { head ->
            FeeHead(
                label = head.label ?: "",
                amount = head.amount,
                entityId = head.entityId ?: 0L,
                createdAt = PgTime.parseOrEpoch(head.createdAt),
                createdBy = head.createdBy,
                updatedAt = PgTime.parseOrEpoch(head.updatedAt),
                updatedBy = head.updatedBy,
            )
        }
        return SessionFeeStructure(
            sessionId = fee.sessionId ?: "",
            cadence = feeType,
            heads = domainHeads,
            academicYear = fee.academicYear,
            dueDate = fee.dueDate,
            lateFineNote = fee.lateFineNote,
            paymentNote = fee.paymentNote,
            entityId = fee.entityId ?: 0L,
            createdAt = PgTime.parseOrEpoch(fee.createdAt),
            createdBy = fee.createdBy,
            updatedAt = PgTime.parseOrEpoch(fee.updatedAt),
            updatedBy = fee.updatedBy,
        )
    }

    fun domainToFeeDto(domain: SessionFeeStructure, updatedBy: String): SessionFeeDto = SessionFeeDto(
        sessionId = domain.sessionId,
        cadence = domain.cadence.name,
        academicYear = domain.academicYear,
        dueDate = domain.dueDate,
        lateFineNote = domain.lateFineNote,
        paymentNote = domain.paymentNote,
        updatedBy = updatedBy,
    )

    fun domainToHeadDtos(domain: SessionFeeStructure, updatedBy: String): List<SessionFeeHeadDto> =
        domain.heads.mapIndexed { index, head ->
            SessionFeeHeadDto(
                sessionId = domain.sessionId,
                label = head.label,
                amount = head.amount,
                position = index,
                updatedBy = updatedBy,
            )
        }
}
