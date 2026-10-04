package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.AtRiskStudent
import com.mbd.cmscommon.domain.model.AttendanceEditRequest
import com.mbd.cmscommon.domain.model.CalendarEvent
import com.mbd.cmscommon.domain.model.MarkEditRequest
import com.mbd.cmscommon.domain.model.Notification
import com.mbd.cmscommon.domain.model.StudentLinkRequest
import com.mbd.cmscommon.domain.model.TaughtClass
import com.mbd.cmscommon.domain.model.ExamStat
import com.mbd.cmscommon.domain.model.SessionOverview
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.domain.model.ShiftScope
import com.mbd.cmscommon.teacher.ResolvedAssignment
import com.mbd.cmscommon.util.StudentIdCodec

/** The department a session belongs to: from the loaded sessions, else the id's own prefix ("IT_2022" -> "IT"). */
fun deptOfSession(sessionId: String, sessions: Collection<AcademicSession>): String =
    sessions.firstOrNull { it.sessionId == sessionId }?.deptId ?: StudentIdCodec.deptIdOf(sessionId)

/**
 * Whether an item that knows only its session (and maybe its shift) is inside the scope. A null [sessionId] is
 * college-wide data and matches any scope; a null [shift] is shared by both shifts of its session.
 */
fun ShiftScope.matchesSessionItem(sessionId: String?, shift: Session?, sessions: Collection<AcademicSession>): Boolean {
    if (sessionId.isNullOrBlank()) return true
    if (!matches(deptOfSession(sessionId, sessions), sessionId, shift)) return false
    // An item without its own shift belongs to a session: it is outside a shift the session doesn't run.
    val session = sessions.firstOrNull { it.sessionId == sessionId }
    return this.shift == null || shift != null || session == null || session.runs(this.shift)
}

/** A teacher's class (one shift of a session) inside the scope. */
fun ShiftScope.matches(assignment: ResolvedAssignment): Boolean =
    // A merged class matches the scope if ANY of its sessions (primary or linked) does.
    assignment.sessionIds.any { sid -> matches(assignment.deptId.ifBlank { null } ?: StudentIdCodec.deptIdOf(sid), sid, assignment.classShift) }

@JvmName("assignmentsInScope")
fun List<ResolvedAssignment>.inScope(scope: ShiftScope): List<ResolvedAssignment> =
    if (scope.isEmpty) this else filter { scope.matches(it) }

/** Sessions inside the scope (a chosen shift keeps only sessions that run it). */
@JvmName("sessionsInScope")
fun List<AcademicSession>.inScope(scope: ShiftScope): List<AcademicSession> = if (scope.isEmpty) this else filter { scope.matches(it) }

/** The sessions and departments a teacher's own classes span: the options of their class filter. */
fun List<ResolvedAssignment>.scopeSessions(): List<AcademicSession> =
    mapNotNull { it.session }.distinctBy { it.sessionId }.sortedWith(compareByDescending<AcademicSession> { it.startYear }.thenBy { it.deptId })

fun List<ResolvedAssignment>.scopeDepartments(): List<Pair<String, String>> =
    map { (it.deptId.ifBlank { null } ?: StudentIdCodec.deptIdOf(it.sessionId)) to it.deptName.ifBlank { it.sessionId } }
        .distinctBy { it.first }
        .sortedBy { it.second }

/** Department options (id to name) for the scope selector, alphabetical. */
fun departmentScopeOptions(departments: List<Department>): List<Pair<String, String>> =
    departments.sortedBy { it.name }.map { it.deptId to it.name }

/**
 * "Information Technology · 2022–2026 · Evening": what an export covers. Only chosen levels are named; null when
 * nothing is chosen (all departments).
 */
fun ShiftScope.title(departments: List<Department>, sessions: Collection<AcademicSession>): String? =
    title(departments.map { it.deptId to it.name }, sessions)

@JvmName("titleFromOptions")
fun ShiftScope.title(departments: List<Pair<String, String>>, sessions: Collection<AcademicSession>): String? {
    if (isEmpty) return null
    val session = sessionId?.let { id -> sessions.firstOrNull { it.sessionId == id } }
    return listOfNotNull(
        deptId?.let { id -> departments.firstOrNull { it.first == id }?.second ?: id },
        session?.label ?: sessionId,
        shift?.let { "${it.label} shift" },
    ).joinToString(" · ")
}

/** Export title lines with the scope added: "Insights" + "Information Technology · 2022–2026 · Evening". */
fun scopedTitle(title: List<String>, scopeTitle: String?): List<String> =
    if (scopeTitle == null) title else title.take(1) + scopeTitle + title.drop(1)

/** A file-name suffix for the scope: "_IT_2022_evening", or "" when nothing is chosen. */
fun ShiftScope.fileSuffix(): String =
    listOfNotNull(deptId, sessionId?.substringAfterLast('_'), shift?.name?.lowercase())
        .joinToString("") { "_$it" }

/** Insights rows inside a Department -> Session -> Shift scope. */
data class ScopedInsights(
    val overviews: List<SessionOverview>,
    val atRisk: List<AtRiskStudent>,
    val examStats: List<ExamStat>,
)

fun scopeInsights(
    overviews: List<SessionOverview>,
    atRisk: List<AtRiskStudent>,
    examStats: List<ExamStat>,
    scope: ShiftScope,
    sessions: Collection<AcademicSession>,
): ScopedInsights {
    if (scope.isEmpty) return ScopedInsights(overviews, atRisk, examStats)
    return ScopedInsights(
        overviews = overviews.filter { scope.matches(it.deptId, it.sessionId, it.shift) },
        atRisk = atRisk.filter { scope.matchesSessionItem(it.sessionId, it.shift, sessions) },
        examStats = examStats.filter { scope.matchesSessionItem(it.sessionId, it.shift, sessions) },
    )
}

/** What a screen's Department -> Session -> Shift filter offers: (id, name) departments and the sessions. */
data class ScopeFilterOptions(
    val departments: List<Pair<String, String>> = emptyList(),
    val sessions: List<AcademicSession> = emptyList(),
) {
    companion object {
        fun of(departments: List<Department>, sessions: List<AcademicSession>) = ScopeFilterOptions(departmentScopeOptions(departments), sessions)
    }
}

/** Class picker entries (key "IT_2022@EVENING" to label) inside the scope; see [shiftClassKey]. */
fun classesInScope(classes: List<Pair<String, String>>, scope: ShiftScope, sessions: Collection<AcademicSession>): List<Pair<String, String>> =
    if (scope.isEmpty) classes else classes.filter { (key, _) ->
        val (sessionId, shift) = parseShiftClassKey(key)
        scope.matchesSessionItem(sessionId, shift, sessions)
    }

/** These requests only know their roll number, not a shift -- a free-form roll number can't be used to infer one, so they match either shift of their session. */
@JvmName("markEditRequestsInScope")
fun List<MarkEditRequest>.inScope(scope: ShiftScope, sessions: Collection<AcademicSession>): List<MarkEditRequest> =
    if (scope.isEmpty) this else filter { scope.matchesSessionItem(it.sessionId, null, sessions) }

@JvmName("attendanceEditRequestsInScope")
fun List<AttendanceEditRequest>.inScope(scope: ShiftScope, sessions: Collection<AcademicSession>): List<AttendanceEditRequest> =
    if (scope.isEmpty) this else filter { scope.matchesSessionItem(it.sessionId, null, sessions) }

/** Link requests by the session the student claimed; a request without a session only matches "All". */
@JvmName("linkRequestsInScope")
fun List<StudentLinkRequest>.inScope(scope: ShiftScope, sessions: Collection<AcademicSession>): List<StudentLinkRequest> =
    if (scope.isEmpty) this else filter {
        val sessionId = it.sessionIdClaimed ?: return@filter false
        scope.matchesSessionItem(sessionId, null, sessions)
    }

/**
 * Events inside the scope. An event's own target works the same way: a college-wide event (no department)
 * reaches every scope, a department event every session of it, and a session event both shifts unless narrowed.
 */
@JvmName("calendarEventsInScope")
fun List<CalendarEvent>.inScope(scope: ShiftScope, sessions: Collection<AcademicSession>): List<CalendarEvent> =
    if (scope.isEmpty) this else filter { event ->
        val deptId = event.deptId ?: event.sessionId?.let { deptOfSession(it, sessions) }
        scope.matches(deptId, event.sessionId, event.shift)
    }

@JvmName("notificationsInScope")
fun List<Notification>.inScope(scope: ShiftScope, sessions: Collection<AcademicSession>): List<Notification> =
    if (scope.isEmpty) this else filter { notice ->
        val deptId = notice.targetDeptId ?: notice.targetOfferingId?.let { deptOfSession(it, sessions) }
        scope.matches(deptId, notice.targetOfferingId, notice.targetShift)
    }

/** A department / intake-year / shift cascade (master timetable, datesheets) seen as a scope. */
fun cascadeScope(deptId: String?, startYear: Int?, shift: Session?, sessions: Collection<AcademicSession>): ShiftScope =
    ShiftScope(deptId, sessions.firstOrNull { it.deptId == deptId && it.startYear == startYear }?.sessionId, shift)

data class ScopeCascade(val deptId: String?, val startYear: Int?, val shift: Session?)

/** The cascade selections for a scope: the session's department and intake year, and the chosen shift. */
fun ShiftScope.toCascade(sessions: Collection<AcademicSession>): ScopeCascade {
    val session = sessionId?.let { id -> sessions.firstOrNull { it.sessionId == id } }
    return ScopeCascade(deptId ?: session?.deptId, session?.startYear, shift)
}

/** Filter options limited to the sessions a teacher's own items span (departments labelled by their code). */
fun ownScopeOptions(sessionIds: Collection<String>, sessions: Collection<AcademicSession>): ScopeFilterOptions {
    val own = sessions.filter { it.sessionId in sessionIds }
    return ScopeFilterOptions(own.map { it.deptId to it.deptId.uppercase() }.distinct().sortedBy { it.second }, own)
}

/** Timetable periods inside the scope, each by its session and shift. */
@JvmName("periodsInScope")
fun List<SessionPeriod>.inScope(scope: ShiftScope, sessions: Collection<AcademicSession>): List<SessionPeriod> =
    if (scope.isEmpty) this else filter { scope.matchesSessionItem(it.sessionId, it.shift, sessions) }

/** A teacher's exam-paper slots inside the scope, by the datesheet's session and shift. */
@JvmName("paperSlotsInScope")
fun List<TeacherPaperSlot>.inScope(scope: ShiftScope, sessions: Collection<AcademicSession>): List<TeacherPaperSlot> =
    if (scope.isEmpty) this else filter { scope.matchesSessionItem(it.datesheet.sessionId, it.datesheet.shift, sessions) }

/** The sessions and shifts a teacher teaches, for event and notification targeting. */
fun List<ResolvedAssignment>.taughtClasses(): Set<TaughtClass> =
    flatMap { a -> a.sessionIds.mapNotNull { sid -> a.classShift?.let { TaughtClass(sid, a.deptId.ifBlank { StudentIdCodec.deptIdOf(sid) }, it) } } }.toSet()
