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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Grading
import androidx.compose.material.icons.filled.Payments
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

private val StudentHomeCanvas = Color(0xFFF7F5F0)
private val StudentHomeBlue = Color(0xFF24577A)
private val StudentHomeGreen = Color(0xFF2F6B4F)
private val StudentHomeGold = Color(0xFF9A651B)
private val StudentHomeRed = Color(0xFFB43A31)

enum class StudentHomeDestination { ATTENDANCE, MARKS, TIMETABLE, FEES }

private data class StudentHomeAction(val title: String, val detail: String, val icon: ImageVector, val destination: StudentHomeDestination)

private val STUDENT_HOME_ACTIONS = listOf(
    StudentHomeAction("Attendance", "Subject-wise presence and shortage", Icons.Filled.FactCheck, StudentHomeDestination.ATTENDANCE),
    StudentHomeAction("Marks", "Midterm and sessional scores", Icons.Filled.Grading, StudentHomeDestination.MARKS),
    StudentHomeAction("Timetable", "Weekly classes, rooms and teachers", Icons.Filled.CalendarMonth, StudentHomeDestination.TIMETABLE),
    StudentHomeAction("Fee Challan", "Current session fee information", Icons.Filled.Payments, StudentHomeDestination.FEES),
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
            item { NextStudentClassCard(snapshot) }
            item {
                Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E0D7))) {
                    Column(Modifier.padding(16.dp)) {
                        StudentStandingRow("CGPA / GPA", snapshot.gpaLabel, last = false)
                        StudentStandingRow("Semester", snapshot.semesterLabel, last = false)
                        StudentStandingRow("Subjects recorded", snapshot.subjectCount.toString(), last = false)
                        StudentStandingRow("Lectures today", snapshot.lecturesToday.toString(), last = true)
                    }
                }
            }
            if (snapshot.weakestSubject != null && snapshot.weakestSubject.percent < 75f) {
                item {
                    Surface(shape = RoundedCornerShape(14.dp), color = Color(0xFFFFEFEB), border = BorderStroke(1.dp, StudentHomeRed.copy(alpha = 0.3f))) {
                        Text(
                            "ATTENDANCE NEEDS ATTENTION: ${snapshot.weakestSubject.courseCode} at ${snapshot.weakestSubject.percent.toInt()}%. Review your subject attendance before the next class.",
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
    Surface(modifier = Modifier.fillMaxWidth().height(150.dp), shape = RoundedCornerShape(18.dp), color = Color(0xFF252321)) {
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

@Composable
private fun NextStudentClassCard(snapshot: StudentHomeSnapshot) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E0D7))) {
        Column(Modifier.padding(16.dp)) {
            Text("NEXT CLASS", color = Color(0xFF77716A), style = CmsTextStyles.eyebrow)
            Spacer(Modifier.height(6.dp))
            val next = snapshot.nextClass
            if (next == null) {
                Text("No upcoming lecture scheduled", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("Your active timetable has no later lecture this week.", color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
            } else {
                Text(next.subjectName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                StudentClassMeta(Icons.Filled.CalendarMonth, "${next.dayLabel} · ${next.timeRange}")
                StudentClassMeta(Icons.Filled.FactCheck, next.teacherName.ifBlank { "Teacher not assigned" })
                if (!next.location.isNullOrBlank()) {
                    StudentClassMeta(Icons.Filled.Payments, next.location)
                }
            }
        }
    }
}

@Composable
private fun StudentClassMeta(icon: ImageVector, value: String) {
    Row(Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = StudentHomeBlue, modifier = Modifier.height(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(value, color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun StudentHomeActionCard(action: StudentHomeAction, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE5E0D7)),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(action.icon, contentDescription = null, tint = StudentHomeBlue)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(action.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(action.detail, color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun StudentStandingRow(label: String, value: String, last: Boolean) {
    Row(Modifier.fillMaxWidth().padding(vertical = if (last) 0.dp else 4.dp)) {
        Text(label, modifier = Modifier.weight(1f), color = Color(0xFF77716A), style = MaterialTheme.typography.bodyMedium)
        Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
    }
}
