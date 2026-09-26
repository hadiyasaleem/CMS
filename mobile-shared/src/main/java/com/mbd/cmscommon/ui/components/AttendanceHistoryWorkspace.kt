package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.domain.model.AttendanceHistorySummary
import com.mbd.cmscommon.domain.model.AttendanceStatus
import com.mbd.cmscommon.domain.model.DailyAttendanceMark
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.domain.model.StudentAttendanceHistorySummary
import com.mbd.cmscommon.domain.model.attendanceHistorySummary
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
import java.time.LocalDate
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
private val RegisterRowHeight = 40.dp

enum class AttendanceHistoryFilter(val label: String) {
    ALL("All"),
    AT_RISK("At risk"),
    LATE("Late"),
    NO_RECORD("No record"),
}

@Composable
fun AttendanceHistoryWorkspace(
    courseCode: String,
    monthLabel: String,
    loading: Boolean,
    roster: List<SessionStudent>,
    marks: Map<String, Map<LocalDate, DailyAttendanceMark>>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onExportCsv: () -> Unit,
    onExportPdf: () -> Unit,
    errorMessage: String?,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(AttendanceHistoryFilter.ALL) }
    var selectedMark by remember { mutableStateOf<Pair<String, DailyAttendanceMark>?>(null) }

    val summary = attendanceHistorySummary(roster, marks)
    // Register columns are the class days of the month -- dates with at least one mark on file.
    val registerDates = remember(marks) { marks.values.flatMap { it.keys }.distinct().sorted() }
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
            HistoryHeader(courseCode, monthLabel, onPreviousMonth, onNextMonth, roster.isNotEmpty(), onExportCsv = onExportCsv, onExportPdf = onExportPdf)
        }
        if (loading) {
            items(4) { SkeletonRow() }
        } else if (roster.isEmpty()) {
            item { HistoryEmpty("No students are enrolled in this session yet.") }
        } else {
            item { HistoryMetrics(summary) }
            item { HistoryFilters(query, { query = it }, filter, { filter = it }) }
            if (registerDates.isEmpty()) {
                item { HistoryEmpty("No attendance has been recorded for this month yet.") }
            } else if (visible.isEmpty()) {
                item { HistoryEmpty("No students match this search or filter.") }
            } else {
                item {
                    AttendanceRegister(visible, registerDates, onMark = { student, mark -> selectedMark = student.student.name to mark })
                }
                item { RegisterLegend() }
            }
        }
    }

    selectedMark?.let { (name, mark) ->
        MarkDetailDialog(name, mark, onDismiss = { selectedMark = null })
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
    onExportCsv: () -> Unit,
    onExportPdf: () -> Unit,
) {
    var showExport by remember { mutableStateOf(false) }

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
                Box {
                    TextButton(onClick = { showExport = true }, enabled = canExport) { Text("Export", color = HistoryGold) }
                    DropdownMenu(expanded = showExport, onDismissRequest = { showExport = false }) {
                        DropdownMenuItem(text = { Text("Export as CSV") }, onClick = { showExport = false; onExportCsv() })
                        DropdownMenuItem(text = { Text("Export as PDF") }, onClick = { showExport = false; onExportPdf() })
                    }
                }
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

@Composable
private fun AttendanceRegister(
    students: List<StudentAttendanceHistorySummary>,
    dates: List<LocalDate>,
    onMark: (StudentAttendanceHistorySummary, DailyAttendanceMark) -> Unit,
) {
    // One scroll state shared by the header and every row keeps the date columns aligned while
    // roll and name stay pinned on the left.
    val dateScroll = rememberScrollState()
    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, HistoryBorder)) {
        Column {
            RegisterHeaderRow(dates, dateScroll)
            students.forEachIndexed { index, student ->
                RegisterStudentRow(student, dates, dateScroll, striped = index % 2 == 1, onMark = { onMark(student, it) })
                if (index < students.lastIndex) HorizontalDivider(color = HistoryBorder)
            }
        }
    }
}

@Composable
private fun RegisterHeaderRow(dates: List<LocalDate>, dateScroll: ScrollState) {
    Row(Modifier.fillMaxWidth().height(48.dp).background(ModInk), verticalAlignment = Alignment.CenterVertically) {
        RegisterHeadText("ROLL", Modifier.width(RollColWidth), TextAlign.Center)
        RegisterHeadText("NAME", Modifier.width(NameColWidth).padding(start = 4.dp), TextAlign.Start)
        Row(Modifier.weight(1f).horizontalScroll(dateScroll), verticalAlignment = Alignment.CenterVertically) {
            dates.forEach { date ->
                Column(Modifier.width(DayColWidth), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        date.dayOfMonth.toString().padStart(2, '0'),
                        color = CmsTheme.colors.onInk,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Text(date.format(RegisterWeekdayFormatter).uppercase(), color = CmsTheme.colors.onInkMuted, style = CmsTextStyles.eyebrow)
                }
            }
            RegisterHeadText("%", Modifier.width(PctColWidth), TextAlign.Center)
        }
    }
}

@Composable
private fun RegisterHeadText(text: String, modifier: Modifier, align: TextAlign) {
    Text(text, modifier = modifier, color = CmsTheme.colors.onInk, textAlign = align, style = CmsTextStyles.eyebrow)
}

@Composable
private fun RegisterStudentRow(
    student: StudentAttendanceHistorySummary,
    dates: List<LocalDate>,
    dateScroll: ScrollState,
    striped: Boolean,
    onMark: (DailyAttendanceMark) -> Unit,
) {
    val byDate = remember(student.marks) { student.marks.associateBy { it.date } }
    Row(
        Modifier.fillMaxWidth().height(RegisterRowHeight).background(if (striped) HistoryCanvas else Color.Transparent),
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
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodySmall,
        )
        Row(Modifier.weight(1f).fillMaxHeight().horizontalScroll(dateScroll), verticalAlignment = Alignment.CenterVertically) {
            dates.forEach { date -> RegisterCell(byDate[date], onMark) }
            Text(
                if (student.total == 0) "--" else "${student.percentage}%",
                modifier = Modifier.width(PctColWidth),
                textAlign = TextAlign.Center,
                color = when {
                    student.total == 0 -> ModMuted
                    student.isAtRisk -> HistoryRed
                    else -> HistoryGreen
                },
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun RegisterCell(mark: DailyAttendanceMark?, onMark: (DailyAttendanceMark) -> Unit) {
    Box(Modifier.width(DayColWidth).fillMaxHeight(), contentAlignment = Alignment.Center) {
        if (mark == null) {
            Text("-", color = ModMuted, style = MaterialTheme.typography.labelMedium)
        } else {
            val color = statusColor(mark.status)
            Box(
                Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(color.copy(alpha = 0.12f))
                    .clickable { onMark(mark) },
                contentAlignment = Alignment.Center,
            ) {
                Text(mark.status.name.take(1), color = color, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelMedium)
                if (mark.isLate || !mark.remark.isNullOrBlank()) {
                    Box(Modifier.align(Alignment.TopEnd).padding(3.dp).size(5.dp).clip(CircleShape).background(ModInk))
                }
            }
        }
    }
}

@Composable
private fun RegisterLegend() {
    Text(
        "P present · A absent · L leave · • late or has a remark. Tap a cell for details.",
        color = ModMuted,
        style = MaterialTheme.typography.bodySmall,
    )
}

@Composable
private fun MarkDetailDialog(name: String, mark: DailyAttendanceMark, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(name, style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column {
                Text("Roll ${mark.rollNumber} · ${mark.date.format(HistoryDateFormatter)}", color = ModMuted, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(10.dp))
                DetailRow("Status", mark.status.name.lowercase().replaceFirstChar { it.uppercase() })
                DetailRow("Late", if (mark.isLate) "Yes" else "No")
                DetailRow("Remarks", mark.remark?.takeIf { it.isNotBlank() } ?: "None")
                DetailRow("Taught", mark.lectureTopic?.takeIf { it.isNotBlank() } ?: "Not recorded")
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
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
