package com.mbd.cmsdesktop.ui.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mbd.cmscommon.domain.model.DeptSemesterScope
import java.time.YearMonth

/** Attendance report granularity offered on [AdminScreen.AttendanceRecords]. */
enum class ReportMode(val label: String, val short: String) {
    SEMESTER("Semester summary", "Semester"),
    MONTHLY("Monthly summary", "Monthly"),
    FULL("Attendance register", "Register"),
}

/** The attendance browser's picks, held by the nav host so they survive opening a student and coming back. */
class AttendanceRecordsSelection {
    /** Department/current-semester/shift(/program type) resolves the batch; [semester] below is
     * separate -- which of that batch's OWN semesters (1..8) to view. */
    var batchScope by mutableStateOf(DeptSemesterScope.ALL)
    var semester by mutableStateOf<Int?>(null)
    var mode by mutableStateOf(ReportMode.FULL)
    var month by mutableStateOf<YearMonth?>(null)
    var course by mutableStateOf<String?>(null)
}
