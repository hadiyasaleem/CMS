package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.PeriodType
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmscommon.ui.theme.ModAccent
import com.mbd.cmscommon.ui.theme.ModGround
import com.mbd.cmscommon.ui.theme.ModInk
import com.mbd.cmscommon.ui.theme.ModMuted
import com.mbd.cmscommon.ui.theme.ModSuccess
import com.mbd.cmscommon.ui.theme.ModSurface
import com.mbd.cmscommon.ui.theme.ModTrack
import com.mbd.cmscommon.ui.theme.ModWarn
import com.mbd.cmscommon.util.Outcome
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

private val ScheduleCanvas = ModGround
private val ScheduleBorder = ModTrack
private val ScheduleGreen = ModSuccess
private val ScheduleGold = ModWarn
private val ScheduleRed = ModAccent
private val ScheduleBlue = ModInk
private val ScheduleDays = listOf(
    DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY,
)

@Composable
fun TeacherScheduleWorkspace(
    heroPainter: Painter,
    periods: List<SessionPeriod>,
    sessions: List<AcademicSession>,
    selectedDay: DayOfWeek,
    outcome: Outcome<Unit>,
    onSelectDay: (DayOfWeek) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val teachingPeriods = periods.filter { it.periodType != PeriodType.BREAK && it.courseCode.isNotBlank() }
    val classDays = teachingPeriods.map { it.day }.distinct().size
    val totalMinutes = teachingPeriods.sumOf { period ->
        val start = scheduleTime(period.startTime)
        val end = scheduleTime(period.endTime)
        if (start != null && end != null && end.isAfter(start)) java.time.Duration.between(start, end).toMinutes().toInt() else 0
    }
    val rooms = teachingPeriods.mapNotNull { it.roomNo?.takeIf { r -> r.isNotBlank() } }.distinct().size
    val busiest = ScheduleDays.maxByOrNull { day -> teachingPeriods.count { it.day == day } }

    val dayPeriods = teachingPeriods.filter { it.day == selectedDay }.sortedBy { scheduleTime(it.startTime) ?: java.time.LocalTime.MAX }

    LazyColumn(
        modifier = modifier.fillMaxWidth().background(ScheduleCanvas),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { ScheduleHeader(heroPainter, teachingPeriods.size) }

        if (outcome is Outcome.Error) {
            item { ScheduleNotice(outcome.message, ScheduleRed, "Retry", onRefresh) }
        }

        item { ScheduleMetrics(teachingPeriods.size, classDays, totalMinutes, rooms, busiest) }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ScheduleDays.forEach { day -> CmsChip(day.getDisplayName(TextStyle.SHORT, Locale.ENGLISH), selected = selectedDay == day, onClick = { onSelectDay(day) }) }
            }
        }

        if (dayPeriods.isEmpty()) {
            item {
                Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ScheduleBorder)) {
                    Text(
                        if (teachingPeriods.isEmpty()) "No periods are assigned to you yet." else "No periods are scheduled for ${selectedDay.getDisplayName(TextStyle.FULL, Locale.ENGLISH)}.",
                        modifier = Modifier.padding(24.dp),
                        color = ModMuted,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        } else {
            items(dayPeriods, key = { it.id }) { period ->
                SchedulePeriodCard(period, sessions.firstOrNull { it.sessionId == period.sessionId })
            }
        }

        item { Spacer(Modifier.height(72.dp)) }
    }
}

@Composable
private fun ScheduleHeader(heroPainter: Painter, total: Int) {
    Surface(modifier = Modifier.fillMaxWidth().height(140.dp), shape = RoundedCornerShape(18.dp), color = ModInk) {
        Box(Modifier.fillMaxSize()) {
            Image(
                painter = heroPainter,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                alignment = Alignment.CenterEnd,
                contentScale = ContentScale.Crop,
                alpha = 0.35f,
            )
            Column(Modifier.align(Alignment.CenterStart).padding(20.dp)) {
                Text("FACULTY WORKSPACE", color = ScheduleGold, style = CmsTextStyles.eyebrow)
                Spacer(Modifier.height(6.dp))
                Text("My schedule", color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(4.dp))
                Text("$total period(s) across your assigned sessions", color = CmsTheme.colors.onInkMuted, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun ScheduleMetrics(total: Int, days: Int, minutes: Int, rooms: Int, busiest: DayOfWeek?) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ScheduleMetric(total.toString(), "Periods", Modifier.weight(1f))
        ScheduleMetric(days.toString(), "Teaching days", Modifier.weight(1f))
        ScheduleMetric(formatMinutes(minutes), "Weekly time", Modifier.weight(1f))
        ScheduleMetric(busiest?.getDisplayName(TextStyle.SHORT, Locale.ENGLISH) ?: "None", "Busiest day", Modifier.weight(1f))
    }
}

private fun formatMinutes(minutes: Int): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return when {
        hours == 0 -> "${mins}m"
        mins == 0 -> "${hours}h"
        else -> "${hours}h ${mins}m"
    }
}

@Composable
private fun ScheduleMetric(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = ModSurface, border = BorderStroke(1.dp, ScheduleBorder)) {
        Column(Modifier.padding(14.dp)) {
            Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(label.uppercase(), color = ModMuted, style = CmsTextStyles.eyebrow)
        }
    }
}

@Composable
private fun SchedulePeriodCard(period: SessionPeriod, session: AcademicSession?) {
    Surface(shape = RoundedCornerShape(14.dp), color = ModSurface, border = BorderStroke(1.dp, ScheduleBorder)) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(period.subjectName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text(session?.label ?: period.sessionId, color = ModMuted, style = MaterialTheme.typography.bodySmall)
                }
                StatusBadge(period.timeRange, BadgeTone.Navy)
            }
            Spacer(Modifier.height(6.dp))
            Text("Room ${listOfNotNull(period.building, period.roomNo).joinToString(" / ").ifBlank { "not assigned" }}", color = ModMuted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ScheduleNotice(message: String, color: Color, action: String, onAction: () -> Unit) {
    Surface(shape = RoundedCornerShape(14.dp), color = color.copy(alpha = 0.1f), border = BorderStroke(1.dp, color.copy(alpha = 0.25f))) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(message, modifier = Modifier.weight(1f), color = color, style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onAction) { Text(action, color = color) }
        }
    }
}
private val scheduleTimeFormatter = java.time.format.DateTimeFormatter.ofPattern("H:mm")

private fun scheduleTime(value: String?): java.time.LocalTime? =
    value?.trim()?.let { runCatching { java.time.LocalTime.parse(it, scheduleTimeFormatter) }.getOrNull() }
