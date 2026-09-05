package com.mbd.cmscommon.domain.model

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

enum class CalendarViewerRole {
    ADMIN,
    TEACHER,
    STUDENT,
}

data class CalendarViewerContext(
    val role: CalendarViewerRole,
    val departmentId: String? = null,
    val sessionIds: Set<String> = emptySet(),
)

data class CalendarSummary(
    val upcoming: Int,
    val ongoing: Int,
    val thisMonth: Int,
    val exams: Int,
    val deadlines: Int,
    val nextEventInDays: Long?,
)

data class CalendarQueueSnapshot(
    val events: List<CalendarEvent>,
    val duplicateCount: Int,
    val invalidCount: Int,
)

fun calendarQueueSnapshot(events: List<CalendarEvent>): CalendarQueueSnapshot {
    val seenIds = mutableSetOf<String>()
    var duplicates = 0
    val normalized = events.filter { event ->
        val id = event.id.trim()
        if (id.isBlank() || seenIds.add(id)) {
            true
        } else {
            duplicates++
            false
        }
    }
    val invalid = normalized.count { persistedValidationMessage(it) != null }
    return CalendarQueueSnapshot(normalized, duplicates, invalid)
}

fun persistedValidationMessage(event: CalendarEvent): String? {
    if (event.id.isBlank()) {
        return "This event has no database ID and cannot be managed safely."
    }
    if (!event.sessionId.isNullOrBlank() && event.deptId.isNullOrBlank()) {
        return "The scoped session has no department reference."
    }
    return validationMessage(event)
}

fun calendarEventKey(event: CalendarEvent): String {
    val trimmed = event.id.trim()
    if (trimmed.isNotBlank()) return trimmed
    return listOf(
        event.title, event.startDate, event.startTime,
        event.audience, event.deptId, event.sessionId, event.createdAt,
    ).joinToString("|")
}

fun startDateOrNull(event: CalendarEvent): LocalDate? =
    runCatching { LocalDate.parse(event.startDate) }.getOrNull()

fun endDateOrStart(event: CalendarEvent): LocalDate? {
    val endDate = event.endDate?.takeIf { it.isNotBlank() }
    if (endDate != null) {
        runCatching { LocalDate.parse(endDate) }.getOrNull()?.let { return it }
    }
    return startDateOrNull(event)
}

fun isOngoingOn(event: CalendarEvent, date: LocalDate): Boolean {
    val start = startDateOrNull(event) ?: return false
    val end = endDateOrStart(event) ?: start
    return !date.isBefore(start) && !date.isAfter(end)
}

fun isUpcomingOn(event: CalendarEvent, date: LocalDate): Boolean {
    val end = endDateOrStart(event) ?: return false
    return !end.isBefore(date)
}

fun isPastOn(event: CalendarEvent, date: LocalDate): Boolean {
    val end = endDateOrStart(event) ?: return false
    return end.isBefore(date)
}

fun calendarSummary(events: List<CalendarEvent>, today: LocalDate): CalendarSummary {
    val monthStart = today.withDayOfMonth(1)
    val monthEnd = today.withDayOfMonth(today.lengthOfMonth())
    val active = events.filter { isUpcomingOn(it, today) }
    val next = active.mapNotNull { startDateOrNull(it) }.filter { !it.isBefore(today) }.minOrNull()

    val ongoing = events.count { isOngoingOn(it, today) }
    val thisMonth = events.count { event ->
        val start = startDateOrNull(event)
        if (start == null) {
            false
        } else {
            val end = endDateOrStart(event) ?: start
            !end.isBefore(monthStart) && !start.isAfter(monthEnd)
        }
    }
    val exams = active.count { it.eventType.equals("EXAM", ignoreCase = true) }
    val deadlines = active.count { it.eventType.equals("DEADLINE", ignoreCase = true) }

    return CalendarSummary(
        upcoming = active.size,
        ongoing = ongoing,
        thisMonth = thisMonth,
        exams = exams,
        deadlines = deadlines,
        nextEventInDays = next?.let { ChronoUnit.DAYS.between(today, it) },
    )
}

fun isVisibleTo(event: CalendarEvent, viewer: CalendarViewerContext): Boolean {
    if (viewer.role == CalendarViewerRole.ADMIN) return true
    val audienceMatches = event.audience.equals("ALL", ignoreCase = true) ||
        event.audience.equals(viewer.role.name, ignoreCase = true)
    if (!audienceMatches) return false
    if (!event.deptId.isNullOrBlank() && !event.deptId.equals(viewer.departmentId, ignoreCase = true)) {
        return false
    }
    return event.sessionId.isNullOrBlank() || viewer.sessionIds.contains(event.sessionId)
}

fun validationMessage(event: CalendarEvent): String? {
    if (event.title.isBlank()) return "Enter an event title."
    if (event.title.trim().length > 120) return "Keep the event title within 120 characters."
    if ((event.venue ?: "").trim().length > 120) return "Keep the venue within 120 characters."
    if ((event.description ?: "").trim().length > 2000) return "Keep the description within 2,000 characters."

    val validTypes = setOf("EVENT", "HOLIDAY", "EXAM", "DEADLINE")
    if (event.eventType.uppercase() !in validTypes) return "Choose a valid event type."

    val validAudiences = setOf("ALL", "ADMIN", "TEACHER", "STUDENT")
    if (event.audience.uppercase() !in validAudiences) return "Choose a valid audience."

    val start = try {
        LocalDate.parse(event.startDate)
    } catch (e: DateTimeParseException) {
        return "Enter the start date as YYYY-MM-DD."
    }

    val end = event.endDate?.takeIf { it.isNotBlank() }?.let {
        try {
            LocalDate.parse(it)
        } catch (e: DateTimeParseException) {
            return "Enter the end date as YYYY-MM-DD."
        }
    }
    if (end != null && end.isBefore(start)) return "End date cannot be before the start date."

    val parsedStartTime = event.startTime?.takeIf { it.isNotBlank() }?.let {
        try {
            LocalTime.parse(it)
        } catch (e: DateTimeParseException) {
            return "Enter the start time as HH:MM."
        }
    }
    val parsedEndTime = event.endTime?.takeIf { it.isNotBlank() }?.let {
        try {
            LocalTime.parse(it)
        } catch (e: DateTimeParseException) {
            return "Enter the end time as HH:MM."
        }
    }
    if (parsedEndTime != null && parsedStartTime == null) return "Add a start time before the end time."

    val sameDay = end == null || end == start
    if (sameDay && parsedStartTime != null && parsedEndTime != null && !parsedEndTime.isAfter(parsedStartTime)) {
        return "End time must be after the start time."
    }
    return null
}
