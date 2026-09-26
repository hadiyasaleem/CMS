package com.mbd.cmscommon.domain.repository

import com.mbd.cmscommon.domain.model.AttendanceEditRequest
import com.mbd.cmscommon.domain.model.AttendanceStatus
import java.time.LocalDate

interface AttendanceEditRequestRepository {
    suspend fun getPendingFor(sessionId: String, courseCode: String, from: LocalDate, to: LocalDate): List<AttendanceEditRequest>
    suspend fun getPendingRequests(): List<AttendanceEditRequest>
    suspend fun submitRequest(
        sessionId: String,
        semester: Int,
        courseCode: String,
        date: LocalDate,
        rollNumber: String,
        currentStatus: AttendanceStatus?,
        currentIsLate: Boolean?,
        requestedStatus: AttendanceStatus,
        requestedIsLate: Boolean,
        reason: String?,
    )
    suspend fun approveRequest(requestId: String, reviewedBy: String)
    suspend fun rejectRequest(requestId: String, reviewedBy: String)
}
