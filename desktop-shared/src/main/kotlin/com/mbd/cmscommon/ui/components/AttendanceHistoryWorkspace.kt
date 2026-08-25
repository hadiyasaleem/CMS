package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.domain.model.AttendanceHistorySummary
import com.mbd.cmscommon.domain.model.AttendanceStatus
import com.mbd.cmscommon.domain.model.DailyAttendanceMark
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.domain.model.StudentAttendanceHistorySummary
import com.mbd.cmscommon.domain.model.attendanceHistorySummary
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val HistoryCanvas = Color(0xFFF8F6F1)
private val HistoryBorder = Color(0xFFE5E0D7)
private val HistoryGreen = Color(0xFF2F6B55)
private val HistoryBlue = Color(0xFF2F6687)
private val HistoryGold = Color(0xFF9A741F)
private val HistoryRed = Color(0xFFB43A31)
private val HistoryDateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

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
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(AttendanceHistoryFilter.ALL) }
    var selectedMark by remember { mutableStateOf<Pair<String, DailyAttendanceMark>?>(null) }

    val summary = attendanceHistorySummary(roster, marks)
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
            if (visible.isEmpty()) {
                item { HistoryEmpty("No students match this search or filter.") }
            } else {
                items(visible, key = { it.student.id }) { student ->
                    StudentHistoryCard(student, onMark = { mark -> selectedMark = student.student.name to mark })
                }
            }
        }
    }

    selectedMark?.let { (name, mark) ->
        MarkDetailDialog(name, mark, onDismiss = { selectedMark = null })
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

    Surface(shape = RoundedCornerShape(18.dp), color = Color(0xFF252321)) {
        Column(Modifier.padding(20.dp)) {
            Text("ATTENDANCE HISTORY", color = HistoryGold, style = CmsTextStyles.eyebrow)
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
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = Color.White, border = BorderStroke(1.dp, HistoryBorder)) {
        Column(Modifier.padding(14.dp)) {
            Text(value, color = if (alert) HistoryRed else Color(0xFF252321), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Text(label.uppercase(), color = Color(0xFF77716A), style = CmsTextStyles.eyebrow)
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
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AttendanceHistoryFilter.entries.forEach { option ->
                CmsChip(option.label, selected = filter == option, onClick = { onFilter(option) })
            }
        }
    }
}

@Composable
private fun StudentHistoryCard(student: StudentAttendanceHistorySummary, onMark: (DailyAttendanceMark) -> Unit) {
    val tint = if (student.isAtRisk) HistoryRed else HistoryGreen
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, HistoryBorder)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(student.student.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(student.student.rollNumber, color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    if (student.total == 0) "No record" else "${student.percentage}%",
                    color = tint,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { if (student.total == 0) 0f else student.percentage / 100f },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = tint,
                trackColor = HistoryBorder,
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                HistoryPill("P ${student.present}", HistoryGreen, Modifier.weight(1f))
                HistoryPill("A ${student.absent}", HistoryRed, Modifier.weight(1f))
                HistoryPill("L ${student.leave}", HistoryGold, Modifier.weight(1f))
                HistoryPill("Late ${student.late}", HistoryBlue, Modifier.weight(1f))
            }
            if (student.marks.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text("RECORDED DAYS", color = CmsTheme.colors.muted, style = CmsTextStyles.eyebrow)
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    items(student.marks, key = { it.date }) { mark ->
                        MarkChip(mark, onClick = { onMark(mark) })
                    }
                }
            }
            if (student.isAtRisk) {
                Spacer(Modifier.height(8.dp))
                Text("BELOW 65% ATTENDANCE", color = HistoryRed, style = CmsTextStyles.eyebrow)
            }
        }
    }
}

@Composable
private fun HistoryPill(text: String, color: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.1f), border = BorderStroke(1.dp, color.copy(alpha = 0.25f))) {
        Text(text, modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), color = color, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun MarkChip(mark: DailyAttendanceMark, onClick: () -> Unit) {
    val color = when (mark.status) {
        AttendanceStatus.PRESENT -> HistoryGreen
        AttendanceStatus.ABSENT -> HistoryRed
        AttendanceStatus.LEAVE -> HistoryGold
    }
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f)),
    ) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 7.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(mark.date.dayOfMonth.toString(), color = color, fontWeight = FontWeight.ExtraBold)
            Text(mark.status.name.take(1) + (if (mark.isLate) "*" else ""), color = color, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun MarkDetailDialog(name: String, mark: DailyAttendanceMark, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(name, style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column {
                Text(mark.date.format(HistoryDateFormatter), color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(10.dp))
                DetailRow("Status", mark.status.name.lowercase().replaceFirstChar { it.uppercase() })
                DetailRow("Late", if (mark.isLate) "Yes" else "No")
                DetailRow("Comment", mark.remark ?: "Not recorded")
                DetailRow("Taught", mark.lectureTopic ?: "Not recorded")
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, modifier = Modifier.weight(1f), color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
    HorizontalDivider(modifier = Modifier.padding(top = 4.dp), color = HistoryBorder)
}

@Composable
private fun HistoryEmpty(message: String) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, HistoryBorder)) {
        Text(message, modifier = Modifier.padding(24.dp), color = Color(0xFF77716A), style = MaterialTheme.typography.bodyMedium)
    }
}
