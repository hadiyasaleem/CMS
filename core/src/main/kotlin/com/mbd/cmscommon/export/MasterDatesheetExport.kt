package com.mbd.cmscommon.export

import com.mbd.cmscommon.controller.MasterDatesheetGrid
import com.mbd.cmscommon.domain.model.ProgramType
import java.util.Locale

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

/** Mirrors [com.mbd.cmscommon.export.masterGridTitleLines]'s printed-timetable title block, for an
 * exam datesheet instead: "DATESHEET B.S 3rd SEMESTER MORNING (2024-2028)" plus the college name. */
fun masterDatesheetGridTitleLines(grid: MasterDatesheetGrid): List<String> {
    val session = grid.rows.firstOrNull()?.session
    val yearsLabel = session?.let { "(${it.startYear}-${it.endYear})" }.orEmpty()
    val semesterWord = if (grid.programType == ProgramType.MA_REPLACEMENT) "INTAKE SEMESTER" else "SEMESTER"
    return listOf(
        "DATESHEET B.S ${ordinalSuffix(grid.semester)} $semesterWord ${grid.shift.label.uppercase(Locale.ROOT)} $yearsLabel".trim(),
        MasterTimetableCollegeName,
    )
}
