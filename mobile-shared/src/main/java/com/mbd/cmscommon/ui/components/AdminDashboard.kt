package com.mbd.cmscommon.ui.components

import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowRight
import compose.icons.tablericons.Calendar
import compose.icons.tablericons.Speakerphone
import compose.icons.tablericons.UserCheck
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.controller.DashboardState
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmscommon.ui.theme.ModInk
import com.mbd.cmscommon.ui.theme.ModMuted
import com.mbd.cmscommon.ui.theme.ModTrack
import com.mbd.cmscommon.ui.theme.ModGround
import com.mbd.cmscommon.ui.theme.ModSurface
import com.mbd.cmscommon.ui.theme.ModSurfaceAlt
import com.mbd.cmscommon.ui.theme.ModSuccess
import com.mbd.cmscommon.ui.theme.ModWarn
import com.mbd.cmscommon.ui.theme.ModRedTint
import java.util.Locale

data class DashboardActionUi(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
)

@Composable
fun AdminDashboardContent(
    state: DashboardState,
    heroPainter: Painter,
    actions: List<DashboardActionUi>,
    onOpenMasterTimetable: () -> Unit,
    onOpenLinkRequests: () -> Unit,
    onOpenNotifications: () -> Unit,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
) {
    BoxWithConstraints(modifier.fillMaxSize().background(ModGround)) {
        val wide = maxWidth >= 900.dp
        val contentPadding = if (wide) 32.dp else 16.dp

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .widthIn(max = 1180.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = contentPadding, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(if (wide) 24.dp else 18.dp),
        ) {
            DashboardHero(heroPainter, wide)

            if (!errorMessage.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = ModRedTint,
                    border = BorderStroke(1.dp, CmsTheme.colors.accent.copy(alpha = 0.22f)),
                ) {
                    Text(errorMessage, modifier = Modifier.padding(16.dp), color = CmsTheme.colors.accent, style = MaterialTheme.typography.bodyMedium)
                }
            }

            DashboardSectionHeading("Needs attention")
            if (wide) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    TimetableCard(onOpenMasterTimetable, Modifier.weight(1.35f))
                    ReviewQueueCard(state.pendingRequests, onOpenLinkRequests, Modifier.weight(1f))
                    BroadcastCard(onOpenNotifications, Modifier.weight(1f))
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TimetableCard(onOpenMasterTimetable, Modifier.fillMaxWidth())
                    ReviewQueueCard(state.pendingRequests, onOpenLinkRequests, Modifier.fillMaxWidth())
                    BroadcastCard(onOpenNotifications, Modifier.fillMaxWidth())
                }
            }

            DashboardSectionHeading("Quick access")
            DashboardGrid(actions, if (wide) 3 else 2) { action, itemModifier -> DashboardActionCard(action, itemModifier) }

            Spacer(Modifier.height(72.dp))
        }
    }
}

@Composable
private fun DashboardHero(heroPainter: Painter, wide: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(if (wide) 278.dp else 236.dp),
        shape = RoundedCornerShape(if (wide) 28.dp else 22.dp),
        color = ModWarn.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, ModTrack),
    ) {
        Box(Modifier.fillMaxSize()) {
            Image(
                painter = heroPainter,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                alignment = Alignment.CenterEnd,
                contentScale = ContentScale.Crop,
            )
            Box(
                Modifier.fillMaxSize().background(
                    Brush.horizontalGradient(
                        0f to ModSurface.copy(alpha = 0.82f),
                        0.55f to ModSurface.copy(alpha = 0.6f),
                        0.8f to ModSurface.copy(alpha = 0.1f),
                        1f to Color.Transparent,
                    ),
                ),
            )
            Column(
                Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth(if (wide) 0.5f else 0.64f)
                    .padding(if (wide) 32.dp else 22.dp),
            ) {
                Text(
                    "Welcome back,\nAdmin.",
                    color = ModInk,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                    style = if (wide) MaterialTheme.typography.displayMedium else MaterialTheme.typography.headlineLarge,
                )
            }
        }
    }
}

@Composable
private fun DashboardSectionHeading(title: String) {
    Text(title, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
}

@Composable
private fun <T> DashboardGrid(items: List<T>, columns: Int, itemContent: @Composable (T, Modifier) -> Unit) {
    val rows = items.chunked(columns)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        rows.forEach { rowItems ->
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowItems.forEach { item -> itemContent(item, Modifier.weight(1f)) }
                repeat(columns - rowItems.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun TimetableCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    DashboardOperationCard(
        "Master timetable", null, "Open schedule",
        TablerIcons.Calendar, ModInk, ModInk.copy(alpha = 0.08f), onClick, modifier,
    )
}

@Composable
private fun ReviewQueueCard(pendingRequests: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val title = if (pendingRequests > 0) "$pendingRequests requests" else "Queue clear"
    val tint = if (pendingRequests > 0) CmsTheme.colors.accent else ModSuccess
    val container = if (pendingRequests > 0) ModRedTint else ModSuccess.copy(alpha = 0.12f)
    DashboardOperationCard(title, null, "Review queue", TablerIcons.UserCheck, tint, container, onClick, modifier)
}

@Composable
private fun BroadcastCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    DashboardOperationCard(
        "Broadcast", null, "New notice",
        TablerIcons.Speakerphone, ModWarn, ModWarn.copy(alpha = 0.14f), onClick, modifier,
    )
}

@Composable
private fun DashboardOperationCard(
    title: String,
    body: String?,
    label: String,
    icon: ImageVector,
    tint: Color,
    container: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = container,
        border = BorderStroke(1.dp, tint.copy(alpha = 0.16f)),
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(title, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            if (body != null) {
                Text(body, color = ModMuted, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(label.uppercase(Locale.ROOT), color = tint, style = CmsTextStyles.eyebrow)
                Icon(TablerIcons.ArrowRight, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun DashboardActionCard(action: DashboardActionUi, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxHeight().clickable(onClick = action.onClick),
        shape = RoundedCornerShape(18.dp),
        color = ModSurface,
        border = BorderStroke(1.dp, ModTrack),
    ) {
        Column(Modifier.padding(16.dp)) {
            Box(Modifier.size(42.dp).background(ModSurfaceAlt, RoundedCornerShape(13.dp)), contentAlignment = Alignment.Center) {
                Icon(action.icon, contentDescription = null, tint = ModInk, modifier = Modifier.size(21.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(action.label, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}
