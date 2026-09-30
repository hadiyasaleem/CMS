package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.util.CmsException
import com.mbd.cmscommon.util.clockDisplay
import com.mbd.cmscommon.util.orThrowValidation
import com.mbd.cmscommon.util.requireValid

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Building
import com.mbd.cmscommon.domain.model.PeriodType
import com.mbd.cmscommon.domain.model.Room
import com.mbd.cmscommon.domain.model.SemesterSubject
import com.mbd.cmscommon.domain.model.SemesterTerm
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.BuildingRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.RoomRepository
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import java.time.DayOfWeek
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class SessionTimetableController(
    val sessionId: String,
    private val timetableRepository: SessionTimetableRepository,
    private val sessionRepository: AcademicSessionRepository,
    curriculumRepository: CurriculumRepository,
    teacherRepository: TeacherRepository,
    buildingRepository: BuildingRepository,
    roomRepository: RoomRepository,
    scope: CoroutineScope,
    initialShift: Session? = null,
    /** When given, a teacher/room clash names the department of the other class; without it the department id is shown. */
    private val departmentRepository: DepartmentRepository? = null,
) : ScreenController(scope) {

    val session: StateFlow<AcademicSession?> =
        sessionRepository.observeSession(sessionId).stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    private val _pickedShift = MutableStateFlow(initialShift)

    /** The Morning/Evening tab being edited: only shifts the session runs, and no combined view. */
    val shift: StateFlow<Session> = combine(session, _pickedShift) { s, picked -> shiftTab(s, picked) }
        .stateIn(scope, SharingStarted.Eagerly, initialShift ?: Session.MORNING)

    val shifts: StateFlow<List<Session>> = session.map { shiftTabs(it) }
        .stateIn(scope, SharingStarted.Eagerly, shiftTabs(null))

    fun selectShift(picked: Session) {
        _pickedShift.value = picked
    }

    /** Every shift's periods; the workspace shows the selected tab's grid. */
    val periods: StateFlow<List<SessionPeriod>> =
        timetableRepository.observeWeek(sessionId).stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Every active session, for the "merge with an existing class"/"add another session" pickers. */
    val allSessions: StateFlow<List<AcademicSession>> =
        sessionRepository.observeAllSessions().stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** All colleges' periods, for the "merge with an existing class" picker (an empty-slot merge target may
     * belong to any session, not just this one). */
    val allPeriods: StateFlow<List<SessionPeriod>> =
        timetableRepository.observeAll().stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subjects: StateFlow<List<SemesterSubject>> = session
        .flatMapLatest { s -> if (s == null) flowOf(emptyList()) else curriculumRepository.observeSemesterSubjects(s.sessionId, s.currentSemester) }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val teachers: StateFlow<List<Teacher>> =
        teacherRepository.observeActiveTeachers().stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val buildings: StateFlow<List<Building>> =
        buildingRepository.observeActiveBuildings().stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rooms: StateFlow<List<Room>> =
        roomRepository.observeActiveRooms().stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** The current semester's configured term dates for this session, if set -- offered as a shortcut for effective from/to. */
    val currentSemesterTerm: StateFlow<SemesterTerm?> = session
        .flatMapLatest { s ->
            if (s == null) flowOf<SemesterTerm?>(null) else flow { emit(curriculumRepository.getSemesterTerm(s.sessionId, s.currentSemester)) }
        }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    fun savePeriod(
        day: DayOfWeek,
        start: String,
        end: String,
        subject: SemesterSubject?,
        teacher: Teacher?,
        periodType: PeriodType,
        roomNo: String?,
        building: String?,
        notes: String?,
        effectiveFrom: LocalDate?,
        effectiveTo: LocalDate?,
        replaces: SessionPeriod?,
        // A new period goes on the open tab; an edit keeps the period's own shift.
        shift: Session = replaces?.shift ?: this.shift.value,
    ) = launch("save the period") {
        requireValid(periodType == PeriodType.BREAK || subject != null) { "Choose a subject for this period." }

        val normalizedStart = start.trim()
        val normalizedEnd = end.trim()
        val period = SessionPeriod(
            id = SessionPeriod.buildId(sessionId, shift, day, normalizedStart),
            sessionId = sessionId,
            shift = shift,
            day = day,
            startTime = normalizedStart,
            endTime = normalizedEnd,
            courseCode = subject?.courseCode ?: "BREAK",
            subjectName = subject?.name ?: "Break",
            teacherId = if (periodType != PeriodType.BREAK) teacher?.teacherId ?: "" else "",
            teacherName = if (periodType != PeriodType.BREAK) teacher?.name ?: "" else "",
            periodType = periodType,
            creditHours = subject?.creditHours,
            roomNo = roomNo?.trim()?.takeIf { it.isNotBlank() },
            building = building?.trim()?.takeIf { it.isNotBlank() },
            notes = notes?.trim()?.takeIf { it.isNotBlank() },
            effectiveFrom = effectiveFrom,
            effectiveTo = effectiveTo,
        )

        validateTimetablePeriod(period, replaces, periods.value).orThrowValidation()
        // Teachers and rooms are shared across every session and shift, so look college-wide for the clash and name it.
        describeTimetableConflict(
            period,
            timetableRepository.observeAll().first(),
            sessionRepository.observeAllSessions().first(),
            departmentRepository?.observeActiveDepartments()?.first().orEmpty(),
            excludedId = replaces?.id,
        )?.let { throw CmsException.Conflict(it) }

        timetableRepository.savePeriod(period)
        // Compare as HH:mm: a stored "09:00:00" and a re-picked "09:00" are the same slot, and treating
        // them as a move would delete the row savePeriod just upserted.
        if (replaces != null && (replaces.shift != period.shift || replaces.day != period.day || clockDisplay(replaces.startTime) != clockDisplay(period.startTime))) {
            timetableRepository.removePeriod(replaces)
        }
    }

    fun removePeriod(period: SessionPeriod) = launch("remove the period") {
        timetableRepository.removePeriod(period)
    }

    /** Sessions that could still be merged into [period]'s lecture. */
    fun eligibleMergeSessions(period: SessionPeriod): List<AcademicSession> =
        com.mbd.cmscommon.controller.eligibleMergeSessions(period, allSessions.value)

    /** Other sessions' existing lectures (this session's own shift tab) that this empty slot could be merged into. */
    fun existingPeriodsForMerge(): List<SessionPeriod> =
        describeExistingPeriodsForMerge(allPeriods.value, sessionId, shift.value)

    /** Merges [targetSessionId] into [period]'s lecture, or removes it from the merge ([link] = false). Only
     * meaningful when [period] is this session's own row ([SessionPeriod.isOwnRow]); a guest view's only
     * valid call is unmerging its own session. */
    fun setPeriodLink(period: SessionPeriod, targetSessionId: String, link: Boolean) =
        launch(if (link) "merge the class" else "remove the class from the merge") {
            timetableRepository.setPeriodLink(period, targetSessionId, link)
        }

    /**
     * Detaches this session from the shared lecture [shared] by giving it its own period(s). The new period must
     * differ from the shared one in teacher or time slot -- otherwise it would just be the same class twice.
     */
    fun leaveMerge(
        shared: SessionPeriod,
        days: Set<DayOfWeek>,
        start: String,
        end: String,
        subject: SemesterSubject?,
        teacher: Teacher?,
        periodType: PeriodType,
        roomNo: String?,
        building: String?,
        notes: String?,
        effectiveFrom: LocalDate?,
        effectiveTo: LocalDate?,
    ) = launch("unmerge the class") {
        requireValid(days.isNotEmpty()) { "Choose at least one day." }
        requireValid(periodType == PeriodType.BREAK || subject != null) { "Choose a subject for this period." }
        val sameSlot = days == setOf(shared.day) && clockDisplay(start.trim()) == clockDisplay(shared.startTime) && clockDisplay(end.trim()) == clockDisplay(shared.endTime)
        requireValid(!sameSlot || teacher?.teacherId != shared.teacherId) {
            "To unmerge, change the teacher or the time slot for this class."
        }
        val all = timetableRepository.observeAll().first()
        val sessionList = sessionRepository.observeAllSessions().first()
        val deptList = departmentRepository?.observeActiveDepartments()?.first().orEmpty()
        val newPeriods = days.map { day ->
            SessionPeriod(
                id = SessionPeriod.buildId(sessionId, shared.shift, day, start.trim()),
                sessionId = sessionId,
                shift = shared.shift,
                day = day,
                startTime = start.trim(),
                endTime = end.trim(),
                courseCode = subject?.courseCode ?: "BREAK",
                subjectName = subject?.name ?: "Break",
                teacherId = if (periodType != PeriodType.BREAK) teacher?.teacherId ?: "" else "",
                teacherName = if (periodType != PeriodType.BREAK) teacher?.name ?: "" else "",
                periodType = periodType,
                creditHours = subject?.creditHours,
                roomNo = roomNo?.trim()?.takeIf { it.isNotBlank() },
                building = building?.trim()?.takeIf { it.isNotBlank() },
                notes = notes?.trim()?.takeIf { it.isNotBlank() },
                effectiveFrom = effectiveFrom,
                effectiveTo = effectiveTo,
            )
        }
        // The shared lecture is still in force until the link is removed, so it must not count as an overlap.
        newPeriods.forEach { validateTimetablePeriod(it, shared, periods.value).orThrowValidation() }
        newPeriods.forEach { p -> describeTimetableConflict(p, all, sessionList, deptList)?.let { throw CmsException.Conflict(it) } }
        newPeriods.forEach { timetableRepository.savePeriod(it) }
        timetableRepository.setPeriodLink(shared, sessionId, false)
    }

    /**
     * Unmerges [unlinkSessionId] from this session's own merged lecture [period]. The other side must end up different:
     * this session's lecture takes the new values (teacher/time slot must change) while [unlinkSessionId] keeps the
     * lecture as it was, as its own period.
     */
    fun unmergeSession(
        period: SessionPeriod,
        unlinkSessionId: String,
        days: Set<DayOfWeek>,
        start: String,
        end: String,
        subject: SemesterSubject?,
        teacher: Teacher?,
        periodType: PeriodType,
        roomNo: String?,
        building: String?,
        notes: String?,
        effectiveFrom: LocalDate?,
        effectiveTo: LocalDate?,
    ) = launch("unmerge the class") {
        requireValid(period.isOwnRow && unlinkSessionId in period.linkedSessionIds) { "That session is not merged into this lecture." }
        requireValid(days.size == 1) { "Choose a single day to unmerge." }
        requireValid(periodType == PeriodType.BREAK || subject != null) { "Choose a subject for this period." }
        val day = days.first()
        val normalizedStart = start.trim()
        val normalizedEnd = end.trim()
        val sameSlot = day == period.day && clockDisplay(normalizedStart) == clockDisplay(period.startTime) && clockDisplay(normalizedEnd) == clockDisplay(period.endTime)
        requireValid(!sameSlot || teacher?.teacherId != period.teacherId) {
            "To unmerge, change the teacher or the time slot for this class."
        }
        val updated = SessionPeriod(
            id = SessionPeriod.buildId(sessionId, period.shift, day, normalizedStart),
            sessionId = sessionId,
            shift = period.shift,
            day = day,
            startTime = normalizedStart,
            endTime = normalizedEnd,
            courseCode = subject?.courseCode ?: "BREAK",
            subjectName = subject?.name ?: "Break",
            teacherId = if (periodType != PeriodType.BREAK) teacher?.teacherId ?: "" else "",
            teacherName = if (periodType != PeriodType.BREAK) teacher?.name ?: "" else "",
            periodType = periodType,
            creditHours = subject?.creditHours,
            roomNo = roomNo?.trim()?.takeIf { it.isNotBlank() },
            building = building?.trim()?.takeIf { it.isNotBlank() },
            notes = notes?.trim()?.takeIf { it.isNotBlank() },
            effectiveFrom = effectiveFrom,
            effectiveTo = effectiveTo,
        )
        val guest = period.copy(
            id = SessionPeriod.buildId(unlinkSessionId, period.shift, period.day, period.startTime),
            sessionId = unlinkSessionId,
            linkedSessionIds = emptySet(),
            isOwnRow = true,
        )
        validateTimetablePeriod(updated, period, periods.value).orThrowValidation()
        val rest = timetableRepository.observeAll().first().filter { it.id != period.id }
        val sessionList = sessionRepository.observeAllSessions().first()
        val deptList = departmentRepository?.observeActiveDepartments()?.first().orEmpty()
        describeTimetableConflict(updated, rest, sessionList, deptList)?.let { throw CmsException.Conflict(it) }
        describeTimetableConflict(guest, rest + updated, sessionList, deptList)?.let { throw CmsException.Conflict(it) }

        timetableRepository.setPeriodLink(period, unlinkSessionId, false)
        timetableRepository.savePeriod(updated)
        if (!sameSlot) {
            // Moving the slot replaces the row (and drops its links), so re-attach the sessions that stay merged
            // to the row it became.
            timetableRepository.removePeriod(period)
            val moved = timetableRepository.observeWeek(sessionId).first().firstOrNull {
                it.isOwnRow && it.shift == updated.shift && it.day == day && clockDisplay(it.startTime) == clockDisplay(normalizedStart)
            }
            if (moved != null) (period.linkedSessionIds - unlinkSessionId).forEach { timetableRepository.setPeriodLink(moved, it, true) }
        }
        timetableRepository.savePeriod(guest)
    }

    /** Attaches this session to an already-existing lecture elsewhere, instead of creating a new period. */
    fun mergeExistingPeriod(existingElsewhere: SessionPeriod) =
        launch("merge with the existing class") {
            timetableRepository.setPeriodLink(existingElsewhere, sessionId, link = true)
        }
}
