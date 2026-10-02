package com.mbd.cmscommon.ui.components

import compose.icons.TablerIcons
import compose.icons.tablericons.CalendarStats
import compose.icons.tablericons.Clipboard
import compose.icons.tablericons.TrendingUp
import compose.icons.tablericons.Upload
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.mbd.cmscommon.domain.model.ExamsHubSnapshot
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

private val ExamCanvas = ModGround
private val ExamBorder = ModTrack
private val ExamBlue = ModInk
private val ExamGreen = ModSuccess
private val ExamGold = ModWarn
private val ExamRed = ModAccent

enum class ExamsDestination { MARKS, EXAM_PAPER, RESULTS, DATESHEETS }

data class ExamAction(
    val destination: ExamsDestination,
    val title: String,
    val status: String,
    val icon: ImageVector,
    val tone: Color,
)

@Composable
fun ExamsHubWorkspace(
    heroPainter: Painter,
    snapshot: ExamsHubSnapshot,
    loading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    onOpen: (ExamsDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val actions = listOf(
        ExamAction(
            ExamsDestination.MARKS, "Marks Entry",
            "${snapshot.assignedClasses} assigned class(es)",
            TablerIcons.Clipboard, ExamBlue,
        ),
        ExamAction(
            ExamsDestination.EXAM_PAPER, "Submit Exam Paper",
            "${snapshot.classesWithPapers}/${snapshot.assignedClasses} covered · ${snapshot.paperSubmissions} file(s)",
            TablerIcons.Upload, if (snapshot.paperCoveragePercent >= 100) ExamGreen else ExamGold,
        ),
        ExamAction(
            ExamsDestination.RESULTS, "Semester Results",
            "${snapshot.assignedSessions} assigned session(s)",
            TablerIcons.TrendingUp, ExamGreen,
        ),
        ExamAction(
            ExamsDestination.DATESHEETS, "Datesheets",
            "${snapshot.publishedDatesheets} published · ${snapshot.upcomingInvigilationSlots} duty",
            TablerIcons.CalendarStats, if (snapshot.upcomingInvigilationSlots > 0) ExamRed else ExamBlue,
        ),
    )

    LazyColumn(
        modifier = modifier.fillMaxWidth().background(ExamCanvas),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (!errorMessage.isNullOrBlank()) {
            item { CmsNotice(errorMessage, tone = NoticeTone.Error, actionLabel = "Retry", onAction = onRetry) }
        }
        item { ExamMetrics(snapshot, loading) }
        items(actions, key = { it.destination }) { action -> ExamActionCard(action, onClick = { onOpen(action.destination) }) }
        item { Spacer(Modifier.height(72.dp)) }
    }
}

private data class ExamMetricItem(val value: String, val label: String, val alert: Boolean)

@Composable
private fun ExamMetrics(snapshot: ExamsHubSnapshot, loading: Boolean) {
    val metrics = listOf(
        ExamMetricItem(if (loading) "--" else snapshot.assignedClasses.toString(), "Classes", alert = false),
        ExamMetricItem(if (loading) "--" else "${snapshot.paperCoveragePercent}%", "Paper coverage", alert = snapshot.paperCoveragePercent < 100),
        ExamMetricItem(if (loading) "--" else snapshot.publishedDatesheets.toString(), "Datesheets", alert = false),
        ExamMetricItem(if (loading) "--" else snapshot.upcomingInvigilationSlots.toString(), "Duties", alert = snapshot.upcomingInvigilationSlots > 0),
    )
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val columns = if (maxWidth >= 900.dp) metrics.size else 2
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            metrics.chunked(columns).forEach { rowItems ->
                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    rowItems.forEach { metric -> ExamMetric(metric.value, metric.label, Modifier.weight(1f).fillMaxHeight(), alert = metric.alert) }
                    repeat(columns - rowItems.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun ExamMetric(value: String, label: String, modifier: Modifier = Modifier, alert: Boolean = false) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = ModSurface, border = BorderStroke(1.dp, ExamBorder)) {
        Column(Modifier.padding(14.dp)) {
            Text(value, color = if (alert) ExamRed else ModInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Text(label.uppercase(), color = ModMuted, style = CmsTextStyles.eyebrow)
        }
    }
}

@Composable
private fun ExamActionCard(action: ExamAction, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = ModSurface,
        border = BorderStroke(1.dp, action.tone.copy(alpha = 0.25f)),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).background(action.tone.copy(alpha = 0.12f), RoundedCornerShape(13.dp)), contentAlignment = Alignment.Center) {
                Icon(action.icon, contentDescription = null, tint = action.tone)
            }
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(action.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(action.status, color = action.tone, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

