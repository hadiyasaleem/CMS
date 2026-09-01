package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FactCheck
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.domain.model.TeacherHomeSnapshot
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme

/** Design-system tone roles for the home action cards; resolved to [CmsTheme] tokens at render. */
private enum class HomeTone { SUCCESS, NAVY, WARN, ACCENT }

@Composable
private fun HomeTone.color(): Color = when (this) {
    HomeTone.SUCCESS -> CmsTheme.colors.success
    HomeTone.NAVY -> CmsTheme.colors.navy
    HomeTone.WARN -> CmsTheme.colors.warn
    HomeTone.ACCENT -> CmsTheme.colors.accent
}

enum class TeacherHomeDestination { ATTENDANCE, MARKS, EXAM_PAPER, STUDENTS, SCHEDULE, NOTIFICATIONS }

private data class HomeAction(val destination: TeacherHomeDestination, val title: String, val detail: String, val icon: ImageVector, val tone: HomeTone)

private val TEACHER_HOME_ACTIONS = listOf(
    HomeAction(TeacherHomeDestination.ATTENDANCE, "Mark Attendance", "Record today's class", Icons.Outlined.FactCheck, HomeTone.SUCCESS),
    HomeAction(TeacherHomeDestination.MARKS, "Marks Entry", "Assessments and scores", Icons.Outlined.RateReview, HomeTone.NAVY),
    HomeAction(TeacherHomeDestination.EXAM_PAPER, "Exam Paper", "Submit a paper", Icons.Outlined.UploadFile, HomeTone.WARN),
    HomeAction(TeacherHomeDestination.STUDENTS, "My Students", "Rosters and progress", Icons.Outlined.Groups, HomeTone.NAVY),
    HomeAction(TeacherHomeDestination.SCHEDULE, "My Schedule", "Full teaching week", Icons.Outlined.CalendarMonth, HomeTone.SUCCESS),
    HomeAction(TeacherHomeDestination.NOTIFICATIONS, "Notifications", "Faculty updates", Icons.Outlined.Notifications, HomeTone.ACCENT),
)

@Composable
fun TeacherHomeWorkspace(
    heroPainter: Painter,
    snapshot: TeacherHomeSnapshot,
    onOpen: (TeacherHomeDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { HomeHeader(heroPainter, snapshot) }
        item { HomeMetrics(snapshot) }
        item { TodayCard(snapshot) }
        item { WeeklyLoadCard(snapshot) }
        items(TEACHER_HOME_ACTIONS) { action -> HomeActionCard(action, onClick = { onOpen(action.destination) }) }
        item { Spacer(Modifier.height(72.dp)) }
    }
}

@Composable
private fun HomeHeader(heroPainter: Painter, snapshot: TeacherHomeSnapshot) {
    Surface(modifier = Modifier.fillMaxWidth().height(140.dp), shape = RoundedCornerShape(18.dp), color = CmsTheme.colors.ink) {
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
                Text("Good day, ${snapshot.name}", color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(4.dp))
                Text(snapshot.dateLabel, color = CmsTheme.colors.onInkMuted, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun HomeMetrics(snapshot: TeacherHomeSnapshot) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        HomeMetric(snapshot.assignedSubjects.toString(), "Subjects", Modifier.weight(1f))
        HomeMetric(snapshot.assignedSessions.toString(), "Sessions", Modifier.weight(1f))
        HomeMetric(snapshot.weeklyLectures.toString(), "This week", Modifier.weight(1f))
    }
}

@Composable
private fun HomeMetric(value: String, label: String, modifier: Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceBright, border = BorderStroke(1.dp, CmsTheme.colors.track)) {
        Column(Modifier.padding(14.dp)) {
            Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Text(label.uppercase(), color = CmsTheme.colors.muted, style = CmsTextStyles.eyebrow)
        }
    }
}

@Composable
private fun TodayCard(snapshot: TeacherHomeSnapshot) {
    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceBright, border = BorderStroke(1.dp, CmsTheme.colors.track)) {
        Column(Modifier.padding(16.dp)) {
            Text("TODAY'S CLASSES", color = CmsTheme.colors.muted, style = CmsTextStyles.eyebrow)
            Spacer(Modifier.height(6.dp))
            if (snapshot.todaysClasses.isEmpty()) {
                Text("No lectures scheduled today.", color = CmsTheme.colors.muted, style = MaterialTheme.typography.bodyMedium)
                Text("Your teaching overview is clear for the rest of today.", color = CmsTheme.colors.muted, style = MaterialTheme.typography.bodySmall)
            } else {
                snapshot.todaysClasses.forEach { period -> ClassRow(period, isNext = period.id == snapshot.nextClass?.id) }
            }
        }
    }
}

@Composable
private fun ClassRow(period: SessionPeriod, isNext: Boolean) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(period.timeRange, modifier = Modifier.width(90.dp), color = CmsTheme.colors.navy, style = MaterialTheme.typography.bodySmall)
        Column(Modifier.weight(1f)) {
            Text(period.subjectName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text(listOfNotNull(period.building, period.roomNo).joinToString(" / "), color = CmsTheme.colors.muted, style = MaterialTheme.typography.bodySmall)
        }
        if (isNext) StatusBadge("NEXT", BadgeTone.Navy)
    }
}

@Composable
private fun WeeklyLoadCard(snapshot: TeacherHomeSnapshot) {
    val barTone = CmsTheme.colors.navy
    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceBright, border = BorderStroke(1.dp, CmsTheme.colors.track)) {
        Column(Modifier.padding(16.dp)) {
            Text("WEEKLY LOAD", color = CmsTheme.colors.muted, style = CmsTextStyles.eyebrow)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                snapshot.weeklyLoad.forEach { day ->
                    Column(Modifier.weight(1f)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (day.isToday) barTone else barTone.copy(alpha = (0.15f + 0.15f * day.count).coerceAtMost(0.9f)),
                        ) {
                            Text(
                                day.count.toString(),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                color = CmsTheme.colors.onInk,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                        Text(day.label, modifier = Modifier.fillMaxWidth(), color = CmsTheme.colors.muted, textAlign = androidx.compose.ui.text.style.TextAlign.Center, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Busiest: ${snapshot.busiestDay}", color = CmsTheme.colors.muted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun HomeActionCard(action: HomeAction, onClick: () -> Unit) {
    val tone = action.tone.color()
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceBright,
        border = BorderStroke(1.dp, tone.copy(alpha = 0.25f)),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(action.icon, contentDescription = null, tint = tone)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(action.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(action.detail, color = CmsTheme.colors.muted, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
