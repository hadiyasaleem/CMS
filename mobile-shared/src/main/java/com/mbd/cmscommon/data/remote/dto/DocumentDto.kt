package com.mbd.cmscommon.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class DocumentDto(
    val entityId: Long? = null,
    val id: String? = null,
    val kind: String? = null,
    val title: String? = null,
    val storagePath: String? = null,
    val body: String? = null,
    val deptId: String? = null,
    val audience: String? = null,
    val tags: List<String> = emptyList(),
    val published: Boolean = false,
    val publishedBy: String? = null,
    val createdAt: String? = null,
    val createdBy: String? = null,
    val updatedAt: String? = null,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: String? = null,
    val deletedBy: String? = null,
)
