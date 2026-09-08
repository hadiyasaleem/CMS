package com.mbd.cmscommon.domain.repository

import com.mbd.cmscommon.domain.model.Fine

interface FineRepository {
    suspend fun getFines(sessionId: String, rollNumber: String): List<Fine>
    suspend fun sync(sessionId: String, rollNumber: String) = Unit

    /** Bulk delta sync for a whole session in one request -- use this for bootstrap/background
     * refresh instead of calling [sync] once per student. */
    suspend fun syncSession(sessionId: String) = Unit
    suspend fun issueFine(sessionId: String, rollNumber: String, category: String, amount: Double, reason: String, issuedBy: String)
    suspend fun deleteFine(id: String)
}
