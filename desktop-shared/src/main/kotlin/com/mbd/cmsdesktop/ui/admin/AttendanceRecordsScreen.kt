package com.mbd.cmsdesktop.ui.admin

import compose.icons.TablerIcons
import compose.icons.tablericons.ChevronLeft
import compose.icons.tablericons.ChevronRight
import compose.icons.tablericons.ChevronUp
import compose.icons.tablericons.Edit
import com.mbd.cmscommon.util.userMessageLogged
import com.mbd.cmscommon.util.FileReadErrors
import com.mbd.cmscommon.ui.components.DialogScrollBody
import com.mbd.cmscommon.controller.availableSemesters
import com.mbd.cmscommon.controller.departmentScopeOptions
import com.mbd.cmscommon.controller.resolveSession
import com.mbd.cmscommon.controller.studentsForTab
import com.mbd.cmscommon.domain.model.DeptSemesterScope
import com.mbd.cmscommon.ui.components.DeptSemesterScopeSelector
import com.mbd.cmscommon.export.toExportDocument
import com.mbd.cmscommon.ui.components.ExportMenuButton
import com.mbd.cmscommon.ui.components.TopBarActions
import com.mbd.cmsdesktop.platform.DocumentExporter
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.VerticalScrollbar
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
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.mbd.cmscommon.ui.components.AttendanceRegisterBrowser
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
 * helper plus [DocumentExporter].
 */
@Composable
fun AttendanceRecordsScreen(
    departmentRepository: DepartmentRepository,
    sessionRepository: AcademicSessionRepository,
    attendanceRepository: SessionAttendanceRepository,
    curriculumRepository: CurriculumRepository,
    window: ComposeWindow,
    onOpenStudent: (sessionId: String, rollNumber: String) -> Unit,
    selection: AttendanceRecordsSelection = remember { AttendanceRecordsSelection() },
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

    var semester by selection::semester
    var mode by selection::mode
    var month by selection::month
    var course by selection::course
    var cellDetail by remember { mutableStateOf<Pair<String, DailyAttendanceMark>?>(null) }
    var actionError by remember { mutableStateOf<String?>(null) }


    LaunchedEffect(departmentRepository) {
        departmentRepository.observeActiveDepartments().collect { departments = it }
    }
    LaunchedEffect(sessionRepository) {
        sessionRepository.observeAllSessions().collect { sessions = it }
    }

    // Department/current-semester/shift(/program type, if two share a dept+semester) resolves the
    // batch; no shift means both shifts of it. "semester" below is separate -- which of that batch's
    // OWN semesters (1..8) to view, since a batch now in semester 5 can still show semester 3 records.
    var batchScope by selection::batchScope
    val selectedSession = batchScope.resolveSession(sessions)
    val deptId = batchScope.deptId
    val year = selectedSession?.startYear
    val shift = batchScope.shift
    val sessionId = selectedSession?.sessionId

    // Reads the cached roster, attendance, term, and curriculum whenever the selected scope changes.
    LaunchedEffect(sessionId, semester, shift, retryVersion) {
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
            val shiftRoster = studentsForTab(loadedRoster, shift)
            val shiftRolls = shiftRoster.map { it.rollNumber }.toSet()
            roster = shiftRoster
            raw = loadedRaw.filter { it.rollNumber in shiftRolls }
            term = loadedTerm
            subjects = loadedSubjects
        } catch (t: Throwable) {
            errorMessage = t.userMessageLogged("AttendanceRecords.loadReport", "Couldn't load the Semester $sem attendance records.")
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
            errorMessage = t.userMessageLogged("AttendanceRecords.loadFull", "Couldn't load the daily $code attendance register for ${m.month.name.lowercase().replaceFirstChar { c -> c.uppercase() }} ${m.year}.")
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
        if (sessionId != null) shift?.label ?: "Both shifts" else null,
        mode.label,
        course,
    ).joinToString("  ·  ")

    if (ready && payload != null) {
        TopBarActions {
            ExportMenuButton(onExport = { format ->
                runCatching { DocumentExporter.export(window, payload.toExportDocument(), format) }
                    .onFailure { actionError = FileReadErrors.describeWrite(it, format.label) }
            }, tint = CmsTheme.colors.onInk)
        }
    }

    Column(Modifier.fillMaxWidth()) {
        SectionHeader("Attendance Records", "Reporting", "Find a batch by department/semester/shift, then pick which semester to view")

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
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = if (expanded) TablerIcons.ChevronUp else TablerIcons.Edit,
                            contentDescription = if (expanded) "Collapse filters" else "Edit filters",
                        )
                    }
                }

                if (expanded) {
                    DeptSemesterScopeSelector(
                        scope = batchScope,
                        departments = departmentScopeOptions(departments),
                        availableSemesters = sessions.availableSemesters(),
                        onScopeChange = { picked ->
                            // Only reset the "which semester to view" pick when it actually resolves
                            // to a different batch -- not just because a filter level changed.
                            if (picked.resolveSession(sessions)?.sessionId != selectedSession?.sessionId) semester = null
                            batchScope = picked
                            course = null
                            full = emptyMap()
                            fullLoading = false
                        },
                        modifier = Modifier.padding(top = 6.dp),
                    )
                    if (sessionId != null) {
                        PickRow("SEMESTER", (selectedSession?.semesterRange ?: 1..8).map { it to "Sem $it" }, semester) {
                            semester = it
                            course = null
                            full = emptyMap()
                            fullLoading = false
                        }
                    }
                    if (sessionId != null && semester != null) {
                        Text(
                            "VIEW",
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

        when {
            sessionId == null || semester == null ->
                EmptyState("Pick a department and current semester to find the batch (add program type if two share it), then choose which semester's records to view.")

            reportLoading ->
                EmptyState("Loading attendance report…")

            errorMessage != null ->
                ErrorBanner(errorMessage!!, onRetry = { retryVersion++ })

            mode == ReportMode.SEMESTER ->
                if (raw.isEmpty()) {
                    EmptyState("No attendance recorded for this semester yet.")
                } else {
                    AttendanceStudentReportCards(raw, roster, months, onOpenStudent = { onOpenStudent(it.sessionId, it.rollNumber) })
                }

            mode == ReportMode.MONTHLY -> {
                MonthNav(months, month) { month = it }
                val m = month
                if (m == null) {
                    EmptyState("No months in range.")
                } else {
                    AttendanceStudentReportCards(raw.filter { YearMonth.from(it.date) == m }, roster, onOpenStudent = { onOpenStudent(it.sessionId, it.rollNumber) })
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
                        AttendanceRegisterBrowser(
                            roster = roster,
                            marks = full,
                            month = m,
                            onOpenStudent = { onOpenStudent(it.sessionId, it.rollNumber) },
                            onCell = { student, _, mark -> if (mark != null) cellDetail = student.name to mark },
                        )
                    }
                }
        }
    }

    cellDetail?.let { (name, mark) ->
        AlertDialog(
            onDismissRequest = { cellDetail = null },
            confirmButton = { TextButton(onClick = { cellDetail = null }) { Text("Close") } },
            title = { Text("$name · ${mark.date}") },
            text = { DialogScrollBody {
                Column {
                    DetailLine("Status", mark.status.name)
                    DetailLine("Late", if (mark.isLate) "Yes" else "No")
                    DetailLine("Comment", mark.remark?.takeIf { it.isNotBlank() } ?: "—")
                    DetailLine("Taught", mark.lectureTopic?.takeIf { it.isNotBlank() } ?: "—")
                }
            }},
        )
    }

    actionError?.let { message ->
        AlertDialog(
            onDismissRequest = { actionError = null },
            confirmButton = { TextButton(onClick = { actionError = null }) { Text("Close") } },
            title = { Text("Export failed") },
            text = { DialogScrollBody { Text(message) }},
        )
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
private fun SimpleSegmented(options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, text ->
            SegmentedButton(
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size),
            ) { Text(text) }
        }
    }
}

/** Register (the teacher-style month grid) or Summary; a summary is either per month or the full semester. */
@Composable
fun ModeSegmented(selected: ReportMode, onSelect: (ReportMode) -> Unit) {
    var lastSummary by remember { mutableStateOf(ReportMode.MONTHLY) }
    val summaryMode = if (selected == ReportMode.FULL) lastSummary else selected
    SimpleSegmented(listOf("Register", "Summary"), if (selected == ReportMode.FULL) 0 else 1) { onSelect(if (it == 0) ReportMode.FULL else summaryMode) }
    if (selected != ReportMode.FULL) {
        Spacer(Modifier.height(6.dp))
        SimpleSegmented(listOf("Per month", "Full semester"), if (selected == ReportMode.MONTHLY) 0 else 1) {
            lastSummary = if (it == 0) ReportMode.MONTHLY else ReportMode.SEMESTER
            onSelect(lastSummary)
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
                TablerIcons.ChevronLeft,
                contentDescription = "Previous month",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        val label = selected?.let { "${it.month.getDisplayName(JTextStyle.SHORT, Locale.ENGLISH)} ${it.year}" } ?: "—"
        Text(label, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium)
        val hasNext = idx in 0 until months.lastIndex
        IconButton(onClick = { if (hasNext) onSelect(months[idx + 1]) }, enabled = hasNext) {
            Icon(
                TablerIcons.ChevronRight,
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

@Composable
private fun riskColor(present: Int, marked: Int): Color =
    if (marked <= 0 || pct(present, marked) >= 75) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error

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
