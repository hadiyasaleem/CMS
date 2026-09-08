package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Building
import com.mbd.cmscommon.domain.model.Datesheet
import com.mbd.cmscommon.domain.model.DatesheetCurriculumDrift
import com.mbd.cmscommon.domain.model.DatesheetScheduleQuality
import com.mbd.cmscommon.domain.model.DatesheetSlot
import com.mbd.cmscommon.domain.model.DatesheetViewerContext
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.Room
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.domain.model.datesheetLabel
import com.mbd.cmscommon.domain.model.isAssignedTo
import com.mbd.cmscommon.domain.model.locationLabel
import com.mbd.cmscommon.domain.model.resolvedEndTime
import com.mbd.cmscommon.domain.model.resolvedStartTime
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmscommon.ui.theme.ModAccent
import com.mbd.cmscommon.ui.theme.ModGround
import com.mbd.cmscommon.ui.theme.ModInk
import com.mbd.cmscommon.ui.theme.ModMuted
import com.mbd.cmscommon.ui.theme.ModSurface
import com.mbd.cmscommon.ui.theme.ModTrack
import com.mbd.cmscommon.ui.theme.ModWarn
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val DatesheetCanvas = ModGround
private val DatesheetGold = ModWarn
private val DatesheetRed = ModAccent
private val DATESHEET_DATE_FORMAT = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy")

enum class DatesheetViewMode { FILTERED, GROUPED, CALENDAR, SEMESTER }

/** Everything needed to render/edit the currently-open datesheet's detail. */
data class DatesheetDetailData(
    val sheet: Datesheet,
    val session: AcademicSession?,
    val department: Department?,
    val slots: List<DatesheetSlot>,
    val quality: DatesheetScheduleQuality?,
    val drift: DatesheetCurriculumDrift?,
    val buildings: List<Building>,
    val rooms: List<Room>,
    val teachers: List<Teacher>,
)

@Composable
fun DatesheetWorkspace(
    viewer: DatesheetViewerContext,
    departments: List<Department>,
    sessions: List<AcademicSession>,
    datesheets: List<Datesheet>,
    allSlots: List<DatesheetSlot>,
    selectedDeptId: String?,
    selectedStartYear: Int?,
    selectedShift: Session?,
    sessionsInDepartment: List<AcademicSession>,
    shiftsForSelection: List<Session>,
    resolvedSession: AcademicSession?,
    onSelectDepartment: (String?) -> Unit,
    onSelectStartYear: (Int?) -> Unit,
    onSelectShift: (Session?) -> Unit,
    buildings: List<Building>,
    loading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    onCreateDatesheet: (defaultStart: String?, defaultEnd: String?, defaultBuildingId: String?, instructions: String?) -> Unit,
    openDatesheetId: String?,
    onOpenDatesheet: (String?) -> Unit,
    detail: DatesheetDetailData?,
    detailBusy: Boolean,
    detailErrorMessage: String?,
    onSetPublished: (Boolean) -> Unit,
    onDeleteDatesheet: () -> Unit,
    onSyncMissingSubjects: () -> Unit,
    onRemovePaper: (String) -> Unit,
    onUpdatePaper: (DatesheetSlot) -> Unit,
    modifier: Modifier = Modifier,
) {
    var viewMode by remember { mutableStateOf(DatesheetViewMode.FILTERED) }
    val slotsByDatesheet = remember(allSlots) { allSlots.groupBy { it.datesheetId } }
    val sessionsById = remember(sessions) { sessions.associateBy { it.sessionId } }

    Box(modifier.fillMaxWidth()) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth().background(DatesheetCanvas),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { DatesheetHeader() }
            if (!errorMessage.isNullOrBlank()) {
                item { CmsNotice(errorMessage, tone = NoticeTone.Error, actionLabel = "Retry", onAction = onRetry) }
            }
            item {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CmsChip("Filtered", selected = viewMode == DatesheetViewMode.FILTERED, onClick = { viewMode = DatesheetViewMode.FILTERED })
                    CmsChip("Grouped", selected = viewMode == DatesheetViewMode.GROUPED, onClick = { viewMode = DatesheetViewMode.GROUPED })
                    CmsChip("Calendar", selected = viewMode == DatesheetViewMode.CALENDAR, onClick = { viewMode = DatesheetViewMode.CALENDAR })
                    CmsChip("Semester", selected = viewMode == DatesheetViewMode.SEMESTER, onClick = { viewMode = DatesheetViewMode.SEMESTER })
                }
            }
            if (loading) {
                item { SkeletonRow() }
            } else {
                item {
                    when (viewMode) {
                        DatesheetViewMode.FILTERED -> FilteredDatesheetView(
                            sessionsInDepartment = sessionsInDepartment,
                            shiftsForSelection = shiftsForSelection,
                            selectedDeptId = selectedDeptId,
                            selectedStartYear = selectedStartYear,
                            selectedShift = selectedShift,
                            resolvedSession = resolvedSession,
                            datesheets = datesheets,
                            departments = departments,
                            buildings = buildings,
                            busy = detailBusy,
                            onSelectDepartment = onSelectDepartment,
                            onSelectStartYear = onSelectStartYear,
                            onSelectShift = onSelectShift,
                            onOpenDatesheet = { onOpenDatesheet(it) },
                            onCreateDatesheet = onCreateDatesheet,
                        )
                        DatesheetViewMode.GROUPED -> GroupedDatesheetView(
                            departments = departments,
                            sessions = sessions,
                            datesheets = datesheets,
                            onOpenDatesheet = { onOpenDatesheet(it) },
                        )
                        DatesheetViewMode.CALENDAR -> CalendarDatesheetView(
                            datesheets = datesheets,
                            sessionsById = sessionsById,
                            departments = departments,
                            slotsByDatesheet = slotsByDatesheet,
                            onOpenDatesheet = { onOpenDatesheet(it) },
                        )
                        DatesheetViewMode.SEMESTER -> SemesterDatesheetView(
                            resolvedSession = resolvedSession,
                            departments = departments,
                            sessionsInDepartment = sessionsInDepartment,
                            shiftsForSelection = shiftsForSelection,
                            selectedDeptId = selectedDeptId,
                            selectedStartYear = selectedStartYear,
                            selectedShift = selectedShift,
                            datesheets = datesheets,
                            slotsByDatesheet = slotsByDatesheet,
                            onSelectDepartment = onSelectDepartment,
                            onSelectStartYear = onSelectStartYear,
                            onSelectShift = onSelectShift,
                            onOpenDatesheet = { onOpenDatesheet(it) },
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(72.dp)) }
        }
    }

    if (openDatesheetId != null && detail != null) {
        DatesheetDetailDialog(
            detail = detail,
            viewer = viewer,
            busy = detailBusy,
            errorMessage = detailErrorMessage,
            onDismiss = { onOpenDatesheet(null) },
            onSetPublished = onSetPublished,
            onDeleteDatesheet = onDeleteDatesheet,
            onSyncMissingSubjects = onSyncMissingSubjects,
            onRemovePaper = onRemovePaper,
            onUpdatePaper = onUpdatePaper,
        )
    }
}

/** The read-only, single-datesheet view students get: no filters, no view switcher. */
@Composable
fun StudentDatesheetWorkspace(
    sheet: Datesheet?,
    session: AcademicSession?,
    slots: List<DatesheetSlot>,
    loading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth().background(DatesheetCanvas),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { DatesheetHeader() }
        if (!errorMessage.isNullOrBlank()) {
            item { CmsNotice(errorMessage, tone = NoticeTone.Error, actionLabel = "Retry", onAction = onRetry) }
        }
        if (loading) {
            item { SkeletonRow() }
        } else if (sheet == null) {
            item {
                Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
                    Text(
                        "Your Mid Term datesheet hasn't been published yet.",
                        modifier = Modifier.padding(24.dp),
                        color = ModMuted,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        } else {
            item { Text(datesheetLabel(sheet, session, null), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) }
            items(slots.sortedWith(compareBy { it.examDate ?: "9999-99-99" }), key = { it.id }) { slot ->
                PaperRow(slot = slot, sheet = sheet, canManage = false, identityKey = null, onEdit = {}, onRemove = {})
            }
        }
        item { Spacer(Modifier.height(72.dp)) }
    }
}

@Composable
private fun DatesheetHeader() {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), color = ModInk) {
        Column(Modifier.padding(20.dp)) {
            Text("EXAM DATESHEETS", color = DatesheetGold, style = CmsTextStyles.eyebrow)
            Spacer(Modifier.height(6.dp))
            Text("Datesheets", color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            Text("Mid Term schedules by department, session, and semester.", color = CmsTheme.colors.onInkMuted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun DatesheetFilterRow(
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

    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        DropdownChip(
            selectedLabel = departmentOptions.firstOrNull { it.id == selectedDeptId }?.label,
            emptyLabel = "All departments",
            options = departmentOptions,
            onSelected = onSelectDepartment,
        )
        DropdownChip(
            selectedLabel = selectedStartYear?.let { "$it–${it + 4}" },
            emptyLabel = "All sessions",
            options = sessionOptions,
            onSelected = { onSelectStartYear(it?.toIntOrNull()) },
            enabled = selectedDeptId != null,
        )
        DropdownChip(
            selectedLabel = selectedShift?.name,
            emptyLabel = "All shifts",
            options = shiftOptions,
            onSelected = { onSelectShift(it?.let(Session::valueOf)) },
            enabled = selectedStartYear != null,
        )
    }
}

@Composable
private fun FilteredDatesheetView(
    departments: List<Department>,
    sessionsInDepartment: List<AcademicSession>,
    shiftsForSelection: List<Session>,
    selectedDeptId: String?,
    selectedStartYear: Int?,
    selectedShift: Session?,
    resolvedSession: AcademicSession?,
    datesheets: List<Datesheet>,
    buildings: List<Building>,
    busy: Boolean,
    onSelectDepartment: (String?) -> Unit,
    onSelectStartYear: (Int?) -> Unit,
    onSelectShift: (Session?) -> Unit,
    onOpenDatesheet: (String) -> Unit,
    onCreateDatesheet: (String?, String?, String?, String?) -> Unit,
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    Column {
        DatesheetFilterRow(departments, sessionsInDepartment, shiftsForSelection, selectedDeptId, selectedStartYear, selectedShift, onSelectDepartment, onSelectStartYear, onSelectShift)
        Spacer(Modifier.height(12.dp))
        // No manual semester picker -- a datesheet is always for whichever semester the session is
        // currently in, same as timetable periods and marks entry.
        when {
            resolvedSession == null -> {
                Text("Choose a department, session, and shift to view or create a datesheet.", color = ModMuted, style = MaterialTheme.typography.bodyMedium)
            }
            else -> {
                val semester = resolvedSession.currentSemester
                val existing = datesheets.firstOrNull { it.sessionId == resolvedSession.sessionId && it.semester == semester }
                if (existing != null) {
                    DatesheetSummaryTile(existing, resolvedSession, onClick = { onOpenDatesheet(existing.id) })
                } else if (!resolvedSession.isActive) {
                    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
                        Column(Modifier.padding(16.dp)) {
                            Text("This session has graduated.", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(4.dp))
                            Text("New Mid Term datesheets can no longer be created for it.", color = ModMuted, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                } else {
                    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
                        Column(Modifier.padding(16.dp)) {
                            Text("No Mid Term datesheet yet for Semester $semester (the session's current semester).", style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(10.dp))
                            CmsPrimaryButton(text = "Create datesheet", onClick = { showCreateDialog = true })
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog && resolvedSession != null) {
        CreateDatesheetDialog(
            session = resolvedSession,
            semester = resolvedSession.currentSemester,
            buildings = buildings,
            busy = busy,
            onDismiss = { showCreateDialog = false },
            onConfirm = { start, end, buildingId, instructions ->
                onCreateDatesheet(start, end, buildingId, instructions)
                showCreateDialog = false
            },
        )
    }
}

@Composable
private fun DatesheetSummaryTile(sheet: Datesheet, session: AcademicSession, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = ModSurface,
        border = BorderStroke(1.dp, ModTrack),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Semester ${sheet.semester}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text("${session.label} · ${session.shift.name}", color = ModMuted, style = MaterialTheme.typography.bodySmall)
            }
            StatusBadge(if (sheet.published) "PUBLISHED" else "DRAFT", if (sheet.published) BadgeTone.Success else BadgeTone.Neutral)
        }
    }
}

@Composable
private fun GroupedDatesheetView(
    departments: List<Department>,
    sessions: List<AcademicSession>,
    datesheets: List<Datesheet>,
    onOpenDatesheet: (String) -> Unit,
) {
    var expandedDeptId by remember { mutableStateOf<String?>(null) }
    var expandedSessionId by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        departments.sortedBy { it.name }.forEach { dept ->
            val deptSessions = sessions.filter { it.deptId == dept.deptId }
            val deptSheetCount = datesheets.count { sheet -> deptSessions.any { it.sessionId == sheet.sessionId } }
            Surface(shape = RoundedCornerShape(14.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
                Column(Modifier.padding(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clickable { expandedDeptId = if (expandedDeptId == dept.deptId) null else dept.deptId }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(dept.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Text("$deptSheetCount datesheet(s)", color = ModMuted, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    if (expandedDeptId == dept.deptId) {
                        deptSessions.sortedByDescending { it.startYear }.forEach { session ->
                            val sessionSheets = datesheets.filter { it.sessionId == session.sessionId }.sortedBy { it.semester }
                            Column(Modifier.padding(start = 12.dp, bottom = 8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                        .clickable { expandedSessionId = if (expandedSessionId == session.sessionId) null else session.sessionId }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        listOfNotNull(dept.code, "Semester ${session.currentSemester}", session.shift.name).joinToString(" · "),
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                    Text("${sessionSheets.size}", color = ModMuted, style = MaterialTheme.typography.bodySmall)
                                }
                                if (expandedSessionId == session.sessionId) {
                                    if (sessionSheets.isEmpty()) {
                                        Text("No datesheets yet.", color = ModMuted, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 12.dp))
                                    } else {
                                        Column(Modifier.padding(start = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            sessionSheets.forEach { sheet -> DatesheetSummaryTile(sheet, session, onClick = { onOpenDatesheet(sheet.id) }) }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDatesheetView(
    datesheets: List<Datesheet>,
    sessionsById: Map<String, AcademicSession>,
    departments: List<Department>,
    slotsByDatesheet: Map<String, List<DatesheetSlot>>,
    onOpenDatesheet: (String) -> Unit,
) {
    data class Entry(val rawDate: String, val column: String, val cell: GridCell, val datesheetId: String)

    val departmentsById = departments.associateBy { it.deptId }
    val entries = datesheets.flatMap { sheet ->
        val session = sessionsById[sheet.sessionId]
        val deptCode = session?.let { departmentsById[it.deptId]?.code }
        val column = listOfNotNull(deptCode, "Semester ${sheet.semester}", session?.shift?.name).joinToString(" · ")
            .ifBlank { sheet.sessionId }
        slotsByDatesheet[sheet.id].orEmpty().mapNotNull { slot ->
            val date = slot.examDate ?: return@mapNotNull null
            Entry(date, column, GridCell(title = slot.subjectName, subtitle = paperLocationLabel(slot, sheet), meta = paperTimeLabel(slot, sheet)), sheet.id)
        }
    }
    if (entries.isEmpty()) {
        val totalPapers = datesheets.sumOf { sheet -> slotsByDatesheet[sheet.id].orEmpty().size }
        val message = if (totalPapers == 0) {
            "No datesheets exist yet."
        } else {
            "$totalPapers paper(s) are waiting to be scheduled -- open a datesheet from the Filtered or Grouped view to set their dates."
        }
        Text(message, color = ModMuted, style = MaterialTheme.typography.bodyMedium)
        return
    }

    val sessionLabels = entries.map { it.column }.distinct().sorted()
    val rawDates = entries.map { it.rawDate }.distinct().sortedBy { runCatching { LocalDate.parse(it) }.getOrDefault(LocalDate.MAX) }
    val dateColumns = rawDates.map { formatExamDate(it) }
    val rawToFormatted = rawDates.zip(dateColumns).toMap()
    val byKey = entries.associateBy { it.column to rawToFormatted[it.rawDate] }
    val rows = sessionLabels.map { session -> GridRow(key = session, label = session, cells = dateColumns.associateWith { col -> byKey[session to col]?.cell }) }

    TimetableGrid(
        timeSlots = dateColumns,
        rows = rows,
        identityHeader = "SESSION",
        onCellClick = { rowKey, colKey -> byKey[rowKey to colKey]?.let { onOpenDatesheet(it.datesheetId) } },
    )
}

@Composable
private fun SemesterDatesheetView(
    resolvedSession: AcademicSession?,
    departments: List<Department>,
    sessionsInDepartment: List<AcademicSession>,
    shiftsForSelection: List<Session>,
    selectedDeptId: String?,
    selectedStartYear: Int?,
    selectedShift: Session?,
    datesheets: List<Datesheet>,
    slotsByDatesheet: Map<String, List<DatesheetSlot>>,
    onSelectDepartment: (String?) -> Unit,
    onSelectStartYear: (Int?) -> Unit,
    onSelectShift: (Session?) -> Unit,
    onOpenDatesheet: (String) -> Unit,
) {
    Column {
        DatesheetFilterRow(departments, sessionsInDepartment, shiftsForSelection, selectedDeptId, selectedStartYear, selectedShift, onSelectDepartment, onSelectStartYear, onSelectShift)
        Spacer(Modifier.height(12.dp))

        if (resolvedSession == null) {
            if (selectedDeptId == null) {
                Text("Choose a department, session, and shift to view its semester grid.", color = ModMuted, style = MaterialTheme.typography.bodyMedium)
                return@Column
            }
            // A department without a fully resolved session -- show every matching session's own
            // current-semester schedule as its own titled section instead of demanding a single pick.
            val candidateSessions = sessionsInDepartment
                .filter { selectedStartYear == null || it.startYear == selectedStartYear }
                .filter { selectedShift == null || it.shift == selectedShift }
                .sortedWith(compareByDescending<AcademicSession> { it.startYear }.thenBy { it.shift.name })
            if (candidateSessions.isEmpty()) {
                Text("This department has no sessions yet.", color = ModMuted, style = MaterialTheme.typography.bodyMedium)
                return@Column
            }
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                candidateSessions.forEach { session ->
                    SessionCurrentSemesterSection(session, departments, datesheets, slotsByDatesheet, onOpenDatesheet)
                }
            }
            return@Column
        }

        data class Entry(val semester: Int, val rawDate: String, val cell: GridCell, val datesheetId: String)

        val sessionSheets = datesheets.filter { it.sessionId == resolvedSession.sessionId }
        val sessionSlots = sessionSheets.flatMap { sheet -> slotsByDatesheet[sheet.id].orEmpty() }
        val entries = sessionSheets.flatMap { sheet ->
            slotsByDatesheet[sheet.id].orEmpty().mapNotNull { slot ->
                val date = slot.examDate ?: return@mapNotNull null
                Entry(sheet.semester, date, GridCell(title = slot.subjectName, subtitle = paperLocationLabel(slot, sheet), meta = paperTimeLabel(slot, sheet)), sheet.id)
            }
        }
        if (entries.isEmpty()) {
            val message = if (sessionSlots.isEmpty()) {
                "No datesheets yet for this session."
            } else {
                "${sessionSlots.size} paper(s) are waiting to be scheduled -- open a semester's datesheet from the Filtered or Grouped view to set their dates."
            }
            Text(message, color = ModMuted, style = MaterialTheme.typography.bodyMedium)
            return@Column
        }

        val rawDates = entries.map { it.rawDate }.distinct().sortedBy { runCatching { LocalDate.parse(it) }.getOrDefault(LocalDate.MAX) }
        val dateColumns = rawDates.map { formatExamDate(it) }
        val rawToFormatted = rawDates.zip(dateColumns).toMap()
        val byKey = entries.associateBy { it.semester.toString() to rawToFormatted[it.rawDate] }
        val rows = (1..8).mapNotNull { semester ->
            if (entries.none { it.semester == semester }) return@mapNotNull null
            GridRow(key = semester.toString(), label = "Semester $semester", cells = dateColumns.associateWith { col -> byKey[semester.toString() to col]?.cell })
        }

        TimetableGrid(
            timeSlots = dateColumns,
            rows = rows,
            identityHeader = "SEM",
            onCellClick = { rowKey, colKey -> byKey[rowKey to colKey]?.let { onOpenDatesheet(it.datesheetId) } },
        )
    }
}

/** One session's own current-semester exam grid, titled with the session so several can sit under one department. */
@Composable
private fun SessionCurrentSemesterSection(
    session: AcademicSession,
    departments: List<Department>,
    datesheets: List<Datesheet>,
    slotsByDatesheet: Map<String, List<DatesheetSlot>>,
    onOpenDatesheet: (String) -> Unit,
) {
    val semester = session.currentSemester
    val sheet = datesheets.firstOrNull { it.sessionId == session.sessionId && it.semester == semester }
    val deptCode = departments.firstOrNull { it.deptId == session.deptId }?.code

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                listOfNotNull(deptCode, "Semester $semester", session.shift.name).joinToString(" · "),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f),
            )
            if (sheet != null) {
                StatusBadge(if (sheet.published) "PUBLISHED" else "DRAFT", if (sheet.published) BadgeTone.Success else BadgeTone.Neutral)
            }
        }
        Spacer(Modifier.height(8.dp))

        if (sheet == null) {
            Text("No Mid Term datesheet yet for this semester.", color = ModMuted, style = MaterialTheme.typography.bodySmall)
            return@Column
        }

        data class Entry(val rawDate: String, val cell: GridCell)

        val slots = slotsByDatesheet[sheet.id].orEmpty()
        val entries = slots.mapNotNull { slot ->
            val date = slot.examDate ?: return@mapNotNull null
            Entry(date, GridCell(title = slot.subjectName, subtitle = paperLocationLabel(slot, sheet), meta = paperTimeLabel(slot, sheet)))
        }
        if (entries.isEmpty()) {
            val message = if (slots.isEmpty()) {
                "No papers yet -- open this datesheet to add some."
            } else {
                "${slots.size} paper(s) are waiting to be scheduled -- open this datesheet to set their dates."
            }
            Text(
                message,
                color = ModMuted,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.clickable { onOpenDatesheet(sheet.id) },
            )
            return@Column
        }

        val rawDates = entries.map { it.rawDate }.distinct().sortedBy { runCatching { LocalDate.parse(it) }.getOrDefault(LocalDate.MAX) }
        val dateColumns = rawDates.map { formatExamDate(it) }
        val rawToFormatted = rawDates.zip(dateColumns).toMap()
        val byColumn = entries.associateBy { rawToFormatted[it.rawDate] }
        val rows = listOf(GridRow(key = sheet.id, label = "Semester $semester", cells = dateColumns.associateWith { col -> byColumn[col]?.cell }))

        TimetableGrid(
            timeSlots = dateColumns,
            rows = rows,
            identityHeader = "SEM",
            onCellClick = { _, _ -> onOpenDatesheet(sheet.id) },
        )
    }
}

@Composable
private fun DatesheetDetailDialog(
    detail: DatesheetDetailData,
    viewer: DatesheetViewerContext,
    busy: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSetPublished: (Boolean) -> Unit,
    onDeleteDatesheet: () -> Unit,
    onSyncMissingSubjects: () -> Unit,
    onRemovePaper: (String) -> Unit,
    onUpdatePaper: (DatesheetSlot) -> Unit,
) {
    var editingSlot by remember { mutableStateOf<DatesheetSlot?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }
    val sheet = detail.sheet

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(datesheetLabel(sheet, detail.session, detail.department)) },
        text = {
            Column(Modifier.heightIn(max = 520.dp).verticalScroll(rememberScrollState())) {
                StatusBadge(if (sheet.published) "PUBLISHED" else "DRAFT", if (sheet.published) BadgeTone.Success else BadgeTone.Neutral)
                Spacer(Modifier.height(10.dp))
                if (viewer.canManage) {
                    detail.quality?.issues?.takeIf { it.isNotEmpty() }?.let { issues ->
                        DatesheetIssuesCard(issues)
                        Spacer(Modifier.height(8.dp))
                    }
                    detail.drift?.takeIf { !it.isClean }?.let { drift ->
                        CmsNotice(
                            buildString {
                                if (drift.missingSubjects.isNotEmpty()) append("${drift.missingSubjects.size} curriculum subject(s) missing a paper. ")
                                if (drift.staleSlots.isNotEmpty()) append("${drift.staleSlots.size} scheduled paper(s) no longer in the curriculum.")
                            },
                            tone = NoticeTone.Warning,
                            actionLabel = if (drift.missingSubjects.isNotEmpty()) "Add missing" else null,
                            onAction = if (drift.missingSubjects.isNotEmpty()) onSyncMissingSubjects else null,
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
                detail.slots.sortedWith(compareBy { it.examDate ?: "9999-99-99" }).forEach { slot ->
                    PaperRow(
                        slot = slot,
                        sheet = sheet,
                        canManage = viewer.canManage,
                        identityKey = viewer.identityKey,
                        onEdit = { editingSlot = slot },
                        onRemove = { onRemovePaper(slot.id) },
                    )
                    Spacer(Modifier.height(8.dp))
                }
                if (viewer.canManage) {
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            onClick = { onSetPublished(!sheet.published) },
                            enabled = !busy && (sheet.published || detail.quality?.canPublish == true),
                        ) { Text(if (sheet.published) "Unpublish" else "Publish") }
                        TextButton(onClick = { confirmDelete = true }, enabled = !busy) { Text("Delete", color = DatesheetRed) }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )

    editingSlot?.let { slot ->
        PaperEditorDialog(
            slot = slot,
            sheet = sheet,
            buildings = detail.buildings,
            rooms = detail.rooms,
            teachers = detail.teachers,
            busy = busy,
            errorMessage = errorMessage,
            onDismiss = { editingSlot = null },
            onSave = { updated -> onUpdatePaper(updated) },
        )
    }

    if (confirmDelete) {
        ConfirmDestructiveActionDialog(
            title = "Delete datesheet",
            dependentSummary = "This permanently removes the datesheet and every scheduled paper in it.",
            onConfirm = { confirmDelete = false; onDeleteDatesheet(); onDismiss() },
            onDismiss = { confirmDelete = false },
        )
    }
}

@Composable
private fun DatesheetIssuesCard(issues: List<String>) {
    Surface(shape = RoundedCornerShape(14.dp), color = DatesheetGold.copy(alpha = 0.1f), border = BorderStroke(1.dp, DatesheetGold.copy(alpha = 0.3f))) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.WarningAmber, contentDescription = null, tint = DatesheetGold)
                Spacer(Modifier.width(8.dp))
                Text("NEEDS REVIEW", color = DatesheetGold, style = CmsTextStyles.eyebrow)
            }
            Spacer(Modifier.height(6.dp))
            issues.forEach { issue ->
                Text("· $issue", color = ModMuted, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun PaperRow(slot: DatesheetSlot, sheet: Datesheet, canManage: Boolean, identityKey: String?, onEdit: () -> Unit, onRemove: () -> Unit) {
    Surface(shape = RoundedCornerShape(14.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(slot.subjectName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text(slot.courseCode, color = ModMuted, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(4.dp))
                Text(formatExamDate(slot.examDate), style = MaterialTheme.typography.bodySmall)
                Text(paperTimeLabel(slot, sheet), color = ModMuted, style = MaterialTheme.typography.bodySmall)
                Text(paperLocationLabel(slot, sheet), color = ModMuted, style = MaterialTheme.typography.bodySmall)
                val invigilator = slot.invigilatorEmail
                if (!invigilator.isNullOrBlank()) {
                    Text(invigilator, color = if (isAssignedTo(slot, identityKey)) DatesheetGold else ModMuted, style = MaterialTheme.typography.bodySmall)
                }
                if (isAssignedTo(slot, identityKey)) {
                    Spacer(Modifier.height(4.dp))
                    StatusBadge("MY DUTY", BadgeTone.Gold)
                }
            }
            if (canManage) {
                Column {
                    TextButton(onClick = onEdit) { Text("Edit") }
                    TextButton(onClick = onRemove) { Text("Remove") }
                }
            }
        }
    }
}

@Composable
private fun CreateDatesheetDialog(
    session: AcademicSession,
    semester: Int,
    buildings: List<Building>,
    busy: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (defaultStart: String?, defaultEnd: String?, defaultBuildingId: String?, instructions: String?) -> Unit,
) {
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var selectedBuildingId by remember { mutableStateOf<String?>(null) }
    var instructions by remember { mutableStateOf("") }

    val startTimeValid = startTime.isBlank() || runCatching { LocalTime.parse(startTime) }.isSuccess
    val endTimeValid = endTime.isBlank() || runCatching { LocalTime.parse(endTime) }.isSuccess
    val timesConsistent = startTime.isBlank() == endTime.isBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Mid Term datesheet") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text("${session.label} · ${session.shift.name} · Semester $semester", color = ModMuted, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(10.dp))
                Text("Papers will be prefilled for every subject in this semester's curriculum.", color = ModMuted, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CmsTimeField(value = startTime, onValueChange = { startTime = it }, label = "Default start", modifier = Modifier.weight(1f))
                    CmsTimeField(value = endTime, onValueChange = { endTime = it }, label = "Default end", modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
                CmsEntityPicker(
                    label = "Default building",
                    selectedId = selectedBuildingId,
                    options = buildings.map { CmsEntityOption(it.buildingId, it.name) },
                    onSelected = { selectedBuildingId = it },
                    optional = true,
                    emptyLabel = "None",
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("Instructions (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(startTime.ifBlank { null }, endTime.ifBlank { null }, selectedBuildingId, instructions.ifBlank { null }) },
                enabled = !busy && startTimeValid && endTimeValid && timesConsistent,
            ) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun PaperEditorDialog(
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
    var selectedRoomId by remember { mutableStateOf(slot.roomId) }
    var roomNo by remember { mutableStateOf(slot.roomNo ?: "") }
    var invigilatorEmail by remember { mutableStateOf(slot.invigilatorEmail ?: "") }

    // The save itself is fire-and-forget (updatePaper runs async on the controller), so this dialog
    // can't just close on click -- it has to wait for busy to flip back off and check whether that
    // landed with an error. Only saveAttempted's own outcome is shown, not a leftover error from
    // something else that happened before this dialog opened.
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
    val timesConsistent = !overrideTime || startTime.isBlank() == endTime.isBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(slot.subjectName) },
        text = {
            Column(Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState())) {
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
                        CmsTimeField(value = endTime, onValueChange = { endTime = it }, label = "End", modifier = Modifier.weight(1f))
                    }
                } else if (sheet.defaultStartTime != null && sheet.defaultEndTime != null) {
                    Text("Uses the datesheet default: ${sheet.defaultStartTime}–${sheet.defaultEndTime}", color = ModMuted, style = MaterialTheme.typography.bodySmall)
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
                if (selectedBuildingId == null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "A building is needed before this paper counts as scheduled and the datesheet can publish. You can still save without one for now.",
                        color = DatesheetGold,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
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

private fun formatExamDate(date: String?): String =
    date?.let { runCatching { LocalDate.parse(it).format(DATESHEET_DATE_FORMAT) }.getOrNull() } ?: "Not scheduled"

private fun paperTimeLabel(slot: DatesheetSlot, sheet: Datesheet): String {
    val start = slot.resolvedStartTime(sheet)
    val end = slot.resolvedEndTime(sheet)
    return if (start != null && end != null) "$start–$end" else "Time not set"
}

/**
 * [locationLabel] only reads the paper's own building/room text, which stays blank whenever a
 * paper is still relying on the datesheet's default building -- that made every freshly prefilled
 * paper wrongly read "No room assigned" even when a default building was set. Say so instead of
 * claiming there's no room at all.
 */
private fun paperLocationLabel(slot: DatesheetSlot, sheet: Datesheet): String {
    if (slot.building != null || slot.roomNo != null) return locationLabel(slot)
    return if (sheet.defaultBuildingId != null) "Uses the datesheet's default building" else "No building assigned"
}
