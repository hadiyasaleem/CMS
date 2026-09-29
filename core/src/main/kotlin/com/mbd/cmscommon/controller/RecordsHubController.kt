package com.mbd.cmscommon.controller

import com.mbd.cmscommon.util.FailureSummary
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.AtRiskStudent
import com.mbd.cmscommon.domain.model.CalendarEvent
import com.mbd.cmscommon.domain.model.Datesheet
import com.mbd.cmscommon.domain.model.ShiftScope
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.model.RecordsHubSnapshot
import com.mbd.cmscommon.domain.model.RecordsSummarySource
import com.mbd.cmscommon.domain.model.recordsHubSnapshot
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.CalendarRepository
import com.mbd.cmscommon.domain.repository.DatesheetRepository
import com.mbd.cmscommon.domain.repository.InsightsRepository
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.supervisorScope

class RecordsHubController(
    private val sessionRepository: AcademicSessionRepository,
    private val calendarRepository: CalendarRepository,
    private val datesheetRepository: DatesheetRepository,
    private val insightsRepository: InsightsRepository,
    scope: CoroutineScope,
    private val today: () -> LocalDate = { LocalDate.now() },
    /** Department names for the filter; without it departments are shown by their code. */
    private val departmentRepository: DepartmentRepository? = null,
) : ScreenController(scope) {

    private val _filterScope = MutableStateFlow(ShiftScope.ALL)

    /** The Department -> Session -> Shift filter the hub's counts follow. */
    val filterScope: StateFlow<ShiftScope> = _filterScope.asStateFlow()

    private val _filterOptions = MutableStateFlow(ScopeFilterOptions())
    val filterOptions: StateFlow<ScopeFilterOptions> = _filterOptions.asStateFlow()

    fun setFilterScope(scope: ShiftScope) {
        _filterScope.value = scope
        publish()
    }

    private var sources: RecordsHubSources? = null

    private val _snapshot = MutableStateFlow<RecordsHubSnapshot?>(null)
    val snapshot: StateFlow<RecordsHubSnapshot?> = _snapshot.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _loadError = MutableStateFlow<String?>(null)
    val loadError: StateFlow<String?> = _loadError.asStateFlow()

    private var loadVersion = 0

    init {
        refresh(fetchRemote = false)
    }

    fun refresh(fetchRemote: Boolean = true) {
        loadVersion++
        val version = loadVersion
        launch {
            _loading.value = true
            _loadError.value = null
            supervisorScope {
                val sessionsDeferred = async { runCatching { sessionRepository.observeAllSessions().first() } }
                val eventsDeferred = async { runCatching { if (fetchRemote) calendarRepository.sync(); calendarRepository.getEvents() } }
                val datesheetsDeferred = async { runCatching { if (fetchRemote) datesheetRepository.sync(); datesheetRepository.observeDatesheets().first() } }
                val risksDeferred = async { runCatching { if (fetchRemote) insightsRepository.sync(); insightsRepository.getAtRiskStudents() } }
                val departmentsDeferred = async { runCatching { departmentRepository?.observeActiveDepartments()?.first() } }

                val sessionsResult = sessionsDeferred.await()
                val eventsResult = eventsDeferred.await()
                val datesheetsResult = datesheetsDeferred.await()
                val risksResult = risksDeferred.await()

                if (version == loadVersion) {
                    val unavailableSources = buildSet {
                        if (sessionsResult.isFailure) add(RecordsSummarySource.SESSIONS)
                        if (eventsResult.isFailure) add(RecordsSummarySource.CALENDAR)
                        if (datesheetsResult.isFailure) add(RecordsSummarySource.DATESHEETS)
                        if (risksResult.isFailure) add(RecordsSummarySource.INSIGHTS)
                    }

                    val sessions = sessionsResult.getOrDefault(emptyList())
                    sources = RecordsHubSources(
                        sessions,
                        eventsResult.getOrDefault(emptyList()),
                        datesheetsResult.getOrDefault(emptyList()),
                        risksResult.getOrDefault(emptyList()),
                        unavailableSources,
                    )
                    _filterOptions.value = departmentsDeferred.await().getOrNull()?.let { ScopeFilterOptions.of(it, sessions) }
                        ?: ScopeFilterOptions(sessions.map { it.deptId to it.deptId.uppercase() }.distinct(), sessions)
                    publish()
                    _loadError.value = FailureSummary.describe(
                        FailureSummary.of(
                            listOf(
                                "sessions" to sessionsResult,
                                "calendar events" to eventsResult,
                                "datesheets" to datesheetsResult,
                                "at-risk students" to risksResult,
                            ),
                        ),
                        "RecordsHubController",
                    )
                    _loading.value = false
                }
            }
        }
    }

    private fun publish() {
        val current = sources ?: return
        _snapshot.value = recordsHubSnapshotInScope(current, _filterScope.value, today())
    }
}

data class RecordsHubSources(
    val sessions: List<AcademicSession>,
    val events: List<CalendarEvent>,
    val datesheets: List<Datesheet>,
    val atRisk: List<AtRiskStudent>,
    val unavailableSources: Set<RecordsSummarySource> = emptySet(),
)

/** The Records hub counts inside a scope: its sessions, the events that reach it, its datesheets and at-risk students. */
fun recordsHubSnapshotInScope(sources: RecordsHubSources, scope: ShiftScope, today: LocalDate): RecordsHubSnapshot =
    recordsHubSnapshot(
        sources.sessions.inScope(scope),
        sources.events.inScope(scope, sources.sessions),
        if (scope.isEmpty) sources.datesheets else sources.datesheets.filter { scope.matchesSessionItem(it.sessionId, it.shift, sources.sessions) },
        scopeInsights(emptyList(), sources.atRisk, emptyList(), scope, sources.sessions).atRisk,
        today,
        sources.unavailableSources,
    )
