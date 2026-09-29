package com.mbd.cmsstudent.feature.datesheets

import com.mbd.cmscommon.util.FailureSummary
import com.mbd.cmscommon.controller.studentDatesheet
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.domain.model.Datesheet
import com.mbd.cmscommon.domain.model.DatesheetSlot
import com.mbd.cmscommon.domain.repository.DatesheetRepository
import com.mbd.cmsstudent.feature.common.CurrentStudentProvider
import com.mbd.cmsstudent.feature.common.StudentContext
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** The student's own Mid Term datesheet: their session's current semester's published papers only. */
@HiltViewModel
class StudentDatesheetsViewModel @Inject constructor(
    currentStudentProvider: CurrentStudentProvider,
    private val datesheetRepository: DatesheetRepository,
) : ViewModel() {

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val context: StateFlow<StudentContext?> = currentStudentProvider.observeContext()
        .distinctUntilChangedBy { it?.studentId to it?.shift }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val sheet: StateFlow<Datesheet?> = context
        .flatMapLatest { ctx ->
            if (ctx == null) {
                flowOf(null)
            } else {
                datesheetRepository.observeDatesheets().map { sheets ->
                    // Datesheets are per shift: the student's own shift's published sheet only.
                    studentDatesheet(sheets, ctx.sessionId, ctx.session?.currentSemester, ctx.shift)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val slots: StateFlow<List<DatesheetSlot>> = sheet
        .flatMapLatest { s -> if (s == null) flowOf(emptyList()) else datesheetRepository.observeSlots(s.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val syncResult = runCatching { datesheetRepository.sync(); datesheetRepository.syncAllSlots() }
                _error.value = FailureSummary.describe(FailureSummary.of(listOf("datesheets" to syncResult)), "StudentDatesheetsViewModel")
            } finally {
                _loading.value = false
            }
        }
    }
}
