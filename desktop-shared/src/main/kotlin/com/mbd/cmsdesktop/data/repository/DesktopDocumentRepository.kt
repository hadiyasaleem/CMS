package com.mbd.cmsdesktop.data.repository

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

/**
 * No in-memory cache at all — [DocumentRepository] is a plain suspend-fun interface with no
 * `observe*`/`sync()`, so every screen just calls [getDocuments] fresh. [deleteDocument] looks the
 * row up first (to resolve its storage path) before hard-deleting it and best-effort removing the
 * uploaded file.
 */
@Singleton
class DesktopDocumentRepository @Inject constructor(
    private val postgrest: Postgrest,
    private val storage: Storage,
) : DocumentRepository {

    override suspend fun getDocuments(): List<Document> {
        val rows = postgrest.from(SupabaseTables.DOCUMENTS).select {
            filter { eq("is_deleted", false) }
        }.decodeList<DocumentDto>()
        return rows.map { DesktopDocumentMapper.dtoToDomain(it) }
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
            tags = tags,
            published = published,
            publishedBy = publishedBy,
        )
        val inserted = postgrest.from(SupabaseTables.DOCUMENTS).insert(dto) { select() }.decodeList<DocumentDto>().first()
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
            tags = tags,
            published = published,
            publishedBy = publishedBy,
        )
        val inserted = postgrest.from(SupabaseTables.DOCUMENTS).insert(dto) { select() }.decodeList<DocumentDto>().first()
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
    }

    override suspend fun deleteDocument(id: String) {
        val existing = runCatching {
            postgrest.from(SupabaseTables.DOCUMENTS).select { filter { eq("id", id) } }
                .decodeList<DocumentDto>().firstOrNull()
        }.getOrNull()

        postgrest.from(SupabaseTables.DOCUMENTS).delete { filter { eq("id", id) } }

        val path = existing?.storagePath
        if (!path.isNullOrBlank()) {
            runCatching { storage.from(SupabaseTables.BUCKET_DOCUMENTS).delete(path) }
        }
    }
}
