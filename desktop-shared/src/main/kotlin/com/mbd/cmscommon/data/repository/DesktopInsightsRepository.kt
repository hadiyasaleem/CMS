package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.data.mapper.DesktopInsightsMapper
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.AtRiskStudentDto
import com.mbd.cmscommon.data.remote.dto.ExamStatDto
import com.mbd.cmscommon.data.remote.dto.SessionOverviewDto
import com.mbd.cmscommon.domain.model.AtRiskStudent
import com.mbd.cmscommon.domain.model.ExamStat
import com.mbd.cmscommon.domain.model.SessionOverview
import com.mbd.cmscommon.domain.repository.InsightsRepository
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Desktop repos are always-online: no local persistence, every call re-fetches from Postgrest.
 *
 * [InsightsRepository] exposes no `observe*`/`sync()` methods — every consumer just calls the
 * suspend getters directly — so there is nothing for a screen to subscribe to. The three caches
 * below are kept purely as small in-memory memoizations of the last fetch (mirroring the
 * template's cache-then-refresh shape) rather than being exposed as Flows; each getter always
 * re-fetches and refreshes its cache, it never serves a stale read from it.
 */
@Singleton
class DesktopInsightsRepository @Inject constructor(
    private val postgrest: Postgrest,
) : InsightsRepository {

    private val sessionOverviewCache = MutableStateFlow<List<SessionOverview>>(emptyList())
    private val atRiskStudentCache = MutableStateFlow<List<AtRiskStudent>>(emptyList())
    private val examStatCache = MutableStateFlow<List<ExamStat>>(emptyList())

    override suspend fun getSessionOverviews(): List<SessionOverview> {
        val rows = postgrest.from(SupabaseTables.VIEW_SESSION_OVERVIEW).select()
            .decodeList<SessionOverviewDto>().map { DesktopInsightsMapper.sessionOverviewToDomain(it) }
        sessionOverviewCache.value = rows
        return rows
    }

    override suspend fun getAtRiskStudents(): List<AtRiskStudent> {
        val rows = postgrest.from(SupabaseTables.VIEW_AT_RISK_STUDENTS).select()
            .decodeList<AtRiskStudentDto>().map { DesktopInsightsMapper.atRiskStudentToDomain(it) }
        atRiskStudentCache.value = rows
        return rows
    }

    override suspend fun getExamStats(): List<ExamStat> {
        val rows = postgrest.from(SupabaseTables.VIEW_EXAM_STATS).select()
            .decodeList<ExamStatDto>().map { DesktopInsightsMapper.examStatToDomain(it) }
        examStatCache.value = rows
        return rows
    }
}
