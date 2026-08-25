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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.HowToReg
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Schedule
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
import com.mbd.cmscommon.domain.model.RecordsHubSnapshot
import com.mbd.cmscommon.domain.model.RecordsSummarySource
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme

private val RecordsCanvas = Color(0xFFF7F5F0)
private val RecordsNavy = Color(0xFF2F4B7A)
private val RecordsBlue = Color(0xFF24577A)
private val RecordsGreen = Color(0xFF2F6B4F)
private val RecordsGold = Color(0xFF9A651B)
private val RecordsRed = Color(0xFFB43A31)

enum class RecordsDestination { ATTENDANCE, CALENDAR, DATESHEETS, DOCUMENTS, TIMETABLE, FEES, INSIGHTS }

private data class RecordsCard(
    val destination: RecordsDestination,
    val title: String,
    val detail: String,
    val status: String,
    val icon: ImageVector,
    val tone: Color,
    val source: RecordsSummarySource,
    val unavailable: Boolean = false,
)

@Composable
fun RecordsHubWorkspace(
    heroPainter: Painter,
    snapshot: RecordsHubSnapshot?,
    loading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    onOpen: (RecordsDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth().background(RecordsCanvas),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { RecordsHeader(heroPainter) }
        if (!errorMessage.isNullOrBlank()) {
            item { RecordsNotice(errorMessage, "Retry", onRetry) }
        }
        if (snapshot != null) {
            item { RecordsSummaryRow(snapshot) }
        }

        if (loading && snapshot == null) {
            items(3) { SkeletonRow() }
        } else if (snapshot != null) {
            items(recordsCards(snapshot), key = { it.destination }) { card -> RecordsActionCard(card, onClick = { onOpen(card.destination) }) }
        }

        item { Spacer(Modifier.height(72.dp)) }
    }
}

@Composable
private fun RecordsHeader(heroPainter: Painter) {
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
                Text("COLLEGE RECORDS", color = RecordsGold, style = CmsTextStyles.eyebrow)
                Spacer(Modifier.height(6.dp))
                Text("Records", color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(4.dp))
                Text("Attendance, calendar, datesheets, documents, timetable, fees, and insights.", color = CmsTheme.colors.onInkMuted, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun RecordsSummaryRow(snapshot: RecordsHubSnapshot) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        RecordsMetric(snapshot.activeSessions.toString(), "Active sessions", Modifier.weight(1f))
        RecordsMetric(snapshot.publishedResources.toString(), "Published", Modifier.weight(1f))
        RecordsMetric(snapshot.atRiskStudents.toString(), "At risk", Modifier.weight(1f), alert = snapshot.atRiskStudents > 0)
    }
}

@Composable
private fun RecordsMetric(value: String, label: String, modifier: Modifier = Modifier, alert: Boolean = false) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E0D7))) {
        Column(Modifier.padding(14.dp)) {
            Text(value, color = if (alert) RecordsRed else Color(0xFF252321), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Text(label.uppercase(), color = Color(0xFF77716A), style = CmsTextStyles.eyebrow)
        }
    }
}

private fun recordsCards(snapshot: RecordsHubSnapshot): List<RecordsCard> = listOf(
    RecordsCard(
        RecordsDestination.ATTENDANCE, "Attendance Records",
        "Semester summaries, monthly totals, and day-by-day registers.",
        "${snapshot.activeSessions} active session(s)",
        Icons.Outlined.HowToReg, RecordsBlue, RecordsSummarySource.SESSIONS,
        RecordsSummarySource.SESSIONS in snapshot.unavailableSources,
    ),
    RecordsCard(
        RecordsDestination.CALENDAR, "Calendar",
        "College holidays, events, exams, and deadlines.",
        "${snapshot.upcomingEvents} upcoming",
        Icons.Outlined.EventAvailable, RecordsGreen, RecordsSummarySource.CALENDAR,
        RecordsSummarySource.CALENDAR in snapshot.unavailableSources,
    ),
    RecordsCard(
        RecordsDestination.DATESHEETS, "Datesheets",
        "Build exam schedules, assign rooms, and publish to students.",
        "${snapshot.publishedDatesheets} published · ${snapshot.draftDatesheets} draft",
        Icons.Outlined.CalendarMonth, RecordsGold, RecordsSummarySource.DATESHEETS,
        RecordsSummarySource.DATESHEETS in snapshot.unavailableSources,
    ),
    RecordsCard(
        RecordsDestination.DOCUMENTS, "Documents",
        "Publish prospectuses, rules, reports, and shared files.",
        "${snapshot.publishedDocuments} published · ${snapshot.draftDocuments} draft",
        Icons.Outlined.Description, RecordsNavy, RecordsSummarySource.DOCUMENTS,
        RecordsSummarySource.DOCUMENTS in snapshot.unavailableSources,
    ),
    RecordsCard(
        RecordsDestination.TIMETABLE, "Master Timetable",
        "Review every active session by day and shift.",
        "${snapshot.activeSessions} session(s) in scope",
        Icons.Outlined.Schedule, RecordsBlue, RecordsSummarySource.SESSIONS,
        RecordsSummarySource.SESSIONS in snapshot.unavailableSources,
    ),
    RecordsCard(
        RecordsDestination.FEES, "Fee Structures",
        "Open a department and manage each session's fee plan.",
        "${snapshot.activeSessions} session(s) in scope",
        Icons.Outlined.Payments, RecordsGold, RecordsSummarySource.SESSIONS,
        RecordsSummarySource.SESSIONS in snapshot.unavailableSources,
    ),
    RecordsCard(
        RecordsDestination.INSIGHTS, "Academic Insights",
        "Review performance, assessment, and student-risk signals.",
        "${snapshot.atRiskStudents} student(s) flagged",
        Icons.Outlined.Assessment, if (snapshot.atRiskStudents > 0) RecordsRed else RecordsGreen, RecordsSummarySource.INSIGHTS,
        RecordsSummarySource.INSIGHTS in snapshot.unavailableSources,
    ),
)

@Composable
private fun RecordsActionCard(card: RecordsCard, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, card.tone.copy(alpha = 0.25f)),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).background(card.tone.copy(alpha = 0.12f), RoundedCornerShape(13.dp)), contentAlignment = Alignment.Center) {
                Icon(card.icon, contentDescription = null, tint = card.tone)
            }
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(card.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(card.detail, color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(4.dp))
                Text(if (card.unavailable) "Data unavailable - tap to retry" else card.status, color = if (card.unavailable) RecordsRed else card.tone, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun RecordsNotice(message: String, action: String, onAction: () -> Unit) {
    Surface(shape = RoundedCornerShape(14.dp), color = RecordsRed.copy(alpha = 0.1f), border = BorderStroke(1.dp, RecordsRed.copy(alpha = 0.25f))) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(message, modifier = Modifier.weight(1f), color = RecordsRed, style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onAction) { Text(action, color = RecordsRed) }
        }
    }
}
