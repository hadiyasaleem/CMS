package com.mbd.cmscommon.data.mapper

import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.dto.DocumentDto
import com.mbd.cmscommon.domain.model.Document

/**
 * Direct DTO<->Domain mapping for the desktop apps (no local Room cache, so there is no
 * Entity intermediate here — just the same field logic mobile's dtoToEntity+entityToDomain
 * pair does, composed into one step). Unlike mobile's Room-backed DocumentEntity, [DocumentDto.tags]
 * is already a `List<String>` (jsonb decoded straight by kotlinx.serialization) so no JSON
 * string round-trip is needed here.
 */
object DesktopDocumentMapper {
    fun dtoToDomain(dto: DocumentDto): Document = Document(
        id = dto.id ?: "",
        kind = dto.kind ?: "",
        title = dto.title ?: "",
        storagePath = dto.storagePath,
        body = dto.body,
        deptId = dto.deptId,
        audience = dto.audience ?: "ALL",
        tags = dto.tags,
        published = dto.published,
        publishedBy = dto.publishedBy,
        entityId = dto.entityId ?: 0L,
        createdAt = PgTime.parseOrEpoch(dto.createdAt),
        createdBy = dto.createdBy,
        updatedAt = PgTime.parseOrEpoch(dto.updatedAt),
        updatedBy = dto.updatedBy,
    )
}
