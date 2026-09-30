package com.mbd.cmscommon.export

import com.mbd.cmscommon.controller.MasterGrid
import com.mbd.cmscommon.domain.model.PeriodType
import com.mbd.cmscommon.domain.model.ProgramType
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.util.clockDisplay
import com.mbd.cmscommon.util.parseClock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

const val MasterTimetableCollegeName = "GOVT GRADUATE COLLEGE M.B.DIN"

private val MasterExportWeekOrder = listOf(
    DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY,
)

/** A run of consecutive-in-the-week days sharing one row's exact period set -- same grouping the
 * on-screen grid uses, kept here rather than shared with the UI layer since `core` can't depend on it. */
private data class MasterExportDayCluster(val days: List<DayOfWeek>, val periods: List<SessionPeriod>)

private fun masterExportDayClusters(periods: List<SessionPeriod>): List<MasterExportDayCluster> {
    val byDay = periods.groupBy { it.day }
    if (byDay.isEmpty()) return emptyList()
    fun signature(dayPeriods: List<SessionPeriod>) = dayPeriods
        .sortedBy { it.startTime }
        .joinToString("|") { "${it.courseCode}@${it.startTime}-${it.endTime}@${periodLocation(it).orEmpty()}" } // a different room is a different row
    return byDay.entries
        .groupBy({ signature(it.value) }, { it.key to it.value })
        .values
        .map { entries ->
            MasterExportDayCluster(
                days = entries.map { it.first }.sortedBy { MasterExportWeekOrder.indexOf(it) },
                periods = entries.first().second,
            )
        }
        .sortedBy { MasterExportWeekOrder.indexOf(it.days.first()) }
}

private fun masterExportDayRangeLabel(days: List<DayOfWeek>): String =
    days.joinToString(" & ") { it.getDisplayName(TextStyle.SHORT, Locale.ENGLISH) }

/** "1st", "3rd", "5th", "7th", ... */
private fun ordinalSuffix(n: Int): String {
    val suffix = when {
        n % 100 in 11..13 -> "th"
        n % 10 == 1 -> "st"
        n % 10 == 2 -> "nd"
        n % 10 == 3 -> "rd"
        else -> "th"
    }
    return "$n$suffix"
}

/** Mirrors the printed timetables' own title block: "TIME TABLE B.S 1st SEMESTER EVENING (2026-2030)",
 * college name, the days this grid actually meets, and its effective-from date. The single source of
 * truth for both the on-screen title (`MasterGridTitleBlock`) and the export's title block. */
fun masterGridTitleLines(grid: MasterGrid): List<String> {
    val allPeriods = grid.rows.flatMap { it.periods }
    val session = grid.rows.firstOrNull()?.session
    val yearsLabel = session?.let { "(${it.startYear}-${it.endYear})" }.orEmpty()
    val days = allPeriods.map { it.day }.distinct().sortedBy { MasterExportWeekOrder.indexOf(it) }
    val daysLabel = when {
        days.size >= 2 -> "${days.first().getDisplayName(TextStyle.FULL, Locale.ENGLISH)} to ${days.last().getDisplayName(TextStyle.FULL, Locale.ENGLISH)}"
        days.size == 1 -> days.first().getDisplayName(TextStyle.FULL, Locale.ENGLISH)
        else -> ""
    }
    val wef = allPeriods.firstNotNullOfOrNull { it.effectiveFrom }
        ?.let { "%02d/%02d/%04d".format(it.dayOfMonth, it.monthValue, it.year) }
        .orEmpty()
    val semesterWord = if (grid.programType == ProgramType.MA_REPLACEMENT) "INTAKE SEMESTER" else "SEMESTER"
    return listOfNotNull(
        "TIME TABLE B.S ${ordinalSuffix(grid.semester)} $semesterWord ${grid.shift.label.uppercase(Locale.ROOT)} $yearsLabel".trim(),
        MasterTimetableCollegeName,
        "Classes days: $daysLabel".takeIf { daysLabel.isNotEmpty() },
        "w.e.f. $wef".takeIf { wef.isNotEmpty() },
    )
}

/** The room a department uses most, written once under its code; a period held elsewhere still prints its own room
 * on the cell. Ties go to the room used first. */
internal fun dominantLocation(periods: List<SessionPeriod>): String? =
    periods.filter { it.periodType != PeriodType.BREAK }.mapNotNull { periodLocation(it) }
        .groupingBy { it }.eachCount().let { counts ->
            val best = counts.values.maxOrNull() ?: return null
            periods.filter { it.periodType != PeriodType.BREAK }.mapNotNull { periodLocation(it) }.first { counts[it] == best }
        }

/** "R#12", "BS Block R#22", or null when neither is recorded. */
private fun periodLocation(period: SessionPeriod): String? =
    listOfNotNull(period.building?.ifBlank { null }, period.roomNo?.ifBlank { null }).joinToString(" ").ifBlank { null }

/** Same shape as the on-screen grid, ready for a merged-cell printed rendering: one column per
 * distinct time slot (plus [breakSlot] when a teacher's own filtered grid would otherwise lose it),
 * one block per department, split into sub-rows wherever its days don't share periods. */
fun masterGridLayout(grid: MasterGrid, breakSlot: Pair<String, String>? = null): TimetableGridLayout {
    val allPeriods = grid.rows.flatMap { it.periods }
    val realKeys = allPeriods.map { clockDisplay(it.startTime) to clockDisplay(it.endTime) }.distinct()
    val slotKeys = (realKeys + listOfNotNull(breakSlot)).distinct().sortedBy { parseClock(it.first) }
    val columns = slotKeys.mapIndexed { i, key -> TimetableGridColumn(i.toString(), "${key.first}-${key.second}") }

    val blocks = grid.rows.map { row ->
        val deptLabel = row.department?.code ?: row.session.deptId
        val usualRoom = dominantLocation(row.periods)
        val subRows = masterExportDayClusters(row.periods).map { cluster ->
            val byKey = cluster.periods.associateBy { clockDisplay(it.startTime) to clockDisplay(it.endTime) }
            val cells = slotKeys.withIndex().mapNotNull { (i, key) ->
                val period = byKey[key]
                when {
                    period != null -> i to TimetableGridPeriodCell(period.courseCode, period.creditHours, period.subjectName, period.teacherName, location = periodLocation(period)?.takeIf { it != usualRoom }, isBreak = period.periodType == PeriodType.BREAK)
                    key == breakSlot -> i to TimetableGridPeriodCell("", null, "", "", isBreak = true)
                    else -> null
                }
            }.toMap()
            TimetableGridSubRow(masterExportDayRangeLabel(cluster.days), cells)
        }
        TimetableGridBlock(listOfNotNull(deptLabel, usualRoom), subRows)
    }
    return TimetableGridLayout(masterGridTitleLines(grid), columns, blocks)
}

/** One grid (a single "Semester X Morning/Evening" card) as its own export, e.g. from that grid's own
 * Export button. [breakSlot] carries a teacher's own break column through, since their filtered grid
 * may otherwise have no period left in that slot. */
fun masterGridExport(grid: MasterGrid, breakSlot: Pair<String, String>? = null): ExportDocument = ExportDocument(
    fileBase = "timetable_sem${grid.semester}${if (grid.programType == ProgramType.MA_REPLACEMENT) "_intake" else ""}_${grid.shift.name.lowercase(Locale.ROOT)}",
    title = emptyList(),
    sections = listOf(ExportSection(grid.title, emptyList(), emptyList(), grid = masterGridLayout(grid, breakSlot))),
)

/** Every grid currently on screen (i.e. matching the active Department/Semester/Shift filters) in one
 * document, one section per grid, from the top-of-screen Export button. [breakSlots], keyed by grid
 * title, carries each grid's own break column through for a teacher's filtered view. */
fun masterGridsExport(grids: List<MasterGrid>, breakSlots: Map<String, Pair<String, String>?> = emptyMap()): ExportDocument = ExportDocument(
    fileBase = "master_timetable_${LocalDate.now()}",
    title = emptyList(),
    sections = grids.map { grid -> ExportSection(grid.title, emptyList(), emptyList(), grid = masterGridLayout(grid, breakSlots[grid.title])) },
)
