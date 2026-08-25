package com.mbd.cmscommon.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "documents",
    indices = [
        Index(value = ["kind", "deptId", "published"]),
        Index(value = ["updatedAt", "entityId"]),
    ],
)
data class DocumentEntity(
    @PrimaryKey val documentId: String,
    val kind: String,
    val title: String,
    val storagePath: String?,
    val body: String?,
    val deptId: String?,
    val audience: String,
    val tagsJson: String,
    val published: Boolean = false,
    val publishedBy: String?,
    val entityId: Long = 0L,
    val createdAt: Long = 0L,
    val createdBy: String? = null,
    val updatedAt: Long = 0L,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val deletedBy: String? = null,
)
