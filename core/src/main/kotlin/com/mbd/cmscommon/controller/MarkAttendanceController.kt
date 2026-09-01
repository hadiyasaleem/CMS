package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AttendanceEntry
import com.mbd.cmscommon.domain.model.AttendanceStatus
import com.mbd.cmscommon.domain.model.NotificationTargetRole
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.NotificationRepository
import com.mbd.cmscommon.domain.repository.SessionAttendanceRepository
import com.mbd.cmscommon.teacher.ResolvedAssignment
import com.mbd.cmscommon.util.Outcome
import com.mbd.cmscommon.util.userMessage
import java.time.LocalDate
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

class MarkAttendanceController(
    private val attendanceRepository: SessionAttendanceRepository,
    private val sessionRepository: AcademicSessionRepository,
    private val notificationRepository: NotificationRepository,
    private val teacherId: String,
    scope: CoroutineScope,
) : ScreenController(scope) {

    private val _selected = MutableStateFlow<ResolvedAssignment?>(null)
    val selected: StateFlow<ResolvedAssignment?> = _selected.asStateFlow()

    private val _alreadyMarked = MutableStateFlow(false)
    val alreadyMarked: StateFlow<Boolean> = _alreadyMarked.asStateFlow()

    private val _lectureTopic = MutableStateFlow("")
    val lectureTopic: StateFlow<String> = _lectureTopic.asStateFlow()

    fun setLectureTopic(text: String) {
        _lectureTopic.value = text.take(200)
    }

    val roster: StateFlow<List<SessionStudent>> = _selected
        .flatMapLatest { assignment -> if (assignment == null) flowOf(emptyList()) else sessionRepository.observeStudents(assignment.sessionId) }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val termPercents: StateFlow<Map<String, Float>> = _selected
        .flatMapLatest { assignment -> if (assignment == null) flowOf(emptyList()) else attendanceRepository.observeTallies(assignment.sessionId, assignment.courseCode) }
        .map { tallies -> tallies.filter { it.total > 0 }.associate { it.rollNumber to it.percentage } }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val _statuses = MutableStateFlow<Map<String, AttendanceStatus>>(emptyMap())
    val statuses: StateFlow<Map<String, AttendanceStatus>> = _statuses.asStateFlow()

    private val _late = MutableStateFlow<Set<String>>(emptySet())
    val late: StateFlow<Set<String>> = _late.asStateFlow()

    private val _remarks = MutableStateFlow<Map<String, String>>(emptyMap())
    val remarks: StateFlow<Map<String, String>> = _remarks.asStateFlow()

    val allMarked: StateFlow<Boolean> = combine(roster, _statuses) { students, statuses ->
        students.isNotEmpty() && students.all { statuses.containsKey(it.rollNumber) }
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), false)

    private val _submitState = MutableStateFlow<Outcome<Unit>?>(null)
    val submitState: StateFlow<Outcome<Unit>?> = _submitState.asStateFlow()

    fun select(assignment: ResolvedAssignment) {
        _selected.value = assignment
        _statuses.value = emptyMap()
        _late.value = emptySet()
        _remarks.value = emptyMap()
        _lectureTopic.value = ""
        _alreadyMarked.value = false
        launch {
            _alreadyMarked.value = runCatching {
                attendanceRepository.isMarkedOn(assignment.sessionId, assignment.courseCode, LocalDate.now())
            }.getOrDefault(false)
        }
    }

    fun setStatus(rollNumber: String, status: AttendanceStatus) {
        if (_alreadyMarked.value) return
        _statuses.value = _statuses.value + (rollNumber to status)
    }

    fun toggleLate(rollNumber: String) {
        if (_alreadyMarked.value) return
        _late.value = if (_late.value.contains(rollNumber)) _late.value - rollNumber else _late.value + rollNumber
    }

    fun setRemark(rollNumber: String, text: String) {
        if (_alreadyMarked.value) return
        _remarks.value = _remarks.value + (rollNumber to text.take(500))
    }

    fun consumeSubmitState() {
        _submitState.value = null
    }

    fun submit() {
        val assignment = _selected.value ?: return
        if (_alreadyMarked.value) return
        if (_submitState.value is Outcome.Loading) return // single-flight: block a double-tap

        val statuses = _statuses.value
        val students = roster.value
        if (students.isEmpty() || !students.all { statuses.containsKey(it.rollNumber) }) {
            _submitState.value = Outcome.Error("Mark every student before submitting.", IllegalStateException("incomplete"))
            return
        }

        if (_lectureTopic.value.trim().length > 200) {
            _submitState.value = Outcome.Error("Attendance notes are too long.", IllegalArgumentException("attendance text length"))
            return
        }
        val remarks = _remarks.value
        if (remarks.values.any { it.trim().length > 500 }) {
            _submitState.value = Outcome.Error("Attendance notes are too long.", IllegalArgumentException("attendance text length"))
            return
        }

        val late = _late.value
        val records = students.associate { student ->
            student.rollNumber to AttendanceEntry(
                status = statuses.getValue(student.rollNumber),
                isLate = late.contains(student.rollNumber),
                remark = remarks[student.rollNumber],
            )
        }
        _submitState.value = Outcome.Loading // set before launch so the guard above sees it synchronously
        submitRecords(assignment, records)
    }

    private fun submitRecords(assignment: ResolvedAssignment, records: Map<String, AttendanceEntry>) = launch {
        try {
            _submitState.value = Outcome.Loading
            attendanceRepository.markAttendance(
                sessionId = assignment.sessionId,
                courseCode = assignment.courseCode,
                date = LocalDate.now(),
                teacherEmail = teacherId,
                entries = records,
                lectureTopic = _lectureTopic.value.trim().takeIf { it.isNotBlank() },
            )
            runCatching {
                notificationRepository.send(
                    title = "Attendance marked",
                    body = "${assignment.subjectLabel} · ${assignment.sessionLabel}",
                    targetRole = NotificationTargetRole.ADMIN,
                    targetOfferingId = assignment.sessionId,
                    createdByUid = teacherId,
                )
            }
            _alreadyMarked.value = true
            _submitState.value = Outcome.Success(Unit)
        } catch (t: Throwable) {
            _submitState.value = Outcome.Error(t.userMessage("Could not submit attendance."), t)
        }
    }
}
