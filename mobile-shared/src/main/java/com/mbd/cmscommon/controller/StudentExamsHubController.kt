package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.Datesheet
import com.mbd.cmscommon.domain.model.DatesheetSlot
import com.mbd.cmscommon.domain.model.SemesterGpa
import com.mbd.cmscommon.domain.model.StudentExamsHubSnapshot
import com.mbd.cmscommon.domain.model.studentExamsHubSnapshot
import com.mbd.cmscommon.domain.repository.DatesheetRepository
import com.mbd.cmscommon.domain.repository.SessionMarksRepository
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class StudentExamsHubController(
    private val sessionId: String,
    private val rollNumber: String,
    private val marksRepository: SessionMarksRepository,
    private val datesheetRepository: DatesheetRepository,
    scope: CoroutineScope,
) : ScreenController(scope) {

    private val results = MutableStateFlow<List<SemesterGpa>>(emptyList())
    private val sheets = MutableStateFlow<List<Datesheet>>(emptyList())
    private val slots = MutableStateFlow<List<DatesheetSlot>>(emptyList())

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    val snapshot: StateFlow<StudentExamsHubSnapshot> = combine(
        marksRepository.observeStudentMarks(sessionId, rollNumber),
        results,
        sheets,
        slots,
    ) { scores, results, sheets, slots ->
        studentExamsHubSnapshot(sessionId, scores, results, sheets, slots, LocalDate.now())
    }.stateIn(
        scope,
        SharingStarted.WhileSubscribed(5000),
        studentExamsHubSnapshot(sessionId, emptyList(), emptyList(), emptyList(), emptyList(), LocalDate.now()),
    )

    init {
        refresh()
    }

    fun refresh() = launch {
        clearError()
        _loading.value = true
        try {
            coroutineScope {
                val marksSync = async { runCatching { marksRepository.syncSession(sessionId) } }
                val resultLoad = async { runCatching { marksRepository.getSemesterGpa(sessionId, rollNumber) } }
                val sheetLoad = async { runCatching { datesheetRepository.getDatesheets() } }

                marksSync.await()
                resultLoad.await().getOrNull()?.let { results.value = it }
                val loadedSheets = sheetLoad.await().getOrDefault(emptyList())
                sheets.value = loadedSheets

                slots.value = loadedSheets
                    .map { sheet -> async { runCatching { datesheetRepository.getSlots(sheet.id) } } }
                    .awaitAll()
                    .flatMap { it.getOrDefault(emptyList()) }
            }
        } finally {
            _loading.value = false
        }
    }
}
