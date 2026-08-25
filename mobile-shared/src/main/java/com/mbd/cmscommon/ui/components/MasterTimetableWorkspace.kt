package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.MasterTimetableSummary
import com.mbd.cmscommon.domain.model.PeriodType
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.domain.model.masterTimetableSummary
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

private val MasterCanvas = Color(0xFFF7F5F0)
private val MasterGreen = Color(0xFF2F6B4F)
private val MasterGold = Color(0xFF9A651B)
private val MasterRed = Color(0xFFB43A31)
private val MasterBlue = Color(0xFF24577A)
private val MasterDays = listOf(
    DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY,
)

@Composable
fun MasterTimetableWorkspace(
    day: DayOfWeek,
    shift: Session,
    departments: List<Department>,
    sessions: List<AcademicSession>,
    periods: List<SessionPeriod>,
    loading: Boolean,
    errorMessage: String?,
    onDayChange: (DayOfWeek) -> Unit,
    onShiftChange: (Session) -> Unit,
    onRetry: () -> Unit,
    onOpenSession: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }

    val shiftSessions = sessions.filter { it.shift == shift }
    val summary = masterTimetableSummary(shiftSessions, periods)

    val visibleSessions = shiftSessions
        .filter { session ->
            if (query.isBlank()) return@filter true
            val dept = departments.firstOrNull { it.deptId == session.deptId }
            (dept?.name ?: "").contains(query, ignoreCase = true) ||
                (dept?.code ?: "").contains(query, ignoreCase = true) ||
                (session.programName ?: "").contains(query, ignoreCase = true)
        }
        .sortedWith(compareBy({ departments.firstOrNull { d -> d.deptId == it.deptId }?.name ?: it.deptId }, { it.startYear }))

    LazyColumn(
        modifier = modifier.fillMaxWidth().background(MasterCanvas),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { MasterHeader(day, shift) }

        if (!errorMessage.isNullOrBlank()) {
            item { MasterNotice(errorMessage, onRetry) }
        }

        item { MasterSummary(summary, loading) }

        item {
            AssignmentNotice(summary.unassignedTeacherCount, summary.missingRoomCount)
        }

        item {
            MasterControls(
                day = day,
                shift = shift,
                query = query,
                onDayChange = onDayChange,
                onShiftChange = onShiftChange,
                onQueryChange = { query = it },
            )
        }

        when {
            loading -> items(3) { SkeletonRow() }
            shiftSessions.isEmpty() -> item {
                MasterEmptyCard("No sessions match your search", "Create a session inside a department to begin scheduling.")
            }
            visibleSessions.isEmpty() -> item {
                MasterEmptyCard("No sessions match your search", "Try a department code, program, or intake year.")
            }
            else -> items(visibleSessions, key = { it.sessionId }) { session ->
                val dept = departments.firstOrNull { it.deptId == session.deptId }
                val sessionPeriods = periods.filter { it.sessionId == session.sessionId && it.day == day }
                    .sortedBy { it.startTime }
                MasterSessionCard(session, dept?.name ?: session.deptId, day, sessionPeriods, onOpenSession = { onOpenSession(session.sessionId) })
            }
        }

        item { Spacer(Modifier.height(72.dp)) }
    }
}

@Composable
private fun MasterHeader(day: DayOfWeek, shift: Session) {
    Surface(shape = RoundedCornerShape(18.dp), color = Color(0xFF252321)) {
        Column(Modifier.padding(20.dp)) {
            Text("COLLEGE-WIDE SCHEDULE", color = MasterGold, style = CmsTextStyles.eyebrow)
            Spacer(Modifier.height(6.dp))
            Text("Master Timetable", color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            Text(
                "${day.getDisplayName(TextStyle.FULL, Locale.ENGLISH)} · $shift",
                color = CmsTheme.colors.onInkMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun MasterSummary(summary: MasterTimetableSummary, loading: Boolean) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        MasterMetric(if (loading) "--" else summary.sessionCount.toString(), "Sessions", Modifier.weight(1f))
        MasterMetric(if (loading) "--" else summary.periodCount.toString(), "Classes", Modifier.weight(1f))
        MasterMetric(if (loading) "--" else summary.timeSlotCount.toString(), "Time slots", Modifier.weight(1f))
        MasterMetric(if (loading) "--" else summary.coveragePercentage?.let { "$it%" } ?: "--", "Coverage", Modifier.weight(1f))
    }
}

@Composable
private fun MasterMetric(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E0D7))) {
        Column(Modifier.padding(14.dp)) {
            Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Text(label.uppercase(), color = Color(0xFF77716A), style = CmsTextStyles.eyebrow)
        }
    }
}

@Composable
private fun AssignmentNotice(teachers: Int, rooms: Int) {
    if (teachers == 0 && rooms == 0) return
    Surface(shape = RoundedCornerShape(14.dp), color = MasterRed.copy(alpha = 0.08f), border = BorderStroke(1.dp, MasterRed.copy(alpha = 0.25f))) {
        Column(Modifier.padding(14.dp)) {
            Text("Setup gaps", color = MasterRed, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
            if (teachers > 0) Text("$teachers class(es) have no teacher assigned.", color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
            if (rooms > 0) Text("$rooms class(es) have no room assigned.", color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun MasterControls(
    day: DayOfWeek,
    shift: Session,
    query: String,
    onDayChange: (DayOfWeek) -> Unit,
    onShiftChange: (Session) -> Unit,
    onQueryChange: (String) -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MasterDays.forEach { option ->
                CmsChip(option.getDisplayName(TextStyle.SHORT, Locale.ENGLISH), selected = day == option, onClick = { onDayChange(option) })
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Session.entries.forEach { option ->
                CmsChip(option.name, selected = shift == option, onClick = { onShiftChange(option) })
            }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search by department, program, or intake year") },
            singleLine = true,
        )
    }
}

@Composable
private fun MasterSessionCard(
    session: AcademicSession,
    departmentName: String,
    day: DayOfWeek,
    periods: List<SessionPeriod>,
    onOpenSession: () -> Unit,
) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E0D7))) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("$departmentName · ${session.label}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(session.programName?.takeIf { it.isNotBlank() } ?: "Program not configured", color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
                }
                TextButton(onClick = onOpenSession) { Text("View schedule") }
            }
            Spacer(Modifier.height(8.dp))
            if (periods.isEmpty()) {
                Text("No periods on ${day.getDisplayName(TextStyle.FULL, Locale.ENGLISH)}.", color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
            } else {
                periods.forEach { period ->
                    val isBreak = period.periodType == PeriodType.BREAK
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(period.timeRange, modifier = Modifier.weight(0.3f), color = MasterBlue, style = MaterialTheme.typography.bodySmall)
                        Column(Modifier.weight(0.7f)) {
                            Text(if (isBreak) "BREAK" else period.subjectName, style = MaterialTheme.typography.bodySmall)
                            if (!isBreak) {
                                Text(
                                    (period.teacherName.ifBlank { "Teacher unassigned" }) + " · Room " + (period.roomNo?.ifBlank { "--" } ?: "--"),
                                    color = if (period.teacherId.isBlank() || period.roomNo.isNullOrBlank()) MasterGold else Color(0xFF77716A),
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MasterNotice(message: String, onRetry: () -> Unit) {
    Surface(shape = RoundedCornerShape(14.dp), color = MasterRed.copy(alpha = 0.1f), border = BorderStroke(1.dp, MasterRed.copy(alpha = 0.25f))) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(message, modifier = Modifier.weight(1f), color = MasterRed, style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onRetry) { Text("Retry", color = MasterRed) }
        }
    }
}

@Composable
private fun MasterEmptyCard(title: String, detail: String) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E0D7))) {
        Column(Modifier.padding(24.dp)) {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(detail, color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
        }
    }
}
