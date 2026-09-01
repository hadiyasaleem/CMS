package com.mbd.cmsteacher.feature.attendance

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.DailyAttendanceMark
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.SessionAttendanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AttendanceHistoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sessionManager: SessionManager,
    private val attendanceRepository: SessionAttendanceRepository,
    private val sessionRepository: AcademicSessionRepository,
) : ViewModel() {

    val sessionId: String = checkNotNull(savedStateHandle["sessionId"])
    val courseCode: String = checkNotNull(savedStateHandle["courseCode"])

    private val _month = MutableStateFlow(LocalDate.now().withDayOfMonth(1))
    val monthLabel: StateFlow<String> = _month.map { it.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    val roster: StateFlow<List<SessionStudent>> = sessionRepository.observeStudents(sessionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val session: StateFlow<AcademicSession?> = sessionRepository.observeSession(sessionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _marks = MutableStateFlow<Map<String, Map<LocalDate, DailyAttendanceMark>>>(emptyMap())
    val marks: StateFlow<Map<String, Map<LocalDate, DailyAttendanceMark>>> = _marks.asStateFlow()

    init {
        viewModelScope.launch { loadMonth() }
    }

    private suspend fun loadMonth() {
        _loading.value = true
        try {
            val from = _month.value
            val to = from.withDayOfMonth(from.lengthOfMonth())
            val dailyMarks = attendanceRepository.marksBetween(sessionId, courseCode, from, to)
            _marks.value = dailyMarks.groupBy { it.rollNumber }
                .mapValues { (_, marks) -> marks.associateBy { it.date } }
        } catch (t: Throwable) {
            // A repository failure here would otherwise be uncaught in viewModelScope and crash the
            // app; keep the previously loaded marks and just clear the loading flag.
        } finally {
            _loading.value = false
        }
    }

    fun previousMonth() {
        _month.value = _month.value.minusMonths(1)
        viewModelScope.launch { loadMonth() }
    }

    fun nextMonth() {
        _month.value = _month.value.plusMonths(1)
        viewModelScope.launch { loadMonth() }
    }

    private fun exportMeta(): ExportMeta {
        val academicSession = session.value
        return ExportMeta(
            teacherName = sessionManager.accountKey.orEmpty(),
            subjectName = courseCode,
            sessionLabel = academicSession?.label.orEmpty(),
            deptId = academicSession?.deptId.orEmpty(),
            shift = academicSession?.shift?.name.orEmpty(),
            semester = academicSession?.currentSemester ?: 0,
        )
    }

    fun exportCsv(context: Context) {
        viewModelScope.launch {
            // File IO / share-intent failures (ActivityNotFoundException, IOException) must not crash the app.
            runCatching {
                val meta = exportMeta()
                val from = _month.value
                val days = (0 until from.lengthOfMonth()).map { from.plusDays(it.toLong()) }
                AttendanceExporter.exportCsv(context, meta, courseCode, monthLabel.value, days, roster.value, marks.value)
            }
        }
    }

    fun exportPdf(context: Context) {
        viewModelScope.launch {
            runCatching {
                val meta = exportMeta()
                AttendanceExporter.exportPdf(context, meta, courseCode, monthLabel.value, roster.value, marks.value)
            }
        }
    }
}
