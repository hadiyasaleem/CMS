package com.mbd.cmscommon.export

/** One time-slot column: [index] is the printed period number ("0", "1", "2"...), [timeLabel] the
 * "start-end" range shown under it. */
data class TimetableGridColumn(val index: String, val timeLabel: String)

/** [location] (room, and building when recorded) lives on the cell, not the department block --
 * a department can meet in a different room for each period, so it can't be merged with the dept
 * column the way a single fixed room could. */
data class TimetableGridPeriodCell(
    val courseCode: String,
    val creditHours: Int?,
    val subjectName: String,
    val teacherName: String,
    val location: String? = null,
    val isBreak: Boolean = false,
)

/** One printed row for a department: the days it covers and its periods, keyed by column index. */
data class TimetableGridSubRow(val daysLabel: String, val cells: Map<Int, TimetableGridPeriodCell>)

/** One department's block: [deptLines] (just the department code) is drawn once, merged vertically
 * across all of [subRows] -- mirroring the printed timetable's merged first column. */
data class TimetableGridBlock(val deptLines: List<String>, val subRows: List<TimetableGridSubRow>)

/** A full printed-style timetable grid: centered title block, period columns, department blocks.
 * [secondColumnHeader] is the "Days" column's header text; null hides that column entirely for grids
 * where it has no meaning (e.g. a datesheet, where each column is already a single absolute date). */
data class TimetableGridLayout(
    val titleLines: List<String>,
    val columns: List<TimetableGridColumn>,
    val blocks: List<TimetableGridBlock>,
    val secondColumnHeader: String? = "Days",
)

/** The lines a period cell prints as, each paired with whether it's drawn bold: course code +
 * credit-hours and subject name bold, teacher name and room/building regular; a break cell is just
 * "BREAK". Shared by every renderer (desktop PDF, mobile PDF, Excel) so their output stays in sync. */
fun timetableGridCellLines(cell: TimetableGridPeriodCell?): List<Pair<String, Boolean>> {
    if (cell == null) return emptyList()
    if (cell.isBreak) return listOf("BREAK" to true)
    val codeLine = cell.courseCode + (cell.creditHours?.let { "($it+0)" } ?: "")
    return listOfNotNull(
        codeLine.takeIf { it.isNotBlank() }?.let { it to true },
        cell.subjectName.takeIf { it.isNotBlank() }?.let { it to true },
        cell.teacherName.takeIf { it.isNotBlank() }?.let { it to false },
        cell.location?.takeIf { it.isNotBlank() }?.let { it to false },
    )
}
