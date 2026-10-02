package com.mbd.cmscommon.ui.components

import com.mbd.cmscommon.controller.MasterDatesheetGrid
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Building
import com.mbd.cmscommon.domain.model.Datesheet
import com.mbd.cmscommon.domain.model.DatesheetSlot
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.ProgramType
import com.mbd.cmscommon.domain.model.Room
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.domain.model.resolvedRoomId
import com.mbd.cmscommon.export.DatesheetGridEntry
import com.mbd.cmscommon.export.ExportDocument
import com.mbd.cmscommon.export.ExportFormat
import com.mbd.cmscommon.export.datesheetGridsExport
import com.mbd.cmscommon.export.masterDatesheetGridTitleLines
import com.mbd.cmscommon.util.clockDisplay
import com.mbd.cmscommon.util.isTimeRangeInvalid
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmscommon.ui.theme.ModGround
import com.mbd.cmscommon.ui.theme.ModInk
import com.mbd.cmscommon.ui.theme.ModMuted
import com.mbd.cmscommon.ui.theme.ModSurface
import com.mbd.cmscommon.ui.theme.ModTrack
import com.mbd.cmscommon.ui.theme.ModWarn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val MasterDatesheetCanvas = ModGround
private val MasterDatesheetGold = ModWarn

@Composable
fun MasterDatesheetWorkspace(
    departments: List<Department>,
    availableSemesters: List<Int>,
    selectedSemester: Int?,
    selectedShift: Session?,
    selectedDeptId: String?,
    selectedProgramType: ProgramType? = null,
    grids: List<MasterDatesheetGrid>,
    loading: Boolean,
    errorMessage: String?,
    onSelectSemester: (Int?) -> Unit,
    onSelectShift: (Session?) -> Unit,
    onSelectDepartment: (String?) -> Unit,
    onSelectProgramType: (ProgramType?) -> Unit = {},
    onClearFilters: () -> Unit,
    onRetry: () -> Unit,
    onOpenSession: (String) -> Unit,
    buildings: List<Building>,
    rooms: List<Room>,
    teachers: List<Teacher>,
    busy: Boolean,
    assignErrorMessage: String?,
    onAssignPaper: (Datesheet, DatesheetSlot) -> Unit,
    onExport: (ExportDocument, ExportFormat) -> Unit,
    modifier: Modifier = Modifier,
) {
    var dismissedError by remember { mutableStateOf<String?>(null) }
    val anyFilterActive = selectedSemester != null || selectedShift != null || selectedDeptId != null || selectedProgramType != null

    LazyColumn(
        modifier = modifier.fillMaxWidth().background(MasterDatesheetCanvas),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            MasterDatesheetHeader(
                onExport = onExport,
                build = {
                    val entries = grids.flatMap { grid -> grid.rows.mapNotNull { row -> row.sheet?.let { sheet -> DatesheetGridEntry(listOfNotNull(row.department?.code, grid.title), sheet, row.slots) } } }
                    datesheetGridsExport("master_datesheet_${LocalDate.now()}", listOf("Master Datesheet", "Every current semester"), entries)
                },
                exportEnabled = grids.isNotEmpty(),
            )
        }

        item {
            MasterDatesheetFilterBar(
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
                MasterDatesheetEmptyCard(
                    if (anyFilterActive) "No datesheet matches these filters" else "No exam datesheets yet",
                    if (anyFilterActive) "Clear a filter above to widen the search." else "Create one from a session's own Detail screen (Datesheet), then its papers appear here.",
                )
            }
            else -> grids.forEach { grid ->
                item {
                    MasterDatesheetGridSection(
                        grid = grid,
                        teachers = teachers,
                        buildings = buildings,
                        rooms = rooms,
                        busy = busy,
                        assignErrorMessage = assignErrorMessage,
                        onOpenSession = onOpenSession,
                        onAssignPaper = onAssignPaper,
                        onExport = onExport,
                    )
                }
            }
        }

        item { Spacer(Modifier.height(72.dp)) }
    }

    if (!errorMessage.isNullOrBlank() && errorMessage != dismissedError) {
        CmsErrorDialog(
            message = errorMessage,
            title = "Couldn't load the master datesheet",
            onDismiss = { dismissedError = errorMessage },
            onRetry = { dismissedError = null; onRetry() },
        )
    }
}

@Composable
private fun MasterDatesheetHeader(onExport: (ExportDocument, ExportFormat) -> Unit, build: () -> ExportDocument, exportEnabled: Boolean) {
    Surface(shape = RoundedCornerShape(18.dp), color = ModInk) {
        Column(Modifier.padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text("COLLEGE-WIDE EXAM SCHEDULE", color = MasterDatesheetGold, style = CmsTextStyles.eyebrow)
                    Spacer(Modifier.height(6.dp))
                    Text("Master Datesheet", color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                }
                ExportMenuButton(onExport = { format -> onExport(build(), format) }, enabled = exportEnabled, tint = CmsTheme.colors.onInk)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "Every current semester's exam grid. Tap a scheduled paper to edit it, an empty date to assign one, or a department's name to open its own datesheet.",
                color = CmsTheme.colors.onInkMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun MasterDatesheetFilterBar(
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

private val MASTER_DATESHEET_DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM")

private fun formatGridDate(raw: String): String =
    runCatching { LocalDate.parse(raw).format(MASTER_DATESHEET_DATE_FORMAT) }.getOrDefault(raw)

private fun buildDatesheetCell(slot: DatesheetSlot, sheet: Datesheet, teachers: List<Teacher>): GridCell {
    val invigilatorLabel = slot.invigilatorEmail?.ifBlank { null }?.let { email -> teachers.firstOrNull { it.email.equals(email, ignoreCase = true) }?.name ?: email }
    val missingRoom = slot.resolvedRoomId(sheet) == null
    val missingInvigilator = invigilatorLabel == null
    return GridCell(
        title = slot.courseCode,
        subtitle = slot.subjectName,
        meta = invigilatorLabel ?: "No duty assigned",
        isWarning = missingRoom || missingInvigilator,
    )
}

@Composable
private fun MasterDatesheetGridSection(
    grid: MasterDatesheetGrid,
    teachers: List<Teacher>,
    buildings: List<Building>,
    rooms: List<Room>,
    busy: Boolean,
    assignErrorMessage: String?,
    onOpenSession: (String) -> Unit,
    onAssignPaper: (Datesheet, DatesheetSlot) -> Unit,
    onExport: (ExportDocument, ExportFormat) -> Unit,
) {
    var pickerTarget by remember(grid.title) { mutableStateOf<Pair<Datesheet, String>?>(null) }
    var assignTarget by remember(grid.title) { mutableStateOf<Pair<Datesheet, DatesheetSlot>?>(null) }
    var hintMessage by remember(grid.title) { mutableStateOf<String?>(null) }

    val rawDates = grid.rows.flatMap { it.slots }.mapNotNull { it.examDate }.distinct()
        .sortedBy { runCatching { LocalDate.parse(it) }.getOrDefault(LocalDate.MAX) }
    val formattedDates = rawDates.map(::formatGridDate)
    val formattedToRaw = formattedDates.zip(rawDates).toMap()

    val rows = grid.rows.map { row ->
        val sheet = row.sheet
        val roomNo = sheet?.defaultRoomId?.let { id -> rooms.firstOrNull { it.roomId == id }?.roomNo }
        val sublabel = when {
            sheet == null -> "No datesheet yet"
            roomNo != null -> roomNo
            else -> "No default room"
        }
        val slotsByDate = row.slots.filter { it.examDate != null }.associateBy { it.examDate }
        GridRow(
            key = row.session.sessionId,
            label = row.department?.code ?: row.session.deptId,
            sublabel = sublabel,
            cells = formattedDates.associateWith { col ->
                val rawDate = formattedToRaw[col] ?: return@associateWith null
                val slot = slotsByDate[rawDate] ?: return@associateWith null
                sheet?.let { buildDatesheetCell(slot, it, teachers) }
            },
        )
    }

    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    masterDatesheetGridTitleLines(grid).forEach { line ->
                        Text(line, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    }
                }
                ExportMenuButton(onExport = { format ->
                    val entries = grid.rows.mapNotNull { row -> row.sheet?.let { sheet -> DatesheetGridEntry(listOfNotNull(row.department?.code), sheet, row.slots) } }
                    onExport(datesheetGridsExport("datesheet_${grid.title.replace(' ', '_')}", masterDatesheetGridTitleLines(grid), entries), format)
                })
            }
            Spacer(Modifier.height(10.dp))
            if (rawDates.isEmpty()) {
                Text("No papers scheduled yet in this grid.", color = ModMuted, style = MaterialTheme.typography.bodyMedium)
            } else {
                TimetableGrid(
                    timeSlots = formattedDates,
                    rows = rows,
                    identityHeader = "DEPT / ROOM",
                    onRowLabelClick = { rowKey -> onOpenSession(rowKey) },
                    onCellClick = { rowKey, colLabel ->
                        val row = grid.rows.firstOrNull { it.session.sessionId == rowKey } ?: return@TimetableGrid
                        val sheet = row.sheet
                        val rawDate = formattedToRaw[colLabel]
                        if (sheet == null || rawDate == null) {
                            hintMessage = "Create this department's datesheet from Session Detail → Datesheet."
                            return@TimetableGrid
                        }
                        val existing = row.slots.firstOrNull { it.examDate == rawDate }
                        if (existing != null) {
                            assignTarget = sheet to existing
                        } else {
                            val unscheduled = row.slots.filter { it.examDate == null }
                            if (unscheduled.isEmpty()) {
                                hintMessage = "All of this department's papers are already scheduled."
                            } else {
                                pickerTarget = sheet to rawDate
                            }
                        }
                    },
                )
            }
        }
    }

    pickerTarget?.let { (sheet, rawDate) ->
        val row = grid.rows.firstOrNull { it.sheet?.id == sheet.id }
        val unscheduled = row?.slots.orEmpty().filter { it.examDate == null }
        UnscheduledPaperPickerDialog(
            examDateLabel = formatGridDate(rawDate),
            papers = unscheduled,
            onDismiss = { pickerTarget = null },
            onPick = { slot ->
                pickerTarget = null
                assignTarget = sheet to slot.copy(examDate = rawDate)
            },
        )
    }

    assignTarget?.let { (sheet, slot) ->
        PaperAssignDialog(
            slot = slot,
            sheet = sheet,
            buildings = buildings,
            rooms = rooms,
            teachers = teachers,
            busy = busy,
            errorMessage = assignErrorMessage,
            onDismiss = { assignTarget = null },
            onSave = { updated -> onAssignPaper(sheet, updated) },
        )
    }

    hintMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { hintMessage = null },
            title = { Text("Can't schedule here") },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = { hintMessage = null }) { Text("OK") } },
        )
    }
}

@Composable
private fun UnscheduledPaperPickerDialog(examDateLabel: String, papers: List<DatesheetSlot>, onDismiss: () -> Unit, onPick: (DatesheetSlot) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule a paper for $examDateLabel") },
        text = {
            DialogScrollBody {
                Column {
                    papers.forEach { slot ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = ModSurface,
                            border = BorderStroke(1.dp, ModTrack),
                        ) {
                            TextButton(onClick = { onPick(slot) }, modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.fillMaxWidth()) {
                                    Text(slot.subjectName, fontWeight = FontWeight.Bold)
                                    Text(slot.courseCode, color = ModMuted, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun PaperAssignDialog(
    slot: DatesheetSlot,
    sheet: Datesheet,
    buildings: List<Building>,
    rooms: List<Room>,
    teachers: List<Teacher>,
    busy: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSave: (DatesheetSlot) -> Unit,
) {
    var examDate by remember { mutableStateOf(slot.examDate ?: "") }
    var overrideTime by remember { mutableStateOf(slot.startTime != null || slot.endTime != null) }
    var startTime by remember { mutableStateOf(slot.startTime ?: "") }
    var endTime by remember { mutableStateOf(slot.endTime ?: "") }
    var selectedBuildingId by remember { mutableStateOf(slot.buildingId ?: sheet.defaultBuildingId) }
    var buildingName by remember { mutableStateOf(buildings.firstOrNull { it.buildingId == selectedBuildingId }?.name ?: slot.building ?: "") }
    var selectedRoomId by remember { mutableStateOf(slot.roomId ?: sheet.defaultRoomId) }
    var roomNo by remember { mutableStateOf(rooms.firstOrNull { it.roomId == selectedRoomId }?.roomNo ?: slot.roomNo ?: "") }
    var invigilatorEmail by remember { mutableStateOf(slot.invigilatorEmail ?: "") }

    var saveAttempted by remember { mutableStateOf(false) }
    var displayedError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(busy) {
        if (saveAttempted && !busy) {
            saveAttempted = false
            if (errorMessage != null) {
                displayedError = errorMessage
            } else {
                onDismiss()
            }
        }
    }

    val startTimeValid = startTime.isBlank() || runCatching { LocalTime.parse(startTime) }.isSuccess
    val endTimeValid = endTime.isBlank() || runCatching { LocalTime.parse(endTime) }.isSuccess
    val timesConsistent = !overrideTime || (startTime.isBlank() == endTime.isBlank() && !isTimeRangeInvalid(startTime, endTime))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(slot.subjectName) },
        text = {
            DialogScrollBody(maxHeight = 460.dp) {
                Text(slot.courseCode, color = ModMuted, style = MaterialTheme.typography.bodySmall)
                displayedError?.let { message ->
                    Spacer(Modifier.height(10.dp))
                    CmsNotice(message, tone = NoticeTone.Error, onDismiss = { displayedError = null })
                }
                Spacer(Modifier.height(10.dp))
                CmsDateField(value = examDate, onValueChange = { examDate = it }, label = "Exam date", optional = true)
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = overrideTime, onCheckedChange = { overrideTime = it })
                    Text("Override the datesheet's default time")
                }
                if (overrideTime) {
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        CmsTimeField(value = startTime, onValueChange = { startTime = it }, label = "Start", modifier = Modifier.weight(1f))
                        CmsTimeField(value = endTime, onValueChange = { endTime = it }, label = "End", minTime = startTime, modifier = Modifier.weight(1f))
                    }
                } else if (sheet.defaultStartTime != null && sheet.defaultEndTime != null) {
                    Text("Uses the datesheet default: ${clockDisplay(sheet.defaultStartTime)}–${clockDisplay(sheet.defaultEndTime)}", color = ModMuted, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(10.dp))
                CmsBuildingRoomPicker(
                    buildings = buildings,
                    rooms = rooms,
                    selectedBuildingId = selectedBuildingId,
                    selectedRoomId = selectedRoomId,
                    onChange = { buildingId, name, roomId, roomNumber ->
                        selectedBuildingId = buildingId
                        buildingName = name ?: ""
                        selectedRoomId = roomId
                        roomNo = roomNumber ?: ""
                    },
                    buildingOptional = false,
                )
                Spacer(Modifier.height(10.dp))
                CmsEntityPicker(
                    label = "Invigilator",
                    selectedId = invigilatorEmail.ifBlank { null },
                    options = teachers.map { CmsEntityOption(it.teacherId, it.name) },
                    onSelected = { invigilatorEmail = it ?: "" },
                    optional = true,
                    emptyLabel = "Not assigned",
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    displayedError = null
                    saveAttempted = true
                    onSave(
                        slot.copy(
                            examDate = examDate.ifBlank { null },
                            startTime = if (overrideTime) startTime.ifBlank { null } else null,
                            endTime = if (overrideTime) endTime.ifBlank { null } else null,
                            buildingId = selectedBuildingId,
                            building = buildingName.ifBlank { null },
                            roomId = selectedRoomId,
                            roomNo = roomNo.ifBlank { null },
                            invigilatorEmail = invigilatorEmail.ifBlank { null },
                        ),
                    )
                },
                enabled = !busy && startTimeValid && endTimeValid && timesConsistent,
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun MasterDatesheetEmptyCard(title: String, detail: String) {
    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(detail, color = ModMuted, style = MaterialTheme.typography.bodySmall)
        }
    }
}
