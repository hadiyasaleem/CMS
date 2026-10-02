package com.mbd.cmscommon.ui.components

import com.mbd.cmscommon.controller.TeacherGrid
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.PeriodType
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.export.ExportDocument
import com.mbd.cmscommon.export.ExportFormat
import com.mbd.cmscommon.export.masterGridExport
import com.mbd.cmscommon.export.masterGridsExport
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmscommon.ui.theme.ModGround
import com.mbd.cmscommon.ui.theme.ModInk
import com.mbd.cmscommon.ui.theme.ModMuted
import com.mbd.cmscommon.ui.theme.ModSuccess
import com.mbd.cmscommon.ui.theme.ModSurface
import com.mbd.cmscommon.ui.theme.ModTrack
import com.mbd.cmscommon.ui.theme.ModWarn
import com.mbd.cmscommon.util.Outcome
import com.mbd.cmscommon.util.parseClock
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

private val ScheduleCanvas = ModGround
private val ScheduleBorder = ModTrack
private val ScheduleGold = ModWarn
private val ScheduleDays = listOf(
    DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY,
)

@Composable
fun TeacherScheduleWorkspace(
    heroPainter: Painter,
    periods: List<SessionPeriod>,
    sessions: List<AcademicSession>,
    grids: List<TeacherGrid>,
    outcome: Outcome<Unit>?,
    onRefresh: () -> Unit,
    onClearError: () -> Unit,
    onExport: (ExportDocument, ExportFormat) -> Unit,
    modifier: Modifier = Modifier,
) {
    val teachingPeriods = periods.filter { it.periodType != PeriodType.BREAK && it.courseCode.isNotBlank() }
    val classDays = teachingPeriods.map { it.day }.distinct().size
    val totalMinutes = teachingPeriods.sumOf { period ->
        val start = parseClock(period.startTime)
        val end = parseClock(period.endTime)
        if (start != null && end != null && end.isAfter(start)) java.time.Duration.between(start, end).toMinutes().toInt() else 0
    }
    val rooms = teachingPeriods.mapNotNull { it.roomNo?.takeIf { r -> r.isNotBlank() } }.distinct().size
    val busiest = ScheduleDays.maxByOrNull { day -> teachingPeriods.count { it.day == day } }

    var detailPeriod by remember { mutableStateOf<SessionPeriod?>(null) }

    val listState = rememberLazyListState()
    WithVerticalScrollbar(listState) {
    LazyColumn( state = listState,
        modifier = modifier.fillMaxWidth().background(ScheduleCanvas),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ScheduleHeader(
                heroPainter,
                teachingPeriods.size,
                onExport = onExport,
                build = { masterGridsExport(grids.map { it.grid }, grids.associate { it.grid.title to it.breakSlot }) },
                exportEnabled = grids.isNotEmpty(),
            )
        }
        item { ScheduleMetrics(teachingPeriods.size, classDays, totalMinutes, rooms, busiest) }

        if (grids.isEmpty()) {
            item {
                Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ScheduleBorder)) {
                    Text(
                        "No periods are assigned to you yet.",
                        modifier = Modifier.padding(24.dp),
                        color = ModMuted,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        } else {
            grids.forEach { teacherGrid ->
                item {
                    TeacherGridSection(teacherGrid, onCellClick = { period -> detailPeriod = period }, onExport = onExport)
                }
            }
        }

        item { Spacer(Modifier.height(72.dp)) }
    }
    }

    detailPeriod?.let { period ->
        TeacherPeriodDetailDialog(period, sessions.firstOrNull { it.sessionId == period.sessionId }, onDismiss = { detailPeriod = null })
    }

    if (outcome is Outcome.Error) {
        CmsErrorDialog(message = outcome.message, onDismiss = onClearError, title = "Couldn't load schedule", onRetry = onRefresh)
    }
}

@Composable
private fun ScheduleHeader(
    heroPainter: Painter,
    total: Int,
    onExport: (ExportDocument, ExportFormat) -> Unit,
    build: () -> ExportDocument,
    exportEnabled: Boolean,
) {
    Surface(modifier = Modifier.fillMaxWidth().height(140.dp), shape = RoundedCornerShape(18.dp), color = ModInk) {
        Box(Modifier.fillMaxSize()) {
            Image(
                painter = heroPainter,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                alignment = Alignment.CenterEnd,
                contentScale = ContentScale.Crop,
                alpha = 0.35f,
            )
            Column(Modifier.align(Alignment.CenterStart).padding(20.dp)) {
                Text("FACULTY WORKSPACE", color = ScheduleGold, style = CmsTextStyles.eyebrow)
                Spacer(Modifier.height(6.dp))
                Text("My schedule", color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(4.dp))
                Text("$total period(s) across your assigned sessions", color = CmsTheme.colors.onInkMuted, style = MaterialTheme.typography.bodySmall)
            }
            ExportMenuButton(
                onExport = { format -> onExport(build(), format) },
                enabled = exportEnabled,
                tint = CmsTheme.colors.onInk,
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
            )
        }
    }
}

@Composable
private fun ScheduleMetrics(total: Int, days: Int, minutes: Int, rooms: Int, busiest: DayOfWeek?) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ScheduleMetric(total.toString(), "Periods", Modifier.weight(1f))
        ScheduleMetric(days.toString(), "Teaching days", Modifier.weight(1f))
        ScheduleMetric(formatMinutes(minutes), "Weekly time", Modifier.weight(1f))
        ScheduleMetric(busiest?.getDisplayName(TextStyle.SHORT, Locale.ENGLISH) ?: "None", "Busiest day", Modifier.weight(1f))
    }
}

fun formatMinutes(minutes: Int): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return when {
        hours == 0 -> "${mins}m"
        mins == 0 -> "${hours}h"
        else -> "${hours}h ${mins}m"
    }
}

@Composable
private fun ScheduleMetric(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = ModSurface, border = BorderStroke(1.dp, ScheduleBorder)) {
        Column(Modifier.padding(14.dp)) {
            Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(label.uppercase(), color = ModMuted, style = CmsTextStyles.eyebrow)
        }
    }
}

/** One visual row within a teacher's own semester+shift grid — same department/day-cluster shape as
 * the master timetable's grid, just narrowed to the teacher's own periods. */
private data class TeacherRow(
    val rowKey: String,
    val deptLabel: String,
    val daysLabel: String,
    val cells: Map<String, GridCell?>,
)

@Composable
fun TeacherGridSection(teacherGrid: TeacherGrid, onCellClick: (SessionPeriod) -> Unit, onExport: (ExportDocument, ExportFormat) -> Unit) {
    val grid = teacherGrid.grid
    val clustersByRow = grid.rows.map { row -> row to dayClustersFor(row.periods) }
    val periodsByRowKey = clustersByRow
        .flatMap { (row, clusters) -> clusters.mapIndexed { i, c -> "${row.session.sessionId}_$i" to c.periods.associateBy { it.timeRange } } }
        .toMap()

    // Only columns where this teacher actually has something scheduled, plus the grid's break gap
    // (if any) so the day still reads sensibly even though nothing of the teacher's own falls there.
    val breakLabel = teacherGrid.breakSlot?.let { "${it.first}–${it.second}" }
    val realSlots = grid.rows.flatMap { it.periods.map { p -> p.timeRange } }.distinct()
    val timeSlots = (realSlots + listOfNotNull(breakLabel)).distinct().sortedBy { it.substringBefore('–') }

    val rows = clustersByRow.flatMap { (row, clusters) ->
        clusters.mapIndexed { index, cluster ->
            val byRange = periodsByRowKey["${row.session.sessionId}_$index"].orEmpty()
            TeacherRow(
                rowKey = "${row.session.sessionId}_$index",
                deptLabel = if (index == 0) (row.department?.code ?: row.session.deptId) else "",
                daysLabel = dayRangeLabel(cluster.days),
                cells = timeSlots.associateWith { slot ->
                    if (slot == breakLabel) {
                        GridCell(title = "BREAK", subtitle = "", meta = "", isBreak = true)
                    } else {
                        byRange[slot]?.let { period ->
                            val codeLine = period.courseCode + (period.creditHours?.let { "($it+0)" } ?: "")
                            val location = listOfNotNull(period.building?.ifBlank { null }, period.roomNo?.ifBlank { null }).joinToString(" ").ifBlank { "No room" }
                            GridCell(
                                title = period.subjectName,
                                subtitle = codeLine,
                                meta = location,
                            )
                        }
                    }
                },
            )
        }
    }

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        MasterGridTitleBlock(grid)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            ExportMenuButton(onExport = { format -> onExport(masterGridExport(grid, teacherGrid.breakSlot), format) })
        }
        TeacherTimetableGrid(
            timeSlots = timeSlots,
            rows = rows,
            onCellClick = { rowKey, slot -> periodsByRowKey[rowKey].orEmpty()[slot]?.let(onCellClick) },
        )
    }
}

private val TeacherDeptW = 90.dp
private val TeacherDaysW = 100.dp
private val TeacherSlotW = 120.dp

/** Same "nothing stays fixed" scrolling as the master timetable's grid, but read-only — no header
 * click to edit, since a teacher can only view their own schedule. */
@Composable
private fun TeacherTimetableGrid(
    timeSlots: List<String>,
    rows: List<TeacherRow>,
    onCellClick: (String, String) -> Unit,
) {
    val hScroll = rememberScrollState()

    CmsCard(Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().background(CmsTheme.colors.ink).horizontalScroll(hScroll).padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TeacherHeaderCell("Departments", TeacherDeptW)
                TeacherHeaderCell("DAYS", TeacherDaysW)
                timeSlots.forEach { slot -> TeacherHeaderCell(slot, TeacherSlotW) }
            }
            HorizontalDivider(thickness = 2.dp, color = CmsTheme.colors.rule)
            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(hScroll),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.width(TeacherDeptW).padding(horizontal = 6.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
                        Text(
                            row.deptLabel,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleSmall,
                            textAlign = TextAlign.Center,
                        )
                    }
                    Box(Modifier.width(TeacherDaysW).padding(horizontal = 6.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
                        Text(
                            row.daysLabel,
                            color = ModMuted,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                        )
                    }
                    timeSlots.forEach { slot ->
                        GridCellBox(
                            cell = row.cells[slot],
                            width = TeacherSlotW,
                            onClick = { onCellClick(row.rowKey, slot) },
                        )
                    }
                }
                HorizontalDivider(color = CmsTheme.colors.rule.copy(alpha = 0.35f))
            }
        }
    }
}

@Composable
private fun TeacherHeaderCell(text: String, width: Dp) {
    Box(Modifier.width(width).padding(horizontal = 4.dp), contentAlignment = Alignment.Center) {
        Text(
            text,
            color = CmsTheme.colors.onInk,
            style = CmsTextStyles.eyebrow,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun TeacherPeriodDetailDialog(period: SessionPeriod, session: AcademicSession?, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(period.subjectName) },
        text = { DialogScrollBody {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ScheduleDetailRow("Session", session?.label ?: period.sessionId)
                ScheduleDetailRow("Shift", period.shift.label)
                ScheduleDetailRow("Day", period.day.getDisplayName(TextStyle.FULL, Locale.ENGLISH))
                ScheduleDetailRow("Time", period.timeRange)
                ScheduleDetailRow("Subject code", period.courseCode)
                ScheduleDetailRow("Room", listOfNotNull(period.building, period.roomNo).joinToString(" / ").ifBlank { "Not assigned" })
                period.notes?.takeIf { it.isNotBlank() }?.let { ScheduleDetailRow("Notes", it) }
            }
        }},
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}

@Composable
private fun ScheduleDetailRow(label: String, value: String) {
    Column {
        Text(label.uppercase(), color = ModMuted, style = CmsTextStyles.eyebrow)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

