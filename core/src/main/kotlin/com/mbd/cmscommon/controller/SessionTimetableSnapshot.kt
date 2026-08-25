package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.PeriodType
import com.mbd.cmscommon.domain.model.SessionPeriod
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
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
    valid.groupBy { it.day }.values.forEach { dayPeriods ->
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

fun validateTimetableDraft(
    day: DayOfWeek,
    start: String,
    end: String,
    effectiveFrom: String,
    effectiveTo: String,
    existing: SessionPeriod?,
    allPeriods: List<SessionPeriod>,
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
        allPeriods.firstOrNull { it.id != existing?.id && periodIssue(it) == null && periodsOverlap(draft, it) }
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
