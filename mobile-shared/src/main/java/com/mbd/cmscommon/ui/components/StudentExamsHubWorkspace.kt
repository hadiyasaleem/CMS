package com.mbd.cmscommon.ui.components

import compose.icons.TablerIcons
import compose.icons.tablericons.CalendarStats
import compose.icons.tablericons.Report
import compose.icons.tablericons.TrendingUp
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
import com.mbd.cmscommon.ui.theme.ModInk
import com.mbd.cmscommon.ui.theme.ModMuted
import com.mbd.cmscommon.ui.theme.ModTrack
import com.mbd.cmscommon.ui.theme.ModGround
import com.mbd.cmscommon.ui.theme.ModSurface
import com.mbd.cmscommon.ui.theme.ModWarn
import java.time.format.DateTimeFormatter

private val StudentExamsCanvas = ModGround
private val StudentExamsGold = ModWarn
private val ExamDateFormat = DateTimeFormatter.ofPattern("dd MMM yyyy")

enum class StudentExamsDestination { MARKS, RESULTS, DATESHEETS }

private data class StudentExamCard(
    val title: String,
    val subtitle: String?,
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
            item { CmsNotice(errorMessage, tone = NoticeTone.Error, actionLabel = "Retry", onAction = onRetry) }
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
    val marksStatus = if (snapshot.absentAssessments == 0) "No absences" else "${snapshot.absentAssessments} absent"

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

    val datesheetSubtitle = snapshot.nextExamDate?.let { "Next exam on ${it.format(ExamDateFormat)}" }
    val datesheetStatus = if (snapshot.publishedDatesheets == 0) "No schedule" else "${snapshot.publishedDatesheets} published"
    val datesheetTone = if (snapshot.publishedDatesheets == 0) BadgeTone.Neutral else BadgeTone.Success

    return listOf(
        StudentExamCard("Marks", null, snapshot.enteredAssessments.toString(), "assessments entered", marksStatus, if (snapshot.absentAssessments == 0) BadgeTone.Success else BadgeTone.Warning, TablerIcons.Report, StudentExamsDestination.MARKS),
        StudentExamCard("Results", null, resultsValue, "current CGPA", resultsStatus, resultsTone, TablerIcons.TrendingUp, StudentExamsDestination.RESULTS),
        StudentExamCard("Datesheets", datesheetSubtitle, snapshot.upcomingPapers.toString(), "upcoming papers", datesheetStatus, datesheetTone, TablerIcons.CalendarStats, StudentExamsDestination.DATESHEETS),
    )
}

@Composable
private fun StudentExamsHeader(heroPainter: Painter) {
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
                Text("ASSESSMENT WORKSPACE", color = StudentExamsGold, style = CmsTextStyles.eyebrow)
                Spacer(Modifier.height(6.dp))
                Text("Exams", color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
            }
        }
    }
}

@Composable
private fun StudentExamNavigationCard(card: StudentExamCard, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = ModSurface,
        border = BorderStroke(1.dp, ModTrack),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(card.icon, contentDescription = null, tint = ModInk)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(card.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                if (card.subtitle != null) {
                    Text(card.subtitle, color = ModMuted, style = MaterialTheme.typography.bodySmall)
                }
            }
            StatusBadge(card.status, card.tone)
        }
    }
}

