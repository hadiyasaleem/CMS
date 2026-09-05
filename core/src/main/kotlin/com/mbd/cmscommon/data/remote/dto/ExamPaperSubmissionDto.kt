package com.mbd.cmscommon.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ExamPaperSubmissionDto(
    val id: String? = null,
    val sessionId: String? = null,
    val semester: Int = 0,
    val courseCode: String? = null,
    val examType: String? = null,
    val teacherEmail: String? = null,
    val storagePath: String? = null,
    val fileName: String? = null,
    val fileSizeBytes: Long? = null,
    val mimeType: String? = null,
    val keyStoragePath: String? = null,
    val teacherNotes: String? = null,
    val reviewStatus: String? = null,
    val reviewedBy: String? = null,
    val reviewedAt: String? = null,
    val uploadedAt: String? = null,
    val createdBy: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: String? = null,
    val deletedBy: String? = null,
)
