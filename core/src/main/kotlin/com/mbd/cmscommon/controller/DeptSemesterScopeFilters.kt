package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.AtRiskStudent
import com.mbd.cmscommon.domain.model.AttendanceEditRequest
import com.mbd.cmscommon.domain.model.CalendarEvent
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.DeptSemesterScope
import com.mbd.cmscommon.domain.model.ExamStat
import com.mbd.cmscommon.domain.model.MarkEditRequest
import com.mbd.cmscommon.domain.model.Notification
import com.mbd.cmscommon.domain.model.ProgramType
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionOverview
import com.mbd.cmscommon.domain.model.StudentLinkRequest
import com.mbd.cmscommon.teacher.ResolvedAssignment

/** Every distinct current-semester number among active sessions, low to high -- the options for a
 * "Semester" dropdown on a [DeptSemesterScope] filter bar. */
fun List<AcademicSession>.availableSemesters(): List<Int> = map { it.currentSemester }.distinct().sorted()

/** Sessions inside the scope. */
@JvmName("sessionsInDeptSemesterScope")
fun List<AcademicSession>.inScope(scope: DeptSemesterScope): List<AcademicSession> =
    if (scope.isEmpty) this else filter { scope.matches(it) }

/**
 * Whether an item that knows only its session (and maybe its own shift) is inside the scope. A null
 * [sessionId] is college-wide data and matches any scope; the session's CURRENT semester is used when
 * the item carries no semester of its own -- pass [itemSemester] when the item has a more authoritative
 * one (e.g. a historical record whose semester shouldn't drift with the session's current one). The
 * session's program type is always used as-is, since program type doesn't drift the way a semester does.
 */
fun DeptSemesterScope.matchesSessionItem(
    sessionId: String?,
    itemSemester: Int?,
    itemShift: Session?,
    sessions: Collection<AcademicSession>,
): Boolean {
    if (sessionId.isNullOrBlank()) return true
    val session = sessions.firstOrNull { it.sessionId == sessionId } ?: return true
    if (deptId != null && deptId != session.deptId) return false
    if (programType != null && programType != session.programType) return false
    val semesterValue = itemSemester ?: session.currentSemester
    if (semester != null && semester != semesterValue) return false
    if (shift == null) return true
    return itemShift?.let { it == shift } ?: session.runs(shift)
}

@JvmName("notificationsInDeptSemesterScope")
fun List<Notification>.inScope(scope: DeptSemesterScope, sessions: Collection<AcademicSession>): List<Notification> =
    if (scope.isEmpty) this else filter { notice ->
        val deptId = notice.targetDeptId ?: notice.targetOfferingId?.let { deptOfSession(it, sessions) }
        if (scope.deptId != null && scope.deptId != deptId) return@filter false
        scope.matchesSessionItem(notice.targetOfferingId, null, notice.targetShift, sessions)
    }

/** A request's own [MarkEditRequest.semester] is used as-is (not the session's current one, which may
 * have moved on since) -- its shift isn't stored, so a chosen shift only requires the session to run it. */
@JvmName("markEditRequestsInDeptSemesterScope")
fun List<MarkEditRequest>.inScope(scope: DeptSemesterScope, sessions: Collection<AcademicSession>): List<MarkEditRequest> =
    if (scope.isEmpty) this else filter { scope.matchesSessionItem(it.sessionId, it.semester, null, sessions) }

@JvmName("attendanceEditRequestsInDeptSemesterScope")
fun List<AttendanceEditRequest>.inScope(scope: DeptSemesterScope, sessions: Collection<AcademicSession>): List<AttendanceEditRequest> =
    if (scope.isEmpty) this else filter { scope.matchesSessionItem(it.sessionId, it.semester, null, sessions) }

/** Link requests by the session the student claimed; a request without a session only matches "All". */
@JvmName("linkRequestsInDeptSemesterScope")
fun List<StudentLinkRequest>.inScope(scope: DeptSemesterScope, sessions: Collection<AcademicSession>): List<StudentLinkRequest> =
    if (scope.isEmpty) this else filter {
        val sessionId = it.sessionIdClaimed ?: return@filter false
        scope.matchesSessionItem(sessionId, null, null, sessions)
    }

/**
 * Events inside the scope. An event's own target works the same way as [ShiftScope.matches]: a
 * college-wide event (no department) reaches every scope, a department event every session of it, and
 * a session event both shifts unless narrowed.
 */
@JvmName("calendarEventsInDeptSemesterScope")
fun List<CalendarEvent>.inScope(scope: DeptSemesterScope, sessions: Collection<AcademicSession>): List<CalendarEvent> =
    if (scope.isEmpty) this else filter { event ->
        val deptId = event.deptId ?: event.sessionId?.let { deptOfSession(it, sessions) }
        if (scope.deptId != null && scope.deptId != deptId) return@filter false
        scope.matchesSessionItem(event.sessionId, null, event.shift, sessions)
    }

/** A teacher's exam-paper slots inside the scope -- the datesheet carries its own session, shift and
 * semester directly, so only the department needs resolving. */
@JvmName("paperSlotsInDeptSemesterScope")
fun List<TeacherPaperSlot>.inScope(scope: DeptSemesterScope, sessions: Collection<AcademicSession>): List<TeacherPaperSlot> =
    if (scope.isEmpty) this else filter { scope.matchesSessionItem(it.datesheet.sessionId, it.datesheet.semester, it.datesheet.shift, sessions) }

/**
 * Insights rows inside a Department/Semester/Shift/Program scope. A [SessionOverview] and [ExamStat]
 * carry their own semester already (used as-is, not the session's current one, which may have moved on
 * since); [AtRiskStudent] doesn't, so its session's current semester is the best available proxy.
 * Department and program type are always resolved from the row's session, via [matchesSessionItem].
 */
fun scopeInsights(
    overviews: List<SessionOverview>,
    atRisk: List<AtRiskStudent>,
    examStats: List<ExamStat>,
    scope: DeptSemesterScope,
    sessions: Collection<AcademicSession>,
): ScopedInsights {
    if (scope.isEmpty) return ScopedInsights(overviews, atRisk, examStats)
    return ScopedInsights(
        overviews = overviews.filter { scope.matchesSessionItem(it.sessionId, it.currentSemester, it.shift, sessions) },
        atRisk = atRisk.filter { scope.matchesSessionItem(it.sessionId, null, it.shift, sessions) },
        examStats = examStats.filter { scope.matchesSessionItem(it.sessionId, it.semester, it.shift, sessions) },
    )
}

/**
 * "Information Technology · Semester 3 · Evening": what a scoped export covers. Only chosen levels
 * are named; null when nothing is chosen (everything).
 */
fun DeptSemesterScope.title(departments: List<Pair<String, String>>): String? {
    if (isEmpty) return null
    return listOfNotNull(
        deptId?.let { id -> departments.firstOrNull { it.first == id }?.second ?: id },
        semester?.let { "Semester $it" },
        programType?.let { it.label },
        shift?.let { "${it.label} shift" },
    ).joinToString(" · ")
}

@JvmName("titleFromDepartments")
fun DeptSemesterScope.title(departments: List<Department>): String? = title(departments.map { it.deptId to it.name })

/**
 * The one session this scope resolves to, once department+semester (and, if two program types share
 * that department+semester, program type too) narrow it down to exactly one active batch -- the same
 * one-active-batch-per-department-per-semester-and-program assumption the master timetable and fee
 * structures grids already rely on. Null while department or semester is still unset, or if more than
 * one active session matches (ambiguous -- the caller should ask for program type too).
 */
fun DeptSemesterScope.resolveSession(sessions: Collection<AcademicSession>): AcademicSession? {
    if (deptId == null || semester == null) return null
    return sessions.filter { it.isActive && matches(it) }.singleOrNull()
}

/**
 * Every (session, shift) pair this scope reaches among active sessions -- for screens that must turn a
 * Department/Semester/Shift/Program browsing filter into concrete sends, e.g. fanning out one
 * notification or calendar event per matching class once a semester or program type is chosen (there's
 * no single session that alone represents "every 3rd-semester class across every department").
 */
fun DeptSemesterScope.resolveTargets(sessions: Collection<AcademicSession>): List<Pair<AcademicSession, Session>> =
    sessions.filter { it.isActive && matches(it) }.flatMap { session ->
        (shift?.let { listOf(it) } ?: session.shifts).map { session to it }
    }

/** A teacher's own classes inside the scope -- each class's semester/program come from its session. */
@JvmName("assignmentsInDeptSemesterScope")
fun List<ResolvedAssignment>.inScope(scope: DeptSemesterScope): List<ResolvedAssignment> =
    if (scope.isEmpty) this else filter { a ->
        scope.matches(a.deptId.ifBlank { null }, a.session?.currentSemester, a.classShift, a.session?.programType)
    }

/** The distinct current semesters among a teacher's own classes, low to high -- the options for the
 * "Semester" dropdown on their class filter. */
fun List<ResolvedAssignment>.scopeSemesters(): List<Int> = mapNotNull { it.session?.currentSemester }.distinct().sorted()

/** The distinct program types among a teacher's own classes -- the options for their class filter. */
fun List<ResolvedAssignment>.scopePrograms(): List<ProgramType> = mapNotNull { it.session?.programType }.distinct()
