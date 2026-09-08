package com.mbd.cmscommon.domain.model

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeParseException

private val EMAIL_PATTERN = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

enum class DatesheetViewerRole {
    ADMIN,
    TEACHER,
    STUDENT,
}

data class DatesheetViewerContext(
    val role: DatesheetViewerRole,
    val sessionId: String? = null,
    val semester: Int? = null,
    val canManage: Boolean = role == DatesheetViewerRole.ADMIN,
    val identityKey: String? = null,
)

data class DatesheetDraft(
    val sessionId: String,
    val semester: Int,
    val defaultStartTime: String? = null,
    val defaultEndTime: String? = null,
    val defaultBuildingId: String? = null,
    val instructions: String? = null,
    val published: Boolean = false,
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

/** Curriculum subjects not yet on the datesheet, and scheduled papers no longer in the curriculum. */
data class DatesheetCurriculumDrift(
    val missingSubjects: List<SemesterSubject>,
    val staleSlots: List<DatesheetSlot>,
) {
    val isClean: Boolean get() = missingSubjects.isEmpty() && staleSlots.isEmpty()
}

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
        if (!slot.isScheduled(sheet)) {
            issues += "${displaySubject(slot)} still needs a date, time, and building."
        }
    }

    // A subject scheduled twice or two papers sharing a date are also rejected by the database
    // (uq_datesheet_slot_course / uq_datesheet_slot_date) -- surfaced here too for feedback while
    // the admin is still editing, before either write hits the server.
    normalizedSlots.groupBy { it.courseCode.uppercase() }
        .filterValues { it.size > 1 }
        .keys
        .forEach { courseCode -> issues += "$courseCode is scheduled more than once." }

    normalizedSlots.filter { it.examDate != null }
        .groupBy { it.examDate }
        .filterValues { it.size > 1 }
        .forEach { (date, clashing) ->
            issues += "More than one paper is scheduled on $date: ${clashing.joinToString { displaySubject(it) }}."
        }

    return DatesheetScheduleQuality(normalizedSlots, duplicates, issues.distinct())
}

/**
 * Client-side warning for a room/invigilator clash with a paper on ANOTHER datesheet -- the
 * authoritative check is the fn_check_datesheet_conflict() database trigger, this just gives
 * immediate feedback while scheduling instead of waiting for the save to be rejected.
 * [otherPapers] pairs each candidate paper with its own owning datesheet, since that paper's
 * effective time may come from that datesheet's own default, not [sheet]'s.
 */
fun datesheetExternalConflicts(slot: DatesheetSlot, sheet: Datesheet, otherPapers: List<Pair<DatesheetSlot, Datesheet>>): List<String> {
    val date = slot.examDate ?: return emptyList()
    val start = slot.resolvedStartTime(sheet)?.let { runCatching { LocalTime.parse(it) }.getOrNull() } ?: return emptyList()
    val end = slot.resolvedEndTime(sheet)?.let { runCatching { LocalTime.parse(it) }.getOrNull() } ?: return emptyList()

    val overlapping = otherPapers.filter { (other, otherSheet) ->
        if (other.id == slot.id || other.examDate != date) return@filter false
        val oStart = other.resolvedStartTime(otherSheet)?.let { runCatching { LocalTime.parse(it) }.getOrNull() } ?: return@filter false
        val oEnd = other.resolvedEndTime(otherSheet)?.let { runCatching { LocalTime.parse(it) }.getOrNull() } ?: return@filter false
        start < oEnd && oStart < end
    }

    val issues = mutableListOf<String>()
    slot.roomId?.let { roomId ->
        overlapping.firstOrNull { it.first.roomId == roomId }?.let {
            issues += "That room is already booked for ${displaySubject(it.first)} at an overlapping time on $date."
        }
    }
    slot.invigilatorEmail?.let { email ->
        overlapping.firstOrNull { it.first.invigilatorEmail.equals(email, ignoreCase = true) }?.let {
            issues += "$email already has an overlapping duty for ${displaySubject(it.first)} on $date."
        }
    }
    return issues
}

/** Which curriculum subjects are missing a paper, and which scheduled papers fell out of the curriculum. */
fun datesheetCurriculumDrift(slots: List<DatesheetSlot>, subjects: List<SemesterSubject>): DatesheetCurriculumDrift {
    val scheduledCodes = slots.map { it.courseCode.trim().uppercase() }.toSet()
    val curriculumCodes = subjects.map { it.courseCode.trim().uppercase() }.toSet()
    val missing = subjects.filter { it.courseCode.trim().uppercase() !in scheduledCodes }
    val stale = slots.filter { it.courseCode.trim().uppercase() !in curriculumCodes }
    return DatesheetCurriculumDrift(missing, stale)
}

fun datesheetKey(datesheet: Datesheet): String {
    val trimmed = datesheet.id.trim()
    if (trimmed.isNotBlank()) return trimmed
    return listOf(datesheet.sessionId, datesheet.semester, datesheet.createdAt).joinToString("|")
}

fun datesheetSlotKey(slot: DatesheetSlot): String {
    val trimmed = slot.id.trim()
    if (trimmed.isNotBlank()) return trimmed
    return listOf(slot.datesheetId, slot.courseCode, slot.createdAt).joinToString("|")
}

private fun displaySubject(slot: DatesheetSlot): String =
    cleanText(slot.subjectName) ?: cleanText(slot.courseCode) ?: "Untitled paper"

/** "Building / Room", "Building" alone, or "No room assigned" if neither is set. */
fun locationLabel(slot: DatesheetSlot): String {
    val parts = listOfNotNull(cleanText(slot.building), cleanText(slot.roomNo))
    return parts.joinToString(" / ").ifBlank { "No room assigned" }
}

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
    val dutyDates = duties.mapNotNull { it.examDate?.let { d -> runCatching { LocalDate.parse(d) }.getOrNull() } }
    val upcomingDuties = dutyDates.count { !it.isBefore(today) }
    return DatesheetDutySummary(papers.size, duties.size, upcomingDuties, dutyDates.distinct().size)
}

fun isVisibleTo(datesheet: Datesheet, viewer: DatesheetViewerContext): Boolean {
    if (viewer.canManage) return true
    if (!datesheet.published) return false
    if (viewer.role == DatesheetViewerRole.STUDENT) {
        return datesheet.sessionId == viewer.sessionId && datesheet.semester == viewer.semester
    }
    return true
}

fun validationMessage(draft: DatesheetDraft): String? {
    if (draft.sessionId.isBlank()) return "Choose a session."
    if (draft.semester !in 1..8) return "Choose a semester."

    val start = cleanText(draft.defaultStartTime)?.let {
        try {
            LocalTime.parse(it)
        } catch (e: DateTimeParseException) {
            return "Enter the default start time as HH:MM."
        }
    }
    val end = cleanText(draft.defaultEndTime)?.let {
        try {
            LocalTime.parse(it)
        } catch (e: DateTimeParseException) {
            return "Enter the default end time as HH:MM."
        }
    }
    if ((start == null) != (end == null)) return "Enter both a default start and end time."
    if (start != null && end != null && !end.isAfter(start)) return "Default end time must be after the start time."
    if ((draft.instructions ?: "").trim().length > 1000) return "Keep instructions within 1,000 characters."
    return null
}

fun validationMessage(slot: DatesheetSlot): String? {
    if (slot.courseCode.isBlank() || slot.subjectName.isBlank()) return "Choose a subject."
    if (slot.courseCode.trim().length > 20) return "Keep the course code within 20 characters."
    if (slot.subjectName.trim().length > 120) return "Keep the subject name within 120 characters."

    val date = slot.examDate?.let {
        try {
            LocalDate.parse(it.trim())
        } catch (e: DateTimeParseException) {
            return "Enter the exam date as YYYY-MM-DD."
        }
    }
    if (date != null && date.year !in 2000..2100) return "Enter a realistic exam date."

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
    if ((start == null) != (end == null)) return "Enter both a start and end time, or leave both blank to use the datesheet default."
    if (start != null && end != null && !end.isAfter(start)) return "End time must be after start time."

    if ((slot.roomNo ?: "").trim().length > 50) return "Keep the room within 50 characters."
    if ((slot.building ?: "").trim().length > 100) return "Keep the building within 100 characters."

    val invigilatorEmail = slot.invigilatorEmail
    if (!invigilatorEmail.isNullOrBlank() && !EMAIL_PATTERN.matches(invigilatorEmail.trim())) {
        return "Enter a valid invigilator email."
    }
    return null
}

fun normalized(slot: DatesheetSlot): DatesheetSlot = slot.copy(
    examDate = cleanText(slot.examDate),
    startTime = cleanTime(slot.startTime),
    endTime = cleanTime(slot.endTime),
    courseCode = slot.courseCode.trim().uppercase(),
    subjectName = slot.subjectName.trim(),
    roomNo = cleanText(slot.roomNo),
    building = cleanText(slot.building),
    invigilatorEmail = cleanText(slot.invigilatorEmail)?.lowercase(),
)

private fun cleanText(value: String?): String? = value?.trim()?.takeIf { it.isNotBlank() }

private fun cleanTime(value: String?): String? {
    val text = cleanText(value) ?: return null
    return runCatching { LocalTime.parse(text).toString() }.getOrDefault(text)
}
