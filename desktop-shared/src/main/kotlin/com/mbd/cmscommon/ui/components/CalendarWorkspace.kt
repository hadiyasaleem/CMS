package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.CalendarEvent
import com.mbd.cmscommon.domain.model.CalendarViewerContext
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.calendarEventKey
import com.mbd.cmscommon.domain.model.calendarSummary
import com.mbd.cmscommon.domain.model.endDateOrStart
import com.mbd.cmscommon.domain.model.isOngoingOn
import com.mbd.cmscommon.domain.model.isVisibleTo
import com.mbd.cmscommon.domain.model.persistedValidationMessage
import com.mbd.cmscommon.domain.model.startDateOrNull
import com.mbd.cmscommon.domain.model.validationMessage
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val CalendarCanvas = Color(0xFFF7F5F0)
private val CalendarBlue = Color(0xFF24577A)
private val CalendarDateFormat = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy")
private val CalendarMonthFormat = DateTimeFormatter.ofPattern("MMM")

private val EVENT_TYPES = listOf("EVENT", "HOLIDAY", "EXAM", "DEADLINE")
private val AUDIENCES = listOf("ALL", "ADMIN", "TEACHER", "STUDENT")

@Composable
fun CalendarWorkspace(
    events: List<CalendarEvent>,
    viewer: CalendarViewerContext,
    departments: List<Department>,
    sessions: List<AcademicSession>,
    canEdit: Boolean,
    loading: Boolean,
    busy: Boolean,
    errorMessage: String?,
    actionMessage: String?,
    onRetry: () -> Unit,
    onCreate: (CalendarEvent) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<String?>(null) }
    var showCreate by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<CalendarEvent?>(null) }

    val today = LocalDate.now()
    val relevant = events.filter { isVisibleTo(it, viewer) }
    val summary = calendarSummary(relevant, today)

    val visible = relevant
        .filter { selectedType == null || it.eventType.equals(selectedType, ignoreCase = true) }
        .filter { query.isBlank() || it.title.contains(query, ignoreCase = true) || (it.venue ?: "").contains(query, ignoreCase = true) }
        .sortedWith(compareBy({ startDateOrNull(it) ?: LocalDate.MAX }, { it.title }))

    LazyColumn(
        modifier = modifier.fillMaxWidth().background(CalendarCanvas),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { CalendarHeader(canEdit) { showCreate = true } }

        if (!errorMessage.isNullOrBlank()) {
            item { CalendarNotice(errorMessage, CmsTheme.colors.accent, action = "Retry", onAction = onRetry) }
        }
        if (!actionMessage.isNullOrBlank()) {
            item { CalendarNotice(actionMessage, Color(0xFF2F6B4F), action = null, onAction = null) }
        }

        item { CalendarSummaryRow(summary.upcoming, summary.thisMonth, summary.exams + summary.deadlines, summary.ongoing) }

        item {
            CalendarFilters(
                query = query,
                onQueryChange = { query = it },
                selectedType = selectedType,
                onTypeChange = { selectedType = it },
            )
        }

        when {
            loading -> items(3) { SkeletonRow() }
            relevant.isEmpty() -> item {
                CalendarEmptyState(
                    "No relevant events are scheduled",
                    canEdit,
                    onAdd = { showCreate = true },
                )
            }
            visible.isEmpty() -> item {
                CalendarEmptyState(
                    "No events match these filters",
                    canEdit = false,
                    onAdd = {},
                    clearLabel = "Clear filters",
                    onClear = { query = ""; selectedType = null },
                )
            }
            else -> items(visible, key = { calendarEventKey(it) }) { event ->
                CalendarEventCard(
                    event = event,
                    today = today,
                    canDelete = canEdit,
                    onDelete = { pendingDelete = event },
                )
            }
        }

        item { Spacer(Modifier.height(72.dp)) }
    }

    if (showCreate) {
        CreateCalendarEventDialog(
            departments = departments,
            sessions = sessions,
            busy = busy,
            onDismiss = { showCreate = false },
            onConfirm = { event -> onCreate(event); showCreate = false },
        )
    }

    pendingDelete?.let { event ->
        ConfirmDestructiveActionDialog(
            title = "Remove event",
            dependentSummary = "Remove \"${event.title}\" from the calendar?",
            onConfirm = { onDelete(event.id); pendingDelete = null },
            onDismiss = { pendingDelete = null },
        )
    }
}

@Composable
private fun CalendarHeader(canEdit: Boolean, onAdd: () -> Unit) {
    Surface(shape = RoundedCornerShape(18.dp), color = Color(0xFF252321)) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("College calendar", color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                Text(
                    "Holidays, activities, exams and deadlines in one timeline",
                    color = CmsTheme.colors.onInkMuted,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (canEdit) {
                CmsPrimaryButton(text = "Add event", onClick = onAdd)
            }
        }
    }
}

@Composable
private fun CalendarSummaryRow(upcoming: Int, thisMonth: Int, examsAndDeadlines: Int, ongoing: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        CalendarMetric("Upcoming", upcoming.toString(), Modifier.weight(1f))
        CalendarMetric("This month", thisMonth.toString(), Modifier.weight(1f))
        CalendarMetric("Exams / deadlines", examsAndDeadlines.toString(), Modifier.weight(1f))
        CalendarMetric("Ongoing", ongoing.toString(), Modifier.weight(1f))
    }
}

@Composable
private fun CalendarMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E0D7))) {
        Column(Modifier.padding(14.dp)) {
            Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Text(label.uppercase(Locale.ROOT), color = Color(0xFF77716A), style = CmsTextStyles.eyebrow)
        }
    }
}

@Composable
private fun CalendarFilters(query: String, onQueryChange: (String) -> Unit, selectedType: String?, onTypeChange: (String?) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search events or venue") },
            singleLine = true,
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CmsChip("All types", selected = selectedType == null, onClick = { onTypeChange(null) })
            EVENT_TYPES.forEach { type ->
                CmsChip(type, selected = selectedType == type, onClick = { onTypeChange(type) })
            }
        }
    }
}

private fun eventTypeTone(type: String): BadgeTone = when (type.uppercase(Locale.ROOT)) {
    "EXAM" -> BadgeTone.Error
    "DEADLINE" -> BadgeTone.Warning
    "HOLIDAY" -> BadgeTone.Gold
    else -> BadgeTone.Navy
}

private fun eventDateStatus(event: CalendarEvent, today: LocalDate): String {
    val start = startDateOrNull(event) ?: return "Date needs review"
    val end = endDateOrStart(event) ?: start
    return when {
        isOngoingOn(event, today) && end != start -> "Ongoing | ends in ${java.time.temporal.ChronoUnit.DAYS.between(today, end)}d"
        start.isEqual(today) -> "Today"
        start.isEqual(today.plusDays(1)) -> "Tomorrow"
        start.isEqual(today.minusDays(1)) -> "Yesterday"
        start.isAfter(today) -> "In ${java.time.temporal.ChronoUnit.DAYS.between(today, start)}d"
        else -> "Ended"
    }
}

@Composable
private fun CalendarEventCard(event: CalendarEvent, today: LocalDate, canDelete: Boolean, onDelete: () -> Unit) {
    val date = startDateOrNull(event)
    val needsReview = persistedValidationMessage(event) != null

    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E0D7))) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            CalendarDateTile(date)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(event.title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    StatusBadge(event.eventType.uppercase(Locale.ROOT), eventTypeTone(event.eventType))
                }
                Spacer(Modifier.height(4.dp))
                Text(eventDateStatus(event, today), color = CalendarBlue, style = MaterialTheme.typography.bodySmall)
                val venue = event.venue
                if (!venue.isNullOrBlank()) {
                    Text(venue, color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
                }
                val description = event.description
                if (!description.isNullOrBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(description, color = Color(0xFF625E58), style = MaterialTheme.typography.bodyMedium)
                }
                if (needsReview) {
                    Spacer(Modifier.height(6.dp))
                    Text("NEEDS REVIEW", color = CmsTheme.colors.accent, style = CmsTextStyles.eyebrow)
                }
                if (canDelete) {
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = onDelete) { Text("Remove", color = CmsTheme.colors.accent) }
                }
            }
        }
    }
}

@Composable
private fun CalendarDateTile(date: LocalDate?) {
    Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFF1EEE8)) {
        Column(Modifier.padding(10.dp).width(52.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(date?.dayOfMonth?.toString() ?: "--", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
            Text(date?.format(CalendarMonthFormat)?.uppercase(Locale.ROOT) ?: "", color = Color(0xFF77716A), style = CmsTextStyles.eyebrow)
        }
    }
}

@Composable
private fun CalendarNotice(message: String, color: Color, action: String?, onAction: (() -> Unit)?) {
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
private fun CalendarEmptyState(
    message: String,
    canEdit: Boolean,
    onAdd: () -> Unit,
    clearLabel: String? = null,
    onClear: (() -> Unit)? = null,
) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E0D7))) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                if (clearLabel != null) "Clear the filters to return to the full calendar." else "New events will appear here when they are published for this audience.",
                color = Color(0xFF77716A),
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.height(12.dp))
            if (clearLabel != null && onClear != null) {
                CmsPrimaryButton(text = clearLabel, onClick = onClear)
            } else if (canEdit) {
                CmsPrimaryButton(text = "Add first event", onClick = onAdd)
            }
        }
    }
}

@Composable
private fun CreateCalendarEventDialog(
    departments: List<Department>,
    sessions: List<AcademicSession>,
    busy: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (CalendarEvent) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("EVENT") }
    var audience by remember { mutableStateOf("ALL") }
    var deptId by remember { mutableStateOf<String?>(null) }
    var sessionId by remember { mutableStateOf<String?>(null) }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var venue by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val draft = CalendarEvent(
        id = "",
        title = title.trim(),
        eventType = type,
        startDate = startDate.trim(),
        endDate = endDate.trim().ifBlank { null },
        startTime = startTime.trim().ifBlank { null },
        endTime = endTime.trim().ifBlank { null },
        description = description.trim().ifBlank { null },
        venue = venue.trim().ifBlank { null },
        audience = audience,
        deptId = deptId,
        sessionId = sessionId,
    )
    val error = validationMessage(draft)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Calendar event", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(10.dp))
                DropdownField("Event type", type, EVENT_TYPES, onSelect = { type = it })
                Spacer(Modifier.height(10.dp))
                DropdownField("Audience", audience, AUDIENCES, onSelect = { audience = it })
                Spacer(Modifier.height(10.dp))
                Text("Academic scope", style = MaterialTheme.typography.labelMedium)
                CmsEntityPicker(
                    label = "Department",
                    selectedId = deptId,
                    options = departments.map { CmsEntityOption(it.deptId, it.name) },
                    onSelected = { deptId = it; sessionId = null },
                    optional = true,
                    emptyLabel = "College wide",
                    modifier = Modifier.padding(top = 6.dp),
                )
                if (deptId != null) {
                    CmsEntityPicker(
                        label = "Academic session",
                        selectedId = sessionId,
                        options = sessions.filter { it.deptId == deptId }.map { CmsEntityOption(it.sessionId, "${it.startYear}-${it.endYear} ${it.shift}") },
                        onSelected = { sessionId = it },
                        optional = true,
                        emptyLabel = "All sessions",
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                Spacer(Modifier.height(10.dp))
                CmsDateField(value = startDate, onValueChange = { startDate = it }, label = "Start date")
                Spacer(Modifier.height(10.dp))
                CmsDateField(value = endDate, onValueChange = { endDate = it }, label = "End date", optional = true)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = startTime, onValueChange = { startTime = it }, label = { Text("Start time") }, placeholder = { Text("HH:MM") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = endTime, onValueChange = { endTime = it }, label = { Text("End time") }, placeholder = { Text("HH:MM") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = venue, onValueChange = { venue = it }, label = { Text("Venue (optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                if (error != null && (title.isNotBlank() || startDate.isNotBlank())) {
                    Spacer(Modifier.height(8.dp))
                    Text(error, color = CmsTheme.colors.accent, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(draft) }, enabled = error == null && !busy) { Text(if (busy) "Saving" else "Add event") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !busy) { Text("Cancel") }
        },
    )
}

@Composable
private fun DropdownField(label: String, value: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(6.dp))
        Box {
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) { Text(value) }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(text = { Text(option) }, onClick = { onSelect(option); expanded = false })
                }
            }
        }
    }
}
