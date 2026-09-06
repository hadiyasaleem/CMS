package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.SemesterGpa
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.SessionMarksRepository
import com.mbd.cmscommon.teacher.ResolvedAssignment
import com.mbd.cmscommon.util.Outcome
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class SemesterResultsController(
    private val marksRepository: SessionMarksRepository,
    private val sessionRepository: AcademicSessionRepository,
    private val curriculumRepository: CurriculumRepository,
    myAssignments: Flow<List<ResolvedAssignment>>,
    scope: CoroutineScope,
) : ScreenController(scope) {

    val sessions: StateFlow<List<Pair<String, String>>> = myAssignments
        .map { assignments -> assignments.map { it.sessionId to it.sessionLabel }.distinct() }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _sessionId = MutableStateFlow<String?>(null)
    val sessionId: StateFlow<String?> = _sessionId.asStateFlow()

    private val _semester = MutableStateFlow(1)
    val semester: StateFlow<Int> = _semester.asStateFlow()

    val roster: StateFlow<List<SessionStudent>> = _sessionId
        .flatMapLatest { sid -> if (sid == null) flowOf(emptyList()) else sessionRepository.observeStudents(sid) }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _results = MutableStateFlow<Map<String, SemesterGpa>>(emptyMap())
    val results: StateFlow<Map<String, SemesterGpa>> = _results.asStateFlow()

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
        reload(fetchRemote = false)
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
        val sid = _sessionId.value ?: return
        launch {
            try {
                _loadState.value = Outcome.Loading
                if (fetchRemote) {
                    runCatching { sessionRepository.syncStudents(sid) }
                    runCatching { curriculumRepository.syncSession(sid) }
                    runCatching { marksRepository.syncSession(sid) }
                }

                _subjects.value = curriculumRepository.observeSemesterSubjects(sid, _semester.value).first().map { it.courseCode }
                _results.value = marksRepository.getSemesterResults(sid, _semester.value).associateBy { it.rollNumber }
                _loadState.value = Outcome.Success(Unit)
            } catch (t: Throwable) {
                _loadState.value = Outcome.Error(t.userMessageLogged("Could not load semester results."), t)
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
        val sid = _sessionId.value ?: return
        launch {
            try {
                _saveState.value = Outcome.Loading
                require(gpa in 0.0..4.0) { "GPA must be between 0 and 4." }
                require(cgpa in 0.0..4.0) { "CGPA must be between 0 and 4." }
                require((termLabel ?: "").trim().length <= 40) { "Term label must not exceed 40 characters." }
                require(result.uppercase(Locale.ROOT) in setOf("PENDING", "PASS", "FAIL", "SUPPLY", "WITHHELD")) {
                    "Choose a valid result status."
                }
                require(position == null || position > 0) { "Class position must be a positive whole number." }
                require((remarks ?: "").trim().length <= 500) { "Remarks must not exceed 500 characters." }
                require(supply.all { _subjects.value.contains(it) }) { "Choose supply subjects from this semester's curriculum." }

                marksRepository.recordSemesterResult(sid, roll, _semester.value, gpa, cgpa, termLabel, result.trim().uppercase(Locale.ROOT), position, remarks, supply)
                _saveState.value = Outcome.Success(Unit)
                _results.value = _results.value + (roll to SemesterGpa(sid, roll, _semester.value, gpa, cgpa, termLabel, result.trim().uppercase(Locale.ROOT), position, remarks, supply))
            } catch (t: Throwable) {
                _saveState.value = Outcome.Error(t.userMessageLogged("Could not save the result."), t)
            }
        }
    }

    fun clearSave() {
        _saveState.value = null
    }
}
