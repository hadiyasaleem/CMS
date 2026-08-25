package com.mbd.cmscommon.domain.repository

import com.mbd.cmscommon.domain.model.Fine

interface FineRepository {
    suspend fun getFines(sessionId: String, rollNumber: String): List<Fine>
    suspend fun issueFine(sessionId: String, rollNumber: String, category: String, amount: Double, reason: String, issuedBy: String)
    suspend fun deleteFine(id: String)
}
