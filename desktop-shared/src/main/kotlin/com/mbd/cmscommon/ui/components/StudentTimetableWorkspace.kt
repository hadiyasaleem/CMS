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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.domain.model.PeriodType
import com.mbd.cmscommon.domain.model.StudentScheduledPeriod
import com.mbd.cmscommon.domain.model.StudentTimetableSnapshot
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmscommon.ui.theme.ModInk
import com.mbd.cmscommon.ui.theme.ModMuted
import com.mbd.cmscommon.ui.theme.ModTrack
import com.mbd.cmscommon.ui.theme.ModGround
import com.mbd.cmscommon.ui.theme.ModSurface
import com.mbd.cmscommon.ui.theme.ModAccent
import com.mbd.cmscommon.ui.theme.ModWarn
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val TimetableCanvas = ModGround
private val TimetableBlue = ModInk
private val TimetableRed = ModAccent
private val DayFormat = DateTimeFormatter.ofPattern("EEE, dd MMM")

@Composable
fun StudentTimetableWorkspace(
    heroPainter: Painter,
    snapshot: StudentTimetableSnapshot?,
    loading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth().background(TimetableCanvas),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { StudentTimetableHeader(heroPainter) }

        if (!errorMessage.isNullOrBlank()) {
            item { ErrorCard(errorMessage, onRetry) }
        }

        when {
            loading && snapshot == null -> items(3) { SkeletonRow() }
            snapshot != null -> {
                item { TimetableOverview(snapshot) }
                val nextLecture = snapshot.nextLecture
                if (nextLecture != null) {
                    item { NextLectureCard(nextLecture) }
                }
                val lectures = snapshot.periods.filter { it.period.periodType == PeriodType.LECTURE && it.period.courseCode.isNotBlank() }
                if (lectures.isEmpty()) {
                    item {
                        Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
                            Text("No lectures scheduled this week.", modifier = Modifier.padding(24.dp), color = ModMuted, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                } else {
                    items(lectures, key = { it.period.id }) { item -> PeriodCard(item) }
                }
            }
        }

        item { Spacer(Modifier.height(72.dp)) }
    }
}

@Composable
private fun StudentTimetableHeader(heroPainter: Painter) {
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
                Text("MY WEEK", color = ModWarn, style = CmsTextStyles.eyebrow)
                Spacer(Modifier.height(6.dp))
                Text("Timetable", color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(4.dp))
                Text("Student timetable and campus schedule", color = CmsTheme.colors.onInkMuted, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun TimetableOverview(snapshot: StudentTimetableSnapshot) {
    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(16.dp)) {
            Text("${snapshot.weekStart.format(DayFormat)} - ${snapshot.weekEnd.format(DayFormat)}", color = ModMuted, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ScheduleMetric(snapshot.lectureCount.toString(), "Lectures")
                ScheduleMetric(snapshot.classDays.toString(), "Class days")
                ScheduleMetric(formatDuration(snapshot.weeklyMinutes), "Weekly time")
                ScheduleMetric(snapshot.todayPeriods.toString(), "Today")
            }
        }
    }
}

private fun formatDuration(minutes: Int): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return when {
        hours == 0 -> "${mins}m"
        mins == 0 -> "${hours}h"
        else -> "${hours}h ${mins}m"
    }
}

@Composable
private fun ScheduleMetric(value: String, label: String) {
    Column {
        Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Text(label.uppercase(), color = ModMuted, style = CmsTextStyles.eyebrow)
    }
}

@Composable
private fun NextLectureCard(item: StudentScheduledPeriod) {
    val today = LocalDate.now()
    val dayLabel = when {
        item.date.isEqual(today) -> "Today"
        else -> item.date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH)
    }
    Surface(shape = RoundedCornerShape(14.dp), color = TimetableBlue.copy(alpha = 0.08f), border = BorderStroke(1.dp, TimetableBlue.copy(alpha = 0.25f))) {
        Column(Modifier.padding(14.dp)) {
            Text("NEXT CLASS", color = TimetableBlue, style = CmsTextStyles.eyebrow)
            Text(item.period.subjectName, color = TimetableBlue, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text("$dayLabel · ${item.period.timeRange}", color = ModMuted, style = MaterialTheme.typography.bodySmall)
            Text("Know where you need to be next", color = ModMuted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun PeriodCard(item: StudentScheduledPeriod) {
    Surface(shape = RoundedCornerShape(14.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(item.period.subjectName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text("${item.date.format(DayFormat)} · ${item.period.timeRange}", color = TimetableBlue, style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(6.dp))
            DetailRow(Icons.Filled.Person, item.period.teacherName.ifBlank { "Teacher not assigned" })
            DetailRow(Icons.Filled.LocationOn, listOfNotNull(item.period.building, item.period.roomNo).joinToString(" / ").ifBlank { "Location not assigned" })
        }
    }
}

@Composable
private fun DetailRow(icon: ImageVector, text: String) {
    Row(Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = ModMuted, modifier = Modifier.height(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, color = ModMuted, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit) {
    Surface(shape = RoundedCornerShape(14.dp), color = TimetableRed.copy(alpha = 0.1f), border = BorderStroke(1.dp, TimetableRed.copy(alpha = 0.25f))) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(message, modifier = Modifier.weight(1f), color = TimetableRed, style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onRetry) { Text("Retry", color = TimetableRed) }
        }
    }
}
