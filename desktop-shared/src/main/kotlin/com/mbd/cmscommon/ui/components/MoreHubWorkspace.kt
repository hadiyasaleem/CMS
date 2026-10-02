package com.mbd.cmscommon.ui.components

import compose.icons.TablerIcons
import compose.icons.tablericons.AlertTriangle
import compose.icons.tablericons.Building
import compose.icons.tablericons.Settings
import compose.icons.tablericons.Shield
import compose.icons.tablericons.Speakerphone
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.mbd.cmscommon.domain.model.MoreHubSnapshot
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
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val MoreCanvas = ModGround
private val MoreGreen = ModSuccess
private val MoreGold = ModWarn
private val MoreRed = ModAccent
private val MoreNavy = ModInk
private val MoreDateFormat = DateTimeFormatter.ofPattern("dd MMM yyyy")

enum class MoreDestination { ADMINISTRATORS, BUILDINGS_ROOMS, NOTIFICATIONS, APP_LOGS, PROFILE }

private data class MoreAction(
    val destination: MoreDestination,
    val title: String,
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
        MoreAction(MoreDestination.ADMINISTRATORS, "Administrators", TablerIcons.Shield, MoreNavy),
        MoreAction(MoreDestination.NOTIFICATIONS, "Notifications", TablerIcons.Speakerphone, MoreNavy),
        MoreAction(MoreDestination.BUILDINGS_ROOMS, "Buildings & Rooms", TablerIcons.Building, MoreGold),
        MoreAction(MoreDestination.APP_LOGS, "App Logs", TablerIcons.AlertTriangle, MoreRed),
        MoreAction(MoreDestination.PROFILE, "Profile & Security", TablerIcons.Settings, MoreGreen),
    )

    val listState = rememberLazyListState()
    WithVerticalScrollbar(listState) {
    LazyColumn( state = listState,
        modifier = modifier.fillMaxWidth().background(MoreCanvas),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { MoreHeader(heroPainter) }
        if (!errorMessage.isNullOrBlank()) {
            item { CmsNotice(errorMessage, tone = NoticeTone.Error, actionLabel = "Retry", onAction = onRetry) }
        }
        item { AccountSummary(snapshot, loading) }
        items(actions, key = { it.destination }) { action -> MoreActionCard(action, onClick = { onOpen(action.destination) }) }
        item { Spacer(Modifier.height(72.dp)) }
    }
    }
}

@Composable
private fun MoreHeader(heroPainter: Painter) {
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
                Text("ACCOUNT & COMMUNICATIONS", color = MoreGold, style = CmsTextStyles.eyebrow)
                Spacer(Modifier.height(6.dp))
                Text("More", color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
            }
        }
    }
}

@Composable
private fun AccountSummary(snapshot: MoreHubSnapshot?, loading: Boolean) {
    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(16.dp)) {
            Text("SIGNED-IN ADMINISTRATOR", color = ModMuted, style = CmsTextStyles.eyebrow)
            Spacer(Modifier.height(6.dp))
            when {
                loading -> Text("Loading account...", color = ModMuted, style = MaterialTheme.typography.bodyMedium)
                snapshot == null -> Text("Directory summary unavailable - retry to restore account details", color = MoreRed, style = MaterialTheme.typography.bodyMedium)
                else -> {
                    Text(snapshot.accountEmail, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StatusBadge(snapshot.accountStatus.uppercase(), if (snapshot.accountStatus.equals("ACTIVE", ignoreCase = true)) BadgeTone.Success else BadgeTone.Neutral)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            snapshot.lastLoginAt?.let { "Last sign-in ${it.atZone(ZoneId.systemDefault()).format(MoreDateFormat)}" } ?: "Last sign-in not recorded",
                            color = ModMuted,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MoreActionCard(action: MoreAction, onClick: () -> Unit) {
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
            Text(action.title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }
    }
}

