package com.mbd.cmscommon.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exam_paper_submissions")
data class ExamPaperSubmissionEntity(
    @PrimaryKey val submissionId: String,
    val datesheetSlotId: String,
    val offeringId: String,
    val semester: Int,
    val subjectId: String,
    val teacherId: String,
    val storagePath: String?,
    val fileName: String?,
    val fileSizeBytes: Long? = null,
    val uploadedAt: Long,
    val mimeType: String? = null,
    val description: String? = null,
    val createdBy: String? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val deletedBy: String? = null,
)
