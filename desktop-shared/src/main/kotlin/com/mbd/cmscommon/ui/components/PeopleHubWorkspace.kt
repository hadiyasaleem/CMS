package com.mbd.cmscommon.ui.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.HowToReg
import androidx.compose.material.icons.outlined.School
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
import com.mbd.cmscommon.domain.model.PeopleHubSnapshot
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

private val PeopleCanvas = ModGround
private val PeopleNavy = ModInk
private val PeopleBlue = ModInk
private val PeopleGreen = ModSuccess
private val PeopleGold = ModWarn
private val PeopleRed = ModAccent

enum class PeopleDestination { ADMINISTRATORS, TEACHERS, STUDENTS, LINK_REQUESTS, MARK_EDIT_REQUESTS, EXAM_PAPER_REVIEW }

private data class PeopleCard(
    val destination: PeopleDestination,
    val title: String,
    val detail: String,
    val status: String,
    val icon: ImageVector,
    val tone: Color,
)

@Composable
fun PeopleHubWorkspace(
    heroPainter: Painter,
    snapshot: PeopleHubSnapshot?,
    loading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    onOpen: (PeopleDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val columns = when {
            maxWidth < 700.dp -> 2
            maxWidth < 1100.dp -> 3
            else -> 4
        }
        CardGrid(Modifier.fillMaxWidth().background(PeopleCanvas), columns = columns) {
            fullSpanItem { PeopleHeader(heroPainter) }
            if (!errorMessage.isNullOrBlank()) {
                fullSpanItem { PeopleNotice(errorMessage, "Retry", onRetry) }
            }
            fullSpanItem { PeopleSummary(snapshot, loading) }

            if (loading && snapshot == null) {
                fullSpanItems(3) { PeopleSkeleton() }
            } else if (snapshot != null) {
                items(peopleCards(snapshot), key = { it.destination }) { card ->
                    PeopleActionCard(card, onClick = { onOpen(card.destination) })
                }
            }

            fullSpanItem { Spacer(Modifier.height(72.dp)) }
        }
    }
}

@Composable
private fun PeopleHeader(heroPainter: Painter) {
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
                Text("COLLEGE COMMUNITY", color = PeopleGold, style = CmsTextStyles.eyebrow)
                Spacer(Modifier.height(6.dp))
                Text("People", color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(4.dp))
                Text("Administrators, faculty, students, and account requests.", color = CmsTheme.colors.onInkMuted, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun PeopleSummary(snapshot: PeopleHubSnapshot?, loading: Boolean) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        PeopleMetric(if (loading || snapshot == null) "--" else snapshot.teacherCount.toString(), "Teachers", Modifier.weight(1f))
        PeopleMetric(if (loading || snapshot == null) "--" else snapshot.studentCount.toString(), "Students", Modifier.weight(1f))
        PeopleMetric(
            if (loading || snapshot == null) "--" else snapshot.pendingReviews.toString(),
            "Pending reviews",
            Modifier.weight(1f),
            alert = (snapshot?.pendingReviews ?: 0) > 0,
        )
    }
}

@Composable
private fun PeopleMetric(value: String, label: String, modifier: Modifier = Modifier, alert: Boolean = false) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(14.dp)) {
            Text(value, color = if (alert) PeopleRed else ModInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Text(label.uppercase(), color = ModMuted, style = CmsTextStyles.eyebrow)
        }
    }
}

private fun peopleCards(snapshot: PeopleHubSnapshot): List<PeopleCard> = listOf(
    PeopleCard(
        PeopleDestination.ADMINISTRATORS, "Administrators",
        "Create and review full-access administrator accounts.",
        if (snapshot.inactiveAdministratorCount == 0) {
            "${snapshot.activeAdministratorCount} active"
        } else {
            "${snapshot.activeAdministratorCount} active / ${snapshot.inactiveAdministratorCount} unavailable"
        },
        Icons.Outlined.AdminPanelSettings, PeopleNavy,
    ),
    PeopleCard(
        PeopleDestination.TEACHERS, "Teachers",
        "Manage faculty profiles, lifecycle status, and permissions.",
        "${snapshot.teacherCount} active · ${snapshot.delegatedTeacherCount} delegated",
        Icons.Outlined.Groups, PeopleBlue,
    ),
    PeopleCard(
        PeopleDestination.STUDENTS, "Student Rosters",
        "Open departments, then choose a session to manage its students.",
        "${snapshot.studentCount} enrolled student(s)",
        Icons.Outlined.School, PeopleGreen,
    ),
    PeopleCard(
        PeopleDestination.LINK_REQUESTS, "Student Link Requests",
        "Verify student claims before connecting app accounts to rosters.",
        "${snapshot.pendingLinkRequests} awaiting review" + if (snapshot.repeatLinkRequests > 0) " / ${snapshot.repeatLinkRequests} repeat" else "",
        Icons.Outlined.HowToReg, if (snapshot.pendingLinkRequests > 0) PeopleRed else PeopleGreen,
    ),
    PeopleCard(
        PeopleDestination.MARK_EDIT_REQUESTS, "Mark Edit Requests",
        "Review teacher requests to change locked assessment scores.",
        "${snapshot.pendingMarkEdits} awaiting review",
        Icons.Outlined.EditNote, if (snapshot.pendingMarkEdits > 0) PeopleGold else PeopleGreen,
    ),
    PeopleCard(
        PeopleDestination.EXAM_PAPER_REVIEW, "Exam Paper Review",
        "Review teacher-submitted exam papers and attach answer keys.",
        "${snapshot.pendingExamReviews} awaiting review",
        Icons.Outlined.Assignment, if (snapshot.pendingExamReviews > 0) PeopleGold else PeopleGreen,
    ),
)

@Composable
private fun PeopleActionCard(card: PeopleCard, onClick: () -> Unit) {
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
            Spacer(Modifier.height(4.dp))
            Text(card.detail, color = ModMuted, style = MaterialTheme.typography.bodySmall, maxLines = 3, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(8.dp))
            Text(card.status, color = card.tone, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun PeopleNotice(message: String, action: String, onAction: () -> Unit) {
    Surface(shape = RoundedCornerShape(14.dp), color = PeopleRed.copy(alpha = 0.1f), border = BorderStroke(1.dp, PeopleRed.copy(alpha = 0.25f))) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(message, modifier = Modifier.weight(1f), color = PeopleRed, style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onAction) { Text(action, color = PeopleRed) }
        }
    }
}

@Composable
private fun PeopleSkeleton() {
    SkeletonRow()
}
