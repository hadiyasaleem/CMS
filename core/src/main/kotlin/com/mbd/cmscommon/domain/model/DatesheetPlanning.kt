package com.mbd.cmscommon.domain.model

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeParseException
import java.util.Locale

private val EMAIL_PATTERN = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

enum class DatesheetViewerRole {
    ADMIN,
    TEACHER,
    STUDENT,
}

data class DatesheetViewerContext(
    val role: DatesheetViewerRole,
    val sessionId: String? = null,
    val canManage: Boolean = role == DatesheetViewerRole.ADMIN,
    val identityKey: String? = null,
)

data class DatesheetDraft(
    val title: String,
    val examType: String?,
    val sessionId: String?,
    val instructions: String?,
    val published: Boolean,
)

data class DatesheetScheduleQuality(
    val slots: List<DatesheetSlot>,
    val duplicateCount: Int,
    val issues: List<String>,
) {
    val canPublish: Boolean get() = slots.isNotEmpty() && issues.isEmpty()
}

data class DatesheetDutySummary(
    val loadedPapers: Int,
    val assignedDuties: Int,
    val upcomingDuties: Int,
    val dutyDates: Int,
)

fun datesheetScheduleQuality(sheet: Datesheet, slots: List<DatesheetSlot>): DatesheetScheduleQuality {
    val seenIds = mutableSetOf<String>()
    var duplicates = 0
    val normalizedSlots = slots.map { normalized(it) }.filter { slot ->
        val id = slot.id.trim()
        if (id.isBlank() || seenIds.add(id)) {
            true
        } else {
            duplicates++
            false
        }
    }

    val issues = mutableListOf<String>()
    if (sheet.id.isBlank()) {
        issues += "The datesheet has no database ID and cannot be managed safely."
    }
    validationMessage(DatesheetDraft(sheet.title, sheet.examType, sheet.sessionId, sheet.instructions, sheet.published))
        ?.let { issues += it }
    if (normalizedSlots.isEmpty()) {
        issues += "Add at least one exam paper before publishing this datesheet."
    }
    if (duplicates > 0) {
        issues += "$duplicates duplicate paper ${if (duplicates == 1) "record was" else "records were"} ignored."
    }

    normalizedSlots.forEach { slot ->
        if (slot.id.isBlank()) {
            issues += "A paper has no database ID and cannot be managed safely."
        }
        if (slot.datesheetId != sheet.id) {
            issues += "${displaySubject(slot)} belongs to a different datesheet."
        }
        validationMessage(slot)?.let { issues += "${displaySubject(slot)}: $it" }
    }

    normalizedSlots.groupBy { it.courseCode?.uppercase()?.takeIf { code -> code.isNotBlank() } }
        .filterKeys { it != null }
        .filterValues { it.size > 1 }
        .keys
        .forEach { courseCode -> issues += "Course $courseCode is scheduled more than once." }

    normalizedSlots.forEachIndexed { index, first ->
        normalizedSlots.drop(index + 1).forEach { second ->
            if (overlaps(first, second)) {
                issues += "${displaySubject(first)} overlaps ${displaySubject(second)} on ${first.examDate}."
                val firstLocation = locationKey(first)
                if (firstLocation != null && firstLocation == locationKey(second)) {
                    issues += "Room ${locationLabel(first)} is double-booked on ${first.examDate}."
                }
                val firstInvigilator = first.invigilatorEmail?.lowercase()?.takeIf { it.isNotBlank() }
                if (firstInvigilator != null && firstInvigilator == second.invigilatorEmail?.lowercase()) {
                    issues += "Invigilator $firstInvigilator is double-booked on ${first.examDate}."
                }
            }
        }
    }

    return DatesheetScheduleQuality(normalizedSlots, duplicates, issues.distinct())
}

fun datesheetKey(datesheet: Datesheet): String {
    val trimmed = datesheet.id.trim()
    if (trimmed.isNotBlank()) return trimmed
    return listOf(datesheet.title, datesheet.sessionId, datesheet.examType, datesheet.createdAt).joinToString("|")
}

fun datesheetSlotKey(slot: DatesheetSlot): String {
    val trimmed = slot.id.trim()
    if (trimmed.isNotBlank()) return trimmed
    return listOf(
        slot.datesheetId, slot.examDate, slot.startTime,
        slot.courseCode, slot.subjectName, slot.createdAt,
    ).joinToString("|")
}

private fun overlaps(slot: DatesheetSlot, other: DatesheetSlot): Boolean {
    if (slot.examDate != other.examDate) return false
    val (start1, end1) = timeRange(slot) ?: return false
    val (start2, end2) = timeRange(other) ?: return false
    return start1 < end2 && start2 < end1
}

private fun timeRange(slot: DatesheetSlot): Pair<LocalTime, LocalTime>? {
    val start = slot.startTime?.let { runCatching { LocalTime.parse(it) }.getOrNull() } ?: return null
    val end = slot.endTime?.let { runCatching { LocalTime.parse(it) }.getOrNull() }
        ?: slot.durationMinutes?.let { start.plusMinutes(it.toLong()) }
        ?: return null
    return start to end
}

private fun locationKey(slot: DatesheetSlot): String? {
    val key = listOfNotNull(cleanText(slot.building), cleanText(slot.roomNo)).joinToString("|").lowercase()
    return key.ifBlank { null }
}

private fun locationLabel(slot: DatesheetSlot): String =
    listOfNotNull(cleanText(slot.building), cleanText(slot.roomNo)).joinToString(" / ")

private fun displaySubject(slot: DatesheetSlot): String =
    cleanText(slot.subjectName) ?: cleanText(slot.courseCode) ?: "Untitled paper"

fun isAssignedTo(slot: DatesheetSlot, identityKey: String?): Boolean {
    if (identityKey.isNullOrBlank()) return false
    val invigilator = slot.invigilatorEmail?.trim() ?: return false
    return invigilator.equals(identityKey.trim(), ignoreCase = true)
}

fun datesheetDutySummary(
    slots: Map<String, List<DatesheetSlot>>,
    identityKey: String?,
    today: LocalDate = LocalDate.now(),
): DatesheetDutySummary {
    val papers = slots.values.flatten()
    val duties = papers.filter { isAssignedTo(it, identityKey) }
    val dutyDates = duties.mapNotNull { runCatching { LocalDate.parse(it.examDate) }.getOrNull() }
    val upcomingDuties = dutyDates.count { !it.isBefore(today) }
    return DatesheetDutySummary(papers.size, duties.size, upcomingDuties, dutyDates.distinct().size)
}

fun isVisibleTo(datesheet: Datesheet, viewer: DatesheetViewerContext): Boolean {
    if (viewer.canManage) return true
    if (!datesheet.published) return false
    return viewer.role != DatesheetViewerRole.STUDENT ||
        datesheet.sessionId == null ||
        datesheet.sessionId == viewer.sessionId
}

fun validationMessage(draft: DatesheetDraft): String? {
    if (draft.title.isBlank()) return "Enter a datesheet title."
    if (draft.title.trim().length > 120) return "Keep the title within 120 characters."
    if (draft.examType != null && draft.examType !in setOf("MIDTERM", "SESSIONAL")) return "Choose a valid exam type."
    if ((draft.instructions ?: "").trim().length > 1000) return "Keep instructions within 1,000 characters."
    return null
}

fun validationMessage(slot: DatesheetSlot): String? {
    val date = try {
        LocalDate.parse(slot.examDate.trim())
    } catch (e: DateTimeParseException) {
        return "Enter the exam date as YYYY-MM-DD."
    }
    if (date.year !in 2000..2100) return "Enter a realistic exam date."

    if (slot.courseCode.isNullOrBlank() && slot.subjectName.isNullOrBlank()) return "Enter or select a subject."
    if ((slot.courseCode ?: "").trim().length > 20) return "Keep the course code within 20 characters."
    if ((slot.subjectName ?: "").trim().length > 120) return "Keep the subject name within 120 characters."
    if ((slot.roomNo ?: "").trim().length > 50) return "Keep the room within 50 characters."
    if ((slot.building ?: "").trim().length > 100) return "Keep the building within 100 characters."

    val start = cleanText(slot.startTime)?.let {
        try {
            LocalTime.parse(it)
        } catch (e: DateTimeParseException) {
            return "Enter the start time as HH:MM."
        }
    }
    val end = cleanText(slot.endTime)?.let {
        try {
            LocalTime.parse(it)
        } catch (e: DateTimeParseException) {
            return "Enter the end time as HH:MM."
        }
    }
    if ((start == null) != (end == null)) return "Enter both start and end times."
    if (start != null && end != null && !end.isAfter(start)) return "End time must be after start time."
    if (slot.durationMinutes != null && slot.durationMinutes !in 1..600) return "Duration must be between 1 and 600 minutes."
    if (start == null && slot.durationMinutes == null) return "Enter exam times or a duration."

    val invigilatorEmail = slot.invigilatorEmail
    if (!invigilatorEmail.isNullOrBlank() && !EMAIL_PATTERN.matches(invigilatorEmail.trim())) {
        return "Enter a valid invigilator email."
    }
    return null
}

fun normalized(slot: DatesheetSlot): DatesheetSlot = slot.copy(
    examDate = slot.examDate.trim(),
    startTime = cleanTime(slot.startTime),
    endTime = cleanTime(slot.endTime),
    courseCode = cleanText(slot.courseCode)?.uppercase(),
    subjectName = cleanText(slot.subjectName),
    roomNo = cleanText(slot.roomNo),
    building = cleanText(slot.building),
    invigilatorEmail = cleanText(slot.invigilatorEmail)?.lowercase(),
)

private fun cleanText(value: String?): String? = value?.trim()?.takeIf { it.isNotBlank() }

private fun cleanTime(value: String?): String? {
    val text = cleanText(value) ?: return null
    return runCatching { LocalTime.parse(text).toString() }.getOrDefault(text)
}
