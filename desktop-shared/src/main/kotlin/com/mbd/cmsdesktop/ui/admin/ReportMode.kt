package com.mbd.cmsdesktop.ui.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mbd.cmscommon.domain.model.ShiftScope
import java.time.YearMonth

/** Attendance report granularity offered on [AdminScreen.AttendanceRecords]. */
enum class ReportMode(val label: String, val short: String) {
    SEMESTER("Semester summary", "Semester"),
    MONTHLY("Monthly summary", "Monthly"),
    FULL("Attendance register", "Register"),
}

/** The attendance browser's picks, held by the nav host so they survive opening a student and coming back. */
class AttendanceRecordsSelection {
    var scope by mutableStateOf(ShiftScope.ALL)
    var semester by mutableStateOf<Int?>(null)
    var mode by mutableStateOf(ReportMode.FULL)
    var month by mutableStateOf<YearMonth?>(null)
    var course by mutableStateOf<String?>(null)
}
