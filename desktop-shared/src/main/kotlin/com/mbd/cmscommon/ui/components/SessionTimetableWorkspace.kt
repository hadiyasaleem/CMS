package com.mbd.cmscommon.ui.components

import com.mbd.cmscommon.controller.periodsForShift
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.util.clockDisplay
import com.mbd.cmscommon.util.isDateRangeReversed
import com.mbd.cmscommon.export.ExportDocument
import com.mbd.cmscommon.export.ExportFormat
import com.mbd.cmscommon.export.timetableExport
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.mbd.cmscommon.controller.eligibleMergeSessions
import com.mbd.cmscommon.controller.describeExistingPeriodsForMerge
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
    onSavePeriod: (DayOfWeek, String, String, SemesterSubject?, List<Teacher>, PeriodType, String, String, String, LocalDate?, LocalDate?, SessionPeriod?) -> Unit,
    onRemovePeriod: (SessionPeriod) -> Unit,
    onClearError: () -> Unit,
    onExport: ((ExportDocument, ExportFormat) -> Unit)? = null,
    modifier: Modifier = Modifier,
    /** Every session and every session's periods, for merging this class with another session's lecture. */
    allSessions: List<AcademicSession> = emptyList(),
    allPeriods: List<SessionPeriod> = emptyList(),
    /** (period, sessionId, link): merge/unmerge [sessionId] into/from [period]'s lecture. */
    onSetLink: (SessionPeriod, String, Boolean) -> Unit = { _, _, _ -> },
    /** Attach this session to another session's existing lecture. */
    onMergeExisting: (SessionPeriod) -> Unit = {},
    /** Gives this session its own period in place of the shared one it leaves: (shared, days, ...same as a save). */
    /** Unmerges one linked session from this session's own lecture: (period, sessionId, days, ...same as a save). */
    onUnmergeSession: (SessionPeriod, String, Set<DayOfWeek>, String, String, SemesterSubject?, List<Teacher>, PeriodType, String, String, String, LocalDate?, LocalDate?) -> Unit = { _, _, _, _, _, _, _, _, _, _, _, _, _ -> },
    onLeaveMerge: (SessionPeriod, Set<DayOfWeek>, String, String, SemesterSubject?, List<Teacher>, PeriodType, String, String, String, LocalDate?, LocalDate?) -> Unit = { _, _, _, _, _, _, _, _, _, _, _, _ -> },
    /** The Morning/Evening tab shown; [periods] holds every shift and only this shift's grid is shown. */
    shift: Session = Session.MORNING,
    shifts: List<Session> = listOf(shift),
    onSelectShift: (Session) -> Unit = {},
) {
    // No combined view: Morning and Evening can use the same slot times, so each shift is its own grid.
    val shown = periodsForShift(periods, shift)
    var editorState by remember { mutableStateOf<SessionPeriod?>(null) }
    var addingPeriodDay by remember { mutableStateOf<DayOfWeek?>(null) }
    var pendingRemove by remember { mutableStateOf<SessionPeriod?>(null) }
    var detailPeriod by remember { mutableStateOf<SessionPeriod?>(null) }
    var choosingSlotDay by remember { mutableStateOf<DayOfWeek?>(null) }
    var mergingExisting by remember { mutableStateOf(false) }
    var linkedDetail by remember { mutableStateOf<SessionPeriod?>(null) }
    var unmerging by remember { mutableStateOf<SessionPeriod?>(null) }
    var ownerUnmerging by remember { mutableStateOf<Pair<SessionPeriod, String>?>(null) }
    val mergeCandidates = describeExistingPeriodsForMerge(allPeriods, session?.sessionId.orEmpty(), shift)

    val conflictIds = conflictingPeriodIds(shown)
    val periodByDayAndSlot = shown.associateBy { it.day to it.timeRange }
    val timeSlots = shown.map { it.timeRange }.distinct().sortedBy { it.substringBefore('–') }

    if (onExport != null) {
        TopBarActions {
            ExportMenuButton(onExport = { format -> onExport(timetableExport(session, shown, shift), format) }, enabled = shown.isNotEmpty(), tint = CmsTheme.colors.onInk)
        }
    }

    Box(modifier.fillMaxSize()) {
        val listState = rememberLazyListState()
        WithVerticalScrollbar(listState) {
        LazyColumn(Modifier.fillMaxWidth(), state = listState, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Column {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        shifts.forEach { option ->
                            CmsChip("${option.label} shift", selected = option == shift, onClick = { onSelectShift(option) })
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (shifts.size > 1) "Each shift has its own weekly grid. Teachers and rooms can't be double-booked across shifts." else "This session runs ${shift.label} only.",
                        color = ModMuted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            if (shown.isEmpty()) {
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
                                            subtitle = if (isBreak) "" else period.teacherLabel.ifBlank { "Unassigned" },
                                            meta = if (isBreak) "" else (period.roomNo?.ifBlank { null } ?: "No room") + if (period.isMergedLecture || !period.isOwnRow) " · Merged" else "",
                                            isBreak = isBreak,
                                            isAlert = period.id in conflictIds,
                                        )
                                    }
                                },
                            )
                        },
                        editable = true,
                        onCellClick = { dayKey, slot ->
                            val day = DayOfWeek.valueOf(dayKey)
                            val period = periodByDayAndSlot[day to slot]
                            if (period == null) {
                                if (mergeCandidates.isEmpty()) addingPeriodDay = day else choosingSlotDay = day
                            } else if (period.isOwnRow) detailPeriod = period else linkedDetail = period
                        },
                    )
                }
            }

            item { Spacer(Modifier.height(72.dp)) }
        }
        }
        CmsFab(
            onClick = { if (mergeCandidates.isEmpty()) addingPeriodDay = DayOfWeek.MONDAY else choosingSlotDay = DayOfWeek.MONDAY },
            contentDescription = "Add period",
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        )
    }

    if (addingPeriodDay != null || editorState != null) {
        PeriodEditorDialog(
            day = editorState?.day ?: addingPeriodDay ?: DayOfWeek.MONDAY,
            existing = editorState?.let { e -> periods.firstOrNull { it.id == e.id && it.isOwnRow } ?: e },
            allSessions = allSessions,
            onSetLink = onSetLink,
            onUnmergeSession = { sid -> ownerUnmerging = requireNotNull(editorState) to sid; addingPeriodDay = null; editorState = null },
            subjects = subjects,
            teachers = teachers,
            buildings = buildings,
            rooms = rooms,
            currentSemesterTerm = currentSemesterTerm,
            initialDays = editorState?.let { siblingDaysFor(it, shown) } ?: setOf(addingPeriodDay ?: DayOfWeek.MONDAY),
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
                        original != null -> shown.firstOrNull {
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

    choosingSlotDay?.let { day ->
        AlertDialog(
            onDismissRequest = { choosingSlotDay = null },
            title = { Text("Add to timetable") },
            text = { Text("Create a new period, or merge this class into a lecture that another session already has (same teacher, room and time).") },
            confirmButton = { TextButton(onClick = { choosingSlotDay = null; addingPeriodDay = day }) { Text("New period") } },
            dismissButton = { TextButton(onClick = { choosingSlotDay = null; mergingExisting = true }) { Text("Merge existing class") } },
        )
    }

    if (mergingExisting) {
        SearchPickDialog(
            title = "Merge with an existing class",
            hint = "Search by subject, session, teacher, day or time",
            options = mergeCandidates.map { p ->
                val owner = allSessions.firstOrNull { it.sessionId == p.sessionId }?.label ?: p.sessionId
                p.id to "${p.subjectName.ifBlank { p.courseCode }} · $owner · ${p.day.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)} ${p.timeRange} · ${p.teacherLabel.ifBlank { "Unassigned" }}"
            },
            onPick = { id -> mergeCandidates.firstOrNull { it.id == id }?.let(onMergeExisting); mergingExisting = false },
            onDismiss = { mergingExisting = false },
        )
    }

    linkedDetail?.let { period ->
        val owner = allSessions.firstOrNull { it.sessionId == period.sessionId }?.label ?: period.sessionId
        AlertDialog(
            onDismissRequest = { linkedDetail = null },
            title = { Text(period.subjectName.ifBlank { period.courseCode }) },
            text = { DialogScrollBody {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatusBadge("MERGED CLASS", BadgeTone.Neutral)
                    DetailRow("Owned by", owner)
                    DetailRow("Day", period.day.getDisplayName(TextStyle.FULL, Locale.ENGLISH))
                    DetailRow("Time", period.timeRange)
                    DetailRow("Teacher", period.teacherLabel.ifBlank { "Unassigned" })
                    DetailRow("Room", period.roomNo?.ifBlank { null } ?: "Not assigned")
                    Text("This lecture is shared with $owner. Edit it from that session's timetable. To unmerge, give this class its own teacher or time slot.", color = ModMuted, style = MaterialTheme.typography.bodySmall)
                }
            }},
            confirmButton = { TextButton(onClick = { linkedDetail = null; unmerging = period }) { Text("Unmerge...", color = CmsTheme.colors.accent) } },
            dismissButton = { TextButton(onClick = { linkedDetail = null }) { Text("Close") } },
        )
    }

    ownerUnmerging?.let { (period, sid) ->
        PeriodEditorDialog(
            day = period.day,
            existing = period.copy(linkedSessionIds = emptySet()),
            subjects = subjects,
            teachers = teachers,
            buildings = buildings,
            rooms = rooms,
            currentSemesterTerm = currentSemesterTerm,
            requireChangeFrom = period,
            onDismiss = { ownerUnmerging = null },
            onSave = { days, start, end, subject, teacher, type, room, building, notes, from, to ->
                onUnmergeSession(period, sid, days, start, end, subject, teacher, type, room, building, notes, from, to)
                ownerUnmerging = null
            },
        )
    }

    unmerging?.let { shared ->
        PeriodEditorDialog(
            day = shared.day,
            existing = shared,
            subjects = subjects,
            teachers = teachers,
            buildings = buildings,
            rooms = rooms,
            currentSemesterTerm = currentSemesterTerm,
            requireChangeFrom = shared,
            onDismiss = { unmerging = null },
            onSave = { days, start, end, subject, teacher, type, room, building, notes, from, to ->
                onLeaveMerge(shared, days, start, end, subject, teacher, type, room, building, notes, from, to)
                unmerging = null
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
            dependentSummary = "Removes ${period.subjectName.ifBlank { period.periodType.name }} at ${clockDisplay(period.startTime)} on ${period.day}.",
            onConfirm = { onRemovePeriod(period); pendingRemove = null },
            onDismiss = { pendingRemove = null },
        )
    }

    if (!errorMessage.isNullOrBlank()) {
        CmsErrorDialog(message = errorMessage, onDismiss = onClearError, title = "Couldn't save period")
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
        text = { DialogScrollBody {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (hasConflict) {
                    StatusBadge("TIME CONFLICT", BadgeTone.Error)
                    Spacer(Modifier.height(4.dp))
                }
                DetailRow("Day", period.day.getDisplayName(TextStyle.FULL, Locale.ENGLISH))
                DetailRow("Time", period.timeRange)
                if (period.isMergedLecture) DetailRow("Merged with", period.linkedSessionIds.joinToString(", "))
                if (!isBreak) {
                    DetailRow("Subject code", period.courseCode)
                    DetailRow("Teacher", period.teacherLabel.ifBlank { "Unassigned" })
                    DetailRow("Room", period.roomNo?.ifBlank { null } ?: "Not assigned")
                    period.building?.takeIf { it.isNotBlank() }?.let { DetailRow("Building", it) }
                    period.notes?.takeIf { it.isNotBlank() }?.let { DetailRow("Notes", it) }
                }
            }
        }},
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
fun PeriodEditorDialog(
    day: DayOfWeek,
    existing: SessionPeriod?,
    subjects: List<SemesterSubject>,
    teachers: List<Teacher>,
    buildings: List<Building>,
    rooms: List<Room>,
    currentSemesterTerm: SemesterTerm?,
    onDismiss: () -> Unit,
    onSave: (Set<DayOfWeek>, String, String, SemesterSubject?, List<Teacher>, PeriodType, String, String, String, LocalDate?, LocalDate?) -> Unit,
    /** Days already checked when the dialog opens -- every day (besides [day]) that repeats this
     * exact lecture, so editing one day of a Mon/Tue/Wed block shows all three checked, not just
     * the one that was clicked. Defaults to just [day] for a brand-new period. */
    initialDays: Set<DayOfWeek> = setOf(day),
    /** Sessions that can be merged into this lecture, and the merge/unmerge callback; hidden when empty. */
    allSessions: List<AcademicSession> = emptyList(),
    onSetLink: (SessionPeriod, String, Boolean) -> Unit = { _, _, _ -> },
    /** Unmerge one linked session: opens the change-required flow for this lecture. */
    onUnmergeSession: (String) -> Unit = {},
    /** When set, [existing] is a shared lecture this class is leaving: it can only be saved with a different
     * teacher or time slot, since the result is a separate class of its own. */
    requireChangeFrom: SessionPeriod? = null,
) {
    var selectedDays by remember { mutableStateOf(initialDays) }
    var start by remember { mutableStateOf(existing?.startTime ?: "") }
    var end by remember { mutableStateOf(existing?.endTime ?: "") }
    var type by remember { mutableStateOf(existing?.periodType ?: PeriodType.LECTURE) }
    var subjectCode by remember { mutableStateOf(existing?.courseCode ?: "") }
    // The main teacher first, then any co-teachers sharing the slot (e.g. a project taught by several teachers).
    var teacherIds by remember { mutableStateOf(existing?.teacherIds ?: emptyList()) }
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
    val changedFromShared = requireChangeFrom == null || teacherIds != requireChangeFrom.teacherIds ||
        selectedDays != setOf(requireChangeFrom.day) ||
        clockDisplay(start.trim()) != clockDisplay(requireChangeFrom.startTime) ||
        clockDisplay(end.trim()) != clockDisplay(requireChangeFrom.endTime)
    val hasSemesterTerm = currentSemesterTerm?.startDate != null && currentSemesterTerm.endDate != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (requireChangeFrom != null) "Unmerge shared class" else if (existing == null) "Timetable period" else "Edit period", style = MaterialTheme.typography.headlineSmall) },
        text = {
            DialogScrollBody(maxHeight = 460.dp) {
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
                if (requireChangeFrom != null) {
                    Text("This lecture is shared with another session. Change the teacher or the time slot to separate the two classes.", color = ModMuted, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(6.dp))
                }
                if (selectedDays.isEmpty()) {
                    Text("Select at least one day.", color = TimetableRed, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CmsTimeField(value = start, onValueChange = { start = it }, label = "Start", modifier = Modifier.weight(1f))
                    CmsTimeField(value = end, onValueChange = { end = it }, label = "End", minTime = start, modifier = Modifier.weight(1f))
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
                        error = if (subjects.isEmpty()) "No subjects configured for this semester yet — add them in Curriculum first." else null,
                    )
                    Spacer(Modifier.height(10.dp))
                    CmsEntityPicker(
                        label = "Teacher (optional)",
                        selectedId = teacherIds.firstOrNull(),
                        options = teachers.map { CmsEntityOption(it.teacherId, it.name) },
                        onSelected = { picked ->
                            // Changing the main teacher keeps the co-teachers; clearing it clears them too.
                            teacherIds = if (picked == null) emptyList() else listOf(picked) + teacherIds.drop(1).filter { it != picked }
                        },
                        optional = true,
                        emptyLabel = "Not assigned",
                    )
                    if (teacherIds.isNotEmpty()) {
                        teacherIds.drop(1).forEach { coId ->
                            Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(teachers.firstOrNull { it.teacherId == coId }?.name ?: coId, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                                TextButton(onClick = { teacherIds = teacherIds - coId }) { Text("Remove") }
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        CmsEntityPicker(
                            label = "Add another teacher (shared slot)",
                            selectedId = null,
                            options = teachers.filter { it.teacherId !in teacherIds }.map { CmsEntityOption(it.teacherId, it.name) },
                            onSelected = { picked -> if (picked != null) teacherIds = teacherIds + picked },
                            optional = true,
                            emptyLabel = "Choose a teacher to add",
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    CmsBuildingRoomPicker(
                        buildings = buildings,
                        rooms = rooms,
                        selectedBuildingId = selectedBuildingId,
                        selectedRoomId = selectedRoomId,
                        onChange = { buildingId, buildingName, roomId, roomNo ->
                            selectedBuildingId = buildingId
                            building = buildingName ?: ""
                            selectedRoomId = roomId
                            room = roomNo ?: ""
                        },
                        buildingLabel = "Building (optional)",
                        roomLabel = "Room (optional)",
                    )
                }
                if (existing != null && existing.isOwnRow && type == PeriodType.LECTURE && allSessions.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Text("MERGED WITH", color = ModMuted, style = CmsTextStyles.eyebrow)
                    existing.linkedSessionIds.forEach { sid ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(allSessions.firstOrNull { it.sessionId == sid }?.label ?: sid, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            TextButton(onClick = { onUnmergeSession(sid) }) { Text("Unmerge...", color = TimetableRed) }
                        }
                    }
                    Text("Unmerging needs a different teacher or time slot for one of the two classes.", color = ModMuted, style = MaterialTheme.typography.bodySmall)
                    val addable = eligibleMergeSessions(existing, allSessions)
                    var addingSession by remember { mutableStateOf(false) }
                    if (addable.isNotEmpty()) TextButton(onClick = { addingSession = true }) { Text("Add another session") }
                    if (addingSession) {
                        SearchPickDialog(
                            title = "Merge another session",
                            hint = "Search sessions",
                            options = addable.map { it.sessionId to it.label },
                            onPick = { id -> onSetLink(existing, id, true); addingSession = false },
                            onDismiss = { addingSession = false },
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
                CmsDateField(value = effectiveTo, onValueChange = { effectiveTo = it }, label = "Effective to", optional = true, minDate = effectiveFrom.ifBlank { null })
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
                    val chosenTeachers = teacherIds.mapNotNull { id -> teachers.firstOrNull { it.teacherId == id } }
                    onSave(
                        selectedDays, start.trim(), end.trim(), subject, chosenTeachers, type, room.trim(), building.trim(), notes.trim(),
                        runCatching { LocalDate.parse(effectiveFrom.trim()) }.getOrNull(),
                        runCatching { LocalDate.parse(effectiveTo.trim()) }.getOrNull(),
                    )
                },
                enabled = changedFromShared && selectedDays.isNotEmpty() && timeValid && !isDateRangeReversed(effectiveFrom, effectiveTo) && (!needsSubject || subjectCode.isNotBlank()),
            ) { Text(if (requireChangeFrom != null) "Unmerge" else if (existing == null) "Add period" else "Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

/** A searchable single-choice list in a dialog: options are (id, label); typing filters by label. */
@Composable
private fun SearchPickDialog(
    title: String,
    hint: String,
    options: List<Pair<String, String>>,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val shown = options.filter { query.isBlank() || it.second.contains(query.trim(), ignoreCase = true) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(value = query, onValueChange = { query = it }, label = { Text(hint) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                if (shown.isEmpty()) {
                    Text("Nothing matches.", color = ModMuted, style = MaterialTheme.typography.bodySmall)
                } else {
                    LazyColumn(Modifier.heightIn(max = 320.dp)) {
                        items(shown, key = { it.first }) { (id, label) ->
                            Text(label, modifier = Modifier.fillMaxWidth().clickable { onPick(id) }.padding(vertical = 12.dp), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
