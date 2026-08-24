package com.mbd.cmscommon.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exam_paper_submissions")
data class ExamPaperSubmissionEntity(
    @PrimaryKey val submissionId: String,
    val offeringId: String,
    val subjectId: String,
    val examType: String,
    val teacherId: String,
    val storagePath: String?,
    val fileName: String?,
    val uploadedAt: Long,
    val createdBy: String? = null,
    val entityId: Long = 0L,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val deletedBy: String? = null,
)
