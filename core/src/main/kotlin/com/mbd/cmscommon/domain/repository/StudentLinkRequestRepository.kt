package com.mbd.cmscommon.domain.repository

import com.mbd.cmscommon.domain.model.StudentLinkRequest
import kotlinx.coroutines.flow.Flow

data class RosterLinkMatch(
    val exists: Boolean,
    val linkedEmail: String? = null,
)

interface StudentLinkRequestRepository {
    fun observePendingRequests(): Flow<List<StudentLinkRequest>>
    fun observeRequestsForStudentUid(requestedByUid: String): Flow<List<StudentLinkRequest>>

    suspend fun sync()
    suspend fun submitRequest(
        sessionId: String,
        rollNumber: String,
        name: String,
        cnic: String,
        dob: String,
        universityRoll: String?,
        registrationNo: String? = null,
        message: String? = null,
        requestedByUid: String? = null,
    )
    suspend fun rosterHas(sessionId: String?, rollNumber: String): Boolean
    suspend fun rosterLinkMatch(sessionId: String?, rollNumber: String): RosterLinkMatch
    suspend fun approveRequest(requestId: String, reviewedByUid: String)
    suspend fun rejectRequest(requestId: String, reviewedByUid: String, reason: String? = null)
}
