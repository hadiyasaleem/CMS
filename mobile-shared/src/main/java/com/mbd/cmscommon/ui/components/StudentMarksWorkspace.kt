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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.controller.StudentMarksSnapshot
import com.mbd.cmscommon.controller.SubjectMarksRow
import com.mbd.cmscommon.domain.model.SubjectType
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme

private val MarksCanvas = Color(0xFFF7F5F0)
private val MarksGreen = Color(0xFF2F6B4F)
private val MarksGold = Color(0xFF9A651B)
private val MarksRed = Color(0xFFB43A31)
private val MarksBlue = Color(0xFF24577A)

@Composable
fun StudentMarksWorkspace(
    snapshot: StudentMarksSnapshot?,
    loading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth().background(MarksCanvas),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { StudentMarksHeader() }

        if (!errorMessage.isNullOrBlank()) {
            item {
                Surface(shape = RoundedCornerShape(14.dp), color = MarksRed.copy(alpha = 0.1f), border = BorderStroke(1.dp, MarksRed.copy(alpha = 0.25f))) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(errorMessage, modifier = Modifier.weight(1f), color = MarksRed, style = MaterialTheme.typography.bodyMedium)
                        TextButton(onClick = onRetry) { Text("Retry", color = MarksRed) }
                    }
                }
            }
        }

        when {
            loading && snapshot == null -> items(3) { SkeletonRow() }
            snapshot != null -> {
                item { MarksOverviewCard(snapshot) }
                if (snapshot.strongestSubject != null) {
                    item { MarksHighlightCard(snapshot) }
                }
                if (snapshot.rows.isEmpty()) {
                    item {
                        Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E0D7))) {
                            Text("Marks are awaiting entry", modifier = Modifier.padding(24.dp), color = Color(0xFF77716A), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                } else {
                    items(snapshot.rows, key = { it.courseCode }) { row -> SubjectMarksCard(row) }
                }
            }
        }

        item { Spacer(Modifier.height(72.dp)) }
    }
}

@Composable
private fun StudentMarksHeader() {
    Surface(shape = RoundedCornerShape(18.dp), color = Color(0xFF252321)) {
        Column(Modifier.padding(20.dp)) {
            Text("ASSESSMENT", color = MarksGold, style = CmsTextStyles.eyebrow)
            Spacer(Modifier.height(6.dp))
            Text("My marks", color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            Text("Midterm and sessional scores recorded by your teachers", color = CmsTheme.colors.onInkMuted, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun MarksOverviewCard(snapshot: StudentMarksSnapshot) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E0D7))) {
        Column(Modifier.padding(16.dp)) {
            Text("RECORDED TOTAL", color = Color(0xFF77716A), style = CmsTextStyles.eyebrow)
            Spacer(Modifier.height(6.dp))
            Text(
                snapshot.percentage?.let { "${snapshot.earnedMarks} / ${snapshot.availableMarks} (${it.toInt()}%)" } ?: "Not entered",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MarksMetric("Recorded", snapshot.assessmentsEntered.toString())
                MarksMetric("Absent", snapshot.absentAssessments.toString())
                MarksMetric("Subjects", snapshot.fullyRecordedSubjects.toString())
            }
        }
    }
}

@Composable
private fun MarksMetric(label: String, value: String) {
    Column {
        Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Text(label.uppercase(), color = Color(0xFF77716A), style = CmsTextStyles.eyebrow)
    }
}

@Composable
private fun MarksHighlightCard(snapshot: StudentMarksSnapshot) {
    val subject = snapshot.strongestSubject ?: return
    Surface(shape = RoundedCornerShape(14.dp), color = MarksGreen.copy(alpha = 0.08f), border = BorderStroke(1.dp, MarksGreen.copy(alpha = 0.25f))) {
        Column(Modifier.padding(14.dp)) {
            Text("HIGHEST RECORDED SUBJECT", color = MarksGreen, style = CmsTextStyles.eyebrow)
            Text("${subject.subjectName} · ${subject.percentage?.toInt() ?: 0}%", color = MarksGreen, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun SubjectMarksCard(row: SubjectMarksRow) {
    Surface(shape = RoundedCornerShape(14.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E0D7))) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(row.subjectName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text(
                        listOfNotNull(row.courseCode, row.semester?.let { "Semester $it" }, row.subjectType?.let { if (it == SubjectType.LAB) "Lab" else "Theory" }, if (row.isElective) "Elective" else null).joinToString(" · "),
                        color = Color(0xFF77716A),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                val percentage = row.percentage
                if (percentage != null) {
                    Text("${percentage.toInt()}%", color = MarksBlue, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AssessmentScore("Midterm", row.midterm, row.midtermMaxMarks, row.midtermAbsent)
                AssessmentScore("Sessional", row.sessional, row.sessionalMaxMarks, row.sessionalAbsent)
            }
        }
    }
}

@Composable
private fun AssessmentScore(label: String, score: Int?, maxMarks: Int?, absent: Boolean) {
    Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFF7F5F0)) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(
                when {
                    absent -> "Absent"
                    maxMarks == null -> "Pending"
                    else -> "${score ?: 0} / $maxMarks"
                },
                fontWeight = FontWeight.Bold,
                color = if (absent) MarksRed else if (maxMarks == null) MarksGold else Color(0xFF252321),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(label.uppercase(), color = Color(0xFF77716A), style = CmsTextStyles.eyebrow)
        }
    }
}
