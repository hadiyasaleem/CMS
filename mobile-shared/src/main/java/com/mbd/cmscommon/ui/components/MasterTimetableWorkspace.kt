package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.PeriodType
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmscommon.ui.theme.ModInk
import com.mbd.cmscommon.ui.theme.ModMuted
import com.mbd.cmscommon.ui.theme.ModTrack
import com.mbd.cmscommon.ui.theme.ModGround
import com.mbd.cmscommon.ui.theme.ModSurface
import com.mbd.cmscommon.ui.theme.ModAccent
import com.mbd.cmscommon.ui.theme.ModWarn
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

private val MasterCanvas = ModGround
private val MasterGold = ModWarn
private val MasterRed = ModAccent
private val MasterDays = listOf(
    DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY,
)

@Composable
fun MasterTimetableWorkspace(
    departments: List<Department>,
    sessions: List<AcademicSession>,
    sessionsInDepartment: List<AcademicSession>,
    shiftsForSelection: List<Session>,
    selectedDeptId: String?,
    selectedStartYear: Int?,
    selectedShift: Session?,
    resolvedSession: AcademicSession?,
    periods: List<SessionPeriod>,
    loading: Boolean,
    errorMessage: String?,
    onSelectDepartment: (String?) -> Unit,
    onSelectStartYear: (Int?) -> Unit,
    onSelectShift: (Session?) -> Unit,
    onRetry: () -> Unit,
    onOpenSession: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var detailPeriod by remember { mutableStateOf<SessionPeriod?>(null) }
    var dismissedError by remember { mutableStateOf<String?>(null) }
    val department = departments.firstOrNull { it.deptId == selectedDeptId }
    val periodByDayAndSlot = periods.associateBy { it.day to it.timeRange }
    val timeSlots = periods.map { it.timeRange }.distinct().sortedBy { it.substringBefore('–') }

    LazyColumn(
        modifier = modifier.fillMaxWidth().background(MasterCanvas),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { MasterHeader() }

        item {
            MasterFilters(
                departments = departments,
                sessionsInDepartment = sessionsInDepartment,
                shiftsForSelection = shiftsForSelection,
                selectedDeptId = selectedDeptId,
                selectedStartYear = selectedStartYear,
                selectedShift = selectedShift,
                onSelectDepartment = onSelectDepartment,
                onSelectStartYear = onSelectStartYear,
                onSelectShift = onSelectShift,
            )
        }

        when {
            loading -> item { SkeletonRow() }
            resolvedSession == null -> item {
                MasterEmptyCard(
                    "Choose a department, session, and shift",
                    "Pick all three filters above to view that class's weekly timetable.",
                )
            }
            else -> {
                item {
                    MasterSessionTile(department?.code ?: resolvedSession.deptId, resolvedSession, onOpenSession = { onOpenSession(resolvedSession.sessionId) })
                }
                if (periods.isEmpty()) {
                    item { MasterEmptyCard("No periods scheduled", "This session has no timetable periods yet.") }
                } else {
                    item {
                        TimetableGrid(
                            timeSlots = timeSlots,
                            rows = MasterDays.map { day ->
                                GridRow(
                                    key = day.name,
                                    label = day.getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                                    cells = timeSlots.associateWith { slot ->
                                        periodByDayAndSlot[day to slot]?.let { period ->
                                            val isBreak = period.periodType == PeriodType.BREAK
                                            GridCell(
                                                title = if (isBreak) "BREAK" else period.subjectName,
                                                subtitle = if (isBreak) "" else period.teacherName.ifBlank { "Unassigned" },
                                                meta = if (isBreak) "" else period.roomNo?.ifBlank { null } ?: "No room",
                                                isBreak = isBreak,
                                                isAlert = !isBreak && (period.teacherId.isBlank() || period.roomNo.isNullOrBlank()),
                                            )
                                        }
                                    },
                                )
                            },
                            onCellClick = { dayKey, slot ->
                                detailPeriod = periodByDayAndSlot[DayOfWeek.valueOf(dayKey) to slot]
                            },
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(72.dp)) }
    }

    detailPeriod?.let { period ->
        PeriodDetailDialog(period, onDismiss = { detailPeriod = null })
    }

    if (!errorMessage.isNullOrBlank() && errorMessage != dismissedError) {
        AlertDialog(
            onDismissRequest = { dismissedError = errorMessage },
            title = { Text("Couldn't load timetable") },
            text = { Text(errorMessage, color = MasterRed) },
            confirmButton = { TextButton(onClick = { dismissedError = null; onRetry() }) { Text("Retry") } },
            dismissButton = { TextButton(onClick = { dismissedError = errorMessage }) { Text("Dismiss") } },
        )
    }
}

@Composable
private fun MasterHeader() {
    Surface(shape = RoundedCornerShape(18.dp), color = ModInk) {
        Column(Modifier.padding(20.dp)) {
            Text("COLLEGE-WIDE SCHEDULE", color = MasterGold, style = CmsTextStyles.eyebrow)
            Spacer(Modifier.height(6.dp))
            Text("Master Timetable", color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            Text("Pick a department, session, and shift to view its weekly grid.", color = CmsTheme.colors.onInkMuted, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun MasterFilters(
    departments: List<Department>,
    sessionsInDepartment: List<AcademicSession>,
    shiftsForSelection: List<Session>,
    selectedDeptId: String?,
    selectedStartYear: Int?,
    selectedShift: Session?,
    onSelectDepartment: (String?) -> Unit,
    onSelectStartYear: (Int?) -> Unit,
    onSelectShift: (Session?) -> Unit,
) {
    val departmentOptions = departments.sortedBy { it.name }.map { CmsEntityOption(it.deptId, "${it.code} · ${it.name}") }
    val sessionOptions = sessionsInDepartment.map { it.startYear }.distinct().sorted().map { CmsEntityOption(it.toString(), "$it–${it + 4}") }
    val shiftOptions = shiftsForSelection.map { CmsEntityOption(it.name, it.name) }

    Column(Modifier.fillMaxWidth()) {
        Text("SHOW", color = ModMuted, style = CmsTextStyles.eyebrow)
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DropdownChip(
                selectedLabel = departmentOptions.firstOrNull { it.id == selectedDeptId }?.label,
                emptyLabel = "All departments",
                options = departmentOptions,
                onSelected = onSelectDepartment,
            )
            DropdownChip(
                selectedLabel = sessionOptions.firstOrNull { it.id == selectedStartYear?.toString() }?.label,
                emptyLabel = "All sessions",
                options = sessionOptions,
                onSelected = { onSelectStartYear(it?.toIntOrNull()) },
                enabled = selectedDeptId != null,
            )
            DropdownChip(
                selectedLabel = shiftOptions.firstOrNull { it.id == selectedShift?.name }?.label,
                emptyLabel = "All shifts",
                options = shiftOptions,
                onSelected = { onSelectShift(it?.let(Session::valueOf)) },
                enabled = selectedStartYear != null,
            )
        }
    }
}

@Composable
private fun MasterSessionTile(deptCode: String, session: AcademicSession, onOpenSession: () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Row(Modifier.padding(16.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("$deptCode - ${session.label} - ${session.shift}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(session.programName?.takeIf { it.isNotBlank() } ?: "Program not configured", color = ModMuted, style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = onOpenSession) { Text("Open editor") }
        }
    }
}

@Composable
private fun PeriodDetailDialog(period: SessionPeriod, onDismiss: () -> Unit) {
    val isBreak = period.periodType == PeriodType.BREAK
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isBreak) "Break" else period.subjectName) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                DetailRow("Day", period.day.getDisplayName(TextStyle.FULL, Locale.ENGLISH))
                DetailRow("Time", period.timeRange)
                if (!isBreak) {
                    DetailRow("Subject code", period.courseCode)
                    DetailRow("Teacher", period.teacherName.ifBlank { "Unassigned" })
                    DetailRow("Room", period.roomNo?.ifBlank { null } ?: "Not assigned")
                    period.building?.takeIf { it.isNotBlank() }?.let { DetailRow("Building", it) }
                    period.creditHours?.let { DetailRow("Credit hours", it.toString()) }
                    period.notes?.takeIf { it.isNotBlank() }?.let { DetailRow("Notes", it) }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(label.uppercase(), color = ModMuted, style = CmsTextStyles.eyebrow)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun MasterEmptyCard(title: String, detail: String) {
    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(24.dp)) {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(detail, color = ModMuted, style = MaterialTheme.typography.bodySmall)
        }
    }
}
