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
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.ManageAccounts
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
import com.mbd.cmscommon.domain.model.MoreHubSnapshot
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val MoreCanvas = Color(0xFFF7F5F0)
private val MoreGreen = Color(0xFF2F6B4F)
private val MoreGold = Color(0xFF9A651B)
private val MoreRed = Color(0xFFB43A31)
private val MoreNavy = Color(0xFF2F4B7A)
private val MoreDateFormat = DateTimeFormatter.ofPattern("dd MMM yyyy")

enum class MoreDestination { NOTIFICATIONS, PROFILE }

private data class MoreAction(
    val destination: MoreDestination,
    val title: String,
    val detail: String,
    val icon: ImageVector,
    val tone: Color,
)

@Composable
fun MoreHubWorkspace(
    heroPainter: Painter,
    snapshot: MoreHubSnapshot?,
    loading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    onOpen: (MoreDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val actions = listOf(
        MoreAction(
            MoreDestination.NOTIFICATIONS, "Notifications",
            "Publish notices, review delivery history, and keep urgent updates visible.",
            Icons.Outlined.Campaign, MoreNavy,
        ),
        MoreAction(
            MoreDestination.PROFILE, "Profile & Security",
            "Review your administrator account, request a password reset, or sign out securely.",
            Icons.Outlined.ManageAccounts, MoreGreen,
        ),
    )

    LazyColumn(
        modifier = modifier.fillMaxWidth().background(MoreCanvas),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { MoreHeader(heroPainter) }
        if (!errorMessage.isNullOrBlank()) {
            item { MoreNotice(errorMessage, onRetry) }
        }
        item { AccountSummary(snapshot, loading) }
        item { MoreMetrics(snapshot, loading) }
        items(actions, key = { it.destination }) { action -> MoreActionCard(action, onClick = { onOpen(action.destination) }) }
        item { Spacer(Modifier.height(72.dp)) }
    }
}

@Composable
private fun MoreHeader(heroPainter: Painter) {
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
                Text("ACCOUNT & COMMUNICATIONS", color = MoreGold, style = CmsTextStyles.eyebrow)
                Spacer(Modifier.height(6.dp))
                Text("More", color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(4.dp))
                Text("Your administrator identity, notice activity, and security controls.", color = CmsTheme.colors.onInkMuted, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun AccountSummary(snapshot: MoreHubSnapshot?, loading: Boolean) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E0D7))) {
        Column(Modifier.padding(16.dp)) {
            Text("SIGNED-IN ADMINISTRATOR", color = Color(0xFF77716A), style = CmsTextStyles.eyebrow)
            Spacer(Modifier.height(6.dp))
            when {
                loading -> Text("Loading account...", color = Color(0xFF77716A), style = MaterialTheme.typography.bodyMedium)
                snapshot == null -> Text("Directory summary unavailable - retry to restore account details", color = MoreRed, style = MaterialTheme.typography.bodyMedium)
                else -> {
                    Text(snapshot.accountEmail, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StatusBadge(snapshot.accountStatus.uppercase(), if (snapshot.accountStatus.equals("ACTIVE", ignoreCase = true)) BadgeTone.Success else BadgeTone.Neutral)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            snapshot.lastLoginAt?.let { "Last sign-in ${it.atZone(ZoneId.systemDefault()).format(MoreDateFormat)}" } ?: "Last sign-in not recorded",
                            color = Color(0xFF77716A),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MoreMetrics(snapshot: MoreHubSnapshot?, loading: Boolean) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        MoreMetric(if (loading || snapshot == null) "--" else snapshot.administratorCount.toString(), "Admins", Modifier.weight(1f))
        MoreMetric(if (loading || snapshot == null) "--" else snapshot.authoredNotifications.toString(), "Authored", Modifier.weight(1f))
        MoreMetric(
            if (loading || snapshot == null) "--" else snapshot.urgentAuthoredNotifications.toString(),
            "Urgent",
            Modifier.weight(1f),
            alert = (snapshot?.urgentAuthoredNotifications ?: 0) > 0,
        )
        MoreMetric(if (loading || snapshot == null) "--" else snapshot.unreadNotifications.toString(), "Unread", Modifier.weight(1f))
    }
}

@Composable
private fun MoreMetric(value: String, label: String, modifier: Modifier = Modifier, alert: Boolean = false) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E0D7))) {
        Column(Modifier.padding(14.dp)) {
            Text(value, color = if (alert) MoreRed else Color(0xFF252321), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Text(label.uppercase(), color = Color(0xFF77716A), style = CmsTextStyles.eyebrow)
        }
    }
}

@Composable
private fun MoreActionCard(action: MoreAction, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, action.tone.copy(alpha = 0.25f)),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).background(action.tone.copy(alpha = 0.12f), RoundedCornerShape(13.dp)), contentAlignment = Alignment.Center) {
                Icon(action.icon, contentDescription = null, tint = action.tone)
            }
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(action.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(action.detail, color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun MoreNotice(message: String, onRetry: () -> Unit) {
    Surface(shape = RoundedCornerShape(14.dp), color = MoreRed.copy(alpha = 0.1f), border = BorderStroke(1.dp, MoreRed.copy(alpha = 0.25f))) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(message, modifier = Modifier.weight(1f), color = MoreRed, style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onRetry) { Text("Retry", color = MoreRed) }
        }
    }
}
