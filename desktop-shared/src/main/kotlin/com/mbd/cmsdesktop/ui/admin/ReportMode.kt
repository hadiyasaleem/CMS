package com.mbd.cmsdesktop.ui.admin

/** Attendance report granularity offered on [AdminScreen.AttendanceRecords]. */
enum class ReportMode(val label: String, val short: String) {
    SEMESTER("Semester summary", "Semester"),
    MONTHLY("Monthly summary", "Monthly"),
    FULL("Monthly full", "Full"),
}
