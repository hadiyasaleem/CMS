package com.mbd.cmsstudent.feature.timetable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.domain.model.StudentTimetableSnapshot
import com.mbd.cmscommon.domain.model.studentTimetableSnapshot
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import com.mbd.cmsstudent.feature.common.CurrentStudentProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MyTimetableViewModel @Inject constructor(
    currentStudentProvider: CurrentStudentProvider,
    private val timetableRepository: SessionTimetableRepository,
) : ViewModel() {

    private var currentSessionId: String? = null

    val snapshot = currentStudentProvider.observeContext()
        .distinctUntilChangedBy { it?.studentId }
        .flatMapLatest { context ->
            if (context == null) {
                currentSessionId = null
                flowOf<StudentTimetableSnapshot?>(null)
            } else {
                currentSessionId = context.sessionId
                timetableRepository.observeWeek(context.sessionId)
                    .map { periods -> studentTimetableSnapshot(periods, LocalDate.now(), LocalTime.now()) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun refresh() {
        val sessionId = currentSessionId ?: return
        viewModelScope.launch { runCatching { timetableRepository.syncSession(sessionId) } }
    }
}
