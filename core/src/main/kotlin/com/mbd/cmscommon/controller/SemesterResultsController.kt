package com.mbd.cmscommon.controller

import com.mbd.cmscommon.util.FailureSummary
import com.mbd.cmscommon.domain.model.ShiftScope
import com.mbd.cmscommon.domain.model.SemesterGpa
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.domain.model.ProgramType
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.SessionMarksRepository
import com.mbd.cmscommon.util.Outcome
import com.mbd.cmscommon.util.requireValid
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class SemesterResultsController(
    private val marksRepository: SessionMarksRepository,
    private val sessionRepository: AcademicSessionRepository,
    private val curriculumRepository: CurriculumRepository,
    sessions: Flow<List<Pair<String, String>>>,
    scope: CoroutineScope,
    /** Departments and sessions offered by the Department -> Session -> Shift filter. */
    filterOptions: Flow<ScopeFilterOptions> = flowOf(ScopeFilterOptions()),
) : ScreenController(scope) {

    val sessions: StateFlow<List<Pair<String, String>>> = sessions
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filterOptions: StateFlow<ScopeFilterOptions> = filterOptions.stateIn(scope, SharingStarted.WhileSubscribed(5000), ScopeFilterOptions())

    private val _filterScope = MutableStateFlow(ShiftScope.ALL)
    val filterScope: StateFlow<ShiftScope> = _filterScope.asStateFlow()

    fun setFilterScope(scope: ShiftScope) {
        _filterScope.value = scope
    }

    /** The classes the picker offers: only those inside the chosen department / session / shift. */
    val visibleSessions: StateFlow<List<Pair<String, String>>> = combine(this.sessions, _filterScope, this.filterOptions) { classes, filter, options ->
        classesInScope(classes, filter, options.sessions)
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** The picked class key (see [shiftClassKey]): one shift of a session, or a bare session id for all of it. */
    private val _sessionId = MutableStateFlow<String?>(null)
    val sessionId: StateFlow<String?> = _sessionId.asStateFlow()

    /** The session behind the picked class, without its shift. */
    private val selectedSessionId: String? get() = _sessionId.value?.let { parseShiftClassKey(it).first }

    /** The valid semester numbers for the picked class's session's program type (1-8 for BS, 5-8 for MA Replacement). */
    val semesterRange: StateFlow<IntRange> = _sessionId
        .flatMapLatest { key ->
            val sid = key?.let { parseShiftClassKey(it).first }
            if (sid == null) flowOf(ProgramType.BS.semesterRange) else sessionRepository.observeSession(sid).map { it?.semesterRange ?: ProgramType.BS.semesterRange }
        }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), ProgramType.BS.semesterRange)

    private val _semester = MutableStateFlow(1)
    val semester: StateFlow<Int> = _semester.asStateFlow()

    val roster: StateFlow<List<SessionStudent>> = _sessionId
        .flatMapLatest { key ->
            if (key == null) {
                flowOf(emptyList())
            } else {
                // Results are recorded per student; a shift's class lists that shift's students only.
                val (sid, shift) = parseShiftClassKey(key)
                sessionRepository.observeStudents(sid).map { studentsForTab(it, shift) }
            }
        }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _results = MutableStateFlow<Map<String, SemesterGpa>>(emptyMap())

    /** The class's results only: the session's other shift is not counted in this class's summary. */
    val results: StateFlow<Map<String, SemesterGpa>> = combine(_results, roster) { all, students ->
        val rolls = students.map { it.rollNumber }.toSet()
        all.filterKeys { it in rolls }
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val _subjects = MutableStateFlow<List<String>>(emptyList())
    val subjects: StateFlow<List<String>> = _subjects.asStateFlow()

    private val _saveState = MutableStateFlow<Outcome<Unit>?>(null)
    val saveState: StateFlow<Outcome<Unit>?> = _saveState.asStateFlow()

    private val _loadState = MutableStateFlow<Outcome<Unit>?>(null)
    val loadState: StateFlow<Outcome<Unit>?> = _loadState.asStateFlow()

    fun selectSession(id: String) {
        _sessionId.value = id
        _results.value = emptyMap()
        _subjects.value = emptyList()
        val sid = parseShiftClassKey(id).first
        launch("load the session") {
            val range = sessionRepository.observeSession(sid).first()?.semesterRange ?: ProgramType.BS.semesterRange
            if (_semester.value !in range) _semester.value = range.first
            reload(fetchRemote = false)
        }
    }

    fun setSemester(n: Int) {
        _semester.value = n
        _results.value = emptyMap()
        _subjects.value = emptyList()
        reload(fetchRemote = false)
    }

    fun refresh() {
        reload(fetchRemote = true)
    }

    private fun reload(fetchRemote: Boolean) {
        val sid = selectedSessionId ?: return
        launch("load the semester results") {
            try {
                _loadState.value = Outcome.Loading
                val syncFailures = if (fetchRemote) {
                    FailureSummary.of(
                        listOf(
                            "students" to runCatching { sessionRepository.syncStudents(sid) },
                            "subjects" to runCatching { curriculumRepository.syncSession(sid) },
                            "marks" to runCatching { marksRepository.syncSession(sid) },
                        ),
                    )
                } else {
                    emptyList()
                }

                _subjects.value = curriculumRepository.observeSemesterSubjects(sid, _semester.value).first().map { it.courseCode }
                _results.value = marksRepository.getSemesterResults(sid, _semester.value).associateBy { it.rollNumber }
                // The saved results are shown either way; a failed refresh is reported so they aren't mistaken for up to date.
                _loadState.value = FailureSummary.describe(syncFailures, "SemesterResultsController", prefix = "Showing saved results. Couldn't refresh")
                    ?.let { Outcome.Error(it, syncFailures.first().cause) }
                    ?: Outcome.Success(Unit)
            } catch (t: Throwable) {
                _loadState.value = Outcome.Error(t.userMessageLogged("Couldn't load the Semester ${_semester.value} results."), t)
            }
        }
    }

    fun record(
        roll: String,
        gpa: Double,
        cgpa: Double,
        termLabel: String?,
        result: String,
        position: Int?,
        remarks: String?,
        supply: List<String>,
    ) {
        val sid = selectedSessionId ?: return
        launch("save the result") {
            try {
                _saveState.value = Outcome.Loading
                requireValid(gpa in 0.0..4.0) { "GPA must be between 0 and 4 (you entered $gpa)." }
                requireValid(cgpa in 0.0..4.0) { "CGPA must be between 0 and 4 (you entered $cgpa)." }
                requireValid((termLabel ?: "").trim().length <= 40) { "Term label must not exceed 40 characters." }
                requireValid(result.uppercase(Locale.ROOT) in setOf("PENDING", "PROMOTED", "PROBATION", "REPEATED")) {
                    "Choose a valid result status."
                }
                requireValid(position == null || position > 0) { "Class position must be a positive whole number." }
                requireValid((remarks ?: "").trim().length <= 500) { "Remarks must not exceed 500 characters." }
                requireValid(supply.all { _subjects.value.contains(it) }) {
                    "${supply.filterNot { _subjects.value.contains(it) }.joinToString()} isn't in Semester ${_semester.value}'s curriculum. Choose supply subjects from that list."
                }

                marksRepository.recordSemesterResult(sid, roll, _semester.value, gpa, cgpa, termLabel, result.trim().uppercase(Locale.ROOT), position, remarks, supply)
                _saveState.value = Outcome.Success(Unit)
                _results.value = _results.value + (roll to SemesterGpa(sid, roll, _semester.value, gpa, cgpa, termLabel, result.trim().uppercase(Locale.ROOT), position, remarks, supply))
            } catch (t: Throwable) {
                _saveState.value = Outcome.Error(t.userMessageLogged("Couldn't save the result for $roll (Semester ${_semester.value})."), t)
            }
        }
    }

    fun clearSave() {
        _saveState.value = null
    }
}
