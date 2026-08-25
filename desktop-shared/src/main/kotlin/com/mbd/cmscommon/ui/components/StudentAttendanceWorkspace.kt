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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.controller.StudentAttendanceSnapshot
import com.mbd.cmscommon.controller.SubjectAttendanceRow
import com.mbd.cmscommon.domain.model.SubjectType
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme

private val AttendanceCanvas = Color(0xFFF7F5F0)
private val AttendanceGreen = Color(0xFF2F6B4F)
private val AttendanceGold = Color(0xFF9A651B)
private val AttendanceRed = Color(0xFFB43A31)
private val AttendanceBlue = Color(0xFF24577A)

@Composable
fun StudentAttendanceWorkspace(
    heroPainter: Painter,
    snapshot: StudentAttendanceSnapshot?,
    loading: Boolean,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth().background(AttendanceCanvas),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { AttendanceHero(heroPainter, snapshot) }

        if (loading && snapshot == null) {
            items(3) { SkeletonRow() }
        } else if (snapshot != null) {
            item { EligibilityTracker(snapshot) }
            if (snapshot.rows.isEmpty()) {
                item {
                    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E0D7))) {
                        Text(
                            "Your subject-wise attendance will appear here as teachers record classes.",
                            modifier = Modifier.padding(24.dp),
                            color = Color(0xFF77716A),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            } else {
                items(snapshot.rows, key = { it.courseCode }) { row -> SubjectAttendanceCard(row) }
            }
        }

        item { Spacer(Modifier.height(72.dp)) }
    }
}

@Composable
private fun AttendanceHero(heroPainter: Painter, snapshot: StudentAttendanceSnapshot?) {
    val percent = snapshot?.overallPercent ?: 0f
    val tone = when {
        snapshot == null -> AttendanceBlue
        percent >= 75f -> AttendanceGreen
        percent >= 70f -> AttendanceGold
        else -> AttendanceRed
    }

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
            Row(Modifier.align(Alignment.CenterStart).padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("MY RECORD", color = AttendanceGold, style = CmsTextStyles.eyebrow)
                    Spacer(Modifier.height(6.dp))
                    Text("Attendance", color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(4.dp))
                    Text("Subject-wise presence and the 75% eligibility threshold", color = CmsTheme.colors.onInkMuted, style = MaterialTheme.typography.bodySmall)
                }
                if (snapshot != null) {
                    Text("${percent.toInt()}%", color = tone, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.displaySmall)
                }
            }
        }
    }
}

@Composable
private fun EligibilityTracker(snapshot: StudentAttendanceSnapshot) {
    val percent = snapshot.overallPercent
    val tone = when {
        percent >= 75f -> AttendanceGreen
        percent >= 70f -> AttendanceGold
        else -> AttendanceRed
    }
    val headline = when {
        percent >= 75f -> "Overall attendance is on track"
        percent >= 70f -> "Attendance needs attention"
        else -> "Attendance is at serious risk"
    }
    val guidance = when {
        percent >= 75f -> "Overall attendance is at ${percent.toInt()}%. All recorded subjects eligible."
        percent >= 70f -> "You are at the 75% threshold; the next missed lecture would put eligibility at risk."
        else -> "Overall attendance is ${percent.toInt()}%. Protect your 75% attendance by attending upcoming lectures."
    }

    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, tone.copy(alpha = 0.3f))) {
        Column(Modifier.padding(16.dp)) {
            Text("ELIGIBILITY TRACKER", color = Color(0xFF77716A), style = CmsTextStyles.eyebrow)
            Spacer(Modifier.height(6.dp))
            Text(headline, color = tone, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(guidance, color = Color(0xFF625E58), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AttendanceCount("Total", snapshot.markedLectures, AttendanceBlue)
                AttendanceCount("Attended", snapshot.attendedLectures, AttendanceGreen)
                AttendanceCount("At risk", snapshot.subjectsAtRisk, if (snapshot.subjectsAtRisk > 0) AttendanceRed else AttendanceGreen)
            }
            snapshot.weakestSubject?.let { weakest ->
                Spacer(Modifier.height(8.dp))
                Text("Lowest recorded subject: ${weakest.subjectName} (${weakest.percentage.toInt()}%)", color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun AttendanceCount(label: String, value: Int, color: Color) {
    Column {
        Text(value.toString(), color = color, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Text(label.uppercase(), color = Color(0xFF77716A), style = CmsTextStyles.eyebrow)
    }
}

@Composable
private fun SubjectAttendanceCard(row: SubjectAttendanceRow) {
    val tone = when {
        row.total == 0 -> AttendanceBlue
        row.percentage >= 75f -> AttendanceGreen
        row.percentage >= 70f -> AttendanceGold
        else -> AttendanceRed
    }
    Surface(shape = RoundedCornerShape(14.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E0D7))) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(row.subjectName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text(
                        listOfNotNull(row.courseCode, row.semester?.let { "Semester $it" }, row.subjectType?.let { if (it == SubjectType.LAB) "Lab" else "Theory" }).joinToString(" · "),
                        color = Color(0xFF77716A),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Text(if (row.total == 0) "Not marked" else "${row.percentage.toInt()}%", color = tone, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
            if (row.total == 0) {
                Spacer(Modifier.height(6.dp))
                Text("Attendance has not been marked for this subject yet.", color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
            } else {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusBadge("Present ${row.present}", BadgeTone.Success)
                    StatusBadge("Absent ${row.absent}", BadgeTone.Error)
                    StatusBadge("Leave ${row.leave}", BadgeTone.Gold)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    if (row.percentage >= 75f) {
                        "You can miss ${row.missesAvailableAbove75} more lecture(s) and stay above 75%."
                    } else {
                        "Attend the next ${row.lecturesNeededFor75} lecture(s) to reach 75%."
                    },
                    color = Color(0xFF77716A),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
