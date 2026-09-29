package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.ExamType
import com.mbd.cmscommon.domain.model.MarkEditRequest
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.MarkEditRequestRepository
import com.mbd.cmscommon.domain.repository.SessionMarksRepository
import com.mbd.cmscommon.teacher.ResolvedAssignment
import com.mbd.cmscommon.util.Outcome
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MarksEntryController(
    private val marksRepository: SessionMarksRepository,
    private val sessionRepository: AcademicSessionRepository,
    private val markEditRequestRepository: MarkEditRequestRepository,
    private val teacherId: String,
    scope: CoroutineScope,
) : ScreenController(scope) {

    private val _selected = MutableStateFlow<ResolvedAssignment?>(null)
    val selected: StateFlow<ResolvedAssignment?> = _selected.asStateFlow()

    private val _examType = MutableStateFlow(ExamType.MIDTERM)
    val examType: StateFlow<ExamType> = _examType.asStateFlow()

    // Keyed by SessionStudent.id ("${sessionId}_$rollNumber"), never bare roll numbers -- a merged class's two
    // sessions may otherwise reuse the same roll number and silently collide.
    private val _absentRolls = MutableStateFlow<Set<String>>(emptySet())
    val absentRolls: StateFlow<Set<String>> = _absentRolls.asStateFlow()

    fun toggleAbsent(studentId: String) {
        if (isLocked(studentId)) return
        _absentRolls.value = if (_absentRolls.value.contains(studentId)) _absentRolls.value - studentId else _absentRolls.value + studentId
    }

    /** The combined roster of every session sharing this lecture, for a merged class. */
    val roster: StateFlow<List<SessionStudent>> = _selected
        .flatMapLatest { assignment ->
            if (assignment == null) {
                flowOf(emptyList())
            } else {
                combine(assignment.sessionIds.map { sid -> sessionRepository.observeStudents(sid).map { studentsForTab(it, assignment.classShift) } }) {
                    it.toList().flatten()
                }
            }
        }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val session: StateFlow<AcademicSession?> = _selected
        .flatMapLatest { assignment -> if (assignment == null) flowOf(null) else sessionRepository.observeSession(assignment.sessionId) }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    val savedScores: StateFlow<Map<String, Int>> = combine(_selected, _examType) { a, t -> a to t }
        .flatMapLatest { (assignment, type) ->
            if (assignment == null) {
                flowOf(emptyMap())
            } else {
                combine(assignment.sessionIds.map { sid -> marksRepository.observeScores(sid, assignment.courseCode, type).map { scores -> sid to scores } }) { pairs ->
                    pairs.toList().flatMap { (sid, scores) -> scores.map { (roll, score) -> SessionStudent.buildId(sid, roll) to score } }.toMap()
                }
            }
        }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val lockedRolls: StateFlow<Set<String>> = savedScores
        .map { it.keys }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptySet())

    val savedAbsentRolls: StateFlow<Set<String>> = combine(_selected, _examType) { a, t -> a to t }
        .flatMapLatest { (assignment, type) ->
            if (assignment == null) {
                flowOf(emptySet())
            } else {
                combine(assignment.sessionIds.map { sid -> marksRepository.observeAbsentRolls(sid, assignment.courseCode, type).map { rolls -> sid to rolls } }) { pairs ->
                    pairs.toList().flatMap { (sid, rolls) -> rolls.map { roll -> SessionStudent.buildId(sid, roll) } }.toSet()
                }
            }
        }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptySet())

    private val _edits = MutableStateFlow<Map<String, String>>(emptyMap())

    val displayScores: StateFlow<Map<String, String>> = combine(savedScores, _edits) { saved, edits ->
        saved.mapValues { it.value.toString() } + edits
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val _saveState = MutableStateFlow<Outcome<Unit>?>(null)
    val saveState: StateFlow<Outcome<Unit>?> = _saveState.asStateFlow()

    private val _pendingByRoll = MutableStateFlow<Map<String, MarkEditRequest>>(emptyMap())
    val pendingByRoll: StateFlow<Map<String, MarkEditRequest>> = _pendingByRoll.asStateFlow()

    private val _requestState = MutableStateFlow<Outcome<Unit>?>(null)
    val requestState: StateFlow<Outcome<Unit>?> = _requestState.asStateFlow()

    fun select(assignment: ResolvedAssignment) {
        _selected.value = assignment
        _edits.value = emptyMap()
        _absentRolls.value = emptySet()

        loadPendingRequests()
    }

    fun selectExamType(type: ExamType) {
        _examType.value = type
        _edits.value = emptyMap()

        loadPendingRequests()
    }

    fun isLocked(studentId: String): Boolean = savedScores.value.containsKey(studentId)

    fun setScore(studentId: String, raw: String) {
        if (isLocked(studentId)) return
        _edits.value = _edits.value + (studentId to raw)
    }

    fun save() {
        val assignment = _selected.value ?: return
        if (_saveState.value is Outcome.Loading) return // single-flight: block a double-tap
        val type = _examType.value
        val display = displayScores.value
        val saved = savedScores.value
        val absent = _absentRolls.value

        val invalidScore = roster.value.firstOrNull { student ->
            if (saved.containsKey(student.id) || absent.contains(student.id)) return@firstOrNull false
            val raw = display[student.id]?.trim().orEmpty()
            raw.isNotEmpty() && raw.toIntOrNull()?.let { it !in 0..type.maxMarks } != false
        }
        if (invalidScore != null) {
            val raw = display[invalidScore.id]?.trim().orEmpty()
            val entered = raw.toIntOrNull()
            val message = when {
                entered == null -> "'$raw' isn't a whole number for ${invalidScore.rollNumber}. Enter a score from 0 to ${type.maxMarks}."
                entered > type.maxMarks -> "${invalidScore.rollNumber}'s score of $entered is above the ${examLabel(type)} maximum of ${type.maxMarks}."
                else -> "${invalidScore.rollNumber}'s score can't be negative. Enter a score from 0 to ${type.maxMarks}."
            }
            _saveState.value = Outcome.Error(message, IllegalArgumentException("score out of range"))
            return
        }
        val parsed = roster.value.mapNotNull { student ->
            if (saved.containsKey(student.id)) return@mapNotNull null
            if (absent.contains(student.id)) {
                student.id to 0
            } else {
                val score = display[student.id]?.trim()?.toIntOrNull()
                if (score != null && score in 0..type.maxMarks) student.id to score else null
            }
        }.toMap()

        _saveState.value = Outcome.Loading // set before launch so the guard above sees it synchronously
        launch("save the marks") {
            try {
                val absentToSave = absent.filter { parsed.containsKey(it) }.toSet()
                val bySession = roster.value.groupBy { it.sessionId }
                for (sid in assignment.sessionIds) {
                    val sessionStudents = bySession[sid].orEmpty()
                    val sessionScores = sessionStudents.mapNotNull { s -> parsed[s.id]?.let { s.rollNumber to it } }.toMap()
                    if (sessionScores.isEmpty()) continue
                    val sessionAbsent = sessionStudents.filter { it.id in absentToSave }.map { it.rollNumber }.toSet()
                    marksRepository.saveScores(sid, assignment.courseCode, type, teacherId, sessionScores, sessionAbsent)
                }
                _saveState.value = Outcome.Success(Unit)
                _edits.value = emptyMap()
                _absentRolls.value = emptySet()
            } catch (t: Throwable) {
                _saveState.value = Outcome.Error(t.userMessageLogged("Couldn't save the ${examLabel(type)} marks for ${assignment.subjectLabel}."), t)
            }
        }
    }

    fun clearRequestState() {
        _requestState.value = null
    }

    private fun loadPendingRequests() {
        val assignment = _selected.value ?: return
        val type = _examType.value
        launch("load pending edit requests") {
            val pending = mutableMapOf<String, MarkEditRequest>()
            for (sid in assignment.sessionIds) {
                markEditRequestRepository.getPendingForAssignment(sid, assignment.courseCode, type)
                    .forEach { pending[SessionStudent.buildId(sid, it.rollNumber)] = it }
            }
            _pendingByRoll.value = pending
        }
    }

    fun requestMarkEdit(student: SessionStudent, requestedScore: Int, reason: String?) {
        val studentId = student.id
        val sessionId = student.sessionId
        val rollNumber = student.rollNumber
        val assignment = _selected.value ?: return
        val type = _examType.value
        val semester = session.value?.currentSemester ?: 1
        val currentScore = savedScores.value[studentId]

        if (currentScore == null) {
            _requestState.value = Outcome.Error("$rollNumber has no saved ${examLabel(type)} score yet, so there is nothing to change. Enter the score directly.", IllegalStateException("no saved score"))
            return
        }
        if (requestedScore !in 0..type.maxMarks) {
            _requestState.value = Outcome.Error("$requestedScore is outside the ${examLabel(type)} range of 0 to ${type.maxMarks}.", IllegalArgumentException("score out of range"))
            return
        }
        if (requestedScore == currentScore) {
            _requestState.value = Outcome.Error("$rollNumber's ${examLabel(type)} score is already $currentScore, so there is nothing to change.", IllegalArgumentException("same score"))
            return
        }
        _pendingByRoll.value[studentId]?.let { pending ->
            _requestState.value = Outcome.Error("A change of $rollNumber's ${examLabel(type)} score to ${pending.requestedScore} is already waiting for the admin's review.", IllegalStateException("pending request"))
            return
        }
        val reasonLength = (reason ?: "").trim().length
        if (reasonLength > 500) {
            _requestState.value = Outcome.Error("The reason is $reasonLength characters; the limit is 500.", IllegalArgumentException("reason length"))
            return
        }

        launch("submit the edit request") {
            try {
                _requestState.value = Outcome.Loading
                markEditRequestRepository.submitRequest(
                    sessionId = sessionId,
                    semester = semester,
                    courseCode = assignment.courseCode,
                    examType = type,
                    rollNumber = rollNumber,
                    currentScore = currentScore,
                    requestedScore = requestedScore,
                    reason = reason,
                    requestedBy = teacherId,
                )
                _requestState.value = Outcome.Success(Unit)
                loadPendingRequests()
            } catch (t: Throwable) {
                _requestState.value = Outcome.Error(t.userMessageLogged("Couldn't submit the edit request for $rollNumber."), t)
            }
        }
    }
}

/** "Midterm" / "Sessional", for messages. */
private fun examLabel(type: ExamType): String = type.name.lowercase().replaceFirstChar { it.uppercase() }
