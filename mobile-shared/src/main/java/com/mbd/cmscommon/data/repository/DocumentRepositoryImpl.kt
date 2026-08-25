package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.data.local.dao.DocumentDao
import com.mbd.cmscommon.data.mapper.DocumentMapper
import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.DocumentDto
import com.mbd.cmscommon.data.sync.SyncCheckpoint
import com.mbd.cmscommon.data.sync.SyncCheckpointDefaults
import com.mbd.cmscommon.data.sync.SyncCheckpointStore
import com.mbd.cmscommon.data.sync.maxRemoteUpdatedAt
import com.mbd.cmscommon.domain.model.Document
import com.mbd.cmscommon.domain.repository.DocumentRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.Storage
import java.io.File
import java.time.Instant
import java.util.Locale
import javax.inject.Inject

class DocumentRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val storage: Storage,
    private val documentDao: DocumentDao,
    private val checkpointStore: SyncCheckpointStore,
    private val sessionManager: SessionManager,
) : DocumentRepository {

    private fun syncOwnerKey(): String = sessionManager.accountKey ?: SyncCheckpointDefaults.ownerKey("anonymous-local")

    override suspend fun getDocuments(): List<Document> {
        runCatching { syncDocuments() }
        return documentDao.getAll().map { DocumentMapper.entityToDomain(it) }
    }

    override suspend fun createTextDocument(
        kind: String,
        title: String,
        body: String,
        audience: String,
        published: Boolean,
        publishedBy: String,
        deptId: String?,
        tags: List<String>,
    ): String {
        val dto = DocumentDto(
            kind = kind,
            title = title.trim(),
            body = body.trim().takeIf { it.isNotBlank() },
            deptId = deptId?.trim()?.takeIf { it.isNotBlank() },
            audience = audience,
            tags = tags.takeIf { it.isNotEmpty() } ?: emptyList(),
            published = published,
            publishedBy = publishedBy,
        )
        val inserted = postgrest.from(SupabaseTables.DOCUMENTS).insert(dto) { select() }.decodeList<DocumentDto>().first()
        documentDao.upsertAll(listOf(DocumentMapper.dtoToEntity(inserted)))
        return inserted.id ?: ""
    }

    override suspend fun uploadDocument(
        kind: String,
        title: String,
        fileBytes: ByteArray,
        fileName: String,
        audience: String,
        published: Boolean,
        publishedBy: String,
        deptId: String?,
        tags: List<String>,
    ): String {
        val safeName = fileName.replace(Regex("[^A-Za-z0-9._-]"), "_")
        val storagePath = "${kind.lowercase(Locale.ROOT)}/${System.currentTimeMillis()}_$safeName"
        storage.from(DOCUMENTS_BUCKET).upload(storagePath, fileBytes) { upsert = true }

        val dto = DocumentDto(
            kind = kind,
            title = title.trim(),
            storagePath = storagePath,
            deptId = deptId?.trim()?.takeIf { it.isNotBlank() },
            audience = audience,
            tags = tags.takeIf { it.isNotEmpty() } ?: emptyList(),
            published = published,
            publishedBy = publishedBy,
        )
        val inserted = postgrest.from(SupabaseTables.DOCUMENTS).insert(dto) { select() }.decodeList<DocumentDto>().first()
        documentDao.upsertAll(listOf(DocumentMapper.dtoToEntity(inserted)))
        return inserted.id ?: ""
    }

    override suspend fun downloadTo(document: Document, targetDir: File): File {
        val path = document.storagePath ?: error("Document has no file")
        val bytes = storage.from(DOCUMENTS_BUCKET).downloadAuthenticated(path)
        val localFile = File(targetDir, document.fileName)
        localFile.writeBytes(bytes)
        return localFile
    }

    override suspend fun setPublished(id: String, published: Boolean) {
        postgrest.from(SupabaseTables.DOCUMENTS).update({ set("published", published) }) {
            filter { eq("id", id) }
        }
        runCatching { syncDocuments() }
    }

    override suspend fun deleteDocument(id: String) {
        val local = documentDao.getById(id)
        val storagePath = local?.storagePath
            ?: runCatching {
                postgrest.from(SupabaseTables.DOCUMENTS).select { filter { eq("id", id) } }
                    .decodeList<DocumentDto>().firstOrNull()?.storagePath
            }.getOrNull()

        if (!storagePath.isNullOrBlank()) {
            runCatching { storage.from(DOCUMENTS_BUCKET).delete(storagePath) }
        }

        postgrest.from(SupabaseTables.DOCUMENTS).update({ set("is_deleted", true) }) {
            filter { eq("id", id) }
        }
        documentDao.deleteById(id)
    }

    private suspend fun syncDocuments() {
        val ownerKey = syncOwnerKey()
        val scopeKey = SyncCheckpointDefaults.globalScope()
        val checkpoint = checkpointStore.get(ownerKey, SupabaseTables.DOCUMENTS, scopeKey)
        val since = checkpoint?.lastUpdatedAt ?: SyncCheckpointDefaults.EPOCH
        var maxUpdatedAt = since

        var offset = 0L
        while (true) {
            val page = postgrest.from(SupabaseTables.DOCUMENTS).select {
                filter { gte("updated_at", since) }
                order("updated_at", Order.ASCENDING)
                range(offset, offset + PAGE_SIZE - 1)
            }.decodeList<DocumentDto>()
            if (page.isEmpty()) break

            val entities = page.map { DocumentMapper.dtoToEntity(it) }
            val (deleted, active) = entities.partition { it.isDeleted }
            documentDao.applyDelta(active, deleted.map { it.documentId })
            maxUpdatedAt = page.maxRemoteUpdatedAt(maxUpdatedAt) { it.updatedAt }

            if (page.size < PAGE_SIZE) break
            offset += PAGE_SIZE
        }

        checkpointStore.upsert(SyncCheckpoint(ownerKey, SupabaseTables.DOCUMENTS, scopeKey, maxUpdatedAt, PgTime.format(Instant.now()) ?: since))
    }

    private companion object {
        const val PAGE_SIZE = 500L
        const val DOCUMENTS_BUCKET = "documents"
    }
}
