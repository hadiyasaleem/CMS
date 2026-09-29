package com.mbd.cmscommon.controller

import com.mbd.cmscommon.util.FailureSummary
import com.mbd.cmscommon.domain.model.AttendanceEntry
import com.mbd.cmscommon.domain.model.AttendanceStatus
import com.mbd.cmscommon.domain.model.NotificationTargetRole
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.model.outlineTopics
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.NotificationRepository
import com.mbd.cmscommon.domain.repository.SessionAttendanceRepository
import com.mbd.cmscommon.teacher.ResolvedAssignment
import com.mbd.cmscommon.util.CmsException
import com.mbd.cmscommon.util.Outcome
import com.mbd.cmscommon.util.orLogCritical
import com.mbd.cmscommon.util.previewText
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MarkAttendanceController(
    private val attendanceRepository: SessionAttendanceRepository,
    private val sessionRepository: AcademicSessionRepository,
    private val notificationRepository: NotificationRepository,
    curriculumRepository: CurriculumRepository,
    private val teacherId: String,
    scope: CoroutineScope,
) : ScreenController(scope) {

    private val _selected = MutableStateFlow<ResolvedAssignment?>(null)
    val selected: StateFlow<ResolvedAssignment?> = _selected.asStateFlow()

    private val _date = MutableStateFlow(LocalDate.now())
    val date: StateFlow<LocalDate> = _date.asStateFlow()

    private var loadToken = 0

    /** Switches the register to [newDate] (never in the future): shows what was marked, or a blank register to mark. */
    fun setDate(newDate: LocalDate) {
        val day = minOf(newDate, LocalDate.now())
        if (day == _date.value) return
        _date.value = day
        _selected.value?.let { loadDay(it) }
    }

    private fun loadDay(assignment: ResolvedAssignment) {
        val token = ++loadToken
        val day = _date.value
        _statuses.value = emptyMap()
        _late.value = emptySet()
        _remarks.value = emptyMap()
        _lectureTopic.value = ""
        _markedSessionIds.value = emptySet()
        _submitState.value = null
        launch("load the register") {
            val statuses = mutableMapOf<String, AttendanceStatus>()
            val late = mutableSetOf<String>()
            val remarks = mutableMapOf<String, String>()
            var topic = ""
            val marked = mutableSetOf<String>()
            val labelledFailures = mutableListOf<Pair<String, Result<*>>>()
            // A merged lecture's register spans every linked session: each is fetched and checked on its own,
            // since a saved attendance row carries no shift/merge info of its own.
            for (sid in assignment.sessionIds) {
                val classRollsLoad = runCatching { studentsForTab(sessionRepository.observeStudents(sid).first(), assignment.classShift) }
                val classRolls = classRollsLoad.getOrDefault(emptyList()).map { it.rollNumber }.toSet()
                // Attendance rows carry no shift, so keep this class's students only: the other shift's register
                // for the same subject and day must not read as already marked here.
                val marksLoad = runCatching { attendanceRepository.marksBetween(sid, assignment.courseCode, day, day) }
                val marks = marksLoad.getOrDefault(emptyList())
                    .filter { assignment.classShift == null || it.rollNumber in classRolls }
                labelledFailures += "$sid's saved register for $day" to marksLoad
                labelledFailures += "$sid's student list" to classRollsLoad
                if (marks.isEmpty()) continue
                marked += sid
                marks.forEach { m ->
                    val id = SessionStudent.buildId(sid, m.rollNumber)
                    statuses[id] = m.status
                    if (m.isLate) late += id
                    m.remark?.takeIf { it.isNotBlank() }?.let { remarks[id] = it }
                }
                if (topic.isBlank()) topic = marks.firstNotNullOfOrNull { it.lectureTopic?.takeIf { t -> t.isNotBlank() } }.orEmpty()
            }
            if (token != loadToken) return@launch // stale: another date/class was picked meanwhile
            // An empty register is only trustworthy if the check itself worked -- otherwise the teacher would mark a day that is already marked.
            val failures = FailureSummary.of(labelledFailures)
            FailureSummary.describe(failures, "MarkAttendanceController")?.let { message ->
                _submitState.value = Outcome.Error("$message Check your connection before marking this register.", failures.first().cause)
            }
            _statuses.value = statuses
            _late.value = late
            _remarks.value = remarks
            _lectureTopic.value = topic
            _markedSessionIds.value = marked
        }
    }

    private val _markedSessionIds = MutableStateFlow<Set<String>>(emptySet())

    /** True only once every linked session's register is already marked for the picked day. */
    val alreadyMarked: StateFlow<Boolean> = combine(_selected, _markedSessionIds) { assignment, marked ->
        assignment != null && assignment.sessionIds.isNotEmpty() && marked.containsAll(assignment.sessionIds)
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), false)

    private val _lectureTopic = MutableStateFlow("")
    val lectureTopic: StateFlow<String> = _lectureTopic.asStateFlow()

    /** Topics from the selected subject's outline (comma separated by the admin), offered as chips. */
    val topics: StateFlow<List<String>> = _selected
        .flatMapLatest { assignment ->
            if (assignment == null) flowOf(emptyList()) else sessionRepository.observeSession(assignment.sessionId).flatMapLatest { session ->
                if (session == null) flowOf(emptyList()) else curriculumRepository.observeSemesterSubjects(session.sessionId, session.currentSemester)
                    .map { subjects -> outlineTopics(subjects.firstOrNull { it.courseCode == assignment.courseCode }?.outline) }
            }
        }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleTopic(topic: String) {
        if (alreadyMarked.value) return
        setLectureTopic(com.mbd.cmscommon.domain.model.toggleTopic(_lectureTopic.value, topic))
    }

    fun setLectureTopic(text: String) {
        _lectureTopic.value = text.take(TOPIC_MAX)
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

    /** Students whose own session is already marked for the picked day -- read-only, even mid-merge. */
    val lockedStudentIds: StateFlow<Set<String>> = combine(roster, _markedSessionIds) { students, marked ->
        students.filter { it.sessionId in marked }.map { it.id }.toSet()
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptySet())

    val termPercents: StateFlow<Map<String, Float>> = _selected
        .flatMapLatest { assignment ->
            if (assignment == null) {
                flowOf(emptyMap())
            } else {
                combine(assignment.sessionIds.map { sid -> attendanceRepository.observeTallies(sid, assignment.courseCode).map { tallies -> sid to tallies } }) { pairs ->
                    pairs.toList().flatMap { (sid, tallies) -> tallies.filter { it.total > 0 }.map { SessionStudent.buildId(sid, it.rollNumber) to it.percentage } }.toMap()
                }
            }
        }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Keyed by SessionStudent.id ("${sessionId}_$rollNumber"), never bare roll numbers -- a merged class's two
    // sessions may otherwise reuse the same roll number and silently collide.
    private val _statuses = MutableStateFlow<Map<String, AttendanceStatus>>(emptyMap())
    val statuses: StateFlow<Map<String, AttendanceStatus>> = _statuses.asStateFlow()

    private val _late = MutableStateFlow<Set<String>>(emptySet())
    val late: StateFlow<Set<String>> = _late.asStateFlow()

    private val _remarks = MutableStateFlow<Map<String, String>>(emptyMap())
    val remarks: StateFlow<Map<String, String>> = _remarks.asStateFlow()

    val allMarked: StateFlow<Boolean> = combine(roster, _statuses) { students, statuses ->
        students.isNotEmpty() && students.all { statuses.containsKey(it.id) }
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), false)

    private val _submitState = MutableStateFlow<Outcome<Unit>?>(null)
    val submitState: StateFlow<Outcome<Unit>?> = _submitState.asStateFlow()

    fun select(assignment: ResolvedAssignment) {
        _selected.value = assignment
        loadDay(assignment)
    }

    fun setStatus(studentId: String, status: AttendanceStatus) {
        if (studentId in lockedStudentIds.value) return
        _statuses.value = _statuses.value + (studentId to status)
    }

    fun toggleLate(studentId: String) {
        if (studentId in lockedStudentIds.value) return
        _late.value = if (_late.value.contains(studentId)) _late.value - studentId else _late.value + studentId
    }

    fun setRemark(studentId: String, text: String) {
        if (studentId in lockedStudentIds.value) return
        _remarks.value = _remarks.value + (studentId to text.take(500))
    }

    fun consumeSubmitState() {
        _submitState.value = null
    }

    fun submit() {
        val assignment = _selected.value ?: return
        if (alreadyMarked.value) return
        if (_submitState.value is Outcome.Loading) return // single-flight: block a double-tap

        val statuses = _statuses.value
        val students = roster.value
        if (students.isEmpty()) {
            _submitState.value = Outcome.Error("This class has no students on its register, so there is nothing to submit.", IllegalStateException("empty roster"))
            return
        }
        val unmarked = students.filter { !statuses.containsKey(it.id) }
        if (unmarked.isNotEmpty()) {
            _submitState.value = Outcome.Error(
                "${unmarked.size} of ${students.size} students still need a status: ${unmarked.map { it.rollNumber }.previewText()}.",
                IllegalStateException("incomplete"),
            )
            return
        }

        val topicLength = _lectureTopic.value.trim().length
        if (topicLength > TOPIC_MAX) {
            _submitState.value = Outcome.Error("The lecture topic is $topicLength characters; the limit is $TOPIC_MAX.", IllegalArgumentException("topic length"))
            return
        }
        val remarks = _remarks.value
        remarks.entries.firstOrNull { it.value.trim().length > 500 }?.let { (id, text) ->
            val roll = students.firstOrNull { it.id == id }?.rollNumber ?: id
            _submitState.value = Outcome.Error("The remark for $roll is ${text.trim().length} characters; the limit is 500.", IllegalArgumentException("remark length"))
            return
        }

        val late = _late.value
        val recordsByStudentId = students.associate { student ->
            student.id to AttendanceEntry(
                status = statuses.getValue(student.id),
                isLate = late.contains(student.id),
                remark = remarks[student.id],
            )
        }
        _submitState.value = Outcome.Loading // set before launch so the guard above sees it synchronously
        submitRecords(assignment, students, recordsByStudentId)
    }

    private fun submitRecords(assignment: ResolvedAssignment, students: List<SessionStudent>, recordsByStudentId: Map<String, AttendanceEntry>) = launch("submit attendance") {
        val day = _date.value
        val bySession = students.groupBy { it.sessionId }
        val toWrite = assignment.sessionIds - _markedSessionIds.value
        val newlyMarked = mutableSetOf<String>()
        try {
            _submitState.value = Outcome.Loading
            for (sid in toWrite) {
                val sessionStudents = bySession[sid].orEmpty()
                if (sessionStudents.isEmpty()) continue
                val sessionRecords = sessionStudents.associate { it.rollNumber to recordsByStudentId.getValue(it.id) }
                // Another device (or the other shift's/session's teacher) may have marked this register since it was
                // opened; the insert would then fail on a duplicate key. Say so plainly and reload what was saved.
                val alreadySaved = attendanceRepository.marksBetween(sid, assignment.courseCode, day, day)
                    .filter { it.rollNumber in sessionRecords.keys }
                if (alreadySaved.isNotEmpty()) {
                    val classLabel = if (assignment.isMerged) "one of the merged classes" else assignment.subjectLabel
                    throw CmsException.Conflict("Attendance for $classLabel on $day was already marked for ${alreadySaved.size} of ${sessionRecords.size} students. It has been reloaded; nothing was overwritten.")
                }
                attendanceRepository.markAttendance(
                    sessionId = sid,
                    courseCode = assignment.courseCode,
                    date = day,
                    teacherEmail = teacherId,
                    entries = sessionRecords,
                    lectureTopic = _lectureTopic.value.trim().takeIf { it.isNotBlank() },
                )
                newlyMarked += sid
            }
            if (newlyMarked.isNotEmpty()) {
                runCatching {
                    notificationRepository.send(
                        title = "Attendance marked",
                        body = "${assignment.subjectLabel} · ${assignment.sessionLabel}",
                        targetRole = NotificationTargetRole.ADMIN,
                        targetOfferingId = assignment.sessionId,
                        createdByUid = teacherId,
                        // The class this register belongs to: its department, session and shift.
                        targetDeptId = assignment.deptId.ifBlank { null },
                        targetShift = assignment.classShift,
                    )
                }.orLogCritical("MarkAttendanceController.notifyAdmin") // the register is already saved; a failed heads-up to admins must not undo that
            }
            _markedSessionIds.value = _markedSessionIds.value + newlyMarked
            _submitState.value = Outcome.Success(Unit)
        } catch (t: Throwable) {
            // Sessions already written in this pass stay written -- only the failing/remaining ones are retried later.
            _markedSessionIds.value = _markedSessionIds.value + newlyMarked
            if (t is CmsException.Conflict) loadDay(assignment)
            _submitState.value = Outcome.Error(t.userMessageLogged("Couldn't submit attendance for ${assignment.subjectLabel} on $day."), t)
        }
    }
}

private const val TOPIC_MAX = 500
