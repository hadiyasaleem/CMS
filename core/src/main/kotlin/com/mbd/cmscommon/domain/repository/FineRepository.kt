package com.mbd.cmscommon.domain.repository

import com.mbd.cmscommon.domain.model.Fine

interface FineRepository {
    suspend fun getFines(sessionId: String, rollNumber: String): List<Fine>
    suspend fun sync(sessionId: String, rollNumber: String) = Unit

    /** Bulk delta sync for a whole session in one request -- use this for bootstrap/background
     * refresh instead of calling [sync] once per student. */
    suspend fun syncSession(sessionId: String) = Unit

    /** One delta query across every session's fines instead of one per session -- RLS already
     * restricts the rows a non-admin caller gets back, so this is a strict improvement for every role. */
    suspend fun syncAll() = Unit
    suspend fun issueFine(sessionId: String, rollNumber: String, category: String, amount: Double, reason: String, issuedBy: String)
    suspend fun deleteFine(id: String)
}
