package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Building
import com.mbd.cmscommon.domain.model.PeriodType
import com.mbd.cmscommon.domain.model.Room
import com.mbd.cmscommon.domain.model.SemesterSubject
import com.mbd.cmscommon.domain.model.SemesterTerm
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmscommon.ui.theme.ModInk
import com.mbd.cmscommon.ui.theme.ModMuted
import com.mbd.cmscommon.ui.theme.ModTrack
import com.mbd.cmscommon.ui.theme.ModSurface
import com.mbd.cmscommon.ui.theme.ModAccent
import com.mbd.cmscommon.ui.theme.ModWarn
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale

private val TimetableGold = ModWarn
private val TimetableRed = ModAccent
private val TIMETABLE_DAYS = listOf(
    DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY,
)

@Composable
fun SessionTimetableWorkspace(
    session: AcademicSession?,
    periods: List<SessionPeriod>,
    subjects: List<SemesterSubject>,
    teachers: List<Teacher>,
    buildings: List<Building>,
    rooms: List<Room>,
    currentSemesterTerm: SemesterTerm?,
    errorMessage: String?,
    onSavePeriod: (DayOfWeek, String, String, SemesterSubject?, Teacher?, PeriodType, String, String, String, LocalDate?, LocalDate?, SessionPeriod?) -> Unit,
    onRemovePeriod: (SessionPeriod) -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var editorState by remember { mutableStateOf<SessionPeriod?>(null) }
    var addingPeriodDay by remember { mutableStateOf<DayOfWeek?>(null) }
    var pendingRemove by remember { mutableStateOf<SessionPeriod?>(null) }
    var detailPeriod by remember { mutableStateOf<SessionPeriod?>(null) }

    val roomsConfigured = periods.count { !it.roomNo.isNullOrBlank() }
    val teacherIds = periods.filter { it.periodType != PeriodType.BREAK }.map { it.teacherId }.filter { it.isNotBlank() }.distinct()
    val conflictIds = conflictingPeriodIds(periods)
    val periodByDayAndSlot = periods.associateBy { it.day to it.timeRange }
    val timeSlots = periods.map { it.timeRange }.distinct().sortedBy { it.substringBefore('–') }

    LazyColumn(modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { TimetableHero(session, onAdd = { addingPeriodDay = DayOfWeek.MONDAY }) }

        item { TimetableSummaryCard(periods.size, roomsConfigured, teacherIds.size, conflictIds.size) }

        if (periods.isEmpty()) {
            item { TimetableEmptyState(onAdd = { addingPeriodDay = DayOfWeek.MONDAY }) }
        } else {
            item {
                TimetableGrid(
                    timeSlots = timeSlots,
                    rows = TIMETABLE_DAYS.map { day ->
                        GridRow(
                            key = day.name,
                            label = day.getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                            cells = timeSlots.associateWith { slot ->
                                periodByDayAndSlot[day to slot]?.let { period ->
                                    val isBreak = period.periodType == PeriodType.BREAK
                                    GridCell(
                                        title = if (isBreak) "BREAK" else period.subjectName.ifBlank { period.courseCode },
                                        subtitle = if (isBreak) "" else period.teacherName.ifBlank { "Unassigned" },
                                        meta = if (isBreak) "" else period.roomNo?.ifBlank { null } ?: "No room",
                                        isBreak = isBreak,
                                        isAlert = period.id in conflictIds,
                                    )
                                }
                            },
                        )
                    },
                    onCellClick = { dayKey, slot ->
                        val day = DayOfWeek.valueOf(dayKey)
                        val period = periodByDayAndSlot[day to slot]
                        if (period != null) detailPeriod = period else addingPeriodDay = day
                    },
                )
            }
        }

        item { Spacer(Modifier.height(72.dp)) }
    }

    if (addingPeriodDay != null || editorState != null) {
        PeriodEditorDialog(
            day = editorState?.day ?: addingPeriodDay ?: DayOfWeek.MONDAY,
            existing = editorState,
            subjects = subjects,
            teachers = teachers,
            buildings = buildings,
            rooms = rooms,
            currentSemesterTerm = currentSemesterTerm,
            onDismiss = { addingPeriodDay = null; editorState = null },
            onSave = { days, start, end, subject, teacher, type, room, building, notes, from, to ->
                // Multiple days can be checked at once. Each checked day resolves its own "replaces"
                // row independently: the day being edited replaces itself, and any OTHER checked day
                // that already has a sibling row from the same original multi-day group (same course
                // and original time) replaces that sibling instead of inserting a duplicate -- which
                // previously read as a self-conflict when re-saving a day that was already scheduled.
                val original = editorState
                days.forEach { day ->
                    val replaces = when {
                        original != null && original.day == day -> original
                        original != null -> periods.firstOrNull {
                            it.day == day && it.courseCode == original.courseCode &&
                                it.startTime == original.startTime && it.endTime == original.endTime
                        }
                        else -> null
                    }
                    onSavePeriod(day, start, end, subject, teacher, type, room, building, notes, from, to, replaces)
                }
                addingPeriodDay = null
                editorState = null
            },
        )
    }

    detailPeriod?.let { period ->
        SessionPeriodDetailDialog(
            period = period,
            hasConflict = period.id in conflictIds,
            onEdit = { detailPeriod = null; editorState = period },
            onRequestRemove = { detailPeriod = null; pendingRemove = period },
            onDismiss = { detailPeriod = null },
        )
    }

    pendingRemove?.let { period ->
        ConfirmDestructiveActionDialog(
            title = "Remove period",
            dependentSummary = "Removes ${period.subjectName.ifBlank { period.periodType.name }} at ${period.startTime} on ${period.day}.",
            onConfirm = { onRemovePeriod(period); pendingRemove = null },
            onDismiss = { pendingRemove = null },
        )
    }

    if (!errorMessage.isNullOrBlank()) {
        AlertDialog(
            onDismissRequest = onClearError,
            title = { Text("Couldn't save period") },
            text = { Text(errorMessage, color = TimetableRed) },
            confirmButton = { TextButton(onClick = onClearError) { Text("OK") } },
        )
    }
}

private fun conflictingPeriodIds(periods: List<SessionPeriod>): Set<String> {
    val conflicts = mutableSetOf<String>()
    val byDay = periods.groupBy { it.day }
    byDay.values.forEach { dayPeriods ->
        for (i in dayPeriods.indices) {
            for (j in i + 1 until dayPeriods.size) {
                val a = dayPeriods[i]
                val b = dayPeriods[j]
                val aStart = runCatching { LocalTime.parse(a.startTime) }.getOrNull()
                val aEnd = runCatching { LocalTime.parse(a.endTime) }.getOrNull()
                val bStart = runCatching { LocalTime.parse(b.startTime) }.getOrNull()
                val bEnd = runCatching { LocalTime.parse(b.endTime) }.getOrNull()
                if (aStart != null && aEnd != null && bStart != null && bEnd != null && aStart < bEnd && bStart < aEnd) {
                    conflicts += a.id
                    conflicts += b.id
                }
            }
        }
    }
    return conflicts
}

@Composable
private fun TimetableHero(session: AcademicSession?, onAdd: () -> Unit) {
    Surface(shape = RoundedCornerShape(18.dp), color = ModInk) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("WEEKLY TIMETABLE", color = TimetableGold, style = CmsTextStyles.eyebrow)
                Spacer(Modifier.height(6.dp))
                Text(session?.label ?: "Session", color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(4.dp))
                Text("Weekly period schedule for this session", color = CmsTheme.colors.onInkMuted, style = MaterialTheme.typography.bodyMedium)
            }
            CmsPrimaryButton(text = "Add period", onClick = onAdd)
        }
    }
}

@Composable
private fun TimetableSummaryCard(periodCount: Int, rooms: Int, teachers: Int, issues: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        TimetableMetric("Periods", periodCount.toString(), Modifier.weight(1f))
        TimetableMetric("Rooms", rooms.toString(), Modifier.weight(1f))
        TimetableMetric("Teachers", teachers.toString(), Modifier.weight(1f))
        TimetableMetric("Issues", issues.toString(), Modifier.weight(1f), alert = issues > 0)
    }
}

@Composable
private fun TimetableMetric(label: String, value: String, modifier: Modifier = Modifier, alert: Boolean = false) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(14.dp)) {
            Text(value, color = if (alert) TimetableRed else ModInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Text(label.uppercase(), color = ModMuted, style = CmsTextStyles.eyebrow)
        }
    }
}

@Composable
private fun SessionPeriodDetailDialog(
    period: SessionPeriod,
    hasConflict: Boolean,
    onEdit: () -> Unit,
    onRequestRemove: () -> Unit,
    onDismiss: () -> Unit,
) {
    val isBreak = period.periodType == PeriodType.BREAK
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isBreak) "Break" else period.subjectName.ifBlank { period.courseCode }) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (hasConflict) {
                    StatusBadge("TIME CONFLICT", BadgeTone.Error)
                    Spacer(Modifier.height(4.dp))
                }
                DetailRow("Day", period.day.getDisplayName(TextStyle.FULL, Locale.ENGLISH))
                DetailRow("Time", period.timeRange)
                if (!isBreak) {
                    DetailRow("Subject code", period.courseCode)
                    DetailRow("Teacher", period.teacherName.ifBlank { "Unassigned" })
                    DetailRow("Room", period.roomNo?.ifBlank { null } ?: "Not assigned")
                    period.building?.takeIf { it.isNotBlank() }?.let { DetailRow("Building", it) }
                    period.notes?.takeIf { it.isNotBlank() }?.let { DetailRow("Notes", it) }
                }
            }
        },
        confirmButton = { TextButton(onClick = onEdit) { Text("Edit") } },
        dismissButton = {
            Row {
                TextButton(onClick = onRequestRemove) { Text("Remove", color = CmsTheme.colors.accent) }
                TextButton(onClick = onDismiss) { Text("Close") }
            }
        },
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
private fun TimetableEmptyState(onAdd: () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No periods scheduled", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text("Keep as a free day or add a period.", color = ModMuted, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(12.dp))
            CmsPrimaryButton(text = "Add period", onClick = onAdd)
        }
    }
}

@Composable
private fun PeriodEditorDialog(
    day: DayOfWeek,
    existing: SessionPeriod?,
    subjects: List<SemesterSubject>,
    teachers: List<Teacher>,
    buildings: List<Building>,
    rooms: List<Room>,
    currentSemesterTerm: SemesterTerm?,
    onDismiss: () -> Unit,
    onSave: (Set<DayOfWeek>, String, String, SemesterSubject?, Teacher?, PeriodType, String, String, String, LocalDate?, LocalDate?) -> Unit,
) {
    var selectedDays by remember { mutableStateOf(setOf(day)) }
    var start by remember { mutableStateOf(existing?.startTime ?: "") }
    var end by remember { mutableStateOf(existing?.endTime ?: "") }
    var type by remember { mutableStateOf(existing?.periodType ?: PeriodType.LECTURE) }
    var subjectCode by remember { mutableStateOf(existing?.courseCode ?: "") }
    var teacherId by remember { mutableStateOf(existing?.teacherId ?: "") }
    var room by remember { mutableStateOf(existing?.roomNo ?: "") }
    var building by remember { mutableStateOf(existing?.building ?: "") }
    var selectedBuildingId by remember { mutableStateOf(buildings.firstOrNull { it.name == existing?.building }?.buildingId) }
    var selectedRoomId by remember {
        mutableStateOf(rooms.firstOrNull { it.roomNo == existing?.roomNo && (selectedBuildingId == null || it.buildingId == selectedBuildingId) }?.roomId)
    }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }
    var effectiveFrom by remember { mutableStateOf(existing?.effectiveFrom?.toString() ?: "") }
    var effectiveTo by remember { mutableStateOf(existing?.effectiveTo?.toString() ?: "") }

    val startTime = runCatching { LocalTime.parse(start.trim()) }
    val endTime = runCatching { LocalTime.parse(end.trim()) }
    val timeValid = startTime.isSuccess && endTime.isSuccess && startTime.getOrNull()!! < endTime.getOrNull()
    val needsSubject = type != PeriodType.BREAK
    val hasSemesterTerm = currentSemesterTerm?.startDate != null && currentSemesterTerm.endDate != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Timetable period" else "Edit period", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState())) {
                Text("DAYS", color = ModMuted, style = CmsTextStyles.eyebrow)
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    TIMETABLE_DAYS.forEach { option ->
                        Row(
                            modifier = Modifier.clickable {
                                selectedDays = if (option in selectedDays) selectedDays - option else selectedDays + option
                            },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(checked = option in selectedDays, onCheckedChange = { checked ->
                                selectedDays = if (checked) selectedDays + option else selectedDays - option
                            })
                            Text(option.getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                        }
                    }
                }
                if (selectedDays.isEmpty()) {
                    Text("Select at least one day.", color = TimetableRed, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CmsTimeField(value = start, onValueChange = { start = it }, label = "Start", modifier = Modifier.weight(1f))
                    CmsTimeField(value = end, onValueChange = { end = it }, label = "End", modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
                Text("PERIOD TYPE", color = ModMuted, style = CmsTextStyles.eyebrow)
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    PeriodType.entries.forEach { option -> CmsChip(option.name, selected = type == option, onClick = { type = option }) }
                }
                if (needsSubject) {
                    Spacer(Modifier.height(10.dp))
                    CmsEntityPicker(
                        label = "Subject / current semester",
                        selectedId = subjectCode.ifBlank { null },
                        options = subjects.map { CmsEntityOption(it.courseCode, it.name) },
                        onSelected = { subjectCode = it ?: "" },
                        optional = true,
                    )
                    Spacer(Modifier.height(10.dp))
                    CmsEntityPicker(
                        label = "Teacher (optional)",
                        selectedId = teacherId.ifBlank { null },
                        options = teachers.map { CmsEntityOption(it.teacherId, it.name) },
                        onSelected = { teacherId = it ?: "" },
                        optional = true,
                        emptyLabel = "Not assigned",
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        CmsEntityPicker(
                            label = "Building (optional)",
                            selectedId = selectedBuildingId,
                            options = buildings.map { CmsEntityOption(it.buildingId, it.name) },
                            onSelected = { id ->
                                selectedBuildingId = id
                                building = buildings.firstOrNull { it.buildingId == id }?.name ?: ""
                                if (selectedRoomId != null && rooms.firstOrNull { it.roomId == selectedRoomId }?.buildingId != id) {
                                    selectedRoomId = null
                                    room = ""
                                }
                            },
                            optional = true,
                            emptyLabel = "Any building",
                            modifier = Modifier.weight(1f),
                        )
                        CmsEntityPicker(
                            label = "Room (optional)",
                            selectedId = selectedRoomId,
                            options = rooms.filter { selectedBuildingId == null || it.buildingId == selectedBuildingId }
                                .map { CmsEntityOption(it.roomId, it.roomNo, it.name) },
                            onSelected = { id ->
                                selectedRoomId = id
                                val picked = rooms.firstOrNull { it.roomId == id }
                                room = picked?.roomNo ?: ""
                                if (picked != null) {
                                    selectedBuildingId = picked.buildingId
                                    building = buildings.firstOrNull { it.buildingId == picked.buildingId }?.name ?: building
                                }
                            },
                            optional = true,
                            emptyLabel = "Not assigned",
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes (optional)") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("EFFECTIVE DATES", modifier = Modifier.weight(1f), color = ModMuted, style = CmsTextStyles.eyebrow)
                    if (hasSemesterTerm) {
                        TextButton(onClick = {
                            effectiveFrom = currentSemesterTerm!!.startDate.toString()
                            effectiveTo = currentSemesterTerm.endDate.toString()
                        }) { Text("Use semester dates") }
                    }
                }
                Spacer(Modifier.height(6.dp))
                CmsDateField(value = effectiveFrom, onValueChange = { effectiveFrom = it }, label = "Effective from", optional = true)
                Spacer(Modifier.height(10.dp))
                CmsDateField(value = effectiveTo, onValueChange = { effectiveTo = it }, label = "Effective to", optional = true)
                if (!timeValid) {
                    Spacer(Modifier.height(8.dp))
                    Text("Time conflict: enter a valid start and end time.", color = TimetableRed, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val subject = subjects.firstOrNull { it.courseCode == subjectCode }
                    val teacher = teachers.firstOrNull { it.teacherId == teacherId }
                    onSave(
                        selectedDays, start.trim(), end.trim(), subject, teacher, type, room.trim(), building.trim(), notes.trim(),
                        runCatching { LocalDate.parse(effectiveFrom.trim()) }.getOrNull(),
                        runCatching { LocalDate.parse(effectiveTo.trim()) }.getOrNull(),
                    )
                },
                enabled = selectedDays.isNotEmpty() && timeValid && (!needsSubject || subjectCode.isNotBlank()),
            ) { Text(if (existing == null) "Add period" else "Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
