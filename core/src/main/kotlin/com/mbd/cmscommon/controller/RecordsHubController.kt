package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.RecordsHubSnapshot
import com.mbd.cmscommon.domain.model.RecordsSummarySource
import com.mbd.cmscommon.domain.model.recordsHubSnapshot
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.CalendarRepository
import com.mbd.cmscommon.domain.repository.DatesheetRepository
import com.mbd.cmscommon.domain.repository.DocumentRepository
import com.mbd.cmscommon.domain.repository.InsightsRepository
import com.mbd.cmscommon.util.userMessage
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
    private val documentRepository: DocumentRepository,
    private val insightsRepository: InsightsRepository,
    scope: CoroutineScope,
    private val today: () -> LocalDate = { LocalDate.now() },
) : ScreenController(scope) {

    private val _snapshot = MutableStateFlow<RecordsHubSnapshot?>(null)
    val snapshot: StateFlow<RecordsHubSnapshot?> = _snapshot.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _loadError = MutableStateFlow<String?>(null)
    val loadError: StateFlow<String?> = _loadError.asStateFlow()

    private var loadVersion = 0

    init {
        refresh()
    }

    fun refresh() {
        loadVersion++
        val version = loadVersion
        launch {
            _loading.value = true
            _loadError.value = null
            supervisorScope {
                val sessionsDeferred = async { runCatching { sessionRepository.observeAllSessions().first() } }
                val eventsDeferred = async { runCatching { calendarRepository.getEvents() } }
                val datesheetsDeferred = async { runCatching { datesheetRepository.getDatesheets() } }
                val documentsDeferred = async { runCatching { documentRepository.getDocuments() } }
                val risksDeferred = async { runCatching { insightsRepository.getAtRiskStudents() } }

                val sessionsResult = sessionsDeferred.await()
                val eventsResult = eventsDeferred.await()
                val datesheetsResult = datesheetsDeferred.await()
                val documentsResult = documentsDeferred.await()
                val risksResult = risksDeferred.await()

                if (version == loadVersion) {
                    val unavailableSources = buildSet {
                        if (sessionsResult.isFailure) add(RecordsSummarySource.SESSIONS)
                        if (eventsResult.isFailure) add(RecordsSummarySource.CALENDAR)
                        if (datesheetsResult.isFailure) add(RecordsSummarySource.DATESHEETS)
                        if (documentsResult.isFailure) add(RecordsSummarySource.DOCUMENTS)
                        if (risksResult.isFailure) add(RecordsSummarySource.INSIGHTS)
                    }

                    _snapshot.value = recordsHubSnapshot(
                        sessionsResult.getOrDefault(emptyList()),
                        eventsResult.getOrDefault(emptyList()),
                        datesheetsResult.getOrDefault(emptyList()),
                        documentsResult.getOrDefault(emptyList()),
                        risksResult.getOrDefault(emptyList()),
                        today(),
                        unavailableSources,
                    )
                    _loadError.value = listOf(sessionsResult, eventsResult, datesheetsResult, documentsResult, risksResult)
                        .firstNotNullOfOrNull { it.exceptionOrNull() }
                        ?.userMessage("Some record summaries could not be loaded.")
                    _loading.value = false
                }
            }
        }
    }
}
