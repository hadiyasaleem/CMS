package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Datesheet
import com.mbd.cmscommon.domain.model.DatesheetDraft
import com.mbd.cmscommon.domain.model.DatesheetSlot
import com.mbd.cmscommon.domain.model.DatesheetViewerContext
import com.mbd.cmscommon.domain.model.SemesterSubject
import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.domain.model.datesheetDutySummary
import com.mbd.cmscommon.domain.model.datesheetKey
import com.mbd.cmscommon.domain.model.datesheetScheduleQuality
import com.mbd.cmscommon.domain.model.isAssignedTo
import com.mbd.cmscommon.domain.model.isVisibleTo
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmscommon.ui.theme.ModGround
import com.mbd.cmscommon.ui.theme.ModInk
import com.mbd.cmscommon.ui.theme.ModMuted
import com.mbd.cmscommon.ui.theme.ModRedTint
import com.mbd.cmscommon.ui.theme.ModSuccess
import com.mbd.cmscommon.ui.theme.ModSurface
import com.mbd.cmscommon.ui.theme.ModTrack
import com.mbd.cmscommon.ui.theme.ModWarn
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DatesheetCanvas = ModGround
private val DatesheetGreen = ModSuccess
private val DatesheetGold = ModWarn
private val DatesheetNavy = ModInk
private val DatesheetDateFormat = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy")

private val EXAM_TYPES = listOf("MIDTERM", "SESSIONAL")

enum class SheetStatus(val label: String) {
    ALL("All schedules"),
    PUBLISHED("Published"),
    DRAFT("Drafts"),
    NEEDS_REVIEW("Needs review"),
}

@Composable
fun DatesheetWorkspace(
    datesheets: List<Datesheet>,
    slots: Map<String, List<DatesheetSlot>>,
    loadingSlots: Set<String>,
    sessions: List<AcademicSession>,
    subjectsBySession: Map<String, List<SemesterSubject>>,
    invigilators: List<Teacher>,
    viewer: DatesheetViewerContext,
    loading: Boolean,
    busy: Boolean,
    errorMessage: String?,
    actionMessage: String?,
    onRetry: () -> Unit,
    onLoadSlots: (String) -> Unit,
    onLoadSubjects: (String) -> Unit,
    onCreate: (DatesheetDraft) -> Unit,
    onUpdate: (String, DatesheetDraft) -> Unit,
    onSetPublished: (String, Boolean) -> Unit,
    onDelete: (String) -> Unit,
    onAddSlot: (DatesheetSlot) -> Unit,
    onUpdateSlot: (DatesheetSlot) -> Unit,
    onDeleteSlot: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(SheetStatus.ALL) }
    var selectedSessionId by remember { mutableStateOf<String?>(null) }
    var expandedId by remember { mutableStateOf<String?>(null) }
    var showCreate by remember { mutableStateOf(false) }
    var editingSheet by remember { mutableStateOf<Datesheet?>(null) }
    var editingSlot by remember { mutableStateOf<Pair<String, DatesheetSlot?>?>(null) }
    var addingSlotDate by remember { mutableStateOf<String?>(null) }
    var detailSlot by remember { mutableStateOf<Pair<String, DatesheetSlot>?>(null) }
    var pendingDeleteSheet by remember { mutableStateOf<Datesheet?>(null) }
    var pendingDeleteSlot by remember { mutableStateOf<Pair<String, DatesheetSlot>?>(null) }

    val visibleSheets = datesheets.filter { isVisibleTo(it, viewer) }
    val duty = datesheetDutySummary(slots, viewer.identityKey)

    val filtered = visibleSheets.filter { sheet ->
        val sheetSlots = slots[sheet.id].orEmpty()
        val quality = datesheetScheduleQuality(sheet, sheetSlots)
        val matchesQuery = query.isBlank() || sheet.title.contains(query, ignoreCase = true)
        val matchesSession = selectedSessionId == null || sheet.sessionId == selectedSessionId
        val matchesStatus = when (status) {
            SheetStatus.ALL -> true
            SheetStatus.PUBLISHED -> sheet.published
            SheetStatus.DRAFT -> !sheet.published
            SheetStatus.NEEDS_REVIEW -> quality.issues.isNotEmpty()
        }
        matchesQuery && matchesSession && matchesStatus
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth().background(DatesheetCanvas),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { DatesheetHeader(viewer.canManage) { showCreate = true } }

        if (!errorMessage.isNullOrBlank()) {
            item { DatesheetNotice(errorMessage, CmsTheme.colors.accent, action = "Retry", onAction = onRetry) }
        }
        if (!actionMessage.isNullOrBlank()) {
            item { DatesheetNotice(actionMessage, DatesheetGreen, action = null, onAction = null) }
        }

        item { DatesheetSummaryRow(visibleSheets.size, slots.values.sumOf { it.size }, duty.upcomingDuties, visibleSheets.count { !it.published }) }

        item {
            DatesheetFilters(
                query = query,
                onQueryChange = { query = it },
                status = status,
                onStatusChange = { status = it },
                selectedSessionId = selectedSessionId,
                onSessionChange = { selectedSessionId = it },
                sessions = sessions,
            )
        }

        when {
            loading -> items(3) { SkeletonRow() }
            visibleSheets.isEmpty() -> item {
                DatesheetEmpty(
                    filtered = false,
                    canManage = viewer.canManage,
                    onAdd = { showCreate = true },
                )
            }
            filtered.isEmpty() -> item { DatesheetEmpty(filtered = true, canManage = false, onAdd = {}) }
            else -> items(filtered, key = { datesheetKey(it) }) { sheet ->
                val sheetSlots = slots[sheet.id].orEmpty()
                val expanded = expandedId == sheet.id
                DatesheetScheduleCard(
                    sheet = sheet,
                    slots = sheetSlots,
                    expanded = expanded,
                    loadingSlots = sheet.id in loadingSlots,
                    canManage = viewer.canManage,
                    identityKey = viewer.identityKey,
                    onToggle = {
                        expandedId = if (expanded) null else sheet.id
                        if (!expanded) {
                            onLoadSlots(sheet.id)
                            sheet.sessionId?.let(onLoadSubjects)
                        }
                    },
                    onEdit = { editingSheet = sheet },
                    onDelete = { pendingDeleteSheet = sheet },
                    onPublish = { onSetPublished(sheet.id, !sheet.published) },
                    onAddPaper = { editingSlot = sheet.id to null },
                    onAddPaperOnDate = { date -> addingSlotDate = date; editingSlot = sheet.id to null },
                    onEditPaper = { slot -> editingSlot = sheet.id to slot },
                    onViewPaper = { slot -> detailSlot = sheet.id to slot },
                    onDeletePaper = { slot -> pendingDeleteSlot = sheet.id to slot },
                )
            }
        }

        item { Spacer(Modifier.height(72.dp)) }
    }

    if (showCreate || editingSheet != null) {
        DatesheetEditorDialog(
            sheet = editingSheet,
            sessions = sessions,
            busy = busy,
            onDismiss = { showCreate = false; editingSheet = null },
            onSave = { draft ->
                val sheet = editingSheet
                if (sheet != null) onUpdate(sheet.id, draft) else onCreate(draft)
                showCreate = false
                editingSheet = null
            },
        )
    }

    editingSlot?.let { (sheetId, slot) ->
        PaperEditorDialog(
            slot = slot,
            initialDate = addingSlotDate.orEmpty(),
            subjects = subjectsBySession[datesheets.firstOrNull { it.id == sheetId }?.sessionId].orEmpty(),
            invigilators = invigilators,
            busy = busy,
            onDismiss = { editingSlot = null; addingSlotDate = null },
            onSave = { draft ->
                if (slot != null) onUpdateSlot(draft) else onAddSlot(draft.copy(datesheetId = sheetId))
                editingSlot = null
                addingSlotDate = null
            },
        )
    }

    detailSlot?.let { (sheetId, slot) ->
        PaperDetailDialog(
            slot = slot,
            canManage = viewer.canManage,
            isMyDuty = isAssignedTo(slot, viewer.identityKey),
            onEdit = { detailSlot = null; editingSlot = sheetId to slot },
            onRequestRemove = { detailSlot = null; pendingDeleteSlot = sheetId to slot },
            onDismiss = { detailSlot = null },
        )
    }

    pendingDeleteSheet?.let { sheet ->
        ConfirmDestructiveActionDialog(
            title = "Delete datesheet",
            dependentSummary = "This permanently removes \"${sheet.title}\" and every scheduled paper in it.",
            onConfirm = { onDelete(sheet.id); pendingDeleteSheet = null },
            onDismiss = { pendingDeleteSheet = null },
        )
    }

    pendingDeleteSlot?.let { (sheetId, slot) ->
        ConfirmDestructiveActionDialog(
            title = "Remove paper",
            dependentSummary = "The paper will be removed from this datesheet.",
            onConfirm = { onDeleteSlot(sheetId, slot.id); pendingDeleteSlot = null },
            onDismiss = { pendingDeleteSlot = null },
        )
    }
}

@Composable
private fun DatesheetHeader(canManage: Boolean, onAdd: () -> Unit) {
    Surface(shape = RoundedCornerShape(18.dp), color = ModInk) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Exam Datesheets", color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                Text(
                    "Published schedules, venues and paper timings in one place.",
                    color = CmsTheme.colors.onInkMuted,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (canManage) {
                CmsPrimaryButton(text = "New datesheet", onClick = onAdd)
            }
        }
    }
}

@Composable
private fun DatesheetSummaryRow(schedules: Int, papers: Int, upcomingDuties: Int, drafts: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        SummaryTile("Schedules", schedules.toString(), Modifier.weight(1f))
        SummaryTile("Papers loaded", papers.toString(), Modifier.weight(1f))
        SummaryTile("My duties", upcomingDuties.toString(), Modifier.weight(1f))
        SummaryTile("Drafts", drafts.toString(), Modifier.weight(1f))
    }
}

@Composable
private fun SummaryTile(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(14.dp)) {
            Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Text(label.uppercase(Locale.ROOT), color = ModMuted, style = CmsTextStyles.eyebrow)
        }
    }
}

@Composable
private fun DatesheetFilters(
    query: String,
    onQueryChange: (String) -> Unit,
    status: SheetStatus,
    onStatusChange: (SheetStatus) -> Unit,
    selectedSessionId: String?,
    onSessionChange: (String?) -> Unit,
    sessions: List<AcademicSession>,
) {
    Column(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search datesheets") },
            singleLine = true,
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SheetStatus.entries.forEach { option ->
                CmsChip(option.label, selected = status == option, onClick = { onStatusChange(option) })
            }
        }
        if (sessions.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            CmsEntityPicker(
                label = "Academic session",
                selectedId = selectedSessionId,
                options = sessions.map { CmsEntityOption(it.sessionId, "${it.startYear}-${it.endYear} ${it.shift}") },
                onSelected = onSessionChange,
                optional = true,
                emptyLabel = "All sessions",
            )
        }
    }
}

@Composable
private fun DatesheetScheduleCard(
    sheet: Datesheet,
    slots: List<DatesheetSlot>,
    expanded: Boolean,
    loadingSlots: Boolean,
    canManage: Boolean,
    identityKey: String?,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPublish: () -> Unit,
    onAddPaper: () -> Unit,
    onAddPaperOnDate: (String) -> Unit,
    onEditPaper: (DatesheetSlot) -> Unit,
    onViewPaper: (DatesheetSlot) -> Unit,
    onDeletePaper: (DatesheetSlot) -> Unit,
) {
    val quality = datesheetScheduleQuality(sheet, slots)
    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth().clickable(onClick = onToggle), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(sheet.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(
                        (sheet.examType ?: "EXAM").uppercase(Locale.ROOT) + " · ${slots.size} papers",
                        color = ModMuted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                StatusBadge(if (sheet.published) "PUBLISHED" else "DRAFT", if (sheet.published) BadgeTone.Success else BadgeTone.Neutral)
            }
            if (quality.issues.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text("NEEDS REVIEW", color = CmsTheme.colors.accent, style = CmsTextStyles.eyebrow)
            }
            if (canManage) {
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onEdit) { Text("Edit datesheet") }
                    TextButton(onClick = onPublish, enabled = quality.canPublish || sheet.published) {
                        Text(if (sheet.published) "Unpublish" else "Publish")
                    }
                    TextButton(onClick = onDelete) { Text("Delete", color = CmsTheme.colors.accent) }
                }
            }
            if (expanded) {
                Spacer(Modifier.height(10.dp))
                when {
                    loadingSlots -> DatesheetSkeletonCard()
                    slots.isEmpty() -> DatesheetInlineEmpty(canManage, onAddPaper)
                    else -> {
                        val timedSlots = slots.filter { !it.startTime.isNullOrBlank() && !it.endTime.isNullOrBlank() }
                        val untimedSlots = slots - timedSlots.toSet()
                        if (timedSlots.isNotEmpty()) {
                            val dates = timedSlots.map { it.examDate }.distinct().sorted()
                            val timeSlots = timedSlots.map { "${it.startTime}–${it.endTime}" }.distinct().sortedBy { it.substringBefore('–') }
                            val byDateAndTime = timedSlots.associateBy { it.examDate to "${it.startTime}–${it.endTime}" }
                            TimetableGrid(
                                timeSlots = timeSlots,
                                identityHeader = "DATE",
                                rows = dates.map { date ->
                                    val parsed = runCatching { LocalDate.parse(date) }.getOrNull()
                                    GridRow(
                                        key = date,
                                        label = parsed?.format(DatesheetDateFormat) ?: date,
                                        cells = timeSlots.associateWith { timeKey ->
                                            byDateAndTime[date to timeKey]?.let { slot ->
                                                GridCell(
                                                    title = slot.subjectName ?: slot.courseCode ?: "Untitled paper",
                                                    subtitle = locationLabel(slot),
                                                    meta = slot.invigilatorEmail ?: "No invigilator",
                                                    isAlert = isAssignedTo(slot, identityKey),
                                                )
                                            }
                                        },
                                    )
                                },
                                onCellClick = { dateKey, timeKey ->
                                    val existing = byDateAndTime[dateKey to timeKey]
                                    if (existing != null) onViewPaper(existing) else if (canManage) onAddPaperOnDate(dateKey)
                                },
                            )
                        }
                        if (untimedSlots.isNotEmpty()) {
                            if (timedSlots.isNotEmpty()) Spacer(Modifier.height(10.dp))
                            Text("UNSCHEDULED", color = ModMuted, style = CmsTextStyles.eyebrow)
                            Spacer(Modifier.height(6.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                untimedSlots.sortedBy { it.examDate }.forEach { slot ->
                                    PaperCard(
                                        slot = slot,
                                        canManage = canManage,
                                        isMyDuty = isAssignedTo(slot, identityKey),
                                        onEdit = { onEditPaper(slot) },
                                        onDelete = { onDeletePaper(slot) },
                                    )
                                }
                            }
                        }
                    }
                }
                if (canManage && slots.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = onAddPaper) { Text("Add exam paper") }
                }
            }
        }
    }
}

@Composable
private fun PaperCard(slot: DatesheetSlot, canManage: Boolean, isMyDuty: Boolean, onEdit: () -> Unit, onDelete: () -> Unit) {
    val date = runCatching { LocalDate.parse(slot.examDate) }.getOrNull()
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isMyDuty) ModRedTint else ModGround,
        border = if (isMyDuty) BorderStroke(1.dp, DatesheetGold) else null,
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(slot.subjectName ?: slot.courseCode ?: "Untitled paper", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                if (isMyDuty) StatusBadge("MY DUTY", BadgeTone.Gold)
            }
            Text(date?.format(DatesheetDateFormat) ?: slot.examDate, color = DatesheetNavy, style = MaterialTheme.typography.bodySmall)
            MetaLine(timeLabel(slot))
            MetaLine(locationLabel(slot))
            MetaLine(slot.invigilatorEmail ?: "No invigilator assigned")
            if (canManage) {
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onEdit) { Text("Edit") }
                    TextButton(onClick = onDelete) { Text("Remove", color = CmsTheme.colors.accent) }
                }
            }
        }
    }
}

@Composable
private fun PaperDetailDialog(
    slot: DatesheetSlot,
    canManage: Boolean,
    isMyDuty: Boolean,
    onEdit: () -> Unit,
    onRequestRemove: () -> Unit,
    onDismiss: () -> Unit,
) {
    val date = runCatching { LocalDate.parse(slot.examDate) }.getOrNull()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(slot.subjectName ?: slot.courseCode ?: "Exam paper") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (isMyDuty) {
                    StatusBadge("MY DUTY", BadgeTone.Gold)
                    Spacer(Modifier.height(4.dp))
                }
                PaperDetailRow("Date", date?.format(DatesheetDateFormat) ?: slot.examDate)
                PaperDetailRow("Time", timeLabel(slot))
                slot.courseCode?.takeIf { it.isNotBlank() }?.let { PaperDetailRow("Course code", it) }
                PaperDetailRow("Room", locationLabel(slot))
                PaperDetailRow("Invigilator", slot.invigilatorEmail ?: "Not assigned")
            }
        },
        confirmButton = {
            if (canManage) TextButton(onClick = onEdit) { Text("Edit") } else TextButton(onClick = onDismiss) { Text("Close") }
        },
        dismissButton = {
            if (canManage) {
                Row {
                    TextButton(onClick = onRequestRemove) { Text("Remove", color = CmsTheme.colors.accent) }
                    TextButton(onClick = onDismiss) { Text("Close") }
                }
            }
        },
    )
}

@Composable
private fun PaperDetailRow(label: String, value: String) {
    Column {
        Text(label.uppercase(Locale.ROOT), color = ModMuted, style = CmsTextStyles.eyebrow)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun timeLabel(slot: DatesheetSlot): String {
    if (slot.startTime != null && slot.endTime != null) return "${slot.startTime} - ${slot.endTime}"
    if (slot.durationMinutes != null) return "${slot.durationMinutes} minutes"
    return "Time not set"
}

private fun locationLabel(slot: DatesheetSlot): String =
    listOfNotNull(slot.building, slot.roomNo).joinToString(" / ").ifBlank { "No room assigned" }

@Composable
private fun MetaLine(text: String) {
    Text(text, color = ModMuted, style = MaterialTheme.typography.bodySmall)
}

@Composable
private fun DatesheetNotice(message: String, color: Color, action: String?, onAction: (() -> Unit)?) {
    Surface(shape = RoundedCornerShape(14.dp), color = color.copy(alpha = 0.1f), border = BorderStroke(1.dp, color.copy(alpha = 0.25f))) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(message, modifier = Modifier.weight(1f), color = color, style = MaterialTheme.typography.bodyMedium)
            if (action != null && onAction != null) {
                TextButton(onClick = onAction) { Text(action, color = color) }
            }
        }
    }
}

@Composable
private fun DatesheetEmpty(filtered: Boolean, canManage: Boolean, onAdd: () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if (filtered) "No matching schedules" else "No exam schedules yet", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                if (filtered) "Try a different search, status or session filter." else "New datesheets start as drafts. Add at least one paper before publishing.",
                color = ModMuted,
                style = MaterialTheme.typography.bodySmall,
            )
            if (!filtered && canManage) {
                Spacer(Modifier.height(12.dp))
                CmsPrimaryButton(text = "New datesheet", onClick = onAdd)
            }
        }
    }
}

@Composable
private fun DatesheetInlineEmpty(canManage: Boolean, onAdd: () -> Unit) {
    Column(Modifier.padding(vertical = 8.dp)) {
        Text("No papers have been scheduled.", color = ModMuted, style = MaterialTheme.typography.bodyMedium)
        if (canManage) {
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onAdd) { Text("Add exam paper") }
        }
    }
}

@Composable
private fun DatesheetSkeletonCard() {
    SkeletonRow()
}

@Composable
private fun DatesheetEditorDialog(
    sheet: Datesheet?,
    sessions: List<AcademicSession>,
    busy: Boolean,
    onDismiss: () -> Unit,
    onSave: (DatesheetDraft) -> Unit,
) {
    var title by remember { mutableStateOf(sheet?.title ?: "") }
    var examType by remember { mutableStateOf(sheet?.examType ?: "MIDTERM") }
    var sessionId by remember { mutableStateOf(sheet?.sessionId) }
    var instructions by remember { mutableStateOf(sheet?.instructions ?: "") }
    var published by remember { mutableStateOf(sheet?.published ?: false) }

    val draft = DatesheetDraft(title.trim(), examType, sessionId, instructions.trim().ifBlank { null }, published)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (sheet == null) "New datesheet" else "Edit datesheet", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(10.dp))
                Text("EXAM TYPE", color = ModMuted, style = CmsTextStyles.eyebrow)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EXAM_TYPES.forEach { type ->
                        CmsChip(type, selected = examType == type, onClick = { examType = type })
                    }
                }
                Spacer(Modifier.height(10.dp))
                CmsEntityPicker(
                    label = "Academic session",
                    selectedId = sessionId,
                    options = sessions.map { CmsEntityOption(it.sessionId, "${it.startYear}-${it.endYear} ${it.shift}") },
                    onSelected = { sessionId = it },
                    optional = true,
                    emptyLabel = "College wide",
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = instructions, onValueChange = { instructions = it }, label = { Text("Instructions") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                if (sheet != null) {
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Published", modifier = Modifier.weight(1f))
                        Switch(checked = published, onCheckedChange = { published = it })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(draft) }, enabled = title.isNotBlank() && !busy) {
                Text(if (sheet == null) "Create" else "Save changes")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !busy) { Text("Cancel") } },
    )
}

@Composable
private fun PaperEditorDialog(
    slot: DatesheetSlot?,
    initialDate: String = "",
    subjects: List<SemesterSubject>,
    invigilators: List<Teacher>,
    busy: Boolean,
    onDismiss: () -> Unit,
    onSave: (DatesheetSlot) -> Unit,
) {
    var courseCode by remember { mutableStateOf(slot?.courseCode ?: "") }
    var subjectName by remember { mutableStateOf(slot?.subjectName ?: "") }
    var examDate by remember { mutableStateOf(slot?.examDate ?: initialDate) }
    var startTime by remember { mutableStateOf(slot?.startTime ?: "") }
    var endTime by remember { mutableStateOf(slot?.endTime ?: "") }
    var roomNo by remember { mutableStateOf(slot?.roomNo ?: "") }
    var building by remember { mutableStateOf(slot?.building ?: "") }
    var invigilatorEmail by remember { mutableStateOf(slot?.invigilatorEmail) }

    val draft = (slot ?: DatesheetSlot(id = "", datesheetId = "", examDate = "")).copy(
        courseCode = courseCode.trim().ifBlank { null },
        subjectName = subjectName.trim().ifBlank { null },
        examDate = examDate.trim(),
        startTime = startTime.trim().ifBlank { null },
        endTime = endTime.trim().ifBlank { null },
        roomNo = roomNo.trim().ifBlank { null },
        building = building.trim().ifBlank { null },
        invigilatorEmail = invigilatorEmail,
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (slot == null) "Add exam paper" else "Edit exam paper", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column {
                if (subjects.isNotEmpty()) {
                    CmsEntityPicker(
                        label = "Select from curriculum",
                        selectedId = courseCode.ifBlank { null },
                        options = subjects.map { CmsEntityOption(it.courseCode, it.name) },
                        onSelected = { id ->
                            val subject = subjects.firstOrNull { it.courseCode == id }
                            courseCode = subject?.courseCode ?: ""
                            subjectName = subject?.name ?: subjectName
                        },
                        optional = true,
                    )
                    Spacer(Modifier.height(10.dp))
                }
                OutlinedTextField(value = subjectName, onValueChange = { subjectName = it }, label = { Text("Subject") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(10.dp))
                CmsDateField(value = examDate, onValueChange = { examDate = it }, label = "Exam date")
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = startTime, onValueChange = { startTime = it }, label = { Text("Start time") }, placeholder = { Text("HH:MM") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = endTime, onValueChange = { endTime = it }, label = { Text("End time") }, placeholder = { Text("HH:MM") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = roomNo, onValueChange = { roomNo = it }, label = { Text("Room") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = building, onValueChange = { building = it }, label = { Text("Building") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                Spacer(Modifier.height(10.dp))
                CmsEntityPicker(
                    label = "Select invigilator",
                    selectedId = invigilatorEmail,
                    options = invigilators.map { CmsEntityOption(it.email, it.name) },
                    onSelected = { invigilatorEmail = it },
                    optional = true,
                    emptyLabel = "No invigilator assigned",
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(draft) }, enabled = examDate.isNotBlank() && !busy) {
                Text(if (slot == null) "Add paper" else "Save changes")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !busy) { Text("Cancel") } },
    )
}
