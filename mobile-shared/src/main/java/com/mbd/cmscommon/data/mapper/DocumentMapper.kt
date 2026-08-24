package com.mbd.cmscommon.data.mapper

import com.mbd.cmscommon.data.local.entity.DocumentEntity
import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.dto.DocumentDto
import com.mbd.cmscommon.domain.model.Document
import java.time.Instant
import org.json.JSONArray

object DocumentMapper {
    private fun tagsToJson(tags: List<String>): String {
        val array = JSONArray()
        tags.forEach { array.put(it) }
        return array.toString()
    }

    private fun tagsFromJson(json: String): List<String> {
        if (json.isBlank()) return emptyList()
        val array = JSONArray(json)
        return (0 until array.length()).map { array.getString(it) }
    }

    fun dtoToEntity(dto: DocumentDto): DocumentEntity = DocumentEntity(
        documentId = dto.id ?: "",
        kind = dto.kind ?: "",
        title = dto.title ?: "",
        storagePath = dto.storagePath,
        body = dto.body,
        deptId = dto.deptId,
        audience = dto.audience ?: "ALL",
        tagsJson = tagsToJson(dto.tags),
        published = dto.published,
        publishedBy = dto.publishedBy,
        entityId = dto.entityId ?: 0L,
        createdAt = PgTime.parseOrEpoch(dto.createdAt).toEpochMilli(),
        createdBy = dto.createdBy,
        updatedAt = PgTime.parseOrEpoch(dto.updatedAt).toEpochMilli(),
        updatedBy = dto.updatedBy,
    )

    fun entityToDomain(entity: DocumentEntity): Document = Document(
        id = entity.documentId,
        kind = entity.kind,
        title = entity.title,
        storagePath = entity.storagePath,
        body = entity.body,
        deptId = entity.deptId,
        audience = entity.audience,
        tags = tagsFromJson(entity.tagsJson),
        published = entity.published,
        publishedBy = entity.publishedBy,
        entityId = entity.entityId,
        createdAt = Instant.ofEpochMilli(entity.createdAt),
        createdBy = entity.createdBy,
        updatedAt = Instant.ofEpochMilli(entity.updatedAt),
        updatedBy = entity.updatedBy,
    )
}
