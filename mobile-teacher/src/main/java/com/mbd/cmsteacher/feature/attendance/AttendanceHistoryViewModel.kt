package com.mbd.cmsteacher.feature.attendance

import com.mbd.cmscommon.util.CmsException
import com.mbd.cmscommon.util.FileReadErrors
import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.AttendanceStatus
import com.mbd.cmscommon.domain.model.DailyAttendanceMark
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.domain.model.parseShift
import com.mbd.cmscommon.controller.studentsForTab
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.AttendanceEditRequestRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.SessionAttendanceRepository
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import com.mbd.cmscommon.export.ExportFormat
import com.mbd.cmscommon.export.attendanceRegisterExport
import com.mbd.cmscommon.export.resolveRegisterContext
import com.mbd.cmscommon.util.DocumentExporter
import com.mbd.cmscommon.util.Outcome
import com.mbd.cmscommon.util.userMessageLogged
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AttendanceHistoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sessionManager: SessionManager,
    private val attendanceRepository: SessionAttendanceRepository,
    private val sessionRepository: AcademicSessionRepository,
    private val editRequestRepository: AttendanceEditRequestRepository,
    private val departmentRepository: DepartmentRepository,
    private val curriculumRepository: CurriculumRepository,
    private val timetableRepository: SessionTimetableRepository,
) : ViewModel() {

    /** A comma-joined list when the class is a merged lecture; the first id is the primary session. */
    private val sessionIds: List<String> = checkNotNull(savedStateHandle.get<String>("sessionId")).split(',').filter { it.isNotBlank() }
    val sessionId: String = sessionIds.first()
    val courseCode: String = checkNotNull(savedStateHandle["courseCode"])
    /** The class's shift; null (legacy "ALL") shows the whole session. */
    val shift: Session? = parseShift(savedStateHandle.get<String>("shift"))

    private val _month = MutableStateFlow(LocalDate.now().withDayOfMonth(1))
    val month: StateFlow<YearMonth> = _month.map { YearMonth.from(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), YearMonth.now())

    val monthLabel: StateFlow<String> = _month.map { it.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    val roster: StateFlow<List<SessionStudent>> =
        kotlinx.coroutines.flow.combine(sessionIds.map { sid -> sessionRepository.observeStudents(sid).map { studentsForTab(it, shift) } }) { it.toList().flatten() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val session: StateFlow<AcademicSession?> = sessionRepository.observeSession(sessionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _marks = MutableStateFlow<Map<String, Map<LocalDate, DailyAttendanceMark>>>(emptyMap())
    val marks: StateFlow<Map<String, Map<LocalDate, DailyAttendanceMark>>> = _marks.asStateFlow()

    private val _pendingCells = MutableStateFlow<Set<Pair<String, LocalDate>>>(emptySet())
    val pendingCells: StateFlow<Set<Pair<String, LocalDate>>> = _pendingCells.asStateFlow()

    private val _requestState = MutableStateFlow<Outcome<Unit>?>(null)
    val requestState: StateFlow<Outcome<Unit>?> = _requestState.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        viewModelScope.launch { loadMonth() }
    }

    private suspend fun loadMonth() {
        _loading.value = true
        _error.value = null
        try {
            val from = _month.value
            val to = from.withDayOfMonth(from.lengthOfMonth())
            val loaded = mutableMapOf<String, Map<LocalDate, DailyAttendanceMark>>()
            for (sid in sessionIds) {
                val classRolls = studentsForTab(sessionRepository.observeStudents(sid).first(), shift).map { it.rollNumber }.toSet()
                attendanceRepository.marksBetween(sid, courseCode, from, to)
                    .filter { shift == null || it.rollNumber in classRolls }
                    .groupBy { it.rollNumber }
                    .forEach { (roll, marks) -> loaded[SessionStudent.buildId(sid, roll)] = marks.associateBy { it.date } }
            }
            _marks.value = loaded
            loadPending(from, to)
        } catch (t: Throwable) {
            // Keep the previously loaded marks on screen (offline-first) but still surface and log it.
            _error.value = t.userMessageLogged("AttendanceHistoryViewModel.loadMonth", "Couldn't load the $courseCode attendance history for ${_month.value.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${_month.value.year}.")
        } finally {
            _loading.value = false
        }
    }

    private suspend fun loadPending(from: LocalDate, to: LocalDate) {
        _pendingCells.value = sessionIds.flatMap { sid ->
            runCatching { editRequestRepository.getPendingFor(sid, courseCode, from, to) }
                .getOrElse {
                    _error.value = it.userMessageLogged("AttendanceHistoryViewModel.loadPending", "Couldn't load the pending attendance edit requests for $courseCode.")
                    emptyList()
                }
                .map { SessionStudent.buildId(sid, it.rollNumber) to it.date }
        }.toSet()
    }

    fun submitEditRequest(
        student: SessionStudent,
        date: LocalDate,
        current: DailyAttendanceMark?,
        status: AttendanceStatus,
        late: Boolean,
        reason: String,
    ) {
        if (_requestState.value is Outcome.Loading) return
        viewModelScope.launch {
            _requestState.value = Outcome.Loading
            try {
                val rollNumber = student.rollNumber
                val semester = sessionRepository.observeSession(student.sessionId).first()?.currentSemester
                    ?: throw CmsException.NotFound("This session could not be found. Refresh and try again.")
                editRequestRepository.submitRequest(
                    sessionId = student.sessionId,
                    semester = semester,
                    courseCode = courseCode,
                    date = date,
                    rollNumber = rollNumber,
                    currentStatus = current?.status,
                    currentIsLate = current?.isLate,
                    requestedStatus = status,
                    requestedIsLate = late,
                    reason = reason,
                )
                _pendingCells.value = _pendingCells.value + (student.id to date)
                _requestState.value = Outcome.Success(Unit)
            } catch (t: Throwable) {
                _requestState.value = Outcome.Error(t.userMessageLogged("AttendanceHistoryViewModel.submitEditRequest", "Couldn't send the attendance edit request for ${student.rollNumber} on $date."), t)
            }
        }
    }

    fun consumeRequestState() {
        _requestState.value = null
    }

    fun previousMonth() {
        _month.value = _month.value.minusMonths(1)
        viewModelScope.launch { loadMonth() }
    }

    fun nextMonth() {
        _month.value = _month.value.plusMonths(1)
        viewModelScope.launch { loadMonth() }
    }

    fun export(context: Context, format: ExportFormat) {
        viewModelScope.launch {
            // File IO / share-intent failures (ActivityNotFoundException, IOException) must not crash the app.
            runCatching {
                val academicSession = sessionRepository.observeSession(sessionId).first()
                val doc = attendanceRegisterExport(courseCode, academicSession, YearMonth.from(_month.value), roster.value, marks.value,
                    resolveRegisterContext(academicSession, courseCode, departmentRepository, curriculumRepository, timetableRepository, shift), shift)
                DocumentExporter.export(context, doc, format)
            }.onFailure { _error.value = FileReadErrors.describeWrite(it, format.label) }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
