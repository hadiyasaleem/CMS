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
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.Grading
import androidx.compose.material.icons.outlined.TrendingUp
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
import com.mbd.cmscommon.domain.model.StudentExamsHubSnapshot
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import java.time.format.DateTimeFormatter

private val StudentExamsCanvas = Color(0xFFF7F5F0)
private val StudentExamsGold = Color(0xFF9A651B)
private val StudentExamsRed = Color(0xFFB43A31)
private val ExamDateFormat = DateTimeFormatter.ofPattern("dd MMM yyyy")

enum class StudentExamsDestination { MARKS, RESULTS, DATESHEETS }

private data class StudentExamCard(
    val title: String,
    val subtitle: String,
    val value: String,
    val valueLabel: String,
    val status: String,
    val tone: BadgeTone,
    val icon: ImageVector,
    val destination: StudentExamsDestination,
)

@Composable
fun StudentExamsHubWorkspace(
    heroPainter: Painter,
    snapshot: StudentExamsHubSnapshot?,
    loading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    onOpen: (StudentExamsDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cards = snapshot?.let { buildStudentExamCards(it) }.orEmpty()

    LazyColumn(
        modifier = modifier.fillMaxWidth().background(StudentExamsCanvas),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { StudentExamsHeader(heroPainter) }
        if (!errorMessage.isNullOrBlank()) {
            item { StudentExamsNotice(errorMessage, onRetry) }
        }

        if (loading && snapshot == null) {
            items(3) { SkeletonRow() }
        } else {
            items(cards, key = { it.destination }) { card -> StudentExamNavigationCard(card, onClick = { onOpen(card.destination) }) }
        }

        item { Spacer(Modifier.height(72.dp)) }
    }
}

private fun buildStudentExamCards(snapshot: StudentExamsHubSnapshot): List<StudentExamCard> {
    val marksSubtitle = "Midterm and sessional scores entered for ${snapshot.subjectsWithScores} ${if (snapshot.subjectsWithScores == 1) "subject" else "subjects"}"
    val marksStatus = if (snapshot.absentAssessments == 0) "No absences" else "${snapshot.absentAssessments} absent"

    val resultsSubtitle = if (snapshot.recordedSemesters == 0) "Semester GPA and CGPA progression will appear after publication" else "Academic progression across recorded semesters"
    val resultsValue = snapshot.currentCgpa?.let { "%.2f".format(it) } ?: "--"
    val resultsStatus = when {
        snapshot.activeSupplyCourses > 0 -> "${snapshot.activeSupplyCourses} supply"
        snapshot.recordedSemesters > 0 -> "${snapshot.recordedSemesters} semesters"
        else -> "Awaiting results"
    }
    val resultsTone = when {
        snapshot.activeSupplyCourses > 0 -> BadgeTone.Warning
        snapshot.recordedSemesters > 0 -> BadgeTone.Success
        else -> BadgeTone.Neutral
    }

    val datesheetSubtitle = snapshot.nextExamDate?.let { "Next exam on ${it.format(ExamDateFormat)}" } ?: "Published exam schedules and paper venues"
    val datesheetStatus = if (snapshot.publishedDatesheets == 0) "No schedule" else "${snapshot.publishedDatesheets} published"
    val datesheetTone = if (snapshot.publishedDatesheets == 0) BadgeTone.Neutral else BadgeTone.Success

    return listOf(
        StudentExamCard("Marks", marksSubtitle, snapshot.enteredAssessments.toString(), "assessments entered", marksStatus, if (snapshot.absentAssessments == 0) BadgeTone.Success else BadgeTone.Warning, Icons.Outlined.Grading, StudentExamsDestination.MARKS),
        StudentExamCard("Results", resultsSubtitle, resultsValue, "current CGPA", resultsStatus, resultsTone, Icons.Outlined.TrendingUp, StudentExamsDestination.RESULTS),
        StudentExamCard("Datesheets", datesheetSubtitle, snapshot.upcomingPapers.toString(), "upcoming papers", datesheetStatus, datesheetTone, Icons.Outlined.EventNote, StudentExamsDestination.DATESHEETS),
    )
}

@Composable
private fun StudentExamsHeader(heroPainter: Painter) {
    Surface(modifier = Modifier.fillMaxWidth().height(140.dp), shape = RoundedCornerShape(18.dp), color = Color(0xFF252321)) {
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
                Text("ASSESSMENT WORKSPACE", color = StudentExamsGold, style = CmsTextStyles.eyebrow)
                Spacer(Modifier.height(6.dp))
                Text("Exams", color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(4.dp))
                Text("Marks, results, and exam schedules in one place.", color = CmsTheme.colors.onInkMuted, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun StudentExamNavigationCard(card: StudentExamCard, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE5E0D7)),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(card.icon, contentDescription = null, tint = Color(0xFF24577A))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(card.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(card.subtitle, color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(6.dp))
                ExamSummaryMetric(card.value, card.valueLabel)
            }
            StatusBadge(card.status, card.tone)
        }
    }
}

@Composable
private fun ExamSummaryMetric(value: String, label: String) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.width(6.dp))
        Text(label, color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun StudentExamsNotice(message: String, onRetry: () -> Unit) {
    Surface(shape = RoundedCornerShape(14.dp), color = StudentExamsRed.copy(alpha = 0.1f), border = BorderStroke(1.dp, StudentExamsRed.copy(alpha = 0.25f))) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(message, modifier = Modifier.weight(1f), color = StudentExamsRed, style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onRetry) { Text("Retry", color = StudentExamsRed) }
        }
    }
}
