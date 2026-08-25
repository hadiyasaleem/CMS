package com.mbd.cmscommon.domain.model

import java.time.Instant

data class Document(
    val id: String,
    val kind: String,
    val title: String,
    val storagePath: String? = null,
    val body: String? = null,
    val deptId: String? = null,
    val audience: String = "ALL",
    val tags: List<String> = emptyList(),
    val published: Boolean = false,
    val publishedBy: String? = null,
    override val entityId: Long = 0L,
    override val createdAt: Instant = Instant.EPOCH,
    override val createdBy: String? = null,
    override val updatedAt: Instant = Instant.EPOCH,
    override val updatedBy: String? = null,
) : BaseEntity() {
    val hasFile: Boolean get() = !storagePath.isNullOrBlank()
    val hasBody: Boolean get() = !body.isNullOrBlank()
    val fileName: String get() =
        storagePath?.substringAfterLast('/')?.takeIf { it.isNotBlank() } ?: "$title.pdf"
}
