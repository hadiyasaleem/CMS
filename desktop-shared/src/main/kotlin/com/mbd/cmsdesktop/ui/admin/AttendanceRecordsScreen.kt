package com.mbd.cmsdesktop.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.AttendanceReportKind
import com.mbd.cmscommon.domain.model.AttendanceReportSummary
import com.mbd.cmscommon.domain.model.AttendanceStatus
import com.mbd.cmscommon.domain.model.DailyAttendanceMark
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.SemesterSubject
import com.mbd.cmscommon.domain.model.SemesterTerm
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.domain.model.attendanceReportSummary
import com.mbd.cmscommon.domain.model.buildAttendanceExportPayload
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.SessionAttendanceRepository
import com.mbd.cmscommon.ui.components.AttendanceStudentReportCards
import com.mbd.cmscommon.ui.components.CmsChip
import com.mbd.cmscommon.ui.components.EmptyState
import com.mbd.cmscommon.ui.components.ErrorBanner
import com.mbd.cmscommon.ui.components.SectionHeader
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmsdesktop.platform.AwtDesktopPlatformServices
import com.mbd.cmscommon.util.userMessage
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle as JTextStyle
import java.util.Locale
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

private val ROLL_W = 74.dp
private val NAME_W = 108.dp
private val DAY_W = 30.dp
private val TOT_W = 38.dp

/**
 * Attendance records browser: pick a department/year/shift to resolve a session, then a
 * semester and (for the full monthly view) a subject + month, and render either the semester
 * summary report cards, the monthly summary cards, or a day-by-day attendance register grid
 * with per-cell detail. CSV/PDF export uses the shared [buildAttendanceExportPayload] domain
 * helper plus [RecordsExporter].
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
    var raw by remember { mutableStateOf<List<DailyAttendanceMark>>(emptyList()) }
    var roster by remember { mutableStateOf<List<SessionStudent>>(emptyList()) }
    var term by remember { mutableStateOf<SemesterTerm?>(null) }
    var subjects by remember { mutableStateOf<List<SemesterSubject>>(emptyList()) }
    var full by remember { mutableStateOf<Map<String, Map<LocalDate, DailyAttendanceMark>>>(emptyMap()) }
    var loading by remember { mutableStateOf(false) }
    var fullLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var retryVersion by remember { mutableStateOf(0) }

    var deptId by remember { mutableStateOf<String?>(null) }
    var year by remember { mutableStateOf<Int?>(null) }
    var semester by remember { mutableStateOf<Int?>(null) }
    var shift by remember { mutableStateOf<Session?>(null) }
    var mode by remember { mutableStateOf(ReportMode.SEMESTER) }
    var month by remember { mutableStateOf<YearMonth?>(null) }
    var course by remember { mutableStateOf<String?>(null) }
    var cellDetail by remember { mutableStateOf<Pair<String, DailyAttendanceMark>?>(null) }
    var actionError by remember { mutableStateOf<String?>(null) }
    var showExport by remember { mutableStateOf(false) }


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

    // Reads the cached roster, attendance, term, and curriculum whenever the selected scope changes.
    LaunchedEffect(sessionId, semester, retryVersion) {
        val sid = sessionId
        val sem = semester
        if (sid == null || sem == null) {
            raw = emptyList()
            roster = emptyList()
            term = null
            subjects = emptyList()
            full = emptyMap()
            fullLoading = false
            errorMessage = null
            return@LaunchedEffect
        }
        loading = true
        errorMessage = null
        try {
            val loadedRoster = sessionRepository.observeStudents(sid).firstOrNull().orEmpty()
            val loadedRaw = attendanceRepository.semesterMarks(sid, sem)
            val loadedTerm = curriculumRepository.getSemesterTerm(sid, sem)
            val loadedSubjects = curriculumRepository.observeSemesterSubjects(sid, sem).firstOrNull().orEmpty()
            roster = loadedRoster
            raw = loadedRaw
            term = loadedTerm
            subjects = loadedSubjects
        } catch (t: Throwable) {
            errorMessage = t.userMessage("Could not load attendance records.")
        } finally {
            loading = false
        }
    }

    // The semester summary spans one column per calendar month covered by the semester term;
    // when no term is stored yet, fall back to the distinct months actually present in the raw
    // marks (or just the current month if there is nothing at all).
    val months = remember(term, raw) { monthRange(term, raw) }

    LaunchedEffect(months) {
        if (month == null || month !in months) {
            month = months.lastOrNull()
        }
    }

    // Loads the day-by-day register for the FULL mode's selected subject + month.
    LaunchedEffect(sessionId, course, month, mode) {
        val sid = sessionId
        val code = course
        val m = month
        if (mode != ReportMode.FULL || sid == null || code == null || m == null) return@LaunchedEffect
        fullLoading = true
        errorMessage = null
        try {
            val monthMarks = attendanceRepository.marksBetween(sid, code, m.atDay(1), m.atEndOfMonth())
            full = monthMarks.groupBy { it.rollNumber }.mapValues { (_, marks) -> marks.associateBy { it.date } }
        } catch (t: Throwable) {
            errorMessage = t.userMessage("Could not load the daily attendance register.")
        } finally {
            fullLoading = false
        }
    }

    val currentDepartment = departments.firstOrNull { it.deptId == deptId }
    val deptName = currentDepartment?.name ?: deptId.orEmpty()

    val payload = remember(mode, raw, roster, months, month, course, full, deptName, year, semester, shift) {
        buildAttendanceExportPayload(
            kind = when (mode) {
                ReportMode.SEMESTER -> AttendanceReportKind.SEMESTER
                ReportMode.MONTHLY -> AttendanceReportKind.MONTHLY
                ReportMode.FULL -> AttendanceReportKind.FULL
            },
            departmentName = deptName,
            departmentId = deptId,
            year = year,
            semester = semester,
            shift = shift,
            raw = raw,
            roster = roster,
            months = months,
            month = month,
            courseCode = course,
            full = full,
        )
    }

    val reportLoading = loading || (mode == ReportMode.FULL && fullLoading)
    val ready = payload != null && payload.rows.isNotEmpty() && !reportLoading && errorMessage == null

    // The filter panel starts expanded and auto-collapses into a compact breadcrumb bar the
    // first time a report becomes ready; the user can re-expand it with the edit icon.
    var expanded by remember { mutableStateOf(true) }
    LaunchedEffect(ready) {
        if (ready) expanded = false
    }

    val crumb = listOfNotNull(
        deptName.takeIf { it.isNotBlank() },
        year?.let { "$it–${it + 4}" },
        semester?.let { "Sem $it" },
        shift?.name,
        mode.label,
        course,
    ).joinToString("  ·  ")

    fun exportPayload() = payload

    fun exportCsv() {
        val p = exportPayload() ?: return
        val target = AwtDesktopPlatformServices.chooseSaveFile(window, "Export attendance report", RecordsExporter.sanitize(p.fileBase) + ".csv") ?: return
        scope.launch {
            try {
                RecordsExporter.exportCsv(target, p.title, p.header, p.rows)
            } catch (t: Throwable) {
                actionError = t.userMessage("Could not export the attendance report.")
            }
        }
    }

    fun exportPdf() {
        val p = exportPayload() ?: return
        val target = AwtDesktopPlatformServices.chooseSaveFile(window, "Export attendance report", RecordsExporter.sanitize(p.fileBase) + ".pdf") ?: return
        scope.launch {
            try {
                RecordsExporter.exportPdf(target, p.title, p.header, p.rows)
            } catch (t: Throwable) {
                actionError = t.userMessage("Could not export the attendance report.")
            }
        }
    }

    Column(Modifier.fillMaxWidth()) {
        SectionHeader("Attendance Records", "Reporting", "Department → session → semester → shift")

        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Column(Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (!expanded && ready) crumb else "Build report",
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = if (!expanded && ready) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                    )
                    if (ready) {
                        TextButton(onClick = { showExport = true }) { Text("Export") }
                    }
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.Edit,
                            contentDescription = if (expanded) "Collapse filters" else "Edit filters",
                        )
                    }
                }

                if (expanded) {
                    PickRow("DEPARTMENT", departments.map { it.deptId to it.name }, deptId) {
                        deptId = it
                        year = null
                        semester = null
                        shift = null
                        course = null
                        full = emptyMap()
                        fullLoading = false
                    }
                    if (deptId != null) {
                        PickRow("SESSION (INTAKE)", years.map { it to "$it–${it + 4}" }, year) {
                            year = it
                            semester = null
                            shift = null
                            course = null
                            full = emptyMap()
                            fullLoading = false
                        }
                    }
                    if (year != null) {
                        PickRow("SEMESTER", (1..8).map { it to "Sem $it" }, semester) {
                            semester = it
                            shift = null
                            course = null
                            full = emptyMap()
                            fullLoading = false
                        }
                    }
                    if (semester != null) {
                        PickRow("SHIFT", shifts.map { it to it.name }, shift) {
                            shift = it
                            course = null
                            full = emptyMap()
                            fullLoading = false
                        }
                    }
                    if (sessionId != null && semester != null) {
                        Text(
                            "REPORT",
                            modifier = Modifier.padding(top = 10.dp, bottom = 6.dp),
                            color = CmsTheme.colors.muted,
                            style = CmsTextStyles.eyebrow,
                        )
                        ModeSegmented(mode) { selectedMode ->
                            full = emptyMap()
                            fullLoading = false
                            mode = selectedMode
                        }
                        if (mode == ReportMode.FULL) {
                            PickRow("SUBJECT", subjects.map { it.courseCode to it.courseCode }, course) {
                                full = emptyMap()
                                fullLoading = false
                                course = it
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        if (ready) {
            AttendanceSummaryStrip(reportMarks(mode, raw, month, full), roster)
        }

        when {
            sessionId == null || semester == null ->
                EmptyState("Pick a department, session, semester and shift.")

            reportLoading ->
                EmptyState("Loading attendance report…")

            errorMessage != null ->
                ErrorBanner(errorMessage!!, onRetry = { retryVersion++ })

            mode == ReportMode.SEMESTER ->
                if (raw.isEmpty()) {
                    EmptyState("No attendance recorded for this semester yet.")
                } else {
                    AttendanceStudentReportCards(raw, roster, months)
                }

            mode == ReportMode.MONTHLY -> {
                MonthNav(months, month) { month = it }
                val m = month
                if (m == null) {
                    EmptyState("No months in range.")
                } else {
                    AttendanceStudentReportCards(raw.filter { YearMonth.from(it.date) == m }, roster)
                }
            }

            mode == ReportMode.FULL ->
                if (subjects.isEmpty()) {
                    EmptyState("This semester has no subjects — add curriculum first.")
                } else if (course == null) {
                    EmptyState("Pick a subject to see its day-by-day register.")
                } else {
                    MonthNav(months, month) { month = it }
                    val m = month
                    if (m == null) {
                        EmptyState("No months in range.")
                    } else {
                        DayGrid(full, roster, m) { rollName, mark -> cellDetail = rollName to mark }
                    }
                }
        }
    }

    if (showExport && payload != null) {
        AlertDialog(
            onDismissRequest = { showExport = false },
            title = { Text("Export ${mode.label}", style = MaterialTheme.typography.titleLarge) },
            text = { Text("Choose a format.", style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = {
                    exportCsv()
                    showExport = false
                }) { Text("Excel (CSV)") }
            },
            dismissButton = {
                TextButton(onClick = {
                    exportPdf()
                    showExport = false
                }) { Text("PDF") }
            },
        )
    }

    cellDetail?.let { (name, mark) ->
        AlertDialog(
            onDismissRequest = { cellDetail = null },
            confirmButton = { TextButton(onClick = { cellDetail = null }) { Text("Close") } },
            title = { Text("$name · ${mark.date}") },
            text = {
                Column {
                    DetailLine("Status", mark.status.name)
                    DetailLine("Late", if (mark.isLate) "Yes" else "No")
                    DetailLine("Comment", mark.remark?.takeIf { it.isNotBlank() } ?: "—")
                    DetailLine("Taught", mark.lectureTopic?.takeIf { it.isNotBlank() } ?: "—")
                }
            },
        )
    }

    actionError?.let { message ->
        AlertDialog(
            onDismissRequest = { actionError = null },
            confirmButton = { TextButton(onClick = { actionError = null }) { Text("Close") } },
            title = { Text("Export failed") },
            text = { Text(message) },
        )
    }
}

private fun reportMarks(
    mode: ReportMode,
    raw: List<DailyAttendanceMark>,
    month: YearMonth?,
    full: Map<String, Map<LocalDate, DailyAttendanceMark>>,
): List<DailyAttendanceMark> = when (mode) {
    ReportMode.SEMESTER -> raw
    ReportMode.MONTHLY -> raw.filter { month != null && YearMonth.from(it.date) == month }
    ReportMode.FULL -> full.values.flatMap { it.values }
}

@Composable
private fun AttendanceSummaryStrip(marks: List<DailyAttendanceMark>, roster: List<SessionStudent>) {
    val summary: AttendanceReportSummary = attendanceReportSummary(marks, roster)
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AttendanceMetric("Students", summary.studentCount.toString())
        AttendanceMetric("Marked entries", summary.markedEntries.toString())
        AttendanceMetric("Attendance", summary.attendancePercentage?.let { "$it%" } ?: "--")
        AttendanceMetric("Late marks", summary.lateEntries.toString(), alert = summary.lateEntries > 0)
        AttendanceMetric("Below 75%", summary.belowTargetStudents.toString(), alert = summary.belowTargetStudents > 0)
    }
}

@Composable
private fun AttendanceMetric(label: String, value: String, alert: Boolean = false) {
    Surface(
        modifier = Modifier.width(132.dp),
        shape = RoundedCornerShape(14.dp),
        color = if (alert) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(value, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
            Text(label.uppercase(), color = MaterialTheme.colorScheme.onSurfaceVariant, style = CmsTextStyles.eyebrow)
        }
    }
}

@Composable
fun DetailLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(label, modifier = Modifier.width(88.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge)
        Text(value, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
    }
}

/** Computes the semester's month-column range from its stored term, falling back to the
 * distinct months actually present in [raw] (or the current month if there is nothing at all). */
private fun monthRange(term: SemesterTerm?, raw: List<DailyAttendanceMark>): List<YearMonth> {
    val s = term?.startDate
    val e = term?.endDate
    if (s == null || e == null || e.isBefore(s)) {
        val fromMarks = raw.map { YearMonth.from(it.date) }.distinct().sorted()
        return fromMarks.ifEmpty { listOf(YearMonth.now()) }
    }
    val out = mutableListOf<YearMonth>()
    val end = YearMonth.from(e)
    var cur = YearMonth.from(s)
    while (!cur.isAfter(end)) {
        out.add(cur)
        cur = cur.plusMonths(1)
    }
    return out
}

@Composable
fun ModeSegmented(selected: ReportMode, onSelect: (ReportMode) -> Unit) {
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        ReportMode.entries.forEachIndexed { index, m ->
            SegmentedButton(
                selected = selected == m,
                onClick = { onSelect(m) },
                shape = SegmentedButtonDefaults.itemShape(index, ReportMode.entries.size),
            ) { Text(m.short) }
        }
    }
}

@Composable
fun <T> PickRow(label: String, options: List<Pair<T, String>>, selected: T?, onPick: (T) -> Unit) {
    Text(label, modifier = Modifier.padding(top = 10.dp, bottom = 6.dp), color = CmsTheme.colors.muted, style = CmsTextStyles.eyebrow)
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        options.forEach { (value, text) ->
            CmsChip(text, selected == value, onClick = { onPick(value) })
        }
    }
}

@Composable
private fun MonthNav(months: List<YearMonth>, selected: YearMonth?, onSelect: (YearMonth) -> Unit) {
    val idx = months.indexOf(selected)
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = { if (idx > 0) onSelect(months[idx - 1]) }, enabled = idx > 0) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous month",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        val label = selected?.let { "${it.month.getDisplayName(JTextStyle.SHORT, Locale.ENGLISH)} ${it.year}" } ?: "—"
        Text(label, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium)
        val hasNext = idx in 0 until months.lastIndex
        IconButton(onClick = { if (hasNext) onSelect(months[idx + 1]) }, enabled = hasNext) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next month",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

private fun pct(present: Int, marked: Int): Int = if (marked == 0) -1 else (present * 100) / marked

private fun pctText(present: Int, marked: Int): String {
    val p = pct(present, marked)
    return if (p < 0) "–" else "$p%"
}

/** Day-by-day attendance register for the FULL report mode: one row per student, one column
 * per calendar day of [month], with an overall percentage column. Clicking a marked cell
 * invokes [onCell] to open the roll/day detail dialog. */
@Composable
private fun DayGrid(
    full: Map<String, Map<LocalDate, DailyAttendanceMark>>,
    roster: List<SessionStudent>,
    month: YearMonth,
    onCell: (String, DailyAttendanceMark) -> Unit,
) {
    val days = (1..month.lengthOfMonth()).map { month.atDay(it) }
    val names = roster.associate { it.rollNumber to it.name }
    val rolls = (roster.map { it.rollNumber } + full.keys).distinct().sorted()
    val presentC = CmsTheme.colors.success
    val absentC = MaterialTheme.colorScheme.error
    val leaveC = CmsTheme.colors.warn
    val mutedC = CmsTheme.colors.muted

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).horizontalScroll(rememberScrollState())) {
        Row(Modifier.height(56.dp).background(CmsTheme.colors.ink)) {
            Head("ROLL", ROLL_W)
            Head("NAME", NAME_W)
            days.forEach { d ->
                Box(Modifier.width(DAY_W).height(56.dp), contentAlignment = Alignment.Center) {
                    if (d.dayOfWeek == DayOfWeek.SUNDAY) {
                        Text("SUN", modifier = Modifier.rotate(-90f), color = CmsTheme.colors.accent, style = CmsTextStyles.eyebrow)
                    } else {
                        Text(d.dayOfMonth.toString(), color = CmsTheme.colors.onInk, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            Head("%", TOT_W)
        }
        rolls.forEach { roll ->
            val marksByDate = full[roll].orEmpty()
            val present = marksByDate.values.count { it.status == AttendanceStatus.PRESENT }
            val marked = marksByDate.size
            Row(Modifier.height(40.dp), verticalAlignment = Alignment.CenterVertically) {
                Cell(roll, ROLL_W, start = true, bold = true)
                Cell(names[roll] ?: roll, NAME_W, start = true)
                days.forEach { d ->
                    val mark = marksByDate[d]
                    val cellBackground = if (d.dayOfWeek == DayOfWeek.SUNDAY) CmsTheme.colors.track else Color.Transparent
                    val clickable = if (mark != null) Modifier.clickable { onCell(names[roll] ?: roll, mark) } else Modifier
                    Box(
                        modifier = Modifier.width(DAY_W).height(40.dp).background(cellBackground).then(clickable),
                        contentAlignment = Alignment.Center,
                    ) {
                        val letter = mark?.status?.let { letterOf(it) } ?: "·"
                        Text(
                            text = letter + if (mark?.isLate == true) "*" else "",
                            color = dayGridColorOf(presentC, absentC, leaveC, mutedC, mark?.status),
                            fontWeight = if (mark?.status != null) FontWeight.Bold else FontWeight.Normal,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
                Cell(pctText(present, marked), TOT_W, bold = true, color = riskColor(present, marked))
            }
            HorizontalDivider(color = CmsTheme.colors.rule.copy(alpha = 0.25f))
        }
    }
}

private fun dayGridColorOf(presentC: Color, absentC: Color, leaveC: Color, mutedC: Color, s: AttendanceStatus?): Color = when (s) {
    null -> mutedC
    AttendanceStatus.PRESENT -> presentC
    AttendanceStatus.ABSENT -> absentC
    AttendanceStatus.LEAVE -> leaveC
}

@Composable
private fun riskColor(present: Int, marked: Int): Color =
    if (marked <= 0 || pct(present, marked) >= 75) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error

private fun letterOf(s: AttendanceStatus): String = when (s) {
    AttendanceStatus.PRESENT -> "P"
    AttendanceStatus.ABSENT -> "A"
    AttendanceStatus.LEAVE -> "L"
}

@Composable
private fun Head(text: String, width: Dp) {
    Box(Modifier.width(width).fillMaxHeight(), contentAlignment = Alignment.Center) {
        Text(text, color = CmsTheme.colors.onInk, style = CmsTextStyles.eyebrow)
    }
}

@Composable
private fun Cell(text: String, width: Dp, start: Boolean = false, bold: Boolean = false, color: Color = Color.Unspecified) {
    Box(
        modifier = Modifier.width(width).height(40.dp).padding(horizontal = 6.dp),
        contentAlignment = if (start) Alignment.CenterStart else Alignment.Center,
    ) {
        Text(
            text = text,
            color = if (color == Color.Unspecified) MaterialTheme.colorScheme.onSurface else color,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
