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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.domain.model.StudentMoreSnapshot
import com.mbd.cmscommon.domain.model.startDateOrNull
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import java.time.format.DateTimeFormatter

private val StudentMoreCanvas = Color(0xFFF7F5F0)
private val StudentMoreGold = Color(0xFF9A651B)
private val StudentMoreRed = Color(0xFFB43A31)
private val StudentMoreBlue = Color(0xFF24577A)
private val MoreDateFormat = DateTimeFormatter.ofPattern("dd MMM yyyy")

enum class StudentMoreDestination { CALENDAR, FEES, NOTIFICATIONS, PROFILE }

private data class StudentPortalCard(
    val destination: StudentMoreDestination,
    val title: String,
    val metric: String,
    val metricLabel: String,
    val subtitle: String,
    val badge: String,
    val badgeTone: BadgeTone,
    val icon: ImageVector,
)

@Composable
fun StudentMoreWorkspace(
    heroPainter: Painter,
    snapshot: StudentMoreSnapshot?,
    loading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    onOpen: (StudentMoreDestination) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var confirmSignOut by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxWidth().background(StudentMoreCanvas),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { StudentMoreHeader(heroPainter) }
        if (!errorMessage.isNullOrBlank()) {
            item { StudentMoreNotice(errorMessage, onRetry) }
        }
        item { AccountRow(onSignOut = { confirmSignOut = true }) }

        if (loading && snapshot == null) {
            items(3) { SkeletonRow() }
        } else if (snapshot != null) {
            items(studentMoreCards(snapshot), key = { it.destination }) { card -> MoreNavigationCard(card, onClick = { onOpen(card.destination) }) }
        }

        item { Spacer(Modifier.height(72.dp)) }
    }

    if (confirmSignOut) {
        ConfirmDestructiveActionDialog(
            title = "Sign out",
            dependentSummary = "You will need to sign in again to access the student portal.",
            onConfirm = { onSignOut(); confirmSignOut = false },
            onDismiss = { confirmSignOut = false },
        )
    }
}

private fun studentMoreCards(snapshot: StudentMoreSnapshot): List<StudentPortalCard> {
    val calendarSubtitle = snapshot.nextEvent?.let { "Next: ${it.title} on ${it.startDate}" } ?: "Holidays, events, exams and deadlines"
    val calendarBadge = snapshot.nextEvent?.let { startDateOrNull(it)?.format(MoreDateFormat) } ?: "No upcoming item"

    val feeMetric = snapshot.feeTotal?.let { "Rs %,.0f".format(it) } ?: "-"
    val feeSubtitle = snapshot.feeDueDate?.let { "Due ${it.format(MoreDateFormat)}; informational fee structure" } ?: "Your session's published fee structure"

    val missing = snapshot.missingProfileFields
    val profileSubtitle = if (missing.isEmpty()) {
        "Your essential identity and contact details are complete"
    } else {
        "Add " + missing.take(2).joinToString(" and ").lowercase() + (if (missing.size > 2) " and ${missing.size - 2} more" else "")
    }

    return listOf(
        StudentPortalCard(StudentMoreDestination.CALENDAR, "Calendar", snapshot.upcomingEvents.toString(), "upcoming items", calendarSubtitle, calendarBadge, if (snapshot.nextEvent == null) BadgeTone.Neutral else BadgeTone.Success, Icons.Outlined.CalendarMonth),
        StudentPortalCard(StudentMoreDestination.FEES, "Fee challan", feeMetric, "configured total", feeSubtitle, if (snapshot.feeConfigured) "Configured" else "Not configured", if (snapshot.feeConfigured) BadgeTone.Success else BadgeTone.Neutral, Icons.Outlined.Payments),
        StudentPortalCard(StudentMoreDestination.NOTIFICATIONS, "Notifications", snapshot.unreadNotifications.toString(), "unread notices", "College, department and session notices relevant to you", if (snapshot.unreadNotifications == 0) "All caught up" else "Needs attention", if (snapshot.unreadNotifications == 0) BadgeTone.Success else BadgeTone.Warning, Icons.Outlined.Notifications),
        StudentPortalCard(StudentMoreDestination.PROFILE, "Profile", "${snapshot.profileCompletion}%", "essential details", profileSubtitle, if (snapshot.profileCompletion == 100) "Complete" else "${missing.size} missing", if (snapshot.profileCompletion == 100) BadgeTone.Success else BadgeTone.Warning, Icons.Outlined.Person),
    )
}

@Composable
private fun StudentMoreHeader(heroPainter: Painter) {
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
                Text("ACCOUNT", color = StudentMoreGold, style = CmsTextStyles.eyebrow)
                Spacer(Modifier.height(6.dp))
                Text("More", color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(4.dp))
                Text("Student portal", color = CmsTheme.colors.onInkMuted, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun AccountRow(onSignOut: () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E0D7))) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Signed in to GGC-MBD Student Portal", modifier = Modifier.weight(1f), color = Color(0xFF77716A), style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onSignOut) { Text("Sign out", color = StudentMoreBlue) }
        }
    }
}

@Composable
private fun MoreNavigationCard(card: StudentPortalCard, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE5E0D7)),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).background(StudentMoreBlue.copy(alpha = 0.1f), RoundedCornerShape(13.dp)), contentAlignment = Alignment.Center) {
                Icon(card.icon, contentDescription = null, tint = StudentMoreBlue)
            }
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(card.title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    StatusBadge(card.badge, card.badgeTone)
                }
                Text(card.subtitle, color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(4.dp))
                MoreSummaryMetric(card.metric, card.metricLabel)
            }
        }
    }
}

@Composable
private fun MoreSummaryMetric(value: String, label: String) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.width(6.dp))
        Text(label, color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun StudentMoreNotice(message: String, onRetry: () -> Unit) {
    Surface(shape = RoundedCornerShape(14.dp), color = StudentMoreRed.copy(alpha = 0.1f), border = BorderStroke(1.dp, StudentMoreRed.copy(alpha = 0.25f))) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(message, modifier = Modifier.weight(1f), color = StudentMoreRed, style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onRetry) { Text("Retry", color = StudentMoreRed) }
        }
    }
}
