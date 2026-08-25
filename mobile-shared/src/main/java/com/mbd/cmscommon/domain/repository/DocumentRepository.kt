package com.mbd.cmscommon.domain.repository

import com.mbd.cmscommon.domain.model.Document
import java.io.File

interface DocumentRepository {
    suspend fun getDocuments(): List<Document>

    suspend fun createTextDocument(
        kind: String,
        title: String,
        body: String,
        audience: String,
        published: Boolean,
        publishedBy: String,
        deptId: String? = null,
        tags: List<String> = emptyList(),
    ): String

    suspend fun uploadDocument(
        kind: String,
        title: String,
        fileBytes: ByteArray,
        fileName: String,
        audience: String,
        published: Boolean,
        publishedBy: String,
        deptId: String? = null,
        tags: List<String> = emptyList(),
    ): String

    suspend fun downloadTo(document: Document, targetDir: File): File
    suspend fun setPublished(id: String, published: Boolean)
    suspend fun deleteDocument(id: String)
}
