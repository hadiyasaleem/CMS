package com.mbd.cmscommon.ui.components

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.HowToReg
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.SpaceDashboard
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
import com.mbd.cmscommon.ui.theme.ModFaint
import com.mbd.cmscommon.ui.theme.ModTrack
import com.mbd.cmscommon.ui.theme.ModGround
import com.mbd.cmscommon.ui.theme.ModSurface
import com.mbd.cmscommon.ui.theme.ModSurfaceAlt
import com.mbd.cmscommon.ui.theme.ModSuccess
import com.mbd.cmscommon.ui.theme.ModWarn
import com.mbd.cmscommon.ui.theme.ModRedTint
import java.util.Locale
import kotlin.math.roundToInt

data class DashboardMetric(
    val label: String,
    val value: String,
    val detail: String,
    val icon: ImageVector,
    val tint: Color,
    val container: Color,
)

data class DashboardActionUi(
    val label: String,
    val description: String,
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
    val studentsPerTeacher = if (state.teachers > 0) (state.students.toDouble() / state.teachers).roundToInt() else 0
    val studentsPerSession = if (state.activeSessions > 0) (state.students.toDouble() / state.activeSessions).roundToInt() else 0
    val sessionsPerDepartment = if (state.departments > 0) (state.activeSessions.toDouble() / state.departments).roundToInt() else 0

    val metrics = listOf(
        DashboardMetric(
            "Students",
            state.students.toString(),
            if (state.activeSessions > 0) "$studentsPerSession per active session" else "No active sessions",
            Icons.Outlined.School,
            ModInk,
            ModInk.copy(alpha = 0.08f),
        ),
        DashboardMetric(
            "Teachers",
            state.teachers.toString(),
            if (state.teachers > 0) "$studentsPerTeacher students per teacher" else "Faculty directory is empty",
            Icons.Outlined.Groups,
            ModSuccess,
            ModSuccess.copy(alpha = 0.12f),
        ),
        DashboardMetric(
            "Departments",
            state.departments.toString(),
            if (state.departments > 0) "$sessionsPerDepartment sessions per department" else "Create the first department",
            Icons.Outlined.SpaceDashboard,
            ModWarn,
            ModWarn.copy(alpha = 0.14f),
        ),
        DashboardMetric(
            "Active sessions",
            state.activeSessions.toString(),
            if (state.activeSessions > 0) "$studentsPerSession students per session" else "No active intakes",
            Icons.Outlined.Schedule,
            ModInk,
            ModInk.copy(alpha = 0.08f),
        ),
        DashboardMetric(
            "Link requests",
            state.pendingRequests.toString(),
            if (state.pendingRequests > 0) "Waiting for review" else "Queue is clear",
            Icons.Outlined.HowToReg,
            if (state.pendingRequests > 0) CmsTheme.colors.accent else ModSuccess,
            if (state.pendingRequests > 0) ModRedTint else ModSuccess.copy(alpha = 0.12f),
        ),
    )

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

            DashboardSectionHeading("College snapshot", "Live cached figures, updated whenever Admin data refreshes")
            DashboardGrid(metrics, if (wide) 5 else 2) { metric, itemModifier -> DashboardMetricCard(metric, itemModifier) }

            DashboardSectionHeading("Needs attention", "The next useful actions, not another status list")
            if (wide) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    TimetableCard(state, onOpenMasterTimetable, Modifier.weight(1.35f))
                    ReviewQueueCard(state.pendingRequests, onOpenLinkRequests, Modifier.weight(1f))
                    BroadcastCard(onOpenNotifications, Modifier.weight(1f))
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TimetableCard(state, onOpenMasterTimetable)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ReviewQueueCard(state.pendingRequests, onOpenLinkRequests, Modifier.weight(1f))
                        BroadcastCard(onOpenNotifications, Modifier.weight(1f))
                    }
                }
            }

            DashboardSectionHeading("Quick access", "Frequently used administration areas")
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
                Spacer(Modifier.height(8.dp))
                Text(
                    "A clear view of people, sessions, and the work that needs attention today.",
                    color = ModInk.copy(alpha = 0.78f),
                    maxLines = if (wide) 3 else 4,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun DashboardSectionHeading(title: String, subtitle: String? = null) {
    Column {
        Text(title, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
        if (subtitle != null) {
            Text(subtitle, color = ModMuted, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun <T> DashboardGrid(items: List<T>, columns: Int, itemContent: @Composable (T, Modifier) -> Unit) {
    val rows = items.chunked(columns)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        rows.forEach { rowItems ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowItems.forEach { item -> itemContent(item, Modifier.weight(1f)) }
                repeat(columns - rowItems.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun DashboardMetricCard(metric: DashboardMetric, modifier: Modifier = Modifier) {
    CmsCard(modifier) {
        Column(Modifier.padding(18.dp)) {
            Box(Modifier.size(40.dp).background(metric.container, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Icon(metric.icon, contentDescription = null, tint = metric.tint, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(metric.value, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
            Text(metric.label.uppercase(Locale.ROOT), color = ModMuted, style = CmsTextStyles.eyebrow)
            Text(
                metric.detail,
                color = metric.tint,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun TimetableCard(state: DashboardState, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val body = if (state.activeSessions > 0) {
        "${state.activeSessions} active sessions are contributing to the college-wide schedule."
    } else {
        "Create a session to start building the college-wide schedule."
    }
    DashboardOperationCard(
        "Master timetable", body, "Open schedule",
        Icons.Outlined.CalendarMonth, ModInk, ModInk.copy(alpha = 0.08f), onClick, modifier,
    )
}

@Composable
private fun ReviewQueueCard(pendingRequests: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val title = if (pendingRequests > 0) "$pendingRequests requests" else "Queue clear"
    val body = if (pendingRequests > 0) "Student accounts are waiting to be linked." else "No student link requests need review."
    val tint = if (pendingRequests > 0) CmsTheme.colors.accent else ModSuccess
    val container = if (pendingRequests > 0) ModRedTint else ModSuccess.copy(alpha = 0.12f)
    DashboardOperationCard(title, body, "Review queue", Icons.Outlined.HowToReg, tint, container, onClick, modifier)
}

@Composable
private fun BroadcastCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    DashboardOperationCard(
        "Broadcast", "Send an announcement to students or faculty.", "New notice",
        Icons.Outlined.Campaign, ModWarn, ModWarn.copy(alpha = 0.14f), onClick, modifier,
    )
}

@Composable
private fun DashboardOperationCard(
    title: String,
    body: String,
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
            Text(body, color = ModMuted, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(label.uppercase(Locale.ROOT), color = tint, style = CmsTextStyles.eyebrow)
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun DashboardActionCard(action: DashboardActionUi, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(112.dp).clickable(onClick = action.onClick),
        shape = RoundedCornerShape(18.dp),
        color = ModSurface,
        border = BorderStroke(1.dp, ModTrack),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).background(ModSurfaceAlt, RoundedCornerShape(13.dp)), contentAlignment = Alignment.Center) {
                Icon(action.icon, contentDescription = null, tint = ModInk, modifier = Modifier.size(21.dp))
            }
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(action.label, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(
                    action.description,
                    color = ModMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = ModFaint, modifier = Modifier.size(17.dp))
        }
    }
}
