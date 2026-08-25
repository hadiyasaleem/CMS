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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.HowToReg
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
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

private val MenuCanvas = Color(0xFFF7F5F0)
private val MenuBlue = Color(0xFF24577A)

private data class TeacherMenuItem(
    val label: String,
    val detail: String,
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
    onOpenDocuments: () -> Unit,
    onOpenInsights: () -> Unit,
    onOpenLinkRequests: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenProfile: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var confirmSignOut by remember { mutableStateOf(false) }

    val linkDetail = if (snapshot.canApproveLinkRequests) "Review students waiting to link their accounts" else "Ask an administrator to grant approval access"
    val linkBadge = if (snapshot.canApproveLinkRequests) "${snapshot.pendingLinkRequests} pending" else "Restricted"
    val linkTone = if (snapshot.pendingLinkRequests > 0) BadgeTone.Warning else if (snapshot.canApproveLinkRequests) BadgeTone.Success else BadgeTone.Neutral

    val notificationDetail = if (snapshot.canSendNotifications) "Read faculty notices or notify assigned sessions" else "Read faculty notices and college alerts"
    val notificationBadge = if (snapshot.unreadNotifications > 0) "${snapshot.unreadNotifications} new" else "Up to date"

    val items = listOf(
        TeacherMenuItem(
            "My Students",
            "${snapshot.assignmentCount} assigned ${if (snapshot.assignmentCount == 1) "class" else "classes"} across ${snapshot.sessionCount} ${if (snapshot.sessionCount == 1) "session" else "sessions"}",
            Icons.Outlined.School, null, null, onOpenMyStudents,
        ),
        TeacherMenuItem("Calendar", "College events, holidays, exams and deadlines", Icons.Outlined.CalendarMonth, null, null, onOpenCalendar),
        TeacherMenuItem("Documents", "Prospectus, rules, reports and shared files", Icons.Outlined.Description, null, null, onOpenDocuments),
        TeacherMenuItem("Insights", "Exam coverage and at-risk trends for your classes", Icons.Outlined.Assessment, null, null, onOpenInsights),
        TeacherMenuItem("Link Requests", linkDetail, Icons.Outlined.HowToReg, linkBadge, linkTone, onOpenLinkRequests),
        TeacherMenuItem("Notifications", notificationDetail, Icons.Outlined.Notifications, notificationBadge, if (snapshot.unreadNotifications > 0) BadgeTone.Warning else BadgeTone.Success, onOpenNotifications),
        TeacherMenuItem(
            "Profile", "Contact details, permissions, assignments and security", Icons.Outlined.Person,
            "${snapshot.profileCompleteness}% complete", if (snapshot.profileCompleteness == 100) BadgeTone.Success else BadgeTone.Neutral, onOpenProfile,
        ),
    )

    LazyColumn(
        modifier = modifier.fillMaxWidth().background(MenuCanvas),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { TeacherMenuHeader(heroPainter, snapshot, onSignOut = { confirmSignOut = true }) }
        items(items) { item -> TeacherMenuCard(item) }
        item { Spacer(Modifier.height(72.dp)) }
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
private fun TeacherMenuHeader(heroPainter: Painter, snapshot: TeacherMenuSnapshot, onSignOut: () -> Unit) {
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
            Row(Modifier.align(Alignment.CenterStart).padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("MENU", color = Color(0xFF9A651B), style = CmsTextStyles.eyebrow)
                    Spacer(Modifier.height(6.dp))
                    Text(snapshot.teacherName, color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                }
                TextButton(onClick = onSignOut) { Text("Sign out", color = CmsTheme.colors.onInk) }
            }
        }
    }
}

@Composable
private fun TeacherMenuCard(item: TeacherMenuItem) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = item.onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE5E0D7)),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(item.icon, contentDescription = null, tint = MenuBlue)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(item.detail, color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
            }
            if (item.badge != null && item.badgeTone != null) {
                StatusBadge(item.badge, item.badgeTone)
            }
        }
    }
}
