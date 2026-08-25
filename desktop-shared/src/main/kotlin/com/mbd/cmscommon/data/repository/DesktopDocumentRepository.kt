package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.data.mapper.DesktopDocumentMapper
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.DocumentDto
import com.mbd.cmscommon.domain.model.Document
import com.mbd.cmscommon.domain.repository.DocumentRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import java.io.File
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * [DocumentRepository] is a plain suspend-fun interface (no `observe*`/`sync()`), so every screen
 * re-fetches on demand via [getDocuments]. We still keep a small in-memory cache mirroring the last
 * fetch, refreshed as a side effect of every mutating call — mirrors mobile's Room cache without a
 * local DB.
 */
@Singleton
class DesktopDocumentRepository @Inject constructor(
    private val postgrest: Postgrest,
    private val storage: Storage,
) : DocumentRepository {

    private val cache = MutableStateFlow<List<Document>>(emptyList())

    override suspend fun getDocuments(): List<Document> {
        val rows = postgrest.from(SupabaseTables.DOCUMENTS).select {
            filter { eq("is_deleted", false) }
        }.decodeList<DocumentDto>()
        val documents = rows.map { DesktopDocumentMapper.dtoToDomain(it) }
        cache.value = documents
        return documents
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
        getDocuments()
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
        storage.from(SupabaseTables.BUCKET_DOCUMENTS).upload(storagePath, fileBytes) { upsert = true }

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
        getDocuments()
        return inserted.id ?: ""
    }

    override suspend fun downloadTo(document: Document, targetDir: File): File {
        val path = document.storagePath ?: error("Document has no file")
        val bytes = storage.from(SupabaseTables.BUCKET_DOCUMENTS).downloadAuthenticated(path)
        val localFile = File(targetDir, document.fileName)
        localFile.writeBytes(bytes)
        return localFile
    }

    override suspend fun setPublished(id: String, published: Boolean) {
        postgrest.from(SupabaseTables.DOCUMENTS).update({ set("published", published) }) {
            filter { eq("id", id) }
        }
        getDocuments()
    }

    override suspend fun deleteDocument(id: String) {
        val storagePath = cache.value.find { it.id == id }?.storagePath
            ?: runCatching {
                postgrest.from(SupabaseTables.DOCUMENTS).select { filter { eq("id", id) } }
                    .decodeList<DocumentDto>().firstOrNull()?.storagePath
            }.getOrNull()

        if (!storagePath.isNullOrBlank()) {
            runCatching { storage.from(SupabaseTables.BUCKET_DOCUMENTS).delete(storagePath) }
        }

        postgrest.from(SupabaseTables.DOCUMENTS).update({ set("is_deleted", true) }) {
            filter { eq("id", id) }
        }
        getDocuments()
    }
}
