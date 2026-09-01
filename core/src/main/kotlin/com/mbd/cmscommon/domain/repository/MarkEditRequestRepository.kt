package com.mbd.cmscommon.domain.repository

import com.mbd.cmscommon.domain.model.ExamType
import com.mbd.cmscommon.domain.model.MarkEditRequest

interface MarkEditRequestRepository {
    suspend fun sync() = Unit
    suspend fun getPendingForAssignment(sessionId: String, courseCode: String, examType: ExamType): List<MarkEditRequest>
    suspend fun getPendingRequests(): List<MarkEditRequest>
    suspend fun submitRequest(
        sessionId: String,
        semester: Int,
        courseCode: String,
        examType: ExamType,
        rollNumber: String,
        currentScore: Int?,
        requestedScore: Int,
        reason: String?,
        requestedBy: String,
    )
    suspend fun approveRequest(requestId: String, reviewedBy: String)
    suspend fun rejectRequest(requestId: String, reviewedBy: String)
}
