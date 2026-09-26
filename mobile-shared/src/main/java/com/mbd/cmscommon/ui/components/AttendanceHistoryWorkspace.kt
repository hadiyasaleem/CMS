package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mbd.cmscommon.domain.model.AttendanceHistorySummary
import com.mbd.cmscommon.export.ExportFormat
import com.mbd.cmscommon.domain.model.AttendanceStatus
import com.mbd.cmscommon.domain.model.DailyAttendanceMark
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.domain.model.StudentAttendanceHistorySummary
import com.mbd.cmscommon.domain.model.attendanceHistorySummary
import com.mbd.cmscommon.domain.model.isRegisterHoliday
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmscommon.ui.theme.ModAccent
import com.mbd.cmscommon.ui.theme.ModGround
import com.mbd.cmscommon.ui.theme.ModInk
import com.mbd.cmscommon.ui.theme.ModMuted
import com.mbd.cmscommon.ui.theme.ModSuccess
import com.mbd.cmscommon.ui.theme.ModSurface
import com.mbd.cmscommon.ui.theme.ModTrack
import com.mbd.cmscommon.ui.theme.ModWarn
import com.mbd.cmscommon.util.Outcome
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val HistoryCanvas = ModGround
private val HistoryBorder = ModTrack
private val HistoryGreen = ModSuccess
private val HistoryGold = ModWarn
private val HistoryRed = ModAccent
private val HistoryDateFormatter = DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy", Locale.ENGLISH)
private val RegisterWeekdayFormatter = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH)

private val RollColWidth = 40.dp
private val NameColWidth = 124.dp
private val DayColWidth = 36.dp
private val PctColWidth = 50.dp
private val RegisterHeaderHeight = 48.dp
private val RegisterRowHeight = 40.dp
private val RegisterDivider = 1.dp
private const val REASON_MAX = 500

enum class AttendanceHistoryFilter(val label: String) {
    ALL("All"),
    AT_RISK("At risk"),
    LATE("Late"),
    NO_RECORD("No record"),
}

private data class RegisterCellRef(val student: SessionStudent, val date: LocalDate, val mark: DailyAttendanceMark?)

@Composable
fun AttendanceHistoryWorkspace(
    courseCode: String,
    month: YearMonth,
    monthLabel: String,
    loading: Boolean,
    roster: List<SessionStudent>,
    marks: Map<String, Map<LocalDate, DailyAttendanceMark>>,
    pendingCells: Set<Pair<String, LocalDate>>,
    requestState: Outcome<Unit>?,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onOpenStudent: (rollNumber: String) -> Unit,
    onSubmitEditRequest: (rollNumber: String, date: LocalDate, current: DailyAttendanceMark?, status: AttendanceStatus, late: Boolean, reason: String) -> Unit,
    onRequestStateConsumed: () -> Unit,
    onExport: (ExportFormat) -> Unit,
    errorMessage: String?,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
) {
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(AttendanceHistoryFilter.ALL) }
    var selectedCell by remember { mutableStateOf<RegisterCellRef?>(null) }
    var sentNotice by remember { mutableStateOf(false) }

    LaunchedEffect(requestState) {
        if (requestState is Outcome.Success) {
            selectedCell = null
            sentNotice = true
            onRequestStateConsumed()
        }
    }

    val summary = attendanceHistorySummary(roster, marks)
    val datesWithMarks = remember(marks) { marks.values.flatMap { it.keys }.toSet() }
    val visible = summary.students.filter { student ->
        val matchesQuery = query.isBlank() ||
            student.student.name.contains(query, ignoreCase = true) ||
            student.student.rollNumber.contains(query, ignoreCase = true)
        val matchesFilter = when (filter) {
            AttendanceHistoryFilter.ALL -> true
            AttendanceHistoryFilter.AT_RISK -> student.isAtRisk
            AttendanceHistoryFilter.LATE -> student.late > 0
            AttendanceHistoryFilter.NO_RECORD -> student.total == 0
        }
        matchesQuery && matchesFilter
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth().background(HistoryCanvas),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            HistoryHeader(courseCode, monthLabel, onPreviousMonth, onNextMonth, roster.isNotEmpty(), onExport)
        }
        if (sentNotice) {
            item {
                CmsNotice("Edit request sent. It will apply once an admin approves it.", tone = NoticeTone.Success, onDismiss = { sentNotice = false })
            }
        }
        if (loading) {
            items(4) { SkeletonRow() }
        } else if (roster.isEmpty()) {
            item { HistoryEmpty("No students are enrolled in this session yet.") }
        } else {
            item { HistoryMetrics(summary) }
            item { HistoryFilters(query, { query = it }, filter, { filter = it }) }
            if (visible.isEmpty()) {
                item { HistoryEmpty("No students match this search or filter.") }
            } else {
                item {
                    AttendanceRegister(
                        students = visible,
                        month = month,
                        datesWithMarks = datesWithMarks,
                        pendingCells = pendingCells,
                        today = today,
                        onOpenStudent = { onOpenStudent(it.student.rollNumber) },
                        onCell = { student, date, mark -> selectedCell = RegisterCellRef(student.student, date, mark) },
                    )
                }
                item { RegisterLegend() }
            }
        }
    }

    selectedCell?.let { cell ->
        CellDetailDialog(
            cell = cell,
            pending = (cell.student.rollNumber to cell.date) in pendingCells,
            requestState = requestState,
            onSubmit = { status, late, reason -> onSubmitEditRequest(cell.student.rollNumber, cell.date, cell.mark, status, late, reason) },
            onDismiss = {
                selectedCell = null
                if (requestState is Outcome.Error) onRequestStateConsumed()
            },
        )
    }

    if (!errorMessage.isNullOrBlank()) {
        CmsErrorDialog(message = errorMessage, onDismiss = onClearError)
    }
}

@Composable
private fun HistoryHeader(
    courseCode: String,
    month: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    canExport: Boolean,
    onExport: (ExportFormat) -> Unit,
) {
    Surface(shape = RoundedCornerShape(18.dp), color = ModInk) {
        Column(Modifier.padding(20.dp)) {
            Text("ATTENDANCE REGISTER", color = HistoryGold, style = CmsTextStyles.eyebrow)
            Spacer(Modifier.height(6.dp))
            Text(courseCode, color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onPrevious) { Text("‹ Prev", color = CmsTheme.colors.onInk) }
                Text(month, modifier = Modifier.weight(1f), color = CmsTheme.colors.onInk, style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onNext) { Text("Next ›", color = CmsTheme.colors.onInk) }
                ExportMenuButton(onExport = onExport, enabled = canExport, tint = HistoryGold)
            }
        }
    }
}

@Composable
private fun HistoryMetrics(summary: AttendanceHistorySummary) {
    val metrics = listOf(
        summary.students.size.toString() to "Students",
        "${summary.averagePercentage}%" to "Average",
        summary.atRiskStudents.toString() to "At risk",
        summary.late.toString() to "Late marks",
    )
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        metrics.forEach { (value, label) ->
            HistoryMetric(value, label, Modifier.weight(1f), alert = label == "At risk" && summary.atRiskStudents > 0)
        }
    }
}

@Composable
private fun HistoryMetric(value: String, label: String, modifier: Modifier = Modifier, alert: Boolean = false) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = ModSurface, border = BorderStroke(1.dp, HistoryBorder)) {
        Column(Modifier.padding(14.dp)) {
            Text(value, color = if (alert) HistoryRed else ModInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Text(label.uppercase(), color = ModMuted, style = CmsTextStyles.eyebrow)
        }
    }
}

@Composable
private fun HistoryFilters(
    query: String,
    onQuery: (String) -> Unit,
    filter: AttendanceHistoryFilter,
    onFilter: (AttendanceHistoryFilter) -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = query,
            onValueChange = onQuery,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search student or roll number") },
            singleLine = true,
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AttendanceHistoryFilter.entries.forEach { option ->
                CmsChip(option.label, selected = filter == option, onClick = { onFilter(option) })
            }
        }
    }
}

/** "IT-21-09" -> "09": the register only needs the serial part of the roll number. */
private fun registerRoll(rollNumber: String): String =
    rollNumber.substringAfterLast('-').filter { it.isDigit() }.ifEmpty { rollNumber }

private fun statusColor(status: AttendanceStatus): Color = when (status) {
    AttendanceStatus.PRESENT -> HistoryGreen
    AttendanceStatus.ABSENT -> HistoryRed
    AttendanceStatus.LEAVE -> HistoryGold
}

private fun statusLabel(status: AttendanceStatus): String = status.name.lowercase().replaceFirstChar { it.uppercase() }

private fun stripe(index: Int): Color = if (index % 2 == 1) HistoryCanvas else Color.Transparent

/**
 * Column-major register: roll + name are pinned on the left, and every day of [month] is a column in
 * one horizontally scrolling block, so a Sunday can be drawn as a single merged HOLIDAY cell.
 */
@Composable
private fun AttendanceRegister(
    students: List<StudentAttendanceHistorySummary>,
    month: YearMonth,
    datesWithMarks: Set<LocalDate>,
    pendingCells: Set<Pair<String, LocalDate>>,
    today: LocalDate,
    onOpenStudent: (StudentAttendanceHistorySummary) -> Unit,
    onCell: (StudentAttendanceHistorySummary, LocalDate, DailyAttendanceMark?) -> Unit,
) {
    val dates = remember(month) { (1..month.lengthOfMonth()).map(month::atDay) }
    val marksByStudent = remember(students) { students.map { s -> s.marks.associateBy { it.date } } }
    val bodyHeight = RegisterRowHeight * students.size + RegisterDivider * (students.size - 1).coerceAtLeast(0)
    val dateScroll = rememberScrollState()

    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, HistoryBorder)) {
        Column {
            Row {
                Column(Modifier.width(RollColWidth + NameColWidth)) {
                    Row(Modifier.fillMaxWidth().height(RegisterHeaderHeight).background(ModInk), verticalAlignment = Alignment.CenterVertically) {
                        RegisterHeadText("ROLL", Modifier.width(RollColWidth), TextAlign.Center)
                        RegisterHeadText("NAME", Modifier.width(NameColWidth).padding(start = 4.dp), TextAlign.Start)
                    }
                    students.forEachIndexed { index, student ->
                        Row(
                            Modifier.fillMaxWidth().height(RegisterRowHeight).background(stripe(index)).clickable { onOpenStudent(student) },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                registerRoll(student.student.rollNumber),
                                modifier = Modifier.width(RollColWidth),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Text(
                                student.student.name,
                                modifier = Modifier.width(NameColWidth).padding(start = 4.dp, end = 6.dp),
                                color = ModInk,
                                textDecoration = TextDecoration.Underline,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        if (index < students.lastIndex) HorizontalDivider(thickness = RegisterDivider, color = HistoryBorder)
                    }
                }
                Row(Modifier.weight(1f).horizontalScroll(dateScroll)) {
                    dates.forEach { date ->
                        Column(Modifier.width(DayColWidth)) {
                            DayHeader(date)
                            if (isRegisterHoliday(date, datesWithMarks)) {
                                HolidayColumn(bodyHeight)
                            } else {
                                students.forEachIndexed { index, student ->
                                    Box(Modifier.fillMaxWidth().height(RegisterRowHeight).background(stripe(index)), contentAlignment = Alignment.Center) {
                                        RegisterCell(
                                            mark = marksByStudent[index][date],
                                            pending = (student.student.rollNumber to date) in pendingCells,
                                            enabled = !date.isAfter(today),
                                            onClick = { onCell(student, date, marksByStudent[index][date]) },
                                        )
                                    }
                                    if (index < students.lastIndex) HorizontalDivider(thickness = RegisterDivider, color = HistoryBorder)
                                }
                            }
                        }
                    }
                    Column(Modifier.width(PctColWidth)) {
                        Box(Modifier.fillMaxWidth().height(RegisterHeaderHeight).background(ModInk), contentAlignment = Alignment.Center) {
                            RegisterHeadText("%", Modifier, TextAlign.Center)
                        }
                        students.forEachIndexed { index, student ->
                            Box(Modifier.fillMaxWidth().height(RegisterRowHeight).background(stripe(index)), contentAlignment = Alignment.Center) {
                                Text(
                                    if (student.total == 0) "--" else "${student.percentage}%",
                                    color = when {
                                        student.total == 0 -> ModMuted
                                        student.isAtRisk -> HistoryRed
                                        else -> HistoryGreen
                                    },
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            }
                            if (index < students.lastIndex) HorizontalDivider(thickness = RegisterDivider, color = HistoryBorder)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayHeader(date: LocalDate) {
    val sunday = date.dayOfWeek == DayOfWeek.SUNDAY
    Column(
        Modifier.fillMaxWidth().height(RegisterHeaderHeight).background(ModInk),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            date.dayOfMonth.toString().padStart(2, '0'),
            color = if (sunday) HistoryGold else CmsTheme.colors.onInk,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium,
        )
        Text(date.format(RegisterWeekdayFormatter).uppercase(), color = if (sunday) HistoryGold else CmsTheme.colors.onInkMuted, style = CmsTextStyles.eyebrow)
    }
}

@Composable
private fun HolidayColumn(height: androidx.compose.ui.unit.Dp) {
    Box(
        Modifier.fillMaxWidth().height(height).background(HistoryGold.copy(alpha = 0.10f)).clipToBounds(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            "HOLIDAY".forEach { letter ->
                Text(letter.toString(), color = HistoryGold, fontWeight = FontWeight.Bold, fontSize = 11.sp, lineHeight = 13.sp)
            }
        }
    }
}

@Composable
private fun RegisterHeadText(text: String, modifier: Modifier, align: TextAlign) {
    Text(text, modifier = modifier, color = CmsTheme.colors.onInk, textAlign = align, style = CmsTextStyles.eyebrow)
}

@Composable
private fun RegisterCell(mark: DailyAttendanceMark?, pending: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val color = mark?.let { statusColor(it.status) } ?: ModMuted
    Box(
        Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(if (mark != null) color.copy(alpha = 0.12f) else Color.Transparent)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(mark?.status?.name?.take(1) ?: "-", color = color, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelMedium)
        if (mark != null && (mark.isLate || !mark.remark.isNullOrBlank())) {
            Box(Modifier.align(Alignment.TopEnd).padding(3.dp).size(5.dp).clip(CircleShape).background(ModInk))
        }
        if (pending) {
            Box(Modifier.align(Alignment.BottomEnd).padding(2.dp).size(6.dp).clip(CircleShape).background(HistoryGold))
        }
    }
}

@Composable
private fun RegisterLegend() {
    Text(
        "P present · A absent · L leave · dark dot = late or has a remark · gold dot = edit request pending. " +
            "Select a name for the term summary, or a cell for details and edit requests.",
        color = ModMuted,
        style = MaterialTheme.typography.bodySmall,
    )
}

@Composable
private fun CellDetailDialog(
    cell: RegisterCellRef,
    pending: Boolean,
    requestState: Outcome<Unit>?,
    onSubmit: (AttendanceStatus, Boolean, String) -> Unit,
    onDismiss: () -> Unit,
) {
    val mark = cell.mark
    var editing by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf(mark?.status ?: AttendanceStatus.PRESENT) }
    var late by remember { mutableStateOf(mark?.isLate ?: false) }
    var reason by remember { mutableStateOf("") }
    val submitting = requestState is Outcome.Loading
    val changed = mark == null || status != mark.status || late != mark.isLate

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(cell.student.name, style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column {
                Text("Roll ${cell.student.rollNumber} · ${cell.date.format(HistoryDateFormatter)}", color = ModMuted, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(10.dp))
                if (mark != null) {
                    DetailRow("Status", statusLabel(mark.status))
                    DetailRow("Late", if (mark.isLate) "Yes" else "No")
                    DetailRow("Remarks", mark.remark?.takeIf { it.isNotBlank() } ?: "None")
                    DetailRow("Taught", mark.lectureTopic?.takeIf { it.isNotBlank() } ?: "Not recorded")
                } else {
                    Text("No attendance was recorded for this student on this day.", style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(12.dp))
                when {
                    pending -> CmsNotice("An edit request for this day is waiting for admin review.", tone = NoticeTone.Warning)
                    !editing -> TextButton(onClick = { editing = true }) { Text("Request edit") }
                    else -> {
                        Text("REQUEST A CHANGE", color = ModMuted, style = CmsTextStyles.eyebrow)
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AttendanceStatus.entries.forEach { option ->
                                CmsChip(statusLabel(option), selected = status == option, onClick = { status = option })
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = late, onCheckedChange = { late = it })
                            Text("Arrived late")
                        }
                        OutlinedTextField(
                            value = reason,
                            onValueChange = { if (it.length <= REASON_MAX) reason = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Reason for the change") },
                            minLines = 2,
                        )
                        if (!changed) {
                            Text("Pick a different status or late flag to request a change.", color = ModMuted, style = MaterialTheme.typography.bodySmall)
                        }
                        if (requestState is Outcome.Error) {
                            Spacer(Modifier.height(6.dp))
                            Text(requestState.message, color = HistoryRed, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (editing && !pending) {
                TextButton(onClick = { onSubmit(status, late, reason) }, enabled = changed && reason.isNotBlank() && !submitting) {
                    Text(if (submitting) "Sending..." else "Send request")
                }
            } else {
                TextButton(onClick = onDismiss) { Text("Close") }
            }
        },
        dismissButton = if (editing && !pending) {
            { TextButton(onClick = onDismiss) { Text("Cancel") } }
        } else {
            null
        },
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, modifier = Modifier.weight(1f), color = ModMuted, style = MaterialTheme.typography.bodySmall)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
    HorizontalDivider(modifier = Modifier.padding(top = 4.dp), color = HistoryBorder)
}

@Composable
private fun HistoryEmpty(message: String) {
    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, HistoryBorder)) {
        Text(message, modifier = Modifier.padding(24.dp), color = ModMuted, style = MaterialTheme.typography.bodyMedium)
    }
}
