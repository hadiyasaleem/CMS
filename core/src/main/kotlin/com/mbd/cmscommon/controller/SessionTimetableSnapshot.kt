package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.PeriodType
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.util.clockDisplay
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale

data class SessionTimetableSnapshot(
    val teachingDays: Int,
    val teacherAssigned: Int,
    val roomAssigned: Int,
    val uniqueTeachers: Int,
    val conflicts: List<TimetableConflict>,
    val malformedPeriodIds: Set<String>,
) {
    val issueCount: Int get() = conflicts.size + malformedPeriodIds.size
    val conflictingPeriodIds: Set<String> get() = conflicts.flatMap { listOf(it.firstId, it.secondId) }.toSet()
}

fun sessionTimetableSnapshot(periods: List<SessionPeriod>): SessionTimetableSnapshot {
    val malformed = periods.filter { periodIssue(it) != null }.map { it.id }.toSet()
    val valid = periods.filterNot { malformed.contains(it.id) }

    val conflicts = mutableListOf<TimetableConflict>()
    valid.groupBy { it.shift to it.day }.values.forEach { dayPeriods ->
        dayPeriods.forEachIndexed { index, period ->
            dayPeriods.drop(index + 1).filter { periodsOverlap(period, it) }.forEach { other ->
                conflicts += TimetableConflict(period.id, other.id)
            }
        }
    }

    val teachingDays = periods.map { it.day }.filter { it.value <= DayOfWeek.SATURDAY.value }.distinct().size
    val teacherAssigned = periods.count { it.periodType == PeriodType.BREAK || it.teacherId.isNotBlank() }
    val roomAssigned = periods.count { it.periodType == PeriodType.BREAK || !it.roomNo.isNullOrBlank() }
    val uniqueTeachers = periods.map { it.teacherId }.filter { it.isNotBlank() }.distinct().size

    return SessionTimetableSnapshot(teachingDays, teacherAssigned, roomAssigned, uniqueTeachers, conflicts, malformed)
}

enum class ConflictKind { TEACHER, ROOM }

/** The other period a period double-books, and whether it's the teacher or the room that clashes. */
data class PeriodConflict(val kind: ConflictKind, val other: SessionPeriod)

/**
 * Every teacher/room double-booking across the *whole college's* timetable, keyed by period id.
 * Mirrors the database's `fn_check_timetable_conflict` trigger exactly (LECTURE periods only, same
 * day, overlapping time range and effective-date range, same teacher email or same room) so the
 * Master Timetable can explain *why* a cell is flagged, client-side, before anyone tries to save
 * anything. Unlike [sessionTimetableSnapshot] (one session's own periods only), this checks across
 * every session and shift, the same way the trigger does.
 */
fun masterTimetableConflicts(periods: List<SessionPeriod>): Map<String, List<PeriodConflict>> {
    val lectures = periods.filter { it.periodType == PeriodType.LECTURE }
    val result = mutableMapOf<String, MutableList<PeriodConflict>>()
    fun record(id: String, conflict: PeriodConflict) {
        result.getOrPut(id) { mutableListOf() } += conflict
    }
    lectures.groupBy { it.day }.values.forEach { dayPeriods ->
        dayPeriods.forEachIndexed { index, period ->
            dayPeriods.drop(index + 1).forEach { other ->
                if (!periodsOverlap(period, other)) return@forEach
                if (period.teacherId.isNotBlank() && period.teacherId == other.teacherId) {
                    record(period.id, PeriodConflict(ConflictKind.TEACHER, other))
                    record(other.id, PeriodConflict(ConflictKind.TEACHER, period))
                }
                if (!period.roomNo.isNullOrBlank() && period.roomNo == other.roomNo) {
                    record(period.id, PeriodConflict(ConflictKind.ROOM, other))
                    record(other.id, PeriodConflict(ConflictKind.ROOM, period))
                }
            }
        }
    }
    return result
}

/** A human-readable "which class is this" label for [period], for explaining a conflict -- department
 * code + semester + shift + subject, e.g. "ENG Semester 5 Morning — World Literatures in Translation". */
private fun describePeriodForConflict(period: SessionPeriod, sessions: List<AcademicSession>, departments: List<Department>): String {
    val session = sessions.firstOrNull { it.sessionId == period.sessionId }
    val department = session?.let { s -> departments.firstOrNull { it.deptId == s.deptId } }
    val classLabel = listOfNotNull(
        department?.code ?: session?.deptId ?: period.sessionId,
        session?.let { "Semester ${it.currentSemester}" },
        period.shift.label,
    ).joinToString(" ")
    return "$classLabel — ${period.subjectName} (${period.courseCode})"
}

/**
 * If [candidate] would double-book a teacher or room already scheduled somewhere in [allPeriods]
 * (any session, any department -- [excludedId] is the period being edited in place, so it doesn't
 * conflict with its own prior self), returns a message naming exactly which other class/subject it
 * clashes with. Mirrors the database's own no-double-booking trigger (same day, overlapping time and
 * effective-date range, same teacher or same room, LECTURE periods only) but -- unlike the trigger's
 * own raw error -- names the other class so an admin can tell a real clash from a false alarm (e.g.
 * two shifts that happen to share a teacher by mistake) without leaving this dialog.
 */
fun describeTimetableConflict(
    candidate: SessionPeriod,
    allPeriods: List<SessionPeriod>,
    sessions: List<AcademicSession>,
    departments: List<Department>,
    excludedId: String? = null,
): String? {
    if (candidate.periodType != PeriodType.LECTURE) return null
    val other = allPeriods.firstOrNull { other ->
        other.id != candidate.id && other.id != excludedId && other.periodType == PeriodType.LECTURE &&
            periodsOverlap(candidate, other) &&
            (
                (candidate.teacherId.isNotBlank() && candidate.teacherId == other.teacherId) ||
                    (!candidate.roomNo.isNullOrBlank() && candidate.roomNo == other.roomNo)
                )
    } ?: return null

    val who = if (candidate.teacherId.isNotBlank() && candidate.teacherId == other.teacherId) {
        "Teacher ${candidate.teacherName.ifBlank { candidate.teacherId }}"
    } else {
        "Room ${candidate.roomNo}"
    }
    val dayLabel = candidate.day.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
    val timeLabel = "${clockDisplay(candidate.startTime)}-${clockDisplay(candidate.endTime)}"
    val otherClass = describePeriodForConflict(other, sessions, departments)
    return "$who already has a lecture with $otherClass on $dayLabel at $timeLabel."
}

fun validateTimetableDraft(
    day: DayOfWeek,
    start: String,
    end: String,
    effectiveFrom: String,
    effectiveTo: String,
    existing: SessionPeriod?,
    allPeriods: List<SessionPeriod>,
    // Morning and Evening are separate grids, so a draft only overlaps periods of its own shift. Teacher and
    // room double-booking is checked across every shift and session by the database.
    shift: Session = existing?.shift ?: Session.MORNING,
): TimetableDraftValidation {
    val startTime = parseTimetableTime(start)
    val endTime = parseTimetableTime(end)
    val timeError = when {
        day.value > DayOfWeek.SATURDAY.value -> "Choose a day from Monday to Saturday."
        startTime == null || endTime == null -> "Use 24-hour time in HH:mm format."
        !startTime.isBefore(endTime) -> "End time must be later than start time."
        else -> null
    }

    val fromDate = parseTimetableDate(effectiveFrom)
    val toDate = parseTimetableDate(effectiveTo)
    val dateError = when {
        effectiveFrom.isNotBlank() && fromDate == null -> "Effective-from date must use YYYY-MM-DD."
        effectiveTo.isNotBlank() && toDate == null -> "Effective-to date must use YYYY-MM-DD."
        fromDate != null && toDate != null && fromDate.isAfter(toDate) -> "Effective-to date cannot be before effective-from date."
        else -> null
    }

    val candidate = if (timeError == null && dateError == null) {
        SessionPeriod(
            id = "draft",
            sessionId = existing?.sessionId ?: "",
            day = day,
            startTime = start.trim(),
            endTime = end.trim(),
            shift = shift,
            courseCode = "DRAFT",
            subjectName = "Draft",
            teacherId = "",
            teacherName = "",
            effectiveFrom = fromDate,
            effectiveTo = toDate,
        )
    } else {
        null
    }

    val overlapping = candidate?.let { draft ->
        allPeriods.firstOrNull { it.id != existing?.id && it.shift == draft.shift && periodIssue(it) == null && periodsOverlap(draft, it) }
    }

    val overlapError = overlapping?.let {
        val dayLabel = it.day.name.lowercase(Locale.ROOT).replaceFirstChar { c -> c.uppercase(Locale.ROOT) }
        "Overlaps ${it.courseCode} (${it.timeRange}) on $dayLabel."
    }

    return TimetableDraftValidation(timeError, overlapError, dateError)
}

fun validateTimetablePeriod(period: SessionPeriod, existing: SessionPeriod?, allPeriods: List<SessionPeriod>): String? {
    periodIssue(period)?.let { return it }
    return validateTimetableDraft(
        day = period.day,
        start = period.startTime,
        end = period.endTime,
        effectiveFrom = period.effectiveFrom?.toString() ?: "",
        effectiveTo = period.effectiveTo?.toString() ?: "",
        existing = existing,
        allPeriods = allPeriods,
        shift = period.shift,
    ).firstError
}

private fun periodIssue(period: SessionPeriod): String? {
    if (period.sessionId.isBlank()) return "Session is required."
    if (period.day.value > DayOfWeek.SATURDAY.value) return "Choose a day from Monday to Saturday."
    val startTime = parseTimetableTime(period.startTime) ?: return "Use 24-hour time in HH:mm format."
    val endTime = parseTimetableTime(period.endTime) ?: return "Use 24-hour time in HH:mm format."
    if (!startTime.isBefore(endTime)) return "End time must be later than start time."
    if (period.effectiveFrom != null && period.effectiveTo != null && period.effectiveFrom.isAfter(period.effectiveTo)) {
        return "Effective-to date cannot be before effective-from date."
    }
    if (period.periodType != PeriodType.BREAK && (period.courseCode.isBlank() || period.subjectName.isBlank())) {
        return "Choose a subject for this period."
    }
    if ((period.roomNo ?: "").trim().length > 50) return "Room must not exceed 50 characters."
    if ((period.building ?: "").trim().length > 100) return "Building must not exceed 100 characters."
    if ((period.notes ?: "").trim().length > 500) return "Notes must not exceed 500 characters."
    return null
}

private fun periodsOverlap(first: SessionPeriod, second: SessionPeriod): Boolean {
    if (first.day != second.day) return false
    val firstStart = parseTimetableTime(first.startTime) ?: return false
    val firstEnd = parseTimetableTime(first.endTime) ?: return false
    val secondStart = parseTimetableTime(second.startTime) ?: return false
    val secondEnd = parseTimetableTime(second.endTime) ?: return false

    val timesOverlap = firstStart.isBefore(secondEnd) && firstEnd.isAfter(secondStart)
    val datesOverlap = (first.effectiveTo == null || second.effectiveFrom == null || !first.effectiveTo.isBefore(second.effectiveFrom)) &&
        (second.effectiveTo == null || first.effectiveFrom == null || !second.effectiveTo.isBefore(first.effectiveFrom))
    return timesOverlap && datesOverlap
}

private fun parseTimetableTime(value: String): LocalTime? = runCatching { LocalTime.parse(value.trim()) }.getOrNull()

private fun parseTimetableDate(value: String): LocalDate? {
    val trimmed = value.trim().takeIf { it.isNotBlank() } ?: return null
    return runCatching { LocalDate.parse(trimmed) }.getOrNull()
}
