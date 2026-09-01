package com.mbd.cmsdesktop.ui.teacher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.awt.ComposeWindow
import com.mbd.cmscommon.domain.model.AttendanceHistorySummary
import com.mbd.cmscommon.domain.model.AttendanceStatus
import com.mbd.cmscommon.domain.model.DailyAttendanceMark
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.domain.model.attendanceHistorySummary
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.SessionAttendanceRepository
import com.mbd.cmscommon.ui.components.AttendanceHistoryWorkspace
import com.mbd.cmsdesktop.platform.AwtDesktopPlatformServices
import com.mbd.cmsdesktop.ui.admin.RecordsExporter
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * Month-by-month attendance register for one session/course, reachable from
 * [MarkAttendanceScreen]'s history action. CSV/PDF export is real here (unlike the earlier
 * Android-derived stopgap): both formats are built with the shared desktop
 * [RecordsExporter] (CSV writer + Apache PDFBox table renderer, also used by the admin app),
 * saved via [AwtDesktopPlatformServices.chooseSaveFile], and opened automatically afterwards.
 */
@Composable
fun AttendanceHistoryScreen(
    sessionId: String,
    courseCode: String,
    sessionRepository: AcademicSessionRepository,
    attendanceRepository: SessionAttendanceRepository,
    window: ComposeWindow,
) {
    val roster by sessionRepository.observeStudents(sessionId).collectAsState(initial = emptyList())
    var month by remember { mutableStateOf(YearMonth.now()) }
    var marks by remember { mutableStateOf<Map<String, Map<LocalDate, DailyAttendanceMark>>>(emptyMap()) }
    var loading by remember { mutableStateOf(true) }

    val days = remember(month) { (1..month.lengthOfMonth()).map { month.atDay(it) } }
    val monthLabel = remember(month) { "${month.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)} ${month.year}" }


    LaunchedEffect(sessionId, courseCode, month) {
        loading = true
        val from = month.atDay(1)
        val to = month.atEndOfMonth()
        val dailyMarks = attendanceRepository.marksBetween(sessionId, courseCode, from, to)
        marks = dailyMarks.groupBy { it.rollNumber }.mapValues { (_, ms) -> ms.associateBy { it.date } }
        loading = false
    }

    AttendanceHistoryWorkspace(
        courseCode = courseCode,
        monthLabel = monthLabel,
        loading = loading,
        roster = roster,
        marks = marks,
        onPreviousMonth = { month = month.minusMonths(1) },
        onNextMonth = { month = month.plusMonths(1) },
        onExportCsv = { exportCsv(window, courseCode, monthLabel, days, roster, marks) },
        onExportPdf = { exportPdf(window, courseCode, monthLabel, roster, marks) },
    )
}

private fun exportCsv(
    window: ComposeWindow,
    courseCode: String,
    monthLabel: String,
    days: List<LocalDate>,
    roster: List<SessionStudent>,
    marks: Map<String, Map<LocalDate, DailyAttendanceMark>>,
) {
    val target = AwtDesktopPlatformServices.chooseSaveFile(
        window,
        "Export attendance report",
        "attendance_${courseCode}_$monthLabel.csv",
    ) ?: return

    val summary: AttendanceHistorySummary = attendanceHistorySummary(roster, marks)
    val totalsByRoll = summary.students.associateBy { it.student.rollNumber }

    val header = listOf("Roll", "Name") +
        days.map { it.dayOfMonth.toString() } +
        listOf("Present", "Absent", "Leave", "Present%", "Late")

    val rows = roster.map { student ->
        val totals = requireNotNull(totalsByRoll[student.rollNumber]) { "Required value was null." }
        val dayCells = days.map { day ->
            marks[student.rollNumber]?.get(day)?.status?.let { shortLabel(it) } ?: ""
        }
        listOf(student.rollNumber, student.name) + dayCells +
            listOf(
                totals.present.toString(),
                totals.absent.toString(),
                totals.leave.toString(),
                "${totals.percentage}%",
                totals.late.toString(),
            )
    }

    RecordsExporter.exportCsv(target, listOf("Attendance Report", "$courseCode - $monthLabel"), header, rows)
}

private fun exportPdf(
    window: ComposeWindow,
    courseCode: String,
    monthLabel: String,
    roster: List<SessionStudent>,
    marks: Map<String, Map<LocalDate, DailyAttendanceMark>>,
) {
    val target = AwtDesktopPlatformServices.chooseSaveFile(
        window,
        "Export attendance report",
        "attendance_${courseCode}_$monthLabel.pdf",
    ) ?: return

    val rows = attendanceHistorySummary(roster, marks).students.map { student ->
        listOf(
            student.student.rollNumber,
            student.student.name,
            student.present.toString(),
            student.absent.toString(),
            student.leave.toString(),
            "${student.percentage}%",
            student.late.toString(),
        )
    }

    RecordsExporter.exportPdf(
        target,
        listOf("Attendance Report", "$courseCode - $monthLabel"),
        listOf("Roll", "Name", "P", "A", "L", "%", "Late"),
        rows,
    )
}

private fun shortLabel(status: AttendanceStatus): String = when (status) {
    AttendanceStatus.PRESENT -> "P"
    AttendanceStatus.ABSENT -> "A"
    AttendanceStatus.LEAVE -> "L"
}
