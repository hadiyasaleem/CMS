package com.mbd.cmscommon.domain.repository

import com.mbd.cmscommon.domain.model.AtRiskStudent
import com.mbd.cmscommon.domain.model.ExamStat
import com.mbd.cmscommon.domain.model.SessionOverview

interface InsightsRepository {
    suspend fun getSessionOverviews(): List<SessionOverview>
    suspend fun getAtRiskStudents(): List<AtRiskStudent>
    suspend fun getExamStats(): List<ExamStat>
}
