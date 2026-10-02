package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.domain.model.Building
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.PeriodType
import com.mbd.cmscommon.domain.model.ProgramType
import com.mbd.cmscommon.domain.model.Room
import com.mbd.cmscommon.domain.model.SemesterSubject
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.controller.ConflictKind
import com.mbd.cmscommon.controller.MasterGrid
import com.mbd.cmscommon.controller.PeriodConflict
import com.mbd.cmscommon.controller.timeSlotCascade
import com.mbd.cmscommon.export.ExportDocument
import com.mbd.cmscommon.export.ExportFormat
import com.mbd.cmscommon.export.masterGridExport
import com.mbd.cmscommon.export.masterGridsExport
import com.mbd.cmscommon.export.masterGridTitleLines
import com.mbd.cmscommon.util.clockDisplay
import com.mbd.cmscommon.util.isTimeRangeInvalid
import com.mbd.cmscommon.util.parseClock
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmscommon.ui.theme.ModInk
import com.mbd.cmscommon.ui.theme.ModMuted
import com.mbd.cmscommon.ui.theme.ModTrack
import com.mbd.cmscommon.ui.theme.ModGround
import com.mbd.cmscommon.ui.theme.ModSurface
import com.mbd.cmscommon.ui.theme.ModWarn
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private val MasterCanvas = ModGround

@Composable
fun MasterTimetableWorkspace(
    departments: List<Department>,
    availableSemesters: List<Int>,
    selectedSemester: Int?,
    selectedShift: Session?,
    selectedDeptId: String?,
    selectedProgramType: ProgramType? = null,
    grids: List<MasterGrid>,
    periodConflicts: Map<String, List<PeriodConflict>> = emptyMap(),
    loading: Boolean,
    errorMessage: String?,
    errorTitle: String = "Couldn't load timetable",
    onSelectSemester: (Int?) -> Unit,
    onSelectShift: (Session?) -> Unit,
    onSelectDepartment: (String?) -> Unit,
    onSelectProgramType: (ProgramType?) -> Unit = {},
    onClearFilters: () -> Unit,
    onRetry: () -> Unit,
    onOpenSession: (String) -> Unit,
    onSaveShifts: (MasterGrid, Map<Pair<String, String>, Pair<String, String>>) -> Unit,
    onExport: (ExportDocument, ExportFormat) -> Unit,
    teachers: List<Teacher> = emptyList(),
    buildings: List<Building> = emptyList(),
    rooms: List<Room> = emptyList(),
    onLoadSubjects: suspend (sessionId: String, semester: Int) -> List<SemesterSubject> = { _, _ -> emptyList() },
    onSavePeriod: (SessionPeriod, Set<DayOfWeek>, String, String, SemesterSubject?, List<Teacher>, PeriodType, String, String, String, LocalDate?, LocalDate?) -> Unit = { _, _, _, _, _, _, _, _, _, _, _, _ -> },
    modifier: Modifier = Modifier,
) {
    var detailContext by remember { mutableStateOf<Pair<SessionPeriod, Int>?>(null) }
    var editingContext by remember { mutableStateOf<Pair<SessionPeriod, Int>?>(null) }
    var dismissedError by remember { mutableStateOf<String?>(null) }
    // Column edits are staged here (per grid, keyed by title) and only reach onSaveShifts on an explicit Save.
    var pendingByGrid by remember { mutableStateOf<Map<String, Map<Pair<String, String>, Pair<String, String>>>>(emptyMap()) }
    val anyFilterActive = selectedSemester != null || selectedShift != null || selectedDeptId != null || selectedProgramType != null

    TopBarActions {
        ExportMenuButton(onExport = { format -> onExport(masterGridsExport(grids), format) }, enabled = grids.isNotEmpty(), tint = CmsTheme.colors.onInk)
    }

    val listState = rememberLazyListState()
    WithVerticalScrollbar(listState) {
    LazyColumn( state = listState,
        modifier = modifier.fillMaxWidth().background(MasterCanvas),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            MasterFilterBar(
                departments = departments,
                availableSemesters = availableSemesters,
                selectedSemester = selectedSemester,
                selectedShift = selectedShift,
                selectedDeptId = selectedDeptId,
                selectedProgramType = selectedProgramType,
                anyFilterActive = anyFilterActive,
                onSelectSemester = onSelectSemester,
                onSelectShift = onSelectShift,
                onSelectDepartment = onSelectDepartment,
                onSelectProgramType = onSelectProgramType,
                onClearFilters = onClearFilters,
            )
        }

        when {
            loading -> item { SkeletonRow() }
            grids.isEmpty() -> item {
                MasterEmptyCard(
                    if (anyFilterActive) "No timetable matches these filters" else "No timetable periods yet",
                    if (anyFilterActive) "Clear a filter above to widen the search." else "Once periods are added they'll appear here, grouped by semester and shift.",
                )
            }
            else -> grids.forEach { grid ->
                item {
                    MasterGridSection(
                        grid = grid,
                        pending = pendingByGrid[grid.title].orEmpty(),
                        periodConflicts = periodConflicts,
                        onOpenSession = onOpenSession,
                        onCellClick = { period -> detailContext = period to grid.semester },
                        onStageEdit = { updated -> pendingByGrid = pendingByGrid + (grid.title to updated) },
                        onSave = {
                            pendingByGrid[grid.title]?.takeIf { it.isNotEmpty() }?.let { onSaveShifts(grid, it) }
                            pendingByGrid = pendingByGrid - grid.title
                        },
                        onDiscard = { pendingByGrid = pendingByGrid - grid.title },
                        onExport = onExport,
                    )
                }
            }
        }

        item { Spacer(Modifier.height(72.dp)) }
    }
    }

    detailContext?.let { (period, semester) ->
        PeriodDetailDialog(
            period,
            conflicts = periodConflicts[period.id.substringBefore("::")].orEmpty(),
            sessionLabel = { sessionId ->
                grids.flatMap { it.rows }.firstOrNull { it.session.sessionId == sessionId }
                    ?.let { row -> "${row.department?.code ?: row.session.deptId} ${row.session.label}" }
                    ?: sessionId
            },
            onDismiss = { detailContext = null },
            onEdit = {
                detailContext = null
                // A linked session's copy of a merged lecture is edited through the session that owns it.
                val ownerId = period.id.substringBefore("::")
                val owner = if (period.isOwnRow) null else grids.flatMap { g -> g.rows.map { g to it } }
                    .firstNotNullOfOrNull { (g, row) -> row.periods.firstOrNull { it.id == ownerId && it.isOwnRow }?.let { it to g.semester } }
                editingContext = owner ?: (period to semester)
            },
        )
    }

    editingContext?.let { (period, semester) ->
        var subjects by remember(period.id) { mutableStateOf<List<SemesterSubject>>(emptyList()) }
        LaunchedEffect(period.id) { subjects = onLoadSubjects(period.sessionId, semester) }
        // The clicked cell may collapse several identical days into one (e.g. "Mon & Tue"), so the
        // dialog needs every sibling day from this period's own row, not just the one that was clicked.
        val siblingPeriods = grids.flatMap { it.rows }.firstOrNull { it.session.sessionId == period.sessionId }?.periods.orEmpty()
        PeriodEditorDialog(
            day = period.day,
            existing = period,
            subjects = subjects,
            teachers = teachers,
            buildings = buildings,
            rooms = rooms,
            currentSemesterTerm = null,
            initialDays = siblingDaysFor(period, siblingPeriods),
            onDismiss = { editingContext = null },
            onSave = { days, start, end, subject, teacher, type, room, buildingName, notes, from, to ->
                onSavePeriod(period, days, start, end, subject, teacher, type, room, buildingName, notes, from, to)
                editingContext = null
            },
        )
    }

    if (!errorMessage.isNullOrBlank() && errorMessage != dismissedError) {
        CmsErrorDialog(
            message = errorMessage,
            title = errorTitle,
            onDismiss = { dismissedError = errorMessage },
            onRetry = { dismissedError = null; onRetry() },
        )
    }
}

@Composable
private fun MasterFilterBar(
    departments: List<Department>,
    availableSemesters: List<Int>,
    selectedSemester: Int?,
    selectedShift: Session?,
    selectedDeptId: String?,
    selectedProgramType: ProgramType?,
    anyFilterActive: Boolean,
    onSelectSemester: (Int?) -> Unit,
    onSelectShift: (Session?) -> Unit,
    onSelectDepartment: (String?) -> Unit,
    onSelectProgramType: (ProgramType?) -> Unit,
    onClearFilters: () -> Unit,
) {
    val deptOptions = departments.map { CmsEntityOption(it.deptId, it.code) }
    val semesterOptions = availableSemesters.map { CmsEntityOption(it.toString(), "Semester $it") }
    val shiftOptions = Session.entries.map { CmsEntityOption(it.name, it.label) }
    val programOptions = ProgramType.entries.map { CmsEntityOption(it.name, it.label) }

    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DropdownChip(
                    selectedLabel = deptOptions.firstOrNull { it.id == selectedDeptId }?.label,
                    emptyLabel = "All departments",
                    options = deptOptions,
                    onSelected = onSelectDepartment,
                )
                DropdownChip(
                    selectedLabel = semesterOptions.firstOrNull { it.id == selectedSemester?.toString() }?.label,
                    emptyLabel = "All semesters",
                    options = semesterOptions,
                    onSelected = { id -> onSelectSemester(id?.toIntOrNull()) },
                )
                DropdownChip(
                    selectedLabel = shiftOptions.firstOrNull { it.id == selectedShift?.name }?.label,
                    emptyLabel = "All shifts",
                    options = shiftOptions,
                    onSelected = { id -> onSelectShift(id?.let { name -> Session.entries.firstOrNull { it.name == name } }) },
                )
                DropdownChip(
                    selectedLabel = programOptions.firstOrNull { it.id == selectedProgramType?.name }?.label,
                    emptyLabel = "All programs",
                    options = programOptions,
                    onSelected = { id -> onSelectProgramType(id?.let { name -> ProgramType.entries.firstOrNull { it.name == name } }) },
                )
            }
            if (anyFilterActive) {
                TextButton(onClick = onClearFilters) { Text("Clear filters") }
            }
        }
    }
}

/** One visual row of the master grid: a department, possibly narrowed to just the days sharing its
 * period set (see [DayCluster]). [deptLabel] is blank on a row after the first for the same
 * department, approximating the printed timetables' merged department cell without true row-span. */
private data class MasterRow(
    val rowKey: String,
    val deptLabel: String,
    val daysLabel: String,
    val cells: Map<String, GridCell?>,
)

private fun originalKeyOf(period: SessionPeriod): Pair<String, String> =
    clockDisplay(period.startTime) to clockDisplay(period.endTime)

private fun effectiveKeyOf(period: SessionPeriod, pending: Map<Pair<String, String>, Pair<String, String>>): Pair<String, String> =
    pending[originalKeyOf(period)] ?: originalKeyOf(period)

private fun slotLabel(key: Pair<String, String>): String = "${key.first}–${key.second}"

private fun slotKey(slot: String): Pair<String, String> {
    val parts = slot.split('–', limit = 2)
    return parts[0] to parts.getOrElse(1) { parts[0] }
}

@Composable
private fun MasterGridSection(
    grid: MasterGrid,
    pending: Map<Pair<String, String>, Pair<String, String>>,
    periodConflicts: Map<String, List<PeriodConflict>>,
    onOpenSession: (String) -> Unit,
    onCellClick: (SessionPeriod) -> Unit,
    onStageEdit: (Map<Pair<String, String>, Pair<String, String>>) -> Unit,
    onSave: () -> Unit,
    onDiscard: () -> Unit,
    onExport: (ExportDocument, ExportFormat) -> Unit,
) {
    val allPeriods = grid.rows.flatMap { it.periods }
    // The columns actually shown reflect any not-yet-saved edits — editing a second column cascades
    // against what's currently on screen, not what's still in the database.
    val effColumns = allPeriods.map { effectiveKeyOf(it, pending) }.distinct().sortedBy { parseClock(it.first) }
    val timeSlots = effColumns.map(::slotLabel)

    val clustersByRow = grid.rows.map { row -> row to dayClustersFor(row.periods) }
    val periodsByRowKey = clustersByRow
        .flatMap { (row, clusters) ->
            clusters.mapIndexed { i, c -> "${row.session.sessionId}_$i" to c.periods.associateBy { p -> slotLabel(effectiveKeyOf(p, pending)) } }
        }
        .toMap()
    val rows = clustersByRow.flatMap { (row, clusters) ->
        clusters.mapIndexed { index, cluster ->
            val byRange = periodsByRowKey["${row.session.sessionId}_$index"].orEmpty()
            MasterRow(
                rowKey = "${row.session.sessionId}_$index",
                deptLabel = if (index == 0) (row.department?.code ?: row.session.deptId) else "",
                daysLabel = dayRangeLabel(cluster.days),
                cells = timeSlots.associateWith { slot ->
                    byRange[slot]?.let { period ->
                        val isBreak = period.periodType == PeriodType.BREAK
                        val codeLine = period.courseCode + (period.creditHours?.let { "($it+0)" } ?: "")
                        val location = listOfNotNull(period.building?.ifBlank { null }, period.roomNo?.ifBlank { null }).joinToString(" ").ifBlank { "No room" }
                        val hasConflict = !isBreak && periodConflicts[period.id.substringBefore("::")].orEmpty().isNotEmpty()
                        val incomplete = !isBreak && (period.teacherId.isBlank() || period.roomNo.isNullOrBlank())
                        GridCell(
                            title = if (isBreak) "BREAK" else period.subjectName,
                            subtitle = if (isBreak) "" else listOfNotNull(codeLine.takeIf { it.isNotBlank() }, period.teacherLabel.ifBlank { "Unassigned" }).joinToString(" · "),
                            meta = if (isBreak) "" else location + mergedTag(period, grid),
                            isBreak = isBreak,
                            isAlert = hasConflict,
                            isWarning = incomplete && !hasConflict,
                        )
                    }
                },
            )
        }
    }

    CmsCard(Modifier.fillMaxWidth()) {
        Column {
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.weight(1f)) { MasterGridTitleBlock(grid) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (pending.isNotEmpty()) {
                        TextButton(onClick = onDiscard) { Text("Discard") }
                        TextButton(onClick = onSave) { Text("Save changes") }
                    }
                    ExportMenuButton(onExport = { format -> onExport(masterGridExport(grid), format) })
                }
            }
            if (timeSlots.isEmpty()) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                    Text("No periods yet", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text("This grid has no timetable periods scheduled.", color = ModMuted, style = MaterialTheme.typography.bodySmall)
                }
            } else {
                MasterTimetableGridBody(
                    timeSlots = timeSlots,
                    rows = rows,
                    onCellClick = { rowKey, slot -> periodsByRowKey[rowKey].orEmpty()[slot]?.let(onCellClick) },
                    onEditColumn = { slot, newStart, newEnd ->
                        val effKey = slotKey(slot)
                        val effShifts = timeSlotCascade(effColumns, effKey, newStart, newEnd)
                        if (effShifts.isNotEmpty()) {
                            val effToOrig = allPeriods
                                .map { originalKeyOf(it) to effectiveKeyOf(it, pending) }
                                .distinct()
                                .groupBy({ it.second }, { it.first })
                            val updated = pending.toMutableMap()
                            for ((oldEff, newEff) in effShifts) {
                                effToOrig[oldEff].orEmpty().forEach { orig -> updated[orig] = newEff }
                            }
                            onStageEdit(updated)
                        }
                    },
                )
            }
        }
    }
}

/** Mirrors the printed timetables' own title block: "TIME TABLE B.S 1st SEMESTER EVENING (2026-2030)",
 * college name, the days this grid actually meets, and its effective-from date -- text sourced from
 * [masterGridTitleLines] so the on-screen title and the export's title block never drift apart. */
@Composable
fun MasterGridTitleBlock(grid: MasterGrid) {
    val lines = masterGridTitleLines(grid)
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        lines.take(2).forEach { line ->
            Text(line, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        }
        lines.drop(2).forEach { line ->
            Text(line, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        }
    }
}

private val MasterDeptW = 90.dp
private val MasterDaysW = 100.dp
private val MasterSlotW = 120.dp

/** Unlike the shared [TimetableGrid] (used for single-session weekly views), nothing here stays fixed
 * while scrolling — DEPT and DAYS columns scroll away with the time slots, all under one scroll state,
 * and there's a dedicated DAYS column instead of a sublabel under the department code. */
@Composable
private fun MasterTimetableGridBody(
    timeSlots: List<String>,
    rows: List<MasterRow>,
    onCellClick: (String, String) -> Unit,
    onEditColumn: (String, String, String) -> Unit,
) {
    val hScroll = rememberScrollState()
    var editingSlot by remember { mutableStateOf<String?>(null) }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth().background(CmsTheme.colors.ink).horizontalScroll(hScroll).padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MasterHeaderCell("Departments", MasterDeptW)
            MasterHeaderCell("DAYS", MasterDaysW)
            timeSlots.forEach { slot -> MasterHeaderCell(slot, MasterSlotW, onClick = { editingSlot = slot }) }
        }
        HorizontalDivider(thickness = 2.dp, color = CmsTheme.colors.rule)
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(hScroll),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.width(MasterDeptW).padding(horizontal = 6.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
                    Text(
                        row.deptLabel,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleSmall,
                        textAlign = TextAlign.Center,
                    )
                }
                Box(Modifier.width(MasterDaysW).padding(horizontal = 6.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
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
                        width = MasterSlotW,
                        onClick = { onCellClick(row.rowKey, slot) },
                    )
                }
            }
            HorizontalDivider(color = CmsTheme.colors.rule.copy(alpha = 0.35f))
        }
    }

    editingSlot?.let { slot ->
        val (oldStart, oldEnd) = slotKey(slot)
        TimeSlotEditDialog(
            oldStart = oldStart,
            oldEnd = oldEnd,
            onDismiss = { editingSlot = null },
            onSave = { newStart, newEnd ->
                onEditColumn(slot, newStart, newEnd)
                editingSlot = null
            },
        )
    }
}

@Composable
private fun MasterHeaderCell(text: String, width: Dp, onClick: (() -> Unit)? = null) {
    Box(
        Modifier.width(width).padding(horizontal = 4.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
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
private fun TimeSlotEditDialog(oldStart: String, oldEnd: String, onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var start by remember { mutableStateOf(oldStart) }
    var end by remember { mutableStateOf(oldEnd) }
    val invalid = isTimeRangeInvalid(start, end)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit time slot") },
        text = { DialogScrollBody {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Applies to every cell in this column. If the new time overlaps the next column, that one shifts too, and so on. " +
                        "Nothing is saved until you click \"Save changes\" above the table.",
                    color = ModMuted,
                    style = MaterialTheme.typography.bodySmall,
                )
                CmsTimeField(value = start, onValueChange = { start = it }, label = "Start time")
                CmsTimeField(value = end, onValueChange = { end = it }, label = "End time", isError = invalid)
                if (invalid) {
                    Text("End time must be after the start time.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        }},
        confirmButton = {
            TextButton(onClick = { onSave(start, end) }, enabled = !invalid) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun PeriodDetailDialog(
    period: SessionPeriod,
    conflicts: List<PeriodConflict> = emptyList(),
    sessionLabel: (String) -> String = { it },
    onDismiss: () -> Unit,
    onEdit: (() -> Unit)? = null,
) {
    val isBreak = period.periodType == PeriodType.BREAK
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isBreak) "Break" else period.subjectName) },
        text = { DialogScrollBody {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (conflicts.isNotEmpty()) {
                    StatusBadge("CONFLICT", BadgeTone.Error)
                    Spacer(Modifier.height(2.dp))
                }
                DetailRow("Shift", period.shift.label)
                DetailRow("Day", period.day.getDisplayName(TextStyle.FULL, Locale.ENGLISH))
                DetailRow("Time", period.timeRange)
                if (!isBreak) {
                    DetailRow("Subject code", period.courseCode)
                    DetailRow("Teacher", period.teacherLabel.ifBlank { "Unassigned" })
                    DetailRow("Room", period.roomNo?.ifBlank { null } ?: "Not assigned")
                    period.building?.takeIf { it.isNotBlank() }?.let { DetailRow("Building", it) }
                    period.creditHours?.let { DetailRow("Credit hours", it.toString()) }
                    period.notes?.takeIf { it.isNotBlank() }?.let { DetailRow("Notes", it) }
                }
                if (conflicts.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text("WHY THIS CONFLICTS", color = ModMuted, style = CmsTextStyles.eyebrow)
                    conflicts.forEach { conflict ->
                        val kindLabel = when (conflict.kind) {
                            ConflictKind.TEACHER -> "Teacher ${period.sharedTeacherWith(conflict.other)?.second ?: conflict.other.teacherLabel} is also teaching"
                            ConflictKind.ROOM -> "Room ${conflict.other.roomNo.orEmpty()} is also booked for"
                        }
                        Text(
                            "$kindLabel ${conflict.other.subjectName} (${conflict.other.courseCode}) — " +
                                "${sessionLabel(conflict.other.sessionId)}, ${conflict.other.timeRange}",
                            color = CmsTheme.colors.accent,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }},
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        dismissButton = onEdit?.let { edit -> { TextButton(onClick = edit) { Text("Edit") } } },
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

/** " · MERGED with IT, CS": the other departments sharing this lecture. */
private fun mergedTag(period: SessionPeriod, grid: MasterGrid): String {
    if (period.linkedSessionIds.isEmpty()) return ""
    val rows = grid.rows
    val codes = period.linkedSessionIds.map { sid -> rows.firstOrNull { it.session.sessionId == sid }?.department?.code ?: sid.substringBefore('_').uppercase() }
    return " · MERGED with " + codes.distinct().joinToString(", ")
}
