package com.mbd.cmsadmin.feature.records

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.AttendanceReportKind
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
import com.mbd.cmscommon.ui.components.EmptyState
import com.mbd.cmscommon.ui.components.AttendanceStudentReportCards
import com.mbd.cmscommon.ui.components.ErrorBanner
import com.mbd.cmscommon.ui.components.CmsChip
import com.mbd.cmscommon.ui.components.SectionHeader
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmscommon.util.userMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

enum class ReportMode(val label: String, val short: String) {
    SEMESTER("Semester summary", "Semester"), MONTHLY("Monthly summary", "Monthly"), FULL("Monthly full", "Full")
}

private fun ReportMode.exportKind() = when (this) {
    ReportMode.SEMESTER -> AttendanceReportKind.SEMESTER
    ReportMode.MONTHLY -> AttendanceReportKind.MONTHLY
    ReportMode.FULL -> AttendanceReportKind.FULL
}

@HiltViewModel
class AttendanceRecordsViewModel @Inject constructor(
    private val departmentRepository: DepartmentRepository,
    private val sessionRepository: AcademicSessionRepository,
    private val attendanceRepository: SessionAttendanceRepository,
    private val curriculumRepository: CurriculumRepository,
) : ViewModel() {
    private var reportLoadVersion = 0
    private var fullLoadVersion = 0

    val departments: StateFlow<List<Department>> = departmentRepository.observeActiveDepartments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val sessions: StateFlow<List<AcademicSession>> = sessionRepository.observeAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _raw = MutableStateFlow<List<DailyAttendanceMark>>(emptyList())
    val raw: StateFlow<List<DailyAttendanceMark>> = _raw.asStateFlow()
    private val _roster = MutableStateFlow<List<SessionStudent>>(emptyList())
    val roster: StateFlow<List<SessionStudent>> = _roster.asStateFlow()
    private val _term = MutableStateFlow<SemesterTerm?>(null)
    val term: StateFlow<SemesterTerm?> = _term.asStateFlow()
    private val _subjects = MutableStateFlow<List<SemesterSubject>>(emptyList())
    val subjects: StateFlow<List<SemesterSubject>> = _subjects.asStateFlow()
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /** roll → (date → mark) for the selected subject + month (monthly-full grid). */
    private val _full = MutableStateFlow<Map<String, Map<LocalDate, DailyAttendanceMark>>>(emptyMap())
    val full: StateFlow<Map<String, Map<LocalDate, DailyAttendanceMark>>> = _full.asStateFlow()
    private val _fullLoading = MutableStateFlow(false)
    val fullLoading: StateFlow<Boolean> = _fullLoading.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching { departmentRepository.sync() }
            val depts = runCatching { departmentRepository.observeActiveDepartments().first() }.getOrDefault(emptyList())
            depts.forEach { runCatching { sessionRepository.syncSessionsForDept(it.deptId) } }
        }
    }

    fun clear() {
        reportLoadVersion++
        fullLoadVersion++
        _raw.value = emptyList(); _roster.value = emptyList(); _term.value = null; _subjects.value = emptyList(); _full.value = emptyMap()
        _loading.value = false
        _fullLoading.value = false
        _error.value = null
    }

    fun clearFull() {
        fullLoadVersion++
        _full.value = emptyMap()
        _fullLoading.value = false
    }

    fun load(sessionId: String, semester: Int) {
        val version = ++reportLoadVersion
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            runCatching { sessionRepository.syncStudents(sessionId) }
            runCatching { curriculumRepository.syncSession(sessionId) }
            runCatching {
                val roster = sessionRepository.observeStudents(sessionId).first()
                val raw = attendanceRepository.semesterMarks(sessionId, semester)
                val term = curriculumRepository.getSemesterTerm(sessionId, semester)
                val subjects = curriculumRepository.observeSemesterSubjects(sessionId, semester).first()
                if (version == reportLoadVersion) {
                    _roster.value = roster
                    _raw.value = raw
                    _term.value = term
                    _subjects.value = subjects
                }
            }.onFailure { if (version == reportLoadVersion) {
                _error.value = it.userMessage("Could not load attendance records.")
            } }
            if (version == reportLoadVersion) {
                _loading.value = false
            }
        }
    }

    fun loadFull(sessionId: String, course: String, month: YearMonth) {
        val version = ++fullLoadVersion
        viewModelScope.launch {
            _fullLoading.value = true
            _error.value = null
            runCatching {
                attendanceRepository.marksBetween(sessionId, course, month.atDay(1), month.atEndOfMonth())
                    .groupBy { it.rollNumber }.mapValues { (_, l) -> l.associate { it.date to it } }
            }.onSuccess { if (version == fullLoadVersion) _full.value = it }
                .onFailure { if (version == fullLoadVersion) {
                    _error.value = it.userMessage("Could not load the daily attendance register.")
                } }
            if (version == fullLoadVersion) _fullLoading.value = false
        }
    }
}

private val ROLL_W = 74.dp
private val NAME_W = 108.dp
private val MON_W = 56.dp
private val DAY_W = 30.dp
private val TOT_W = 38.dp

@Composable
fun AttendanceRecordsScreen(viewModel: AttendanceRecordsViewModel = hiltViewModel()) {
    val departments by viewModel.departments.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val raw by viewModel.raw.collectAsState()
    val roster by viewModel.roster.collectAsState()
    val term by viewModel.term.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val full by viewModel.full.collectAsState()
    val fullLoading by viewModel.fullLoading.collectAsState()

    var deptId by remember { mutableStateOf<String?>(null) }
    var year by remember { mutableStateOf<Int?>(null) }
    var semester by remember { mutableStateOf<Int?>(null) }
    var shift by remember { mutableStateOf<Session?>(null) }
    var mode by remember { mutableStateOf(ReportMode.SEMESTER) }
    var month by remember { mutableStateOf<YearMonth?>(null) }
    var course by remember { mutableStateOf<String?>(null) }
    var showExport by remember { mutableStateOf(false) }
    var cellDetail by remember { mutableStateOf<Pair<String, DailyAttendanceMark>?>(null) }
    var actionError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val years = sessions.filter { it.deptId == deptId }.map { it.startYear }.distinct().sortedDescending()
    val shifts = sessions.filter { it.deptId == deptId && it.startYear == year }.map { it.shift }.distinct()
    val sessionId = if (deptId != null && year != null && shift != null) "${deptId}_${year}_${shift!!.name}" else null

    LaunchedEffect(sessionId, semester) {
        val sid = sessionId; val sem = semester
        if (sid != null && sem != null) viewModel.load(sid, sem) else viewModel.clear()
    }

    // Months allowed: bounded by the semester's class dates when set, else derived from the data.
    val months: List<YearMonth> = remember(term, raw) { monthRange(term, raw) }
    LaunchedEffect(months) { if (month == null || month !in months) month = months.lastOrNull() }
    // (Re)load the day grid whenever subject/month changes in FULL mode.
    LaunchedEffect(sessionId, course, month, mode) {
        val sid = sessionId; val c = course; val m = month
        if (mode == ReportMode.FULL && sid != null && c != null && m != null) viewModel.loadFull(sid, c, m)
    }

    val deptName = departments.firstOrNull { it.deptId == deptId }?.name ?: deptId.orEmpty()
    val payload = remember(mode, raw, roster, months, month, course, full, deptName, year, semester, shift) {
        buildAttendanceExportPayload(
            kind = mode.exportKind(),
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
    val ready = payload != null && payload.rows.isNotEmpty() && !reportLoading && error == null
    var expanded by remember { mutableStateOf(true) }
    // Collapse the filters to a breadcrumb once a report is ready, to give the grid room.
    LaunchedEffect(ready) { if (ready) expanded = false }

    val crumb = listOfNotNull(
        deptName.takeIf { it.isNotBlank() },
        year?.let { "$it–${it + 4}" },
        semester?.let { "Sem $it" },
        shift?.name,
        mode.label,
        course,
    ).joinToString("  ·  ")

    Column(modifier = Modifier.fillMaxSize()) {
        SectionHeader(eyebrow = "Reporting", title = "Attendance Records", subtitle = "Department → session → semester → shift")

        // ── Filter header: breadcrumb (collapsed) / Export / expand-collapse toggle ──
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Column(Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (!expanded && ready) crumb else "Build report",
                        style = if (!expanded && ready) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        modifier = Modifier.weight(1f),
                    )
                    if (ready) TextButton(onClick = { showExport = true }) { Text("Export") }
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.Edit,
                            contentDescription = if (expanded) "Collapse filters" else "Edit filters",
                        )
                    }
                }
                if (expanded) {
                    PickRow("DEPARTMENT", departments.map { it.deptId to it.name }, deptId) { deptId = it; year = null; semester = null; shift = null; course = null; viewModel.clearFull() }
                    if (deptId != null) PickRow("SESSION (INTAKE)", years.map { it to "$it–${it + 4}" }, year) { year = it; semester = null; shift = null; course = null; viewModel.clearFull() }
                    if (year != null) PickRow("SEMESTER", (1..8).map { it to "Sem $it" }, semester) { semester = it; shift = null; course = null; viewModel.clearFull() }
                    if (semester != null) PickRow("SHIFT", shifts.map { it to it.name }, shift) { shift = it; course = null; viewModel.clearFull() }
                    if (sessionId != null && semester != null) {
                        Text("REPORT", style = CmsTextStyles.eyebrow, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 10.dp, bottom = 6.dp))
                        ModeSegmented(mode) { selectedMode -> viewModel.clearFull(); mode = selectedMode }
                        if (mode == ReportMode.FULL) PickRow("SUBJECT", subjects.map { it.courseCode to it.courseCode }, course) {
                            viewModel.clearFull(); course = it
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        if (ready) AttendanceSummaryStrip(reportMarks(mode, raw, month, full), roster)

        when {
            sessionId == null || semester == null -> EmptyState("Pick a department, session, semester and shift.")
            reportLoading -> EmptyState("Loading attendance report…")
            error != null -> ErrorBanner(error!!, onRetry = {
                viewModel.load(sessionId, semester!!)
                if (mode == ReportMode.FULL && course != null && month != null) viewModel.loadFull(sessionId, course!!, month!!)
            })
            mode == ReportMode.SEMESTER -> {
                if (raw.isEmpty()) EmptyState("No attendance recorded for this semester yet.")
                else AttendanceStudentReportCards(raw, roster, months)
            }
            mode == ReportMode.MONTHLY -> {
                MonthNav(months, month) { month = it }
                val m = month
                if (m == null) EmptyState("No months in range.")
                else AttendanceStudentReportCards(raw.filter { YearMonth.from(it.date) == m }, roster)
            }
            mode == ReportMode.FULL -> {
                if (subjects.isEmpty()) EmptyState("This semester has no subjects — add curriculum first.")
                else if (course == null) EmptyState("Pick a subject to see its day-by-day register.")
                else {
                    MonthNav(months, month) { selectedMonth -> viewModel.clearFull(); month = selectedMonth }
                    val m = month
                    if (m == null) EmptyState("No months in range.") else DayGrid(full, roster, m) { n, mk -> cellDetail = n to mk }
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
                    runCatching { RecordsExporter.exportCsv(context, payload.fileBase, payload.title, payload.header, payload.rows) }
                        .onFailure { actionError = it.userMessage("Could not export the attendance report.") }
                    showExport = false
                }) { Text("Excel (CSV)") }
            },
            dismissButton = {
                TextButton(onClick = {
                    runCatching { RecordsExporter.exportPdf(context, payload.fileBase, payload.title, payload.header, payload.rows) }
                        .onFailure { actionError = it.userMessage("Could not export the attendance report.") }
                    showExport = false
                }) { Text("PDF") }
            },
        )
    }

    cellDetail?.let { (name, mark) ->
        AlertDialog(
            onDismissRequest = { cellDetail = null },
            title = { Text("$name · ${mark.date}") },
            text = {
                Column {
                    DetailLine("Status", mark.status.name)
                    DetailLine("Late", if (mark.isLate) "Yes" else "No")
                    DetailLine("Comment", mark.remark?.takeIf { it.isNotBlank() } ?: "—")
                    DetailLine("Taught", mark.lectureTopic?.takeIf { it.isNotBlank() } ?: "—")
                }
            },
            confirmButton = { TextButton(onClick = { cellDetail = null }) { Text("Close") } },
        )
    }

    actionError?.let { message ->
        AlertDialog(
            onDismissRequest = { actionError = null },
            title = { Text("Export failed") },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = { actionError = null }) { Text("Close") } },
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
    val summary = attendanceReportSummary(marks, roster)
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 6.dp),
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
        color = if (alert) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            Text(label.uppercase(), style = CmsTextStyles.eyebrow, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(88.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

/** Semester class dates bound the selectable months; fall back to months present in the data. */
private fun monthRange(term: SemesterTerm?, raw: List<DailyAttendanceMark>): List<YearMonth> {
    val s = term?.startDate; val e = term?.endDate
    if (s != null && e != null && !e.isBefore(s)) {
        val out = mutableListOf<YearMonth>()
        var cur = YearMonth.from(s); val end = YearMonth.from(e)
        while (!cur.isAfter(end)) { out.add(cur); cur = cur.plusMonths(1) }
        return out
    }
    return raw.map { YearMonth.from(it.date) }.distinct().sorted().ifEmpty { listOf(YearMonth.now()) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModeSegmented(selected: ReportMode, onSelect: (ReportMode) -> Unit) {
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        ReportMode.entries.forEachIndexed { i, m ->
            SegmentedButton(
                selected = selected == m,
                onClick = { onSelect(m) },
                shape = SegmentedButtonDefaults.itemShape(i, ReportMode.entries.size),
            ) { Text(m.short) }
        }
    }
}

@Composable
private fun <T> PickRow(label: String, options: List<Pair<T, String>>, selected: T?, onPick: (T) -> Unit) {
    Text(label, style = CmsTextStyles.eyebrow, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 10.dp, bottom = 6.dp))
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        options.forEach { (value, text) -> CmsChip(text, selected = selected == value, onClick = { onPick(value) }) }
    }
}

@Composable
private fun MonthNav(months: List<YearMonth>, selected: YearMonth?, onSelect: (YearMonth) -> Unit) {
    val idx = months.indexOf(selected)
    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { if (idx > 0) onSelect(months[idx - 1]) }, enabled = idx > 0) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous month", tint = MaterialTheme.colorScheme.onSurface)
        }
        Text(
            selected?.let { "${it.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)} ${it.year}" } ?: "—",
            style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface,
        )
        IconButton(onClick = { if (idx in 0 until months.lastIndex) onSelect(months[idx + 1]) }, enabled = idx in 0 until months.lastIndex) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next month", tint = MaterialTheme.colorScheme.onSurface)
        }
    }
}

private fun pct(present: Int, marked: Int) = if (marked == 0) -1 else present * 100 / marked
private fun pctText(present: Int, marked: Int) = pct(present, marked).let { if (it < 0) "–" else "$it%" }

// ── Semester summary: months as % columns + overall ──
@Composable
private fun SemesterGrid(raw: List<DailyAttendanceMark>, roster: List<SessionStudent>, months: List<YearMonth>) {
    val nameByRoll = roster.associate { it.rollNumber to it.name }
    val byRoll = raw.groupBy { it.rollNumber }
    val rolls = (roster.map { it.rollNumber } + byRoll.keys).distinct().sorted()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).horizontalScroll(rememberScrollState())) {
        Row(Modifier.height(40.dp).background(CmsTheme.colors.ink)) {
            Head("ROLL", ROLL_W); Head("NAME", NAME_W)
            months.forEach { Head(it.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH), MON_W) }
            Head("OVERALL", MON_W)
        }
        rolls.forEach { roll ->
            val rm = byRoll[roll].orEmpty()
            Row(Modifier.height(40.dp), verticalAlignment = Alignment.CenterVertically) {
                Cell(roll, ROLL_W, start = true, bold = true); Cell(nameByRoll[roll] ?: roll, NAME_W, start = true)
                months.forEach { m ->
                    val cm = rm.filter { YearMonth.from(it.date) == m }
                    Cell(pctText(cm.count { it.status == AttendanceStatus.PRESENT }, cm.size), MON_W)
                }
                val op = rm.count { it.status == AttendanceStatus.PRESENT }
                Cell(pctText(op, rm.size), MON_W, bold = true, color = riskColor(op, rm.size))
            }
            HorizontalDivider(color = CmsTheme.colors.rule.copy(alpha = 0.25f))
        }
    }
}

// ── Monthly summary: P/A/L + % for one month ──
@Composable
private fun MonthlySummaryGrid(monthMarks: List<DailyAttendanceMark>, roster: List<SessionStudent>) {
    val nameByRoll = roster.associate { it.rollNumber to it.name }
    val byRoll = monthMarks.groupBy { it.rollNumber }
    val rolls = (roster.map { it.rollNumber } + byRoll.keys).distinct().sorted()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).horizontalScroll(rememberScrollState())) {
        Row(Modifier.height(40.dp).background(CmsTheme.colors.ink)) {
            Head("ROLL", ROLL_W); Head("NAME", NAME_W); Head("P", TOT_W); Head("A", TOT_W); Head("L", TOT_W); Head("%", MON_W)
        }
        rolls.forEach { roll ->
            val rm = byRoll[roll].orEmpty()
            val p = rm.count { it.status == AttendanceStatus.PRESENT }
            val a = rm.count { it.status == AttendanceStatus.ABSENT }
            val l = rm.count { it.status == AttendanceStatus.LEAVE }
            Row(Modifier.height(40.dp), verticalAlignment = Alignment.CenterVertically) {
                Cell(roll, ROLL_W, start = true, bold = true); Cell(nameByRoll[roll] ?: roll, NAME_W, start = true)
                Cell("$p", TOT_W, color = CmsTheme.colors.success); Cell("$a", TOT_W, color = MaterialTheme.colorScheme.error); Cell("$l", TOT_W, color = CmsTheme.colors.warn)
                Cell(pctText(p, rm.size), MON_W, bold = true, color = riskColor(p, rm.size))
            }
            HorizontalDivider(color = CmsTheme.colors.rule.copy(alpha = 0.25f))
        }
    }
}

// ── Monthly full: day-by-day P/A/L grid for one subject ──
@Composable
private fun DayGrid(
    full: Map<String, Map<LocalDate, DailyAttendanceMark>>,
    roster: List<SessionStudent>,
    month: YearMonth,
    onCell: (String, DailyAttendanceMark) -> Unit,
) {
    val days = (1..month.lengthOfMonth()).map { month.atDay(it) }
    val nameByRoll = roster.associate { it.rollNumber to it.name }
    val rolls = (roster.map { it.rollNumber } + full.keys).distinct().sorted()
    val presentC = CmsTheme.colors.success
    val absentC = MaterialTheme.colorScheme.error
    val leaveC = CmsTheme.colors.warn
    val mutedC = CmsTheme.colors.muted
    fun colorOf(s: AttendanceStatus?) = when (s) {
        AttendanceStatus.PRESENT -> presentC
        AttendanceStatus.ABSENT -> absentC
        AttendanceStatus.LEAVE -> leaveC
        null -> mutedC
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).horizontalScroll(rememberScrollState())) {
        Row(Modifier.height(56.dp).background(CmsTheme.colors.ink)) {
            Head("ROLL", ROLL_W); Head("NAME", NAME_W)
            days.forEach { d ->
                Box(Modifier.width(DAY_W).height(56.dp), contentAlignment = Alignment.Center) {
                    if (d.dayOfWeek == DayOfWeek.SUNDAY) Text("SUN", style = CmsTextStyles.eyebrow, color = CmsTheme.colors.accent, modifier = Modifier.rotate(-90f))
                    else Text(d.dayOfMonth.toString(), style = MaterialTheme.typography.labelMedium, color = CmsTheme.colors.onInk)
                }
            }
            Head("%", TOT_W)
        }
        rolls.forEach { roll ->
            val byDate = full[roll].orEmpty()
            val p = byDate.values.count { it.status == AttendanceStatus.PRESENT }
            val marked = byDate.size
            Row(Modifier.height(40.dp), verticalAlignment = Alignment.CenterVertically) {
                val name = nameByRoll[roll] ?: roll
                Cell(roll, ROLL_W, start = true, bold = true); Cell(name, NAME_W, start = true)
                days.forEach { d ->
                    val mark = byDate[d]
                    val st = mark?.status
                    Box(
                        Modifier.width(DAY_W).height(40.dp)
                            .background(if (d.dayOfWeek == DayOfWeek.SUNDAY) CmsTheme.colors.track else Color.Transparent)
                            .then(if (mark != null) Modifier.clickable { onCell(name, mark) } else Modifier),
                        contentAlignment = Alignment.Center,
                    ) {
                        // "*" flags a late mark; tap a cell for the full detail + comment.
                        Text((st?.let(::letterOf) ?: "·") + if (mark?.isLate == true) "*" else "",
                            style = MaterialTheme.typography.labelLarge, color = colorOf(st),
                            fontWeight = if (st != null) FontWeight.Bold else FontWeight.Normal)
                    }
                }
                Cell(pctText(p, marked), TOT_W, bold = true, color = riskColor(p, marked))
            }
            HorizontalDivider(color = CmsTheme.colors.rule.copy(alpha = 0.25f))
        }
    }
}

@Composable
private fun riskColor(present: Int, marked: Int): Color =
    if (marked > 0 && pct(present, marked) < 75) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface

private fun letterOf(s: AttendanceStatus) = when (s) {
    AttendanceStatus.PRESENT -> "P"; AttendanceStatus.ABSENT -> "A"; AttendanceStatus.LEAVE -> "L"
}

@Composable
private fun Head(text: String, width: Dp) {
    Box(Modifier.width(width).fillMaxHeight(), contentAlignment = Alignment.Center) {
        Text(text, style = CmsTextStyles.eyebrow, color = CmsTheme.colors.onInk)
    }
}

@Composable
private fun Cell(text: String, width: Dp, start: Boolean = false, bold: Boolean = false, color: Color = Color.Unspecified) {
    Box(Modifier.width(width).height(40.dp).padding(horizontal = 6.dp), contentAlignment = if (start) Alignment.CenterStart else Alignment.Center) {
        Text(text, style = MaterialTheme.typography.bodySmall, maxLines = 1,
            color = if (color == Color.Unspecified) MaterialTheme.colorScheme.onSurface else color,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
    }
}

// ── Export payload building (plain strings; no Compose) ──
