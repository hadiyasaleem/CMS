package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.CalendarEvent
import com.mbd.cmscommon.domain.model.CalendarViewerContext
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.calendarSummary
import com.mbd.cmscommon.domain.model.isOngoingOn
import com.mbd.cmscommon.domain.model.isVisibleTo
import com.mbd.cmscommon.domain.model.validationMessage
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmscommon.ui.theme.ModGround
import com.mbd.cmscommon.ui.theme.ModInk
import com.mbd.cmscommon.ui.theme.ModMuted
import com.mbd.cmscommon.ui.theme.ModSurface
import com.mbd.cmscommon.ui.theme.ModTrack
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val CalendarCanvas = ModGround

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
    var visibleMonth by remember { mutableStateOf(YearMonth.from(LocalDate.now())) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var creatingEventDate by remember { mutableStateOf<String?>(null) }
    var pendingDelete by remember { mutableStateOf<CalendarEvent?>(null) }

    val today = LocalDate.now()
    val relevant = events.filter { isVisibleTo(it, viewer) }
    val summary = calendarSummary(relevant, today)

    Box(modifier.fillMaxSize()) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth().background(CalendarCanvas),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { CalendarHeader() }

        if (!errorMessage.isNullOrBlank()) {
            item { CmsNotice(errorMessage, tone = NoticeTone.Error, actionLabel = "Retry", onAction = onRetry) }
        }
        if (!actionMessage.isNullOrBlank()) {
            item { CmsNotice(actionMessage, tone = NoticeTone.Success) }
        }

        item { CalendarSummaryRow(summary.upcoming, summary.thisMonth, summary.exams + summary.deadlines, summary.ongoing) }

        item { CalendarLegend() }

        if (loading) {
            items(3) { SkeletonRow() }
        } else {
            item {
                MonthCalendarGrid(
                    month = visibleMonth,
                    today = today,
                    events = relevant,
                    onPreviousMonth = { visibleMonth = visibleMonth.minusMonths(1) },
                    onNextMonth = { visibleMonth = visibleMonth.plusMonths(1) },
                    onDaySelected = { date -> selectedDate = date },
                )
            }
        }

        item { Spacer(Modifier.height(72.dp)) }
    }
        if (canEdit) {
            CmsFab(
                onClick = { creatingEventDate = today.toString() },
                contentDescription = "Add event",
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            )
        }
    }

    selectedDate?.let { date ->
        DayDetailDialog(
            date = date,
            events = relevant.filter { isOngoingOn(it, date) }.sortedBy { it.title },
            canEdit = canEdit,
            onAddEvent = { creatingEventDate = date.toString() },
            onDeleteEvent = { pendingDelete = it },
            onDismiss = { selectedDate = null },
        )
    }

    creatingEventDate?.let { date ->
        CreateCalendarEventDialog(
            initialDate = date,
            departments = departments,
            sessions = sessions,
            busy = busy,
            onDismiss = { creatingEventDate = null },
            onConfirm = { event -> onCreate(event); creatingEventDate = null; selectedDate = null },
        )
    }

    pendingDelete?.let { event ->
        ConfirmDestructiveActionDialog(
            title = "Remove event",
            dependentSummary = "Remove \"${event.title}\" from the calendar?",
            onConfirm = { onDelete(event.id); pendingDelete = null; selectedDate = null },
            onDismiss = { pendingDelete = null },
        )
    }
}

@Composable
private fun CalendarHeader() {
    Surface(shape = RoundedCornerShape(18.dp), color = ModInk) {
        Column(Modifier.padding(20.dp)) {
            Text("College calendar", color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
            Text(
                "Holidays, activities, exams and deadlines in one timeline",
                color = CmsTheme.colors.onInkMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
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
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(14.dp)) {
            Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Text(label.uppercase(Locale.ROOT), color = ModMuted, style = CmsTextStyles.eyebrow)
        }
    }
}

@Composable
private fun CalendarLegend() {
    Surface(shape = RoundedCornerShape(12.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            EVENT_TYPES.forEach { type ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(eventTypeDotColor(type)))
                    Text(type.lowercase(Locale.ROOT).replaceFirstChar { it.uppercase() }, color = ModMuted, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

private fun eventTypeDotColor(type: String): Color = when (type.uppercase(Locale.ROOT)) {
    "EXAM" -> Color(0xFFEF4444)
    "DEADLINE" -> Color(0xFFA855F7)
    "HOLIDAY" -> Color(0xFFEAB308)
    else -> Color(0xFF3B82F6)
}

private val CalendarMonthYearFormat = DateTimeFormatter.ofPattern("MMMM yyyy")
private val CALENDAR_DAY_HEADERS = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

@Composable
private fun MonthCalendarGrid(
    month: YearMonth,
    today: LocalDate,
    events: List<CalendarEvent>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDaySelected: (LocalDate) -> Unit,
) {
    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    month.format(CalendarMonthYearFormat),
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                )
                IconButton(onClick = onPreviousMonth) { Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous month") }
                IconButton(onClick = onNextMonth) { Icon(Icons.Filled.ChevronRight, contentDescription = "Next month") }
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth()) {
                CALENDAR_DAY_HEADERS.forEach { label ->
                    Text(
                        label,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = ModMuted,
                        style = CmsTextStyles.eyebrow,
                    )
                }
            }
            Spacer(Modifier.height(6.dp))

            val offset = month.atDay(1).dayOfWeek.value % 7
            val daysInMonth = month.lengthOfMonth()
            val rowCount = (offset + daysInMonth + 6) / 7
            repeat(rowCount) { rowIndex ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(7) { colIndex ->
                        val dayNumber = rowIndex * 7 + colIndex - offset + 1
                        if (dayNumber < 1 || dayNumber > daysInMonth) {
                            Spacer(Modifier.weight(1f).height(52.dp))
                        } else {
                            val date = month.atDay(dayNumber)
                            val dayEvents = events.filter { isOngoingOn(it, date) }
                            CalendarDayCell(
                                date = date,
                                isToday = date == today,
                                dotColors = dayEvents.map { eventTypeDotColor(it.eventType) }.distinct().take(4),
                                onClick = { onDaySelected(date) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun CalendarDayCell(date: LocalDate, isToday: Boolean, dotColors: List<Color>, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(10.dp),
        color = if (isToday) CmsTheme.colors.accent.copy(alpha = 0.12f) else ModGround,
        border = if (isToday) BorderStroke(1.5.dp, CmsTheme.colors.accent) else null,
    ) {
        Column(Modifier.fillMaxSize().padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween) {
            Text(
                date.dayOfMonth.toString(),
                color = if (isToday) CmsTheme.colors.accent else CmsTheme.colors.ink,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                style = MaterialTheme.typography.bodySmall,
            )
            if (dotColors.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    dotColors.forEach { color -> Box(Modifier.size(5.dp).clip(CircleShape).background(color)) }
                }
            }
        }
    }
}

@Composable
private fun DayDetailDialog(
    date: LocalDate,
    events: List<CalendarEvent>,
    canEdit: Boolean,
    onAddEvent: () -> Unit,
    onDeleteEvent: (CalendarEvent) -> Unit,
    onDismiss: () -> Unit,
) {
    val dayFormat = remember { DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(date.format(dayFormat)) },
        text = {
            Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (events.isEmpty()) {
                    Text("No events on this day.", color = ModMuted, style = MaterialTheme.typography.bodyMedium)
                } else {
                    events.forEach { event ->
                        DayDetailEventRow(event, canDelete = canEdit, onDelete = { onDeleteEvent(event) })
                    }
                }
                if (canEdit) {
                    Spacer(Modifier.height(4.dp))
                    TextButton(onClick = onAddEvent) { Text("+ Add event") }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}

@Composable
private fun DayDetailEventRow(event: CalendarEvent, canDelete: Boolean, onDelete: () -> Unit) {
    Surface(shape = RoundedCornerShape(12.dp), color = ModGround, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(event.title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                StatusBadge(event.eventType.uppercase(Locale.ROOT), eventTypeTone(event.eventType))
            }
            val time = listOfNotNull(event.startTime, event.endTime.takeIf { !it.isNullOrBlank() }).joinToString(" - ").ifBlank { null }
            if (time != null) {
                Spacer(Modifier.height(2.dp))
                Text(time, color = ModMuted, style = MaterialTheme.typography.bodySmall)
            }
            val venue = event.venue
            if (!venue.isNullOrBlank()) {
                Text(venue, color = ModMuted, style = MaterialTheme.typography.bodySmall)
            }
            val description = event.description
            if (!description.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(description, color = ModMuted, style = MaterialTheme.typography.bodySmall)
            }
            if (canDelete) {
                Spacer(Modifier.height(6.dp))
                TextButton(onClick = onDelete) { Text("Remove", color = CmsTheme.colors.accent) }
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

@Composable
private fun CreateCalendarEventDialog(
    initialDate: String = "",
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
    var startDate by remember { mutableStateOf(initialDate) }
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
                    CmsTimeField(value = startTime, onValueChange = { startTime = it }, label = "Start time", modifier = Modifier.weight(1f))
                    CmsTimeField(value = endTime, onValueChange = { endTime = it }, label = "End time", modifier = Modifier.weight(1f))
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
