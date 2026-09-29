package com.mbd.cmscommon.export

import com.mbd.cmscommon.domain.model.Datesheet
import com.mbd.cmscommon.domain.model.DatesheetSlot
import com.mbd.cmscommon.domain.model.resolvedEndTime
import com.mbd.cmscommon.domain.model.resolvedStartTime
import com.mbd.cmscommon.util.clockDisplay
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/** One row of a datesheet grid: [rowLabel] is drawn merged in the first column (a department code,
 * or "Semester N" -- whatever the caller is grouping rows by), [sheet] supplies default times, and
 * [slots] are its papers. */
data class DatesheetGridEntry(val rowLabel: List<String>, val sheet: Datesheet, val slots: List<DatesheetSlot>)

/** "09:00-12:00 · R#12 BS Block" -- the paper's effective time and room, shown on the cell itself
 * since a datesheet has no recurring time-slot columns the way a timetable does. */
private fun datesheetCellLocation(slot: DatesheetSlot, sheet: Datesheet): String? {
    val start = slot.resolvedStartTime(sheet)
    val end = slot.resolvedEndTime(sheet)
    val time = if (start != null && end != null) "${clockDisplay(start)}-${clockDisplay(end)}" else null
    val room = listOfNotNull(slot.building?.ifBlank { null }, slot.roomNo?.ifBlank { null }).joinToString(" ").ifBlank { null }
    return listOfNotNull(time, room).joinToString(" · ").ifBlank { null }
}

/**
 * The same printed-grid shape the Master Timetable exports use, built from a datesheet's papers
 * instead of a timetable's periods: columns are every distinct exam date across [entries] (day-of-
 * month + month bold, weekday below), rows are [entries] merged vertically per block -- a datesheet
 * has no recurring weekly pattern, so each entry is always a single sub-row. [teacherNames] resolves
 * an invigilator's email to a display name, falling back to the email itself when absent.
 */
fun datesheetGridLayout(titleLines: List<String>, entries: List<DatesheetGridEntry>, teacherNames: Map<String, String> = emptyMap()): TimetableGridLayout {
    val rawDates = entries.flatMap { e -> e.slots.mapNotNull { it.examDate } }.distinct()
        .sortedBy { runCatching { LocalDate.parse(it) }.getOrDefault(LocalDate.MAX) }
    val columns = rawDates.map { raw ->
        val parsed = runCatching { LocalDate.parse(raw) }.getOrNull()
        val indexLabel = parsed?.let { "%02d %s".format(it.dayOfMonth, it.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)) } ?: raw
        val dayLabel = parsed?.dayOfWeek?.getDisplayName(TextStyle.FULL, Locale.ENGLISH).orEmpty()
        TimetableGridColumn(indexLabel, dayLabel)
    }

    val blocks = entries.map { entry ->
        val byDate = entry.slots.associateBy { it.examDate }
        val cells = rawDates.withIndex().mapNotNull { (i, date) ->
            val slot = byDate[date] ?: return@mapNotNull null
            val invigilator = slot.invigilatorEmail?.ifBlank { null }?.let { teacherNames[it] ?: it }.orEmpty()
            i to TimetableGridPeriodCell(
                courseCode = slot.courseCode,
                creditHours = null,
                subjectName = slot.subjectName,
                teacherName = invigilator,
                location = datesheetCellLocation(slot, entry.sheet),
            )
        }.toMap()
        TimetableGridBlock(entry.rowLabel, listOf(TimetableGridSubRow("", cells)))
    }
    return TimetableGridLayout(titleLines, columns, blocks, secondColumnHeader = null)
}

/** Wraps [entries] into an [ExportDocument] carrying a single grid section built by
 * [datesheetGridLayout] -- used both for one datesheet's own Export button and for a combined
 * Calendar/Semester view's top-of-screen Export button. */
fun datesheetGridsExport(fileBase: String, titleLines: List<String>, entries: List<DatesheetGridEntry>, teacherNames: Map<String, String> = emptyMap()): ExportDocument = ExportDocument(
    fileBase = fileBase,
    title = emptyList(),
    sections = listOf(ExportSection("Datesheets", emptyList(), emptyList(), grid = datesheetGridLayout(titleLines, entries, teacherNames))),
)
