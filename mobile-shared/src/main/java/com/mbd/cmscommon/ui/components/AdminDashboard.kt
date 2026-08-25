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
import com.mbd.cmscommon.ui.theme.CollegeInfo
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
            Color(0xFF24577A),
            Color(0xFFEAF4FA),
        ),
        DashboardMetric(
            "Teachers",
            state.teachers.toString(),
            if (state.teachers > 0) "$studentsPerTeacher students per teacher" else "Faculty directory is empty",
            Icons.Outlined.Groups,
            Color(0xFF3C6B52),
            Color(0xFFEDF6EF),
        ),
        DashboardMetric(
            "Departments",
            state.departments.toString(),
            if (state.departments > 0) "$sessionsPerDepartment sessions per department" else "Create the first department",
            Icons.Outlined.SpaceDashboard,
            Color(0xFF775A24),
            Color(0xFFFFF5DD),
        ),
        DashboardMetric(
            "Active sessions",
            state.activeSessions.toString(),
            if (state.activeSessions > 0) "$studentsPerSession students per session" else "No active intakes",
            Icons.Outlined.Schedule,
            Color(0xFF5C4B8A),
            Color(0xFFF1EDFA),
        ),
        DashboardMetric(
            "Link requests",
            state.pendingRequests.toString(),
            if (state.pendingRequests > 0) "Waiting for review" else "Queue is clear",
            Icons.Outlined.HowToReg,
            if (state.pendingRequests > 0) CmsTheme.colors.accent else Color(0xFF3C6B52),
            if (state.pendingRequests > 0) Color(0xFFFFEFEB) else Color(0xFFEDF6EF),
        ),
    )

    BoxWithConstraints(modifier.fillMaxSize().background(Color(0xFFF7F5F0))) {
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
                    color = Color(0xFFFFEFEB),
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
        color = Color(0xFFFFFBF3),
        border = BorderStroke(1.dp, Color(0xFFE5DED2)),
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
                        0f to Color(0xFFFFFBF3),
                        0.48f to Color(0xFFFFFBF3).copy(alpha = 0.94f),
                        0.72f to Color(0xFFFFFBF3).copy(alpha = 0.18f),
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
                Surface(shape = RoundedCornerShape(999.dp), color = Color.White.copy(alpha = 0.9f)) {
                    CollegeCrestBadge()
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    "Welcome back,\nAdmin.",
                    color = Color(0xFF252321),
                    fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                    style = if (wide) MaterialTheme.typography.displayMedium else MaterialTheme.typography.headlineLarge,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "A clear view of people, sessions, and the work that needs attention today.",
                    color = Color(0xFF625E58),
                    maxLines = if (wide) 3 else 4,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun CollegeCrestBadge() {
    Text(
        CollegeInfo.NAME,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        color = Color(0xFF252321),
        style = CmsTextStyles.eyebrow,
    )
}

@Composable
private fun DashboardSectionHeading(title: String, subtitle: String? = null) {
    Column {
        Text(title, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
        if (subtitle != null) {
            Text(subtitle, color = Color(0xFF77716A), style = MaterialTheme.typography.bodyMedium)
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
            Text(metric.label.uppercase(Locale.ROOT), color = Color(0xFF716B64), style = CmsTextStyles.eyebrow)
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
        Icons.Outlined.CalendarMonth, Color(0xFF24577A), Color(0xFFEAF4FA), onClick, modifier,
    )
}

@Composable
private fun ReviewQueueCard(pendingRequests: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val title = if (pendingRequests > 0) "$pendingRequests requests" else "Queue clear"
    val body = if (pendingRequests > 0) "Student accounts are waiting to be linked." else "No student link requests need review."
    val tint = if (pendingRequests > 0) CmsTheme.colors.accent else Color(0xFF3C6B52)
    val container = if (pendingRequests > 0) Color(0xFFFFEFEB) else Color(0xFFEDF6EF)
    DashboardOperationCard(title, body, "Review queue", Icons.Outlined.HowToReg, tint, container, onClick, modifier)
}

@Composable
private fun BroadcastCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    DashboardOperationCard(
        "Broadcast", "Send an announcement to students or faculty.", "New notice",
        Icons.Outlined.Campaign, Color(0xFF775A24), Color(0xFFFFF5DD), onClick, modifier,
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
            Text(body, color = Color(0xFF625E58), style = MaterialTheme.typography.bodyMedium)
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
        modifier = modifier.clickable(onClick = action.onClick),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE7E2DA)),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).background(Color(0xFFF1EEE8), RoundedCornerShape(13.dp)), contentAlignment = Alignment.Center) {
                Icon(action.icon, contentDescription = null, tint = Color(0xFF383532), modifier = Modifier.size(21.dp))
            }
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(action.label, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(
                    action.description,
                    color = Color(0xFF77716A),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFFAAA39A), modifier = Modifier.size(17.dp))
        }
    }
}
