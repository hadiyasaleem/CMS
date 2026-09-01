package com.mbd.cmsstudent.feature.exams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.domain.model.StudentExamsHubSnapshot
import com.mbd.cmscommon.domain.model.studentExamsHubSnapshot
import com.mbd.cmscommon.domain.repository.DatesheetRepository
import com.mbd.cmscommon.domain.repository.SessionMarksRepository
import com.mbd.cmsstudent.feature.common.CurrentStudentProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class StudentExamsHubViewModel @Inject constructor(
    currentStudentProvider: CurrentStudentProvider,
    private val marksRepository: SessionMarksRepository,
    private val datesheetRepository: DatesheetRepository,
) : ViewModel() {

    private val _refreshTrigger = MutableStateFlow(0)
    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    val snapshot: StateFlow<StudentExamsHubSnapshot?> = currentStudentProvider.observeContext()
        .distinctUntilChangedBy { it?.studentId }
        .flatMapLatest { context ->
            if (context == null) {
                _loading.value = false // unlinked/no student: resolve loading so the UI can show an empty state
                flowOf<StudentExamsHubSnapshot?>(null)
            } else {
                _refreshTrigger.map {
                    _loading.value = true
                    try {
                        // Pull remote marks before reading local cache (mirrors StudentExamsHubController.refresh),
                        // otherwise the hub only ever shows stale/empty cached data.
                        runCatching { marksRepository.syncSession(context.sessionId) }
                        val scores = runCatching { marksRepository.observeStudentMarks(context.sessionId, context.rollNumber).first() }.getOrDefault(emptyList())
                        val results = runCatching { marksRepository.getSemesterGpa(context.sessionId, context.rollNumber) }.getOrDefault(emptyList())
                        val datesheets = runCatching { datesheetRepository.getDatesheets() }.getOrDefault(emptyList())
                        val slots = datesheets.flatMap { sheet -> runCatching { datesheetRepository.getSlots(sheet.id) }.getOrDefault(emptyList()) }
                        studentExamsHubSnapshot(
                            sessionId = context.sessionId,
                            scores = scores,
                            results = results,
                            datesheets = datesheets,
                            slots = slots,
                            today = LocalDate.now(),
                        )
                    } finally {
                        _loading.value = false
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun refresh() {
        viewModelScope.launch { _refreshTrigger.value += 1 }
    }
}
