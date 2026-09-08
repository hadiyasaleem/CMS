package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.Datesheet
import com.mbd.cmscommon.domain.model.DatesheetSlot
import com.mbd.cmscommon.domain.model.SemesterGpa
import com.mbd.cmscommon.domain.model.StudentExamsHubSnapshot
import com.mbd.cmscommon.domain.model.studentExamsHubSnapshot
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DatesheetRepository
import com.mbd.cmscommon.domain.repository.SessionMarksRepository
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn

class StudentExamsHubController(
    private val sessionId: String,
    private val rollNumber: String,
    private val marksRepository: SessionMarksRepository,
    private val datesheetRepository: DatesheetRepository,
    sessionRepository: AcademicSessionRepository,
    scope: CoroutineScope,
) : ScreenController(scope) {

    private val results = MutableStateFlow<List<SemesterGpa>>(emptyList())

    private val session = sessionRepository.observeSession(sessionId)
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    private val sheets: StateFlow<List<Datesheet>> = datesheetRepository.observeDatesheets()
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val allSlots: StateFlow<List<DatesheetSlot>> = datesheetRepository.observeAllSlots()
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _loadError = MutableStateFlow<String?>(null)
    val loadError: StateFlow<String?> = _loadError.asStateFlow()

    val snapshot: StateFlow<StudentExamsHubSnapshot> = combine(
        marksRepository.observeStudentMarks(sessionId, rollNumber),
        results,
        session,
        sheets,
        allSlots,
    ) { scores, results, session, sheets, slots ->
        studentExamsHubSnapshot(sessionId, session?.currentSemester ?: 0, scores, results, sheets, slots, LocalDate.now())
    }.stateIn(
        scope,
        SharingStarted.WhileSubscribed(5000),
        studentExamsHubSnapshot(sessionId, 0, emptyList(), emptyList(), emptyList(), emptyList(), LocalDate.now()),
    )

    init {
        refresh(fetchRemote = false)
    }

    fun refresh(fetchRemote: Boolean = true) = launch {
        clearError()
        _loadError.value = null
        _loading.value = true
        try {
            coroutineScope {
                val marksSync = async { if (fetchRemote) runCatching { marksRepository.syncSession(sessionId) } else Result.success(Unit) }
                val datesheetSync = async { runCatching { if (fetchRemote) { datesheetRepository.sync(); datesheetRepository.syncAllSlots() } } }

                val marksSyncResult = marksSync.await()
                val resultLoad = async { runCatching { marksRepository.getSemesterGpa(sessionId, rollNumber) } }

                val resultLoadResult = resultLoad.await()
                resultLoadResult.getOrNull()?.let { results.value = it }
                val datesheetSyncResult = datesheetSync.await()

                _loadError.value = listOf(marksSyncResult, resultLoadResult, datesheetSyncResult)
                    .firstNotNullOfOrNull { it.exceptionOrNull() }
                    ?.userMessageLogged("Some exam data could not be loaded.")
            }
        } finally {
            _loading.value = false
        }
    }
}
