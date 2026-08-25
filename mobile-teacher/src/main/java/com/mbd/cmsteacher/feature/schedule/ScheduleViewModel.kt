package com.mbd.cmsteacher.feature.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import com.mbd.cmscommon.util.Outcome
import com.mbd.cmscommon.util.userMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.DayOfWeek
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    sessionManager: SessionManager,
    private val timetableRepository: SessionTimetableRepository,
    sessionRepository: AcademicSessionRepository,
) : ViewModel() {

    private val teacherId = sessionManager.accountKey.orEmpty()

    val periods = timetableRepository.observeMyPeriods(teacherId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val sessions = sessionRepository.observeAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedDay = MutableStateFlow(DayOfWeek.MONDAY)
    val selectedDay: StateFlow<DayOfWeek> = _selectedDay.asStateFlow()

    private val _outcome = MutableStateFlow<Outcome<Unit>>(Outcome.Success(Unit))
    val outcome: StateFlow<Outcome<Unit>> = _outcome.asStateFlow()

    fun selectDay(day: DayOfWeek) {
        _selectedDay.value = day
    }

    fun refresh() {
        viewModelScope.launch {
            _outcome.value = Outcome.Loading
            _outcome.value = try {
                periods.value.map { it.sessionId }.distinct().forEach { sessionId ->
                    runCatching { timetableRepository.syncSession(sessionId) }
                }
                Outcome.Success(Unit)
            } catch (t: Throwable) {
                Outcome.Error(t.userMessage("Refresh failed. Please try again."), t)
            }
        }
    }
}
