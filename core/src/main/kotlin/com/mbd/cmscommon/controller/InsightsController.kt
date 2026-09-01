package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AtRiskStudent
import com.mbd.cmscommon.domain.model.ExamStat
import com.mbd.cmscommon.domain.model.SessionOverview
import com.mbd.cmscommon.domain.model.riskSignals
import com.mbd.cmscommon.domain.repository.InsightsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class InsightsController(
    private val repo: InsightsRepository,
    scope: CoroutineScope,
) : ScreenController(scope) {

    private val _overviews = MutableStateFlow<List<SessionOverview>?>(null)
    val overviews: StateFlow<List<SessionOverview>?> = _overviews.asStateFlow()

    private val _atRisk = MutableStateFlow<List<AtRiskStudent>?>(null)
    val atRisk: StateFlow<List<AtRiskStudent>?> = _atRisk.asStateFlow()

    private val _examStats = MutableStateFlow<List<ExamStat>?>(null)
    val examStats: StateFlow<List<ExamStat>?> = _examStats.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    init {
        refresh(fetchRemote = false)
    }

    fun refresh(fetchRemote: Boolean = true) = launch {
        clearError()
        try {
            _refreshing.value = true
            if (fetchRemote) repo.sync()
            coroutineScope {
                val overviewsDeferred = async { repo.getSessionOverviews() }
                val atRiskDeferred = async { repo.getAtRiskStudents() }
                val examStatsDeferred = async { repo.getExamStats() }

                _overviews.value = overviewsDeferred.await()
                _atRisk.value = atRiskDeferred.await().sortedWith(
                    compareByDescending<AtRiskStudent> { riskSignals(it).size }
                        .thenBy { it.attendance ?: Double.MAX_VALUE }
                        .thenBy { it.cgpa ?: Double.MAX_VALUE },
                )
                _examStats.value = examStatsDeferred.await()
            }
        } finally {
            _refreshing.value = false
        }
    }
}
