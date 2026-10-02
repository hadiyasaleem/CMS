package com.mbd.cmscommon.ui.components

import compose.icons.TablerIcons
import compose.icons.tablericons.Bell
import compose.icons.tablericons.Calendar
import compose.icons.tablericons.ChartBar
import compose.icons.tablericons.School
import compose.icons.tablericons.User
import compose.icons.tablericons.UserCheck
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.mbd.cmscommon.domain.model.TeacherMenuSnapshot
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmscommon.ui.theme.ModGround
import com.mbd.cmscommon.ui.theme.ModInk
import com.mbd.cmscommon.ui.theme.ModMuted
import com.mbd.cmscommon.ui.theme.ModSurface
import com.mbd.cmscommon.ui.theme.ModTrack
import com.mbd.cmscommon.ui.theme.ModWarn

private val MenuCanvas = ModGround
private val MenuBlue = ModInk

private data class TeacherMenuItem(
    val label: String,
    val detail: String?,
    val icon: ImageVector,
    val badge: String?,
    val badgeTone: BadgeTone?,
    val onClick: () -> Unit,
)

@Composable
fun TeacherMenuWorkspace(
    heroPainter: Painter,
    snapshot: TeacherMenuSnapshot,
    onOpenMyStudents: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenInsights: () -> Unit,
    onOpenLinkRequests: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenProfile: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var confirmSignOut by remember { mutableStateOf(false) }

    val linkBadge = if (snapshot.canApproveLinkRequests) "${snapshot.pendingLinkRequests} pending" else "Restricted"
    val linkTone = if (snapshot.pendingLinkRequests > 0) BadgeTone.Warning else if (snapshot.canApproveLinkRequests) BadgeTone.Success else BadgeTone.Neutral

    val notificationBadge = if (snapshot.unreadNotifications > 0) "${snapshot.unreadNotifications} new" else "Up to date"

    val items = listOf(
        TeacherMenuItem(
            "My Students",
            "${snapshot.assignmentCount} assigned ${if (snapshot.assignmentCount == 1) "class" else "classes"} across ${snapshot.sessionCount} ${if (snapshot.sessionCount == 1) "session" else "sessions"}",
            TablerIcons.School, null, null, onOpenMyStudents,
        ),
        TeacherMenuItem("Calendar", null, TablerIcons.Calendar, null, null, onOpenCalendar),
        TeacherMenuItem("Insights", null, TablerIcons.ChartBar, null, null, onOpenInsights),
        TeacherMenuItem("Link Requests", null, TablerIcons.UserCheck, linkBadge, linkTone, onOpenLinkRequests),
        TeacherMenuItem("Notifications", null, TablerIcons.Bell, notificationBadge, if (snapshot.unreadNotifications > 0) BadgeTone.Warning else BadgeTone.Success, onOpenNotifications),
        TeacherMenuItem(
            "Profile", null, TablerIcons.User,
            "${snapshot.profileCompleteness}% complete", if (snapshot.profileCompleteness == 100) BadgeTone.Success else BadgeTone.Neutral, onOpenProfile,
        ),
    )

    TopBarActions {
        TextButton(onClick = { confirmSignOut = true }) { Text("Sign out", color = CmsTheme.colors.onInk) }
    }

    val listState = rememberLazyListState()
    WithVerticalScrollbar(listState) {
    LazyColumn( state = listState,
        modifier = modifier.fillMaxWidth().background(MenuCanvas),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { TeacherMenuHeader(heroPainter, snapshot) }
        items(items) { item -> TeacherMenuCard(item) }
        item { Spacer(Modifier.height(72.dp)) }
    }
    }

    if (confirmSignOut) {
        ConfirmDestructiveActionDialog(
            title = "Sign out",
            dependentSummary = "You will need to sign in again to access the faculty portal.",
            onConfirm = { onSignOut(); confirmSignOut = false },
            onDismiss = { confirmSignOut = false },
        )
    }
}

@Composable
private fun TeacherMenuHeader(heroPainter: Painter, snapshot: TeacherMenuSnapshot) {
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
                Text("MENU", color = ModWarn, style = CmsTextStyles.eyebrow)
                Spacer(Modifier.height(6.dp))
                Text(snapshot.teacherName, color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
            }
        }
    }
}

@Composable
private fun TeacherMenuCard(item: TeacherMenuItem) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = item.onClick),
        shape = RoundedCornerShape(16.dp),
        color = ModSurface,
        border = BorderStroke(1.dp, ModTrack),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(item.icon, contentDescription = null, tint = MenuBlue)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                if (item.detail != null) {
                    Text(item.detail, color = ModMuted, style = MaterialTheme.typography.bodySmall)
                }
            }
            if (item.badge != null && item.badgeTone != null) {
                StatusBadge(item.badge, item.badgeTone)
            }
        }
    }
}
