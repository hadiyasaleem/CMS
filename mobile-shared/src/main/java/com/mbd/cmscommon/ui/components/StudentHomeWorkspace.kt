package com.mbd.cmscommon.ui.components

import compose.icons.TablerIcons
import compose.icons.tablericons.Calendar
import compose.icons.tablericons.ClipboardCheck
import compose.icons.tablericons.CreditCard
import compose.icons.tablericons.Report
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
import com.mbd.cmscommon.domain.model.StudentHomeSnapshot
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmscommon.ui.theme.ModInk
import com.mbd.cmscommon.ui.theme.ModMuted
import com.mbd.cmscommon.ui.theme.ModTrack
import com.mbd.cmscommon.ui.theme.ModGround
import com.mbd.cmscommon.ui.theme.ModSurface
import com.mbd.cmscommon.ui.theme.ModSuccess
import com.mbd.cmscommon.ui.theme.ModAccent
import com.mbd.cmscommon.ui.theme.ModWarn
import com.mbd.cmscommon.ui.theme.ModRedTint

private val StudentHomeCanvas = ModGround
private val StudentHomeBlue = ModInk
private val StudentHomeGreen = ModSuccess
private val StudentHomeGold = ModWarn
private val StudentHomeRed = ModAccent

enum class StudentHomeDestination { ATTENDANCE, MARKS, TIMETABLE, FEES }

private data class StudentHomeAction(val title: String, val icon: ImageVector, val destination: StudentHomeDestination)

private val STUDENT_HOME_ACTIONS = listOf(
    StudentHomeAction("Attendance", TablerIcons.ClipboardCheck, StudentHomeDestination.ATTENDANCE),
    StudentHomeAction("Marks", TablerIcons.Report, StudentHomeDestination.MARKS),
    StudentHomeAction("Timetable", TablerIcons.Calendar, StudentHomeDestination.TIMETABLE),
    StudentHomeAction("Fee Challan", TablerIcons.CreditCard, StudentHomeDestination.FEES),
)

@Composable
fun StudentHomeWorkspace(
    heroPainter: Painter,
    snapshot: StudentHomeSnapshot?,
    loading: Boolean,
    onOpen: (StudentHomeDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth().background(StudentHomeCanvas),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { StudentHomeHero(heroPainter, snapshot) }

        if (loading && snapshot == null) {
            items(3) { SkeletonRow() }
        } else if (snapshot != null) {
            item { TodaysScheduleCard(snapshot) }
            item {
                Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
                    Column(Modifier.padding(16.dp)) {
                        StudentStandingRow("CGPA / GPA", snapshot.gpaLabel, last = false)
                        StudentStandingRow("Semester", snapshot.semesterLabel, last = false)
                        StudentStandingRow("Subjects recorded", snapshot.subjectCount.toString(), last = false)
                        StudentStandingRow("Lectures today", snapshot.todaysClasses.size.toString(), last = true)
                    }
                }
            }
            val weakestSubject = snapshot.weakestSubject
            if (weakestSubject != null && weakestSubject.percent < 75f) {
                item {
                    Surface(shape = RoundedCornerShape(14.dp), color = ModRedTint, border = BorderStroke(1.dp, StudentHomeRed.copy(alpha = 0.3f))) {
                        Text(
                            "ATTENDANCE NEEDS ATTENTION: ${weakestSubject.courseCode} at ${weakestSubject.percent.toInt()}%. Review your subject attendance before the next class.",
                            modifier = Modifier.padding(14.dp),
                            color = StudentHomeRed,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
            items(STUDENT_HOME_ACTIONS) { action -> StudentHomeActionCard(action, onClick = { onOpen(action.destination) }) }
        }

        item { Spacer(Modifier.height(72.dp)) }
    }
}

@Composable
private fun StudentHomeHero(heroPainter: Painter, snapshot: StudentHomeSnapshot?) {
    Surface(modifier = Modifier.fillMaxWidth().height(150.dp), shape = RoundedCornerShape(18.dp), color = ModInk) {
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
                Text("Assalam-o-Alaikum, ${snapshot?.name ?: "Student"}", color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(4.dp))
                Text(snapshot?.let { "Roll ${it.rollNumber} · ${it.programLine}" } ?: "Academic dashboard", color = CmsTheme.colors.onInkMuted, style = MaterialTheme.typography.bodySmall)
                if (snapshot != null) {
                    Spacer(Modifier.height(8.dp))
                    Text("${snapshot.overallAttendance.toInt()}% attendance", color = studentAttendanceColor(snapshot.overallAttendance), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

private fun studentAttendanceColor(percent: Float): Color = when {
    percent >= 75f -> StudentHomeGreen
    percent >= 70f -> StudentHomeGold
    else -> StudentHomeRed
}

/** Mirrors the teacher app's own "Today's classes" card: every lecture still scheduled today, in
 * order, instead of just a single "next class" lookahead. */
@Composable
private fun TodaysScheduleCard(snapshot: StudentHomeSnapshot) {
    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(16.dp)) {
            Text("TODAY'S CLASSES", color = ModMuted, style = CmsTextStyles.eyebrow)
            Spacer(Modifier.height(6.dp))
            if (snapshot.todaysClasses.isEmpty()) {
                Text("No lectures scheduled today", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            } else {
                snapshot.todaysClasses.forEach { period -> StudentClassRow(period, isNext = period.id == snapshot.nextClassId) }
            }
        }
    }
}

@Composable
private fun StudentClassRow(period: com.mbd.cmscommon.domain.model.SessionPeriod, isNext: Boolean) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(period.timeRange, modifier = Modifier.width(90.dp), color = StudentHomeBlue, style = MaterialTheme.typography.bodySmall)
        Column(Modifier.weight(1f)) {
            Text(period.subjectName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            val meta = listOfNotNull(
                period.teacherLabel.ifBlank { null },
                listOfNotNull(period.building?.ifBlank { null }, period.roomNo?.ifBlank { null }).joinToString(" / ").ifBlank { null },
            ).joinToString(" · ")
            if (meta.isNotBlank()) Text(meta, color = ModMuted, style = MaterialTheme.typography.bodySmall)
        }
        if (isNext) StatusBadge("NEXT", BadgeTone.Navy)
    }
}

@Composable
private fun StudentHomeActionCard(action: StudentHomeAction, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = ModSurface,
        border = BorderStroke(1.dp, ModTrack),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(action.icon, contentDescription = null, tint = StudentHomeBlue)
            Spacer(Modifier.width(12.dp))
            Text(action.title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun StudentStandingRow(label: String, value: String, last: Boolean) {
    Row(Modifier.fillMaxWidth().padding(vertical = if (last) 0.dp else 4.dp)) {
        Text(label, modifier = Modifier.weight(1f), color = ModMuted, style = MaterialTheme.typography.bodyMedium)
        Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
    }
}
