package com.mbd.cmsdesktop.ui.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.AttendanceReportKind
import com.mbd.cmscommon.domain.model.DailyAttendanceMark
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.domain.model.buildAttendanceExportPayload
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.SessionAttendanceRepository
import com.mbd.cmscommon.ui.components.AttendanceHistoryWorkspace
import com.mbd.cmscommon.ui.components.AttendanceStudentReportCards
import com.mbd.cmscommon.ui.components.InlineErrorCard
import com.mbd.cmscommon.ui.components.SkeletonList
import com.mbd.cmscommon.util.userMessage
import com.mbd.cmsdesktop.platform.AwtDesktopPlatformServices
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * Attendance records browser: pick a department/year/shift to resolve a session, then a
 * semester and (for monthly views) a course + month, and render either the semester summary
 * report cards or the monthly per-day history grid. CSV/PDF export uses the shared
 * [buildAttendanceExportPayload] domain helper plus [RecordsExporter].
 */
@Composable
fun AttendanceRecordsScreen(
    departmentRepository: DepartmentRepository,
    sessionRepository: AcademicSessionRepository,
    attendanceRepository: SessionAttendanceRepository,
    curriculumRepository: CurriculumRepository,
    window: ComposeWindow,
) {
    val scope = rememberCoroutineScope()

    var departments by remember { mutableStateOf<List<Department>>(emptyList()) }
    var sessions by remember { mutableStateOf<List<AcademicSession>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var actionError by remember { mutableStateOf<String?>(null) }

    var deptId by remember { mutableStateOf<String?>(null) }
    var year by remember { mutableStateOf<Int?>(null) }
    var shift by remember { mutableStateOf<Session?>(null) }
    var semester by remember { mutableStateOf<Int?>(null) }
    var mode by remember { mutableStateOf(ReportMode.SEMESTER) }
    var month by remember { mutableStateOf<YearMonth?>(null) }
    var courseCode by remember { mutableStateOf<String?>(null) }

    var raw by remember { mutableStateOf<List<DailyAttendanceMark>>(emptyList()) }
    var roster by remember { mutableStateOf<List<SessionStudent>>(emptyList()) }
    var full by remember { mutableStateOf<Map<String, Map<LocalDate, DailyAttendanceMark>>>(emptyMap()) }

    LaunchedEffect(departmentRepository, sessionRepository) {
        try {
            departmentRepository.sync()
        } catch (t: Throwable) {
            errorMessage = t.userMessage("Could not load departments.")
        }
    }
    LaunchedEffect(departmentRepository) {
        departmentRepository.observeActiveDepartments().collect { departments = it }
    }
    LaunchedEffect(sessionRepository) {
        sessionRepository.observeAllSessions().collect { sessions = it }
    }

    val years = sessions.filter { it.deptId == deptId }.map { it.startYear }.distinct().sortedDescending()
    val shifts = sessions.filter { it.deptId == deptId && it.startYear == year }.map { it.shift }.distinct()
    val sessionId = if (deptId != null && year != null && shift != null) {
        AcademicSession.buildId(deptId!!, year!!, shift!!)
    } else {
        null
    }
    val currentSession = sessions.firstOrNull { it.sessionId == sessionId }
    val currentDepartment = departments.firstOrNull { it.deptId == deptId }

    var subjects by remember { mutableStateOf<List<com.mbd.cmscommon.domain.model.SemesterSubject>>(emptyList()) }
    LaunchedEffect(sessionId, semester) {
        val sid = sessionId
        val sem = semester
        subjects = if (sid != null && sem != null) {
            curriculumRepository.observeSemesterSubjects(sid, sem).firstOrNull().orEmpty()
        } else {
            emptyList()
        }
    }

    // The semester summary report shows one column per calendar month covered so far;
    // without a stored semester term on hand we fall back to an empty list, which
    // buildAttendanceExportPayload treats as "no month breakdown columns".
    val months = remember(raw) { raw.map { YearMonth.from(it.date) }.distinct().sorted() }

    LaunchedEffect(sessionId, semester) {
        val sid = sessionId
        val sem = semester
        if (sid == null || sem == null) return@LaunchedEffect
        loading = true
        errorMessage = null
        try {
            attendanceRepository.syncSession(sid)
            roster = sessionRepository.observeStudents(sid).firstOrNull().orEmpty()
            raw = attendanceRepository.semesterMarks(sid, sem)
        } catch (t: Throwable) {
            errorMessage = t.userMessage("Could not load attendance records.")
        } finally {
            loading = false
        }
    }

    LaunchedEffect(sessionId, courseCode, month, mode) {
        val sid = sessionId
        val code = courseCode
        if (sid == null || code == null || mode != ReportMode.FULL) return@LaunchedEffect
        try {
            val m = month ?: YearMonth.now()
            val monthMarks = attendanceRepository.marksBetween(sid, code, m.atDay(1), m.atEndOfMonth())
            full = monthMarks.groupBy { it.rollNumber }.mapValues { (_, marks) -> marks.associateBy { it.date } }
        } catch (t: Throwable) {
            errorMessage = t.userMessage("Could not load the daily attendance grid.")
        }
    }

    fun exportPayload() = buildAttendanceExportPayload(
        kind = when (mode) {
            ReportMode.SEMESTER -> AttendanceReportKind.SEMESTER
            ReportMode.MONTHLY -> AttendanceReportKind.MONTHLY
            ReportMode.FULL -> AttendanceReportKind.FULL
        },
        departmentName = currentDepartment?.name ?: deptId.orEmpty(),
        departmentId = deptId,
        year = year,
        semester = semester,
        shift = shift,
        raw = raw,
        roster = roster,
        months = months,
        month = month,
        courseCode = courseCode,
        full = full,
    )

    fun exportCsv() {
        val payload = exportPayload() ?: return
        val target = AwtDesktopPlatformServices.chooseSaveFile(window, "Export attendance report", RecordsExporter.sanitize(payload.fileBase) + ".csv") ?: return
        scope.launch {
            try {
                RecordsExporter.exportCsv(target, payload.title, payload.header, payload.rows)
            } catch (t: Throwable) {
                actionError = t.userMessage("Could not export the attendance report.")
            }
        }
    }

    fun exportPdf() {
        val payload = exportPayload() ?: return
        val target = AwtDesktopPlatformServices.chooseSaveFile(window, "Export attendance report", RecordsExporter.sanitize(payload.fileBase) + ".pdf") ?: return
        scope.launch {
            try {
                RecordsExporter.exportPdf(target, payload.title, payload.header, payload.rows)
            } catch (t: Throwable) {
                actionError = t.userMessage("Could not export the attendance report.")
            }
        }
    }

    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        Text("Attendance records", style = MaterialTheme.typography.titleLarge)
        Row(Modifier.padding(vertical = 8.dp)) {
            DropdownPicker(
                label = currentDepartment?.name ?: "Department",
                options = departments.map { it.deptId to it.name },
                onSelect = { deptId = it; year = null; shift = null },
            )
            Spacer(Modifier.width(8.dp))
            DropdownPicker(
                label = year?.toString() ?: "Year",
                options = years.map { it to it.toString() },
                onSelect = { year = it; shift = null },
            )
            Spacer(Modifier.width(8.dp))
            DropdownPicker(
                label = shift?.name ?: "Shift",
                options = shifts.map { it to it.name },
                onSelect = { shift = it },
            )
            Spacer(Modifier.width(8.dp))
            DropdownPicker(
                label = semester?.let { "Sem $it" } ?: "Semester",
                options = (1..8).map { it to "Sem $it" },
                onSelect = { semester = it },
            )
        }

        SingleChoiceSegmentedButtonRow {
            ReportMode.entries.forEachIndexed { index, m ->
                SegmentedButton(
                    selected = mode == m,
                    onClick = { mode = m },
                    shape = SegmentedButtonDefaults.itemShape(index, ReportMode.entries.size),
                ) { Text(m.short) }
            }
        }

        if (mode == ReportMode.FULL) {
            Row(Modifier.padding(vertical = 8.dp)) {
                DropdownPicker(
                    label = courseCode ?: "Course",
                    options = subjects.map { it.courseCode to "${it.courseCode} · ${it.name}" },
                    onSelect = { courseCode = it },
                )
            }
        }

        errorMessage?.let { InlineErrorCard(it, "Dismiss", { errorMessage = null }, Modifier.fillMaxWidth()) }
        actionError?.let { InlineErrorCard(it, "Dismiss", { actionError = null }, Modifier.fillMaxWidth()) }

        Row(Modifier.padding(vertical = 8.dp)) {
            TextButton(onClick = { month = (month ?: YearMonth.now()).minusMonths(1) }) { Text("Previous month") }
            TextButton(onClick = { month = (month ?: YearMonth.now()).plusMonths(1) }) { Text("Next month") }
            OutlinedButton(onClick = ::exportCsv) { Text("Export CSV") }
            Spacer(Modifier.width(8.dp))
            OutlinedButton(onClick = ::exportPdf) { Text("Export PDF") }
        }

        if (loading) {
            SkeletonList(3, Modifier.fillMaxWidth())
        } else if (mode == ReportMode.SEMESTER) {
            AttendanceStudentReportCards(marks = raw, roster = roster, months = months)
        } else {
            AttendanceHistoryWorkspace(
                courseCode = courseCode.orEmpty(),
                monthLabel = (month ?: YearMonth.now()).toString(),
                loading = loading,
                roster = roster,
                marks = if (mode == ReportMode.FULL) {
                    full
                } else {
                    raw.groupBy { it.rollNumber }.mapValues { (_, marks) -> marks.associateBy { it.date } }
                },
                onPreviousMonth = { month = (month ?: YearMonth.now()).minusMonths(1) },
                onNextMonth = { month = (month ?: YearMonth.now()).plusMonths(1) },
                onExportCsv = ::exportCsv,
                onExportPdf = ::exportPdf,
            )
        }
    }
}

@Composable
private fun <T> DropdownPicker(label: String, options: List<Pair<T, String>>, onSelect: (T) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    OutlinedButton(onClick = { expanded = true }) { Text(label) }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        options.forEach { (value, text) ->
            DropdownMenuItem(text = { Text(text) }, onClick = { onSelect(value); expanded = false })
        }
    }
}
