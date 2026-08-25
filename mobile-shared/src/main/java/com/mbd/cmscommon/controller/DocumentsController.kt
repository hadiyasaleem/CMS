package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.Document
import com.mbd.cmscommon.domain.model.DocumentDraft
import com.mbd.cmscommon.domain.model.DocumentUploadFile
import com.mbd.cmscommon.domain.model.normalizedDocumentTags
import com.mbd.cmscommon.domain.model.validationMessage
import com.mbd.cmscommon.domain.repository.DocumentRepository
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DocumentsController(
    private val repo: DocumentRepository,
    scope: CoroutineScope,
) : ScreenController(scope) {

    private val _docs = MutableStateFlow<List<Document>>(emptyList())
    val docs: StateFlow<List<Document>> = _docs.asStateFlow()

    private val _downloadingId = MutableStateFlow<String?>(null)
    val downloadingId: StateFlow<String?> = _downloadingId.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    init {
        load()
    }

    fun load() {
        clearError()
        launch {
            _loading.value = true
            try {
                _docs.value = repo.getDocuments()
            } finally {
                _loading.value = false
            }
        }
    }

    fun createText(draft: DocumentDraft, publishedBy: String) {
        if (_busy.value) return
        launch {
            clearError()
            _actionMessage.value = null
            validationMessage(draft)?.let { throw IllegalArgumentException(it) }
            _busy.value = true
            try {
                repo.createTextDocument(
                    kind = draft.kind,
                    title = draft.title,
                    body = draft.body,
                    audience = draft.audience,
                    published = draft.published,
                    publishedBy = publishedBy,
                    deptId = draft.departmentId,
                    tags = normalizedDocumentTags(draft.tags),
                )
                _docs.value = repo.getDocuments()
                _actionMessage.value = if (draft.published) "Document published." else "Document saved as a draft."
            } finally {
                _busy.value = false
            }
        }
    }

    fun uploadDocument(draft: DocumentDraft, file: DocumentUploadFile, publishedBy: String) {
        if (_busy.value) return
        launch {
            clearError()
            _actionMessage.value = null
            validationMessage(draft, file)?.let { throw IllegalArgumentException(it) }
            _busy.value = true
            try {
                val title = draft.title.ifBlank { file.name.substringBeforeLast('.') }
                repo.uploadDocument(
                    kind = draft.kind,
                    title = title,
                    fileBytes = file.bytes,
                    fileName = file.name,
                    audience = draft.audience,
                    published = draft.published,
                    publishedBy = publishedBy,
                    deptId = draft.departmentId,
                    tags = normalizedDocumentTags(draft.tags),
                )
                _docs.value = repo.getDocuments()
                _actionMessage.value = if (draft.published) "PDF uploaded and published." else "PDF uploaded as a draft."
            } finally {
                _busy.value = false
            }
        }
    }

    fun setPublished(id: String, published: Boolean) {
        if (_busy.value) return
        launch {
            clearError()
            _actionMessage.value = null
            _busy.value = true
            try {
                repo.setPublished(id, published)
                _docs.value = repo.getDocuments()
                _actionMessage.value = if (published) "Document published." else "Document moved to drafts."
            } finally {
                _busy.value = false
            }
        }
    }

    fun delete(id: String) {
        if (_busy.value) return
        launch {
            clearError()
            _actionMessage.value = null
            _busy.value = true
            try {
                repo.deleteDocument(id)
                _docs.value = repo.getDocuments()
                _actionMessage.value = "Document deleted."
            } finally {
                _busy.value = false
            }
        }
    }

    fun openDocument(document: Document, targetDir: File, opener: (File) -> Unit) {
        if (_downloadingId.value != null) return
        _downloadingId.value = document.id
        launch {
            try {
                val file = repo.downloadTo(document, targetDir)
                opener(file)
            } finally {
                _downloadingId.value = null
            }
        }
    }
}
