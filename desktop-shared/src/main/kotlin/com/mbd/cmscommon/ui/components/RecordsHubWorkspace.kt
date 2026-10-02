package com.mbd.cmscommon.ui.components

import compose.icons.TablerIcons
import compose.icons.tablericons.Calendar
import compose.icons.tablericons.CalendarEvent
import compose.icons.tablericons.ChartBar
import compose.icons.tablericons.Clock
import compose.icons.tablericons.CreditCard
import compose.icons.tablericons.TrendingUp
import compose.icons.tablericons.UserCheck
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.domain.model.RecordsHubSnapshot
import com.mbd.cmscommon.domain.model.RecordsSummarySource
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

private val RecordsCanvas = ModGround
private val RecordsNavy = ModInk
private val RecordsBlue = ModInk
private val RecordsGreen = ModSuccess
private val RecordsGold = ModWarn
private val RecordsRed = ModAccent

enum class RecordsDestination { ATTENDANCE, CALENDAR, DATESHEETS, TIMETABLE, FEES, INSIGHTS, SEMESTER_RESULTS }

private data class RecordsCard(
    val destination: RecordsDestination,
    val title: String,
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
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val columns = when {
            maxWidth < 700.dp -> 2
            maxWidth < 1100.dp -> 3
            else -> 4
        }
        CardGrid(Modifier.fillMaxWidth().background(RecordsCanvas), columns = columns) {
            if (!errorMessage.isNullOrBlank()) {
                fullSpanItem { CmsNotice(errorMessage, tone = NoticeTone.Error, actionLabel = "Retry", onAction = onRetry) }
            }
            if (snapshot != null) {
                fullSpanItem { RecordsSummaryRow(snapshot) }
            }

            if (loading && snapshot == null) {
                fullSpanItems(3) { SkeletonRow() }
            } else if (snapshot != null) {
                items(recordsCards(snapshot), key = { it.destination }) { card -> RecordsActionCard(card, onClick = { onOpen(card.destination) }) }
            }

            fullSpanItem { Spacer(Modifier.height(72.dp)) }
        }
    }
}

@Composable
private fun RecordsSummaryRow(snapshot: RecordsHubSnapshot) {
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        RecordsMetric(snapshot.activeSessions.toString(), "Active sessions", Modifier.weight(1f).fillMaxHeight())
        RecordsMetric(snapshot.publishedResources.toString(), "Published", Modifier.weight(1f).fillMaxHeight())
        RecordsMetric(snapshot.atRiskStudents.toString(), "At risk", Modifier.weight(1f).fillMaxHeight(), alert = snapshot.atRiskStudents > 0)
    }
}

@Composable
private fun RecordsMetric(value: String, label: String, modifier: Modifier = Modifier, alert: Boolean = false) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(14.dp)) {
            Text(value, color = if (alert) RecordsRed else ModInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Text(label.uppercase(), color = ModMuted, style = CmsTextStyles.eyebrow)
        }
    }
}

private fun recordsCards(snapshot: RecordsHubSnapshot): List<RecordsCard> = listOf(
    RecordsCard(
        RecordsDestination.ATTENDANCE, "Attendance Records",
        "${snapshot.activeSessions} active session(s)",
        TablerIcons.UserCheck, RecordsBlue, RecordsSummarySource.SESSIONS,
        RecordsSummarySource.SESSIONS in snapshot.unavailableSources,
    ),
    RecordsCard(
        RecordsDestination.CALENDAR, "Calendar",
        "${snapshot.upcomingEvents} upcoming",
        TablerIcons.CalendarEvent, RecordsGreen, RecordsSummarySource.CALENDAR,
        RecordsSummarySource.CALENDAR in snapshot.unavailableSources,
    ),
    RecordsCard(
        RecordsDestination.DATESHEETS, "Datesheets",
        "${snapshot.publishedDatesheets} published · ${snapshot.draftDatesheets} draft",
        TablerIcons.Calendar, RecordsGold, RecordsSummarySource.DATESHEETS,
        RecordsSummarySource.DATESHEETS in snapshot.unavailableSources,
    ),
    RecordsCard(
        RecordsDestination.TIMETABLE, "Master Timetable",
        "${snapshot.activeSessions} session(s) in scope",
        TablerIcons.Clock, RecordsBlue, RecordsSummarySource.SESSIONS,
        RecordsSummarySource.SESSIONS in snapshot.unavailableSources,
    ),
    RecordsCard(
        RecordsDestination.FEES, "Fee Structures",
        "${snapshot.activeSessions} session(s) in scope",
        TablerIcons.CreditCard, RecordsGold, RecordsSummarySource.SESSIONS,
        RecordsSummarySource.SESSIONS in snapshot.unavailableSources,
    ),
    RecordsCard(
        RecordsDestination.INSIGHTS, "Academic Insights",
        "${snapshot.atRiskStudents} student(s) flagged",
        TablerIcons.ChartBar, if (snapshot.atRiskStudents > 0) RecordsRed else RecordsGreen, RecordsSummarySource.INSIGHTS,
        RecordsSummarySource.INSIGHTS in snapshot.unavailableSources,
    ),
    RecordsCard(
        RecordsDestination.SEMESTER_RESULTS, "Semester Results",
        "${snapshot.activeSessions} session(s) in scope",
        TablerIcons.TrendingUp, RecordsGreen, RecordsSummarySource.SESSIONS,
        RecordsSummarySource.SESSIONS in snapshot.unavailableSources,
    ),
)

@Composable
private fun RecordsActionCard(card: RecordsCard, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxHeight().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = ModSurface,
        border = BorderStroke(1.dp, card.tone.copy(alpha = 0.25f)),
    ) {
        Column(Modifier.padding(16.dp).heightIn(min = 168.dp)) {
            Box(Modifier.size(44.dp).background(card.tone.copy(alpha = 0.12f), RoundedCornerShape(13.dp)), contentAlignment = Alignment.Center) {
                Icon(card.icon, contentDescription = null, tint = card.tone)
            }
            Spacer(Modifier.height(12.dp))
            Text(card.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(8.dp))
            Text(
                if (card.unavailable) "Data unavailable - tap to retry" else card.status,
                color = if (card.unavailable) RecordsRed else card.tone,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

