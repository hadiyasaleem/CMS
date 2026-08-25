package com.mbd.cmscommon.domain.model

import java.time.Duration
import java.time.Instant

const val MAX_DOCUMENT_UPLOAD_BYTES = 10485760L

enum class DocumentViewerRole {
    ADMIN,
    TEACHER,
    STUDENT,
}

data class DocumentViewerContext(
    val role: DocumentViewerRole,
    val departmentId: String? = null,
)

data class DocumentDraft(
    val kind: String,
    val title: String,
    val body: String = "",
    val audience: String = "ALL",
    val published: Boolean = true,
    val departmentId: String? = null,
    val tags: List<String> = emptyList(),
)

data class DocumentUploadFile(
    val name: String,
    val bytes: ByteArray,
) {
    val size: Long get() = bytes.size.toLong()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DocumentUploadFile) return false
        return name == other.name && bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int = name.hashCode() * 31 + bytes.contentHashCode()
}

data class DocumentLibrarySummary(
    val totalResources: Int,
    val pdfResources: Int,
    val textResources: Int,
    val roleTargetedResources: Int,
    val departmentResources: Int,
    val recentlyUpdatedResources: Int,
)

fun activityAt(document: Document): Instant = when {
    document.updatedAt != Instant.EPOCH -> document.updatedAt
    document.createdAt != Instant.EPOCH -> document.createdAt
    else -> Instant.EPOCH
}

fun reviewReasons(document: Document, validDepartmentIds: Set<String> = emptySet()): List<String> {
    val reasons = mutableListOf<String>()
    if (document.id.isBlank()) reasons += "Missing record ID"
    if (document.title.isBlank()) reasons += "Missing title"
    if (document.kind.uppercase() !in setOf("PROSPECTUS", "RULES", "REPORT", "OTHER")) reasons += "Unknown document type"
    if (document.audience.uppercase() !in setOf("ALL", "ADMIN", "TEACHER", "STUDENT")) reasons += "Unknown audience"
    if (!document.hasFile && !document.hasBody) reasons += "No readable content"

    val deptId = document.deptId
    if (!deptId.isNullOrBlank() && validDepartmentIds.isNotEmpty() &&
        validDepartmentIds.none { it.equals(deptId, ignoreCase = true) }
    ) {
        reasons += "Department is inactive or missing"
    }
    if (document.published && document.publishedBy.isNullOrBlank()) {
        reasons += "Publisher is not recorded"
    }
    return reasons
}

fun documentLibrarySummary(
    documents: List<Document>,
    viewer: DocumentViewerContext,
    now: Instant = Instant.now(),
): DocumentLibrarySummary {
    val recentThreshold = now.minus(Duration.ofDays(30))
    val roleTargeted = documents.count { it.audience.equals(viewer.role.name, ignoreCase = true) }
    val departmentResources = documents.count { document ->
        val departmentId = viewer.departmentId
        !departmentId.isNullOrBlank() && document.deptId?.equals(departmentId, ignoreCase = true) == true
    }
    val recentlyUpdated = documents.count {
        val activity = activityAt(it)
        activity != Instant.EPOCH && !activity.isBefore(recentThreshold)
    }

    return DocumentLibrarySummary(
        totalResources = documents.size,
        pdfResources = documents.count { it.hasFile },
        textResources = documents.count { it.hasBody },
        roleTargetedResources = roleTargeted,
        departmentResources = departmentResources,
        recentlyUpdatedResources = recentlyUpdated,
    )
}

fun isVisibleTo(document: Document, viewer: DocumentViewerContext): Boolean {
    if (viewer.role == DocumentViewerRole.ADMIN) return true
    if (!document.published) return false
    if (!document.audience.equals("ALL", ignoreCase = true) &&
        !document.audience.equals(viewer.role.name, ignoreCase = true)
    ) {
        return false
    }
    return document.deptId.isNullOrBlank() || document.deptId.equals(viewer.departmentId, ignoreCase = true)
}

fun validationMessage(draft: DocumentDraft, file: DocumentUploadFile? = null): String? {
    if (draft.title.isBlank()) return "Enter a document title."
    if (draft.title.trim().length > 120) return "Keep the document title within 120 characters."
    if (draft.body.length > 50000) return "Keep document text within 50,000 characters."
    if (draft.tags.size > 20 || draft.tags.any { it.length > 40 }) return "Use at most 20 tags, each within 40 characters."
    if (draft.kind.uppercase() !in setOf("PROSPECTUS", "RULES", "REPORT", "OTHER")) return "Choose a valid document type."
    if (draft.audience.uppercase() !in setOf("ALL", "ADMIN", "TEACHER", "STUDENT")) return "Choose a valid audience."
    if (file == null && draft.body.isBlank()) return "Attach a PDF or enter document text."
    if (file == null) return null
    if (!file.name.endsWith(".pdf", ignoreCase = true)) return "Only PDF files can be uploaded to Documents."
    if (file.size == 0L) return "The selected PDF is empty."
    if (file.size > MAX_DOCUMENT_UPLOAD_BYTES) return "The selected PDF exceeds the 10 MB limit."
    return null
}

fun normalizedDocumentTags(tags: List<String>): List<String> =
    tags.asSequence().map { it.trim() }.filter { it.isNotBlank() }.distinctBy { it.lowercase() }.toList()
